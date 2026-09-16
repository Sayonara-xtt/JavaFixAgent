package com.javafix.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.javafix.shop.entity.Product;
import com.javafix.shop.entity.User;
import com.javafix.shop.exception.BusinessException;
import com.javafix.shop.exception.ErrorCode;
import com.javafix.shop.mapper.ProductMapper;
import com.javafix.shop.mapper.UserMapper;
import com.javafix.shop.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.dao.DataAccessException;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Phase 3 验收：请求边界、整单事务、历史快照与真实 MySQL 并发不变量。 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import({MySqlTestConfiguration.class, Phase3AcceptanceTest.RaceConfiguration.class})
@Timeout(60)
class Phase3AcceptanceTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserMapper users;
    @Autowired private ProductMapper products;
    @Autowired private OrderService orders;
    @Autowired private OrderReadGate readGate;
    private Long userId;
    private Long productId;

    @BeforeEach
    void prepareIsolatedFixtures() {
        jdbc.update("DELETE FROM order_item");
        jdbc.update("DELETE FROM orders");
        jdbc.update("DELETE FROM product");
        jdbc.update("DELETE FROM `user`");
        User user = new User();
        user.setUsername("phase3");
        users.insert(user);
        userId = user.getId();
        productId = product("Keyboard", "99.50", 10);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"userId\":1,\"items\":[]}", "{\"userId\":1,\"items\":null}",
            "{\"userId\":1,\"items\":[null]}",
            "{\"userId\":1,\"items\":[{\"productId\":1,\"quantity\":0}]}",
            "{\"userId\":1,\"items\":[{\"productId\":1,\"quantity\":-1}]}",
            "{\"userId\":1,\"items\":[{\"productId\":1}]}",
            "{\"userId\":1,\"items\":[{\"quantity\":1}]}"
    })
    void invalidRequestsHaveControlledErrorsAndNoWrites(String body) throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
        assertNoOrderWrites();
        assertThat(stock(productId)).isEqualTo(10);
    }

    @Test
    void malformedJsonHasUnifiedValidationResponse() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40000));
        assertNoOrderWrites();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void missingOrDeletedProductsCannotBeOrdered(boolean deleted) throws Exception {
        long id = deleted ? productId : 999999L;
        if (deleted) products.deleteById(id);
        create(body(id, 1), 40002);
        assertThat(stock(productId)).isEqualTo(10);
        assertNoOrderWrites();
    }

    @Test
    void deletedUserCannotCreateOrder() throws Exception {
        users.deleteById(userId);
        create(body(productId, 1), 40001);
        assertThat(stock(productId)).isEqualTo(10);
        assertNoOrderWrites();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void failureOnLaterItemRollsBackEarlierStockDeduction(boolean missing) throws Exception {
        Long second = missing ? 999999L : product("Mouse", "10.25", 1);
        create(multiBody(productId, 2, second, 2), missing ? 40002 : 40003);
        assertThat(stock(productId)).isEqualTo(10);
        if (!missing) assertThat(stock(second)).isEqualTo(1);
        assertNoOrderWrites();
    }

    @Test
    void multiItemOrderKeepsPricesAndNamesAfterProductChanges() throws Exception {
        Long mouseId = product("Mouse", "10.25", 5);
        long orderId = create(multiBody(productId, 2, mouseId, 3), 0);
        assertThat(stock(productId)).isEqualTo(8);
        assertThat(stock(mouseId)).isEqualTo(2);
        jdbc.update("UPDATE product SET name = 'Changed', price = 1.00 WHERE id = ?", productId);
        mvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAmount").value(229.75))
                .andExpect(jsonPath("$.data.items.length()").value(2));
        assertThat(jdbc.queryForObject(
                "SELECT product_name FROM order_item WHERE order_id = ? AND product_id = ?",
                String.class, orderId, productId)).isEqualTo("Keyboard");
        assertThat(jdbc.queryForObject(
                "SELECT product_price FROM order_item WHERE order_id = ? AND product_id = ?",
                BigDecimal.class, orderId, productId)).isEqualByComparingTo("99.50");
        mvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(jsonPath("$.code").value(0));
        assertThat(stock(productId)).isEqualTo(10);
        assertThat(stock(mouseId)).isEqualTo(5);
    }

    @Test
    void repeatedProductLinesCannotExceedAvailableStock() throws Exception {
        create(multiBody(productId, 6, productId, 5), 40003);
        assertThat(stock(productId)).isEqualTo(10);
        assertNoOrderWrites();
    }

    @Test
    void repeatedProductLinesRestoreTheirTotalQuantityOnce() throws Exception {
        long orderId = create(multiBody(productId, 2, productId, 3), 0);
        assertThat(stock(productId)).isEqualTo(5);
        mvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(jsonPath("$.code").value(0));
        mvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(jsonPath("$.code").value(40005));
        assertThat(stock(productId)).isEqualTo(10);
    }

    @Test
    void missingOrdersHaveControlledErrors() throws Exception {
        mvc.perform(get("/api/orders/999999")).andExpect(jsonPath("$.code").value(40004));
        mvc.perform(post("/api/orders/999999/cancel")).andExpect(jsonPath("$.code").value(40004));
        assertThat(stock(productId)).isEqualTo(10);
    }

    @Test
    void paginationDefaultsAndOutOfRangePageAreConsistent() throws Exception {
        mvc.perform(get("/api/orders").param("page", "0").param("size", "-1"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records.length()").value(0));
        create(body(productId, 1), 0);
        mvc.perform(get("/api/orders").param("page", "10").param("size", "2"))
                .andExpect(jsonPath("$.data.page").value(10))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unavailableRestorationTargetRollsBackStatusAndEarlierRestoration(boolean deleted) throws Exception {
        Long second = product("Mouse", "10.25", 5);
        long orderId = create(multiBody(productId, 2, second, 1), 0);
        if (deleted) products.deleteById(second);
        else jdbc.update("DELETE FROM product WHERE id = ?", second);
        // 不能静默接受还库存影响零行：整次取消必须失败并回滚。
        mvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(jsonPath("$.code").value(40002));
        assertThat(stock(productId)).isEqualTo(8);
        assertThat(jdbc.queryForObject("SELECT status FROM orders WHERE id = ?", String.class, orderId))
                .isEqualTo("CREATED");
        if (deleted) assertThat(stock(second)).isEqualTo(4);
    }

    @Test
    void nonCreatedOrderCannotBeCancelled() throws Exception {
        long orderId = create(body(productId, 2), 0);
        jdbc.update("UPDATE orders SET status = 'INVALID' WHERE id = ?", orderId);
        mvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(jsonPath("$.code").value(40005));
        assertThat(stock(productId)).isEqualTo(8);
    }

    @RepeatedTest(3)
    void concurrentOrdersNeverOversell() throws Exception {
        jdbc.update("UPDATE product SET stock = 3 WHERE id = ?", productId);
        var request = json.readValue(body(productId, 1), com.javafix.shop.dto.CreateOrderRequest.class);
        List<Integer> results = parallel(8, () -> {
            try { orders.create(request); return 0; }
            catch (BusinessException ex) { return ex.getCode(); }
        });
        assertThat(results).containsOnly(0, 40003);
        assertThat(results.stream().filter(code -> code == 0).count()).isEqualTo(3);
        assertThat(stock(productId)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT SUM(quantity) FROM order_item", Integer.class)).isEqualTo(3);
    }

    @RepeatedTest(3)
    void concurrentCancellationRestoresStockExactlyOnce() throws Exception {
        long orderId = create(body(productId, 2), 0);
        // 两个真实事务都完成旧状态读取后才继续，稳定复现先查后改的竞态。
        readGate.barrier = new CyclicBarrier(2);
        try {
            List<Integer> results = parallel(2, () -> {
                try { orders.cancel(orderId); return 0; }
                catch (BusinessException ex) { return ex.getCode(); }
            });
            assertThat(results).containsExactlyInAnyOrder(0, 40005);
            assertThat(stock(productId)).isEqualTo(10);
            assertThat(jdbc.queryForObject("SELECT status FROM orders WHERE id = ?", String.class, orderId))
                    .isEqualTo("CANCELLED");
        } finally {
            readGate.barrier = null;
        }
    }

    @Test
    void staleProductUpdateCannotOverwriteDeductedStock() throws Exception {
        Product stale = products.selectById(productId);
        create(body(productId, 2), 0);
        stale.setStock(100);
        assertThat(products.updateById(stale)).isZero();
        assertThat(stock(productId)).isEqualTo(8);
    }

    @Test
    void detailInsertFailureRollsBackOrderHeaderDetailsAndAllStock() throws Exception {
        Long second = product("Mouse", "10.25", 5);
        var request = json.readValue(multiBody(productId, 2, second, 1),
                com.javafix.shop.dto.CreateOrderRequest.class);
        // 在真实数据库中拒绝第二条明细，验证订单头和第一条明细也回滚。
        jdbc.execute("ALTER TABLE order_item ADD CONSTRAINT reject_second_item CHECK (product_id <> " + second + ")");
        try {
            assertThatThrownBy(() -> orders.create(request)).isInstanceOf(DataAccessException.class);
            assertNoOrderWrites();
            assertThat(stock(productId)).isEqualTo(10);
            assertThat(stock(second)).isEqualTo(5);
        } finally {
            jdbc.execute("ALTER TABLE order_item DROP CHECK reject_second_item");
        }
    }

    private Long product(String name, String price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setVersion(0);
        products.insert(product);
        return product.getId();
    }

    private int stock(Long id) {
        return jdbc.queryForObject("SELECT stock FROM product WHERE id = ?", Integer.class, id);
    }

    private void assertNoOrderWrites() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM order_item", Integer.class)).isZero();
    }

    private String body(Long id, int quantity) {
        return "{\"userId\":%d,\"items\":[{\"productId\":%d,\"quantity\":%d}]}"
                .formatted(userId, id, quantity);
    }

    private String multiBody(Long first, int firstQuantity, Long second, int secondQuantity) {
        return "{\"userId\":%d,\"items\":[{\"productId\":%d,\"quantity\":%d},{\"productId\":%d,\"quantity\":%d}]}"
                .formatted(userId, first, firstQuantity, second, secondQuantity);
    }

    private long create(String body, int expectedCode) throws Exception {
        var result = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(expectedCode)).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private List<Integer> parallel(int count, Callable<Integer> action) throws Exception {
        var pool = Executors.newFixedThreadPool(count);
        var start = new CyclicBarrier(count);
        try {
            var futures = new ArrayList<java.util.concurrent.Future<Integer>>();
            for (int i = 0; i < count; i++) futures.add(pool.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return action.call();
            }));
            List<Integer> results = new ArrayList<>();
            for (var future : futures) results.add(future.get(20, TimeUnit.SECONDS));
            return results;
        } finally {
            pool.shutdownNow();
            assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).as("并发工作线程必须退出").isTrue();
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RaceConfiguration {
        @Bean OrderReadGate orderReadGate() { return new OrderReadGate(); }
    }

    /** 仅改变测试中的执行时序；所有 SQL 与事务仍由真实 MySQL 执行，不伪造返回值。 */
    @Intercepts(@Signature(type = Executor.class, method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
    static class OrderReadGate implements Interceptor {
        volatile CyclicBarrier barrier;

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            Object result = invocation.proceed();
            var gate = barrier;
            var statement = (MappedStatement) invocation.getArgs()[0];
            if (gate != null && statement.getId().equals("com.javafix.shop.mapper.OrderMapper.selectById")) {
                gate.await(10, TimeUnit.SECONDS);
            }
            return result;
        }
    }
}
