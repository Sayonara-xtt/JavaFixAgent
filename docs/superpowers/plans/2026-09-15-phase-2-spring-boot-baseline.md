# Phase 2 Spring Boot Baseline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and verify a Java 21 Spring Boot baseline backed by a real MySQL 8.4 Testcontainer, MyBatis-Plus, and Flyway without adding Phase 3 business code.

**Architecture:** `shop-service` is an independent Maven module inside the monorepo. Runtime configuration uses environment-backed MySQL settings, while the startup integration test owns an isolated MySQL container and supplies its connection through Spring Boot service connections. A versioned no-op migration proves Flyway execution before business tables are introduced.

**Tech Stack:** Java 21, Spring Boot 3.5.16, MyBatis-Plus 3.5.17, MySQL 8.4, Flyway, Maven Wrapper 3.9.x, JUnit 5, Mockito, AssertJ, Testcontainers.

**Spec:** `docs/superpowers/specs/2026-09-15-phase-2-spring-boot-baseline-design.md`

## Global Constraints

- Use Java release 21.
- Use Spring Boot parent 3.5.16.
- Use `mybatis-plus-spring-boot3-starter` 3.5.17.
- Use the official `mysql:8.4` image in startup integration tests.
- Use MyBatis-Plus and never add Spring Data JPA.
- Add infrastructure only; do not implement User, Product, Order, OrderItem, REST endpoints, or business behavior.
- Add concise Chinese explanations for package responsibilities and non-obvious English configuration; do not translate obvious syntax line by line.
- Do not commit unless the user explicitly authorizes a commit.

---

### Task 1: Maven build and wrapper

**Files:**
- Delete: `shop-service/.gitkeep`
- Create: `shop-service/pom.xml`
- Create through the official Maven Wrapper plugin: `shop-service/mvnw`
- Create through the official Maven Wrapper plugin: `shop-service/mvnw.cmd`
- Create through the official Maven Wrapper plugin: `shop-service/.mvn/wrapper/maven-wrapper.properties`

**Interfaces:**
- Consumes: Java 21 and Docker for wrapper generation because Maven is not installed globally.
- Produces: `shop-service/mvnw` and `shop-service/mvnw.cmd`, which all later tasks use to build and test the module.

- [ ] **Step 1: Create the Maven project descriptor**

Create `shop-service/pom.xml` with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.16</version>
        <relativePath/>
    </parent>
    <groupId>com.javafix</groupId>
    <artifactId>shop-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>shop-service</name>
    <description>JavaFix Agent 的 Spring Boot 软件工程靶场</description>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.17</mybatis-plus.version>
        <maven-enforcer-plugin.version>3.6.2</maven-enforcer-plugin.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-enforcer-plugin</artifactId>
                <version>${maven-enforcer-plugin.version}</version>
                <executions>
                    <execution>
                        <id>enforce-build-environment</id>
                        <goals><goal>enforce</goal></goals>
                        <configuration>
                            <rules>
                                <requireJavaVersion><version>[21,22)</version></requireJavaVersion>
                                <requireMavenVersion><version>[3.9,4.0)</version></requireMavenVersion>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Generate the Maven Wrapper from the official plugin**

Run a Maven 3.9.x Java 21 container against the verified absolute
`shop-service` directory:

```powershell
docker run --rm --mount "type=bind,source=D:\codework\JavaFixAgent\shop-service,target=/workspace" --workdir /workspace maven:3.9.11-eclipse-temurin-21 mvn wrapper:wrapper -Dtype=only-script -DmavenVersion=3.9.11
```

Expected: exit code 0 and the wrapper scripts plus
`.mvn/wrapper/maven-wrapper.properties` are created.

- [ ] **Step 3: Verify the wrapper and dependency model**

Run:

```powershell
Set-Location shop-service
.\mvnw.cmd --version
.\mvnw.cmd dependency:tree -Dscope=test
```

Expected: Maven 3.9.11, Java 21, resolved Spring Boot/MyBatis-Plus/Flyway/
Testcontainers dependencies, and no Spring Data JPA dependency.

- [ ] **Step 4: Record the suggested commit boundary**

Suggested commit after authorization:

```text
build: initialize shop service dependencies
```

Do not execute the commit without explicit user authorization.

---

### Task 2: Failing infrastructure startup test

**Files:**
- Create: `shop-service/src/test/java/com/javafix/shop/ShopApplicationTests.java`

**Interfaces:**
- Consumes: Maven build and Testcontainers dependencies from Task 1.
- Produces: `contextLoadsWithMySqlInfrastructure()`, the Phase 2 acceptance test that later production files must satisfy.

- [ ] **Step 1: Write the startup integration test before the application**

```java
package com.javafix.shop;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ShopApplicationTests {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("javafix_shop")
            .withUsername("javafix")
            .withPassword("javafix");

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void contextLoadsWithMySqlInfrastructure() throws Exception {
        assertThat(applicationContext).isNotNull();
        assertThat(sqlSessionFactory).isNotNull();
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
        }
        assertThat(flyway.info().applied())
                .extracting(info -> info.getVersion().getVersion())
                .contains("1");
    }
}
```

