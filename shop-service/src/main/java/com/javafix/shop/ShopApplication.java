package com.javafix.shop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JavaFix 商城靶场应用入口。
 *
 * <p>Phase 3 提供订单域 MVP：商品查询、下单、分页、取消与库存扣减/恢复。</p>
 */
@SpringBootApplication
@MapperScan("com.javafix.shop.mapper")
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
