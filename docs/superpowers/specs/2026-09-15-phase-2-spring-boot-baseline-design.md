# Phase 2 Spring Boot Baseline Design

## 1. Goal / 目标

Create a clean Spring Boot baseline in `shop-service/` using Java 21,
Spring Boot 3.5.16, MyBatis-Plus 3.5.17, local MySQL 8, Maven, JUnit 5,
Mockito, and Flyway.

在 `shop-service/` 中建立可验证的 Spring Boot 基线。Phase 2 只建设基础设施，
不实现用户、商品、订单、订单项、REST 接口或其他业务逻辑。

## 2. Selected approach / 选定方案

The startup integration test connects to the MySQL 8 instance already
installed on the developer machine. A dedicated schema and limited account
isolate test state from the normal application database:

- Schema: `javafix_shop_test`
- Account: `javafix_test`
- Bootstrap script: `scripts/mysql/init-test-database.sql`

测试库初始化必须由具备管理权限的开发者执行。脚本不会读取、更改或重置 MySQL
root 密码。Docker 和 Testcontainers 暂不参与 Phase 2 验证。

## 3. Version and dependency policy / 版本与依赖策略

- Java release: 21.
- Spring Boot parent: 3.5.16.
- MyBatis-Plus Spring Boot 3 starter: 3.5.17.
- Maven Wrapper distribution: 3.9.11.
- MySQL: locally installed MySQL 8.x.
- JUnit 5 and Mockito: managed by `spring-boot-starter-test`.
- Flyway: `flyway-core` plus the MySQL database module.
- Testcontainers dependencies are not included in the selected local setup.

Maven Enforcer rejects Java outside `[21,22)` and Maven outside `[3.9,4.0)`.

## 4. Project structure / 项目结构

```text
shop-service/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/wrapper/maven-wrapper.properties
└── src/
    ├── main/
    │   ├── java/com/javafix/shop/
    │   │   ├── ShopApplication.java
    │   │   ├── controller/package-info.java
    │   │   ├── service/package-info.java
    │   │   ├── service/impl/package-info.java
    │   │   ├── mapper/package-info.java
    │   │   ├── entity/package-info.java
    │   │   ├── dto/package-info.java
    │   │   ├── vo/package-info.java
    │   │   ├── enums/package-info.java
    │   │   ├── exception/package-info.java
    │   │   ├── config/package-info.java
    │   │   └── common/package-info.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.yml
    │       ├── mapper/.gitkeep
    │       └── db/migration/V1__initialize_database.sql
    └── test/
        ├── java/com/javafix/shop/ShopApplicationTests.java
        └── resources/application-test.yml
```

`package-info.java` files make intended package boundaries visible and carry
concise Chinese responsibility documentation without adding placeholder
classes.

## 5. Build configuration / 构建配置

`pom.xml` contains only the Phase 2 infrastructure dependencies:

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `mybatis-plus-spring-boot3-starter`
- `mysql-connector-j`
- `flyway-core` and `flyway-mysql`
- `spring-boot-starter-test`

Spring Data JPA and Phase 3 business dependencies are intentionally excluded.

## 6. Configuration / 配置

`application.yml` uses environment-backed runtime values:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`
- Flyway migrations from `classpath:db/migration`
- MyBatis XML mappings from `classpath*:mapper/**/*.xml`

`application-test.yml` uses the dedicated local test schema. Its URL, username,
and password can be overridden by `JAVAFIX_TEST_DB_URL`,
`JAVAFIX_TEST_DB_USERNAME`, and `JAVAFIX_TEST_DB_PASSWORD`.

测试默认凭据只服务于本机隔离测试账户；运行时真实密码不得写入仓库。关键英文配置添加
中文说明，但不逐行翻译显而易见的语法。

## 7. Migration baseline / 迁移基线

`V1__initialize_database.sql` executes a MySQL-compatible no-op query. It lets
Flyway record version `1` without creating business tables. Phase 3 business
tables must begin in a later migration, and an applied migration must not be
edited retroactively.

## 8. Startup test and data flow / 启动测试与数据流

```text
Maven test
  -> application-test.yml selects the dedicated local MySQL schema
  -> Spring Boot creates the application context
  -> Flyway applies V1__initialize_database.sql
  -> MyBatis-Plus creates SqlSessionFactory
  -> assertions verify context, connection, migration, and MyBatis
```

`ShopApplicationTests` verifies:

1. The application context starts.
2. The DataSource opens a valid local MySQL connection.
3. Flyway migration version `1` is applied.
4. MyBatis-Plus contributes a `SqlSessionFactory` bean.

## 9. Failure behavior / 失败行为

- If the local MySQL service is unavailable, the test must fail clearly.
- If the dedicated schema/account is missing, the test must fail with the
  corresponding MySQL connection or authentication error.
- The test must not silently fall back to H2 or another in-memory database.
- If a Flyway migration fails, Spring context startup and Maven must fail.
- Incompatible Java or Maven versions must fail early through Maven Enforcer.
- No task may reset the developer's MySQL administrative password.

## 10. Verification / 验证

Initialize the test schema once through MySQL Workbench using
`scripts/mysql/init-test-database.sql`, then run on Windows:

```powershell
cd shop-service
.\mvnw.cmd test
```

Acceptance requires Maven exit code 0, `BUILD SUCCESS`, a passing startup test,
no whitespace errors, no Spring Data JPA, and no Phase 3 business code. Inspect
`git status` and `git diff`; do not commit without explicit authorization.

## 11. Out of scope / 非本阶段范围

- User, Product, Order, and OrderItem entities or tables.
- Controllers, services, mappers, DTOs, and business exceptions.
- Order workflows, inventory, pagination, and REST APIs.
- Python Agent implementation.
- Docker Compose, Testcontainers, and the future agent sandbox.
- CI and benchmark implementation.