The container credentials are isolated test fixtures, not user secrets. Add
Chinese comments only for the container lifecycle and migration assertion.

- [ ] **Step 2: Run the test and confirm the expected failure**

Run:

```powershell
Set-Location shop-service
.\mvnw.cmd test
```

Expected: FAIL because no `@SpringBootConfiguration` application class exists.
If Docker is unavailable, diagnose Docker first; a Docker failure is not the
required application-level red result.

- [ ] **Step 3: Record the red result**

Capture the Maven exit code and the error proving the missing application
configuration. Do not weaken the test or replace MySQL with H2.

---

### Task 3: Minimal application, configuration, and migration

**Files:**
- Create: `shop-service/src/main/java/com/javafix/shop/ShopApplication.java`
- Create: `shop-service/src/main/resources/application.yml`
- Create: `shop-service/src/main/resources/application-local.yml`
- Create: `shop-service/src/main/resources/db/migration/V1__initialize_database.sql`
- Create: `shop-service/src/main/resources/mapper/.gitkeep`

**Interfaces:**
- Consumes: `contextLoadsWithMySqlInfrastructure()` from Task 2.
- Produces: Spring Boot application context, environment-backed MySQL runtime configuration, Flyway migration version `1`, and MyBatis mapper location configuration.

- [ ] **Step 1: Create the application entry point**

```java
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
```

- [ ] **Step 2: Add shared and local configuration**

Create `application.yml` with the application name, environment-backed MySQL
URL and credentials, Flyway location, and MyBatis-Plus mapper location. Use
`${DB_HOST:localhost}`, `${DB_PORT:3306}`, `${DB_NAME:javafix_shop}`,
`${DB_USERNAME:javafix}`, and `${DB_PASSWORD:javafix}` defaults. Add concise
Chinese comments explaining that defaults are local-only and real secrets must
come from the environment.

Create `application-local.yml` with developer logging levels only:

```yaml
# 本地开发日志配置，不包含数据库密钥。
logging:
  level:
    com.javafix.shop: DEBUG
```

- [ ] **Step 3: Add the infrastructure-only Flyway migration**

```sql
-- Phase 2 仅验证 Flyway 能在真实 MySQL 上执行版本迁移；业务表从 Phase 3 开始创建。
SELECT 1;
```

- [ ] **Step 4: Run the targeted test and confirm green**

Run:

```powershell
Set-Location shop-service
.\mvnw.cmd -Dtest=ShopApplicationTests test
```

Expected: one test passes and Maven reports `BUILD SUCCESS`.

- [ ] **Step 5: Record the suggested commit boundary**

Suggested commit after authorization:

```text
feat: add verified Spring Boot baseline
```

Do not execute the commit without explicit user authorization.

---

### Task 4: Package documentation and project guidance

**Files:**
- Create: `shop-service/src/main/java/com/javafix/shop/controller/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/service/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/service/impl/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/mapper/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/entity/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/dto/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/vo/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/enums/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/exception/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/config/package-info.java`
- Create: `shop-service/src/main/java/com/javafix/shop/common/package-info.java`
- Modify: `README.md`
- Modify: `AGENTS.md`
- Modify: `Makefile`

**Interfaces:**
- Consumes: verified commands and configuration from Task 3.
- Produces: tracked Phase 3 package boundaries and accurate bilingual developer instructions.

- [ ] **Step 1: Add package-level Chinese documentation**

Each `package-info.java` contains one responsibility statement and the exact
package declaration. For example:

```java
/** REST 接口层：负责请求校验、协议转换和响应输出。 */
package com.javafix.shop.controller;
```

Use the corresponding responsibility for service, implementation, mapper,
entity, DTO, VO, enums, exceptions, configuration, and shared components.

- [ ] **Step 2: Update root documentation**

Update `README.md` to mark Phase 2 as implemented, document the Docker
prerequisite, and show both commands:

```powershell
cd shop-service
.\mvnw.cmd test
```

```bash
cd shop-service
./mvnw test
```

Update `AGENTS.md` to allow Phase 2 infrastructure maintenance while keeping
Phase 3 business implementation prohibited. Update `Makefile` with a
`java-test` target that runs `./shop-service/mvnw test -f shop-service/pom.xml`
on Unix-like systems.

- [ ] **Step 3: Run the full regression suite**

Run:

```powershell
Set-Location shop-service
.\mvnw.cmd test
```

Expected: all tests pass and Maven reports `BUILD SUCCESS`.

- [ ] **Step 4: Verify scope and repository state**

Run:

```powershell
git diff --check
git status --short --branch
git diff
```

Search for prohibited dependencies and premature business implementations:

```powershell
rg "spring-data-jpa|JpaRepository|@Entity" .
rg --files shop-service/src/main/java
```

Expected: the first search finds no prohibited persistence dependency or JPA
code. The Java file list contains only `ShopApplication.java` and the planned
`package-info.java` files.

- [ ] **Step 5: Record the suggested commit boundary**

Suggested commit after authorization:

```text
docs: document phase 2 development workflow
```

Do not execute the commit without explicit user authorization.

