package com.javafix.shop;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javafix.shop.entity.Product;
import com.javafix.shop.entity.User;
import com.javafix.shop.exception.ErrorCode;
import com.javafix.shop.mapper.ProductMapper;
import com.javafix.shop.mapper.UserMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Import(MySqlTestConfiguration.class)
@AutoConfigureMockMvc
class OrderDomainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM order_item");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM `user`");

        User user = new User();
        user.setUsername("alice");
        userMapper.insert(user);
        userId = user.getId();

        Product product = new Product();
        product.setName("Keyboard");
        product.setPrice(new BigDecimal("99.50"));
        product.setStock(10);
        product.setVersion(0);
        productMapper.insert(product);
        productId = product.getId();
    }

    @Test
    void getProductReturnsData() throws Exception {
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("Keyboard"))
                .andExpect(jsonPath("$.data.stock").value(10));
    }

    @Test
    void getDeletedProductReturnsBusinessError() throws Exception {
        productMapper.deleteById(productId);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Test
    void createOrderDeductsStockAndComputesAmount() throws Exception {
        String body = """
                {
                  "userId": %d,
                  "items": [{"productId": %d, "quantity": 2}]
                }
                """.formatted(userId, productId);

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalAmount").value(199.00))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andReturn();

        Product product = productMapper.selectById(productId);
        assertThat(product.getStock()).isEqualTo(8);

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        long orderId = root.path("data").path("id").asLong();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    void createOrderRejectsInsufficientStockWithoutChangingInventory() throws Exception {
        String body = """
                {
                  "userId": %d,
                  "items": [{"productId": %d, "quantity": 11}]
                }
                """.formatted(userId, productId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.INSUFFICIENT_STOCK));

        Product product = productMapper.selectById(productId);
        assertThat(product.getStock()).isEqualTo(10);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isZero();
    }

    @Test
    void createOrderRejectsMissingUser() throws Exception {
        String body = """
                {
                  "userId": 999999,
                  "items": [{"productId": %d, "quantity": 1}]
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void cancelRestoresStockOnceAndSecondCancelFails() throws Exception {
        String body = """
                {
                  "userId": %d,
                  "items": [{"productId": %d, "quantity": 3}]
                }
                """.formatted(userId, productId);

        MvcResult created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        long orderId = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        assertThat(productMapper.selectById(productId).getStock()).isEqualTo(10);

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_CANCELLABLE));

        assertThat(productMapper.selectById(productId).getStock()).isEqualTo(10);
    }

    @Test
    void pageOrdersReturnsConsistentFields() throws Exception {
        for (int i = 0; i < 3; i++) {
            String body = """
                    {
                      "userId": %d,
                      "items": [{"productId": %d, "quantity": 1}]
                    }
                    """.formatted(userId, productId);
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(jsonPath("$.code").value(0));
        }

        mockMvc.perform(get("/api/orders").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.records.length()").value(2));
    }
}
