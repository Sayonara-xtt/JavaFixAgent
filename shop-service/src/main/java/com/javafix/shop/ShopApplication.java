package com.javafix.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JavaFix 商城靶场应用入口。
 *
 * <p>Phase 2 只验证基础设施启动，业务能力将在后续阶段实现。</p>
 */
@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
