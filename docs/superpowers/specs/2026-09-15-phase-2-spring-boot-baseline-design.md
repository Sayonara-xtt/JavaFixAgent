# Phase 2 Spring Boot Baseline Design

## 1. Goal / 目标

Create a clean, reproducible Spring Boot baseline in `shop-service/` using
Java 21, Spring Boot 3.5.16, MyBatis-Plus 3.5.17, MySQL 8, Maven, JUnit 5,
Mockito, and Flyway.

在 `shop-service/` 中建立干净、可重复验证的 Spring Boot 基线，技术栈为
Java 21、Spring Boot 3.5.16、MyBatis-Plus 3.5.17、MySQL 8、Maven、
JUnit 5、Mockito 与 Flyway。

Phase 2 establishes infrastructure only. It does not implement User, Product,
Order, OrderItem, REST endpoints, or other business behavior.

Phase 2 只建立基础设施，不实现用户、商品、订单、订单项、REST 接口或其他业务逻辑。

## 2. Selected approach / 选定方案

Use a real MySQL 8 container for the Spring Boot startup test through
Testcontainers. The test owns the database lifecycle and supplies the JDBC
connection to Spring Boot through `@ServiceConnection`.

启动测试通过 Testcontainers 使用真实 MySQL 8。测试负责数据库生命周期，并通过
`@ServiceConnection` 将 JDBC 连接交给 Spring Boot。

This approach is selected because it verifies the actual MySQL dialect and
Flyway integration without depending on a manually maintained local database.
It requires a running Docker engine when integration tests execute.

选择该方案是为了验证真实 MySQL 方言和 Flyway 集成，同时避免依赖人工维护的本地数据库。
运行集成测试时必须保证 Docker 引擎可用。

## 3. Version and dependency policy / 版本与依赖策略

- Java release: 21.
- Spring Boot parent: 3.5.16.
- MyBatis-Plus Spring Boot 3 starter: 3.5.17.
- Maven: a fixed Maven 3.9.x release downloaded through Maven Wrapper.
- MySQL: the official `mysql:8.4` LTS image for integration tests.
- JUnit 5 and Mockito: managed by `spring-boot-starter-test`.
- Testcontainers: managed through Spring Boot dependency management where
  available; do not add an independent version unless Maven resolution proves
  it is required.

版本统一在 `pom.xml` 或 Maven Wrapper 属性中固定，避免开发环境和 CI 使用不同工具版本。

## 4. Project structure / 项目结构

```text
shop-service/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
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
        └── java/com/javafix/shop/ShopApplicationTests.java
```

`package-info.java` files make the intended package boundaries visible to Git
and carry concise Chinese package documentation without adding placeholder
classes.

使用 `package-info.java` 固化包结构并提供简短中文包说明，不为占位而创建无意义的 Java 类。

## 5. Build configuration / 构建配置

`pom.xml` uses `spring-boot-starter-parent` and declares:

- `spring-boot-starter-web` for the future REST application baseline.
- `spring-boot-starter-validation` for future request validation.
- `mybatis-plus-spring-boot3-starter` for persistence integration.
- `mysql-connector-j` at runtime.
- `flyway-core` and `flyway-mysql` for schema migration.
- `spring-boot-starter-test` for JUnit 5 and Mockito.
- `spring-boot-testcontainers`, Testcontainers JUnit Jupiter, and the
  Testcontainers MySQL module for integration testing.

The Spring Boot Maven plugin packages an executable application. Maven compiler
configuration targets Java 21. Dependencies are not added for Phase 3 business
features.

Spring Boot Maven 插件负责生成可执行应用；编译目标固定为 Java 21，不提前添加 Phase 3
业务功能所需依赖。

## 6. Configuration / 配置

