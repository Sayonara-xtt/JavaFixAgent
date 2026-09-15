# Phase 2 Spring Boot Baseline Implementation Plan

**Goal:** Build and verify a Java 21 Spring Boot baseline backed by the local
MySQL 8 service, MyBatis-Plus, and Flyway without adding Phase 3 business code.

**Architecture:** `shop-service` is an independent Maven project in the
monorepo. Runtime configuration is environment-backed. The integration test
uses a dedicated local MySQL schema and limited account created by a reviewed
bootstrap script.

**Tech Stack:** Java 21, Spring Boot 3.5.16, MyBatis-Plus 3.5.17, MySQL 8,
Flyway, Maven Wrapper 3.9.11, JUnit 5, Mockito, and AssertJ.

**Spec:** `docs/superpowers/specs/2026-09-15-phase-2-spring-boot-baseline-design.md`

## Global constraints

- Use Java release 21 and Maven 3.9.x.
- Use MyBatis-Plus; never add Spring Data JPA.
- Use the dedicated local MySQL test database; never silently use H2.
- Do not reset or embed the developer's MySQL administrative password.
- Add infrastructure only; no Phase 3 business behavior.
- Add concise Chinese explanations for responsibilities and non-obvious
  configuration.
- Do not commit unless explicitly authorized.

## Task 1: Maven build and wrapper

Files: `shop-service/pom.xml`, `mvnw`, `mvnw.cmd`, and
`.mvn/wrapper/maven-wrapper.properties`.

- [x] Add Spring Boot, validation, MyBatis-Plus, MySQL, Flyway, and test
  dependencies.
- [x] Pin Maven Wrapper to 3.9.11.
- [x] Enforce Java 21 and Maven 3.9.x.
- [x] Verify the wrapper reports Maven 3.9.11 and Java 21.

## Task 2: Test-first startup contract

Files: `ShopApplicationTests.java` and `application-test.yml`.

- [x] Write the Spring Boot startup integration test before the application.
- [x] Point the test profile to `javafix_shop_test` using `javafix_test`.
- [x] Verify the red result is caused by the missing Spring Boot application
  configuration.
- [x] Keep URL and credentials overridable through environment variables.

## Task 3: Minimal application and migration

Files: `ShopApplication.java`, `application.yml`, `application-local.yml`,
`V1__initialize_database.sql`, and `resources/mapper/.gitkeep`.

- [x] Add the documented Spring Boot entry point.
- [x] Add environment-backed local MySQL runtime configuration.
- [x] Add Flyway and MyBatis XML locations.
- [x] Add infrastructure-only migration version `1`.
- [x] Run the targeted test successfully after the local database bootstrap.

## Task 4: Local MySQL bootstrap

File: `scripts/mysql/init-test-database.sql`.

- [x] Add an idempotent script for the dedicated schema and limited account.
- [x] Document that it does not reset the root password.
- [x] Execute it once in MySQL Workbench as an administrative user.
- [x] Confirm the integration test can authenticate, migrate, and connect.

## Task 5: Package documentation and project guidance

- [x] Add Chinese package-level responsibility documentation.
- [x] Update `README.md`, `AGENTS.md`, `.env.example`, and `Makefile` for the
  local MySQL workflow.
- [x] Update the Phase 2 design and plan from Testcontainers to local MySQL.
- [x] Run the full regression suite.
- [x] Run `git diff --check` and inspect Git state and diff.
- [x] Confirm there is no Spring Data JPA dependency or Phase 3 business code.

## Suggested commit boundary

After all checks pass and only with explicit user authorization:

```text
feat: establish Spring Boot infrastructure baseline
```
