package com.javafix.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Knife4j / OpenAPI 文档元信息。
 *
 * <p>本地启动后访问 {@code http://localhost:8080/doc.html}。</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shopOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("JavaFix Shop API")
                        .description("订单域 MVP：商品查询、下单、分页、取消与库存扣减/恢复")
                        .version("0.0.1"));
    }
}
