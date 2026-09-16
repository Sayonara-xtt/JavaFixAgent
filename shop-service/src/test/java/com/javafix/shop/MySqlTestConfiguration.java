package com.javafix.shop;

import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

/**
 * 测试专用数据库：Spring 管理生命周期，避免缓存上下文引用已停止的容器。
 * 使用随机宿主机端口、不复用容器，不读取本机 MySQL 账号或密码。
 */
@TestConfiguration(proxyBeanMethods = false)
class MySqlTestConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.4")
                .withDatabaseName("javafix_container_test")
                .withUsername("javafix_test")
                .withPassword("container_test_only")
                .withReuse(false)
                .withStartupTimeout(Duration.ofMinutes(3));
    }
}
