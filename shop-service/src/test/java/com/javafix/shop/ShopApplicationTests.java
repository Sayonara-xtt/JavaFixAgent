package com.javafix.shop;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class ShopApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void contextLoadsWithLocalMySqlInfrastructure() throws Exception {
        assertThat(applicationContext).isNotNull();
        assertThat(sqlSessionFactory).isNotNull();

        try (var connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
        }

        // 确认启动成功来自真实迁移，而不只是 Spring 容器创建成功。
        assertThat(flyway.info().applied())
                .extracting(info -> info.getVersion().getVersion())
                .contains("1");
    }
}
