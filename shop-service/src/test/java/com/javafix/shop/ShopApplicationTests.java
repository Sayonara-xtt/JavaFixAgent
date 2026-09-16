package com.javafix.shop;

import javax.sql.DataSource;
import org.testcontainers.containers.MySQLContainer;

import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Import(MySqlTestConfiguration.class)
class ShopApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private MySQLContainer<?> mysqlContainer;

    @Test
    void contextLoadsWithIsolatedMySqlInfrastructure() throws Exception {
        assertThat(applicationContext).isNotNull();
        assertThat(sqlSessionFactory).isNotNull();

        try (var connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            // 自动测试不得连接本机已有的开发/测试库。
            assertThat(connection.getCatalog()).isEqualTo("javafix_container_test");
            // 验证实际 JDBC 连接指向容器随机映射的端口，而非本机默认库。
            assertThat(connection.getMetaData().getURL()).startsWith(
                    "jdbc:mysql://" + mysqlContainer.getHost() + ":"
                            + mysqlContainer.getMappedPort(3306) + "/javafix_container_test");
        }

        // 确认启动成功来自真实迁移，而不只是 Spring 容器创建成功。
        assertThat(flyway.info().applied())
                .extracting(info -> info.getVersion().getVersion())
                .contains("1", "2");
    }
}