`application.yml` contains shared application settings and environment-backed
MySQL connection defaults:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` configure
  the local runtime database.
- Flyway is enabled and reads migrations from `classpath:db/migration`.
- MyBatis mapper XML files are loaded from `classpath*:mapper/**/*.xml`.
- Secrets are never stored with real values in the repository.

`application-local.yml` contains developer-friendly logging and local profile
settings only. It does not duplicate or hard-code credentials.

配置文件中的关键英文属性添加中文说明，实际密码只通过环境变量或本地未跟踪配置传入。

## 7. Migration baseline / 迁移基线

`V1__initialize_database.sql` is an infrastructure-only Flyway migration. It
executes a MySQL-compatible no-op query and records a successful versioned
migration without creating business tables.

`V1__initialize_database.sql` 是仅用于基础设施验证的 Flyway 迁移。它执行兼容 MySQL
的空操作查询，使 Flyway 记录成功的版本迁移，但不创建任何业务表。

Business tables begin in Phase 3 using later versioned migrations. Existing
version numbers are never edited after becoming part of the clean baseline.

业务表从 Phase 3 的后续版本迁移开始；进入干净基线后的迁移版本不得回写修改。

## 8. Startup test and data flow / 启动测试与数据流

The test flow is:

```text
Maven test
  -> Testcontainers starts mysql:8.4
  -> @ServiceConnection supplies JDBC properties
  -> Spring Boot creates the application context
  -> Flyway applies V1__initialize_database.sql
  -> MyBatis-Plus auto-configuration loads
  -> assertions verify context, DataSource, Flyway, and migration state
```

`ShopApplicationTests` is a `@SpringBootTest`. It verifies:

1. The application context starts.
2. A `DataSource` bean is available and can open a connection.
3. A `Flyway` bean exists.
4. Migration version `1` is applied successfully.
5. MyBatis-Plus contributes a `SqlSessionFactory` bean.

The test does not require a permanent local schema and does not reuse database
state across test runs.

测试不依赖永久本地数据库，也不在不同测试运行之间复用数据库状态。

## 9. Failure behavior / 失败行为

- If Docker is unavailable, the test fails with a clear Testcontainers startup
  error; it must not silently fall back to H2.
- If MySQL cannot become healthy, the test fails before application assertions.
- If Flyway migration fails, Spring context startup fails and Maven reports a
  failed test.
- If the Java or Maven version is incompatible, Maven Enforcer fails early with
  a readable requirement message.
- Credentials and full environment dumps must not be printed by custom code.

Docker、MySQL、Flyway 或工具版本不满足要求时必须明确失败，不能通过降低验证强度伪造成功。

## 10. Documentation and annotations / 文档与注释

- Update the root `README.md` current phase and add Windows and Unix test
  commands with Chinese explanations.
- Update `AGENTS.md` so Phase 2 infrastructure work is allowed while Phase 3
  business code remains prohibited.
- Use Chinese Javadoc for application and package responsibilities.
- Use Chinese YAML and SQL comments where configuration intent is not obvious.
- Do not add comments that merely translate Java syntax or repeat a method name.

注释用于解释职责、边界和配置意图，不逐行翻译显而易见的代码。

## 11. Verification / 验证

From `shop-service/` on Windows:

```powershell
.\mvnw.cmd test
```

From Unix-like systems:

```bash
./mvnw test
```

Acceptance requires:

- Maven exits with code 0 and reports `BUILD SUCCESS`.
- The startup integration test passes against MySQL 8.4.
- `git diff --check` reports no whitespace errors.
- `git status` and `git diff` are inspected.
- No Spring Data JPA dependency or Phase 3 business implementation exists.
- No Git commit is created without explicit user authorization.

## 12. Out of scope / 非本阶段范围

- User, Product, Order, and OrderItem entities or tables.
- Controllers, services, mappers, DTOs, and business exceptions.
- Order creation, cancellation, inventory, pagination, or REST APIs.
- Python Agent implementation.
- Docker Compose and the future agent sandbox.
- CI workflow implementation.
- Benchmark task implementation.

