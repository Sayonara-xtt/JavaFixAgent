# JavaFix Agent

JavaFix Agent is a learning and engineering project for building a small,
explainable coding agent in Python. The agent will eventually search, modify,
test, and verify a Java Spring Boot and MyBatis-Plus application in a controlled
workspace.

This is not a chatbot project. Its goal is a reproducible software-engineering
loop grounded in source code, command results, automated tests, and Git diffs.

## Repository structure

```text
javafix-agent/
|-- AGENTS.md              Project rules for coding assistants
|-- README.md              Project overview and roadmap
|-- agent/                 Python coding-agent implementation
|-- shop-service/          Java Spring Boot target application
|-- benchmark/tasks/       Reproducible engineering tasks
|-- infra/                 MySQL and future sandbox infrastructure
|-- scripts/               Development helper scripts
|-- docs/                  Architecture, business rules, and plans
`-- .github/workflows/     Continuous-integration workflows
```

## Technology stack

### Coding agent

- Python 3.12+
- OpenAI API
- Pydantic, Typer, Rich, and pytest
- Git and a controlled shell environment

### Java target

- Java 21 and Spring Boot 3.x
- MyBatis-Plus (Spring Data JPA is intentionally excluded)
- Maven, JUnit 5, Mockito, and Flyway
- MySQL 8.x

Phase 2 tests use a dedicated database and account in the locally installed
MySQL 8 instance. Docker-based isolation remains a later option.

## Current phase

**Phase 2 — Spring Boot Baseline**

The Java target now has a Java 21 Spring Boot baseline with MyBatis-Plus,
Flyway, Maven Wrapper, and a real local MySQL startup test. Business code and
the Python agent remain out of scope until later phases.

## Local MySQL setup

The integration test deliberately uses a separate local schema instead of a
developer's normal application database. In MySQL Workbench, connect as an
administrative user and execute:

```text
scripts/mysql/init-test-database.sql
```

This creates only the local development schema `javafix_shop_test` and the
limited account `javafix_test`. The script does not change the MySQL root
password. Its visible credentials are local test fixtures, not production
secrets.

Run the full Java test suite on Windows:

```powershell
cd shop-service
.\mvnw.cmd test
```

On Unix-like systems:

```bash
cd shop-service
./mvnw test
```

Runtime database values such as `DB_PASSWORD` should be supplied through the
environment. Do not commit real credentials.

## High-level roadmap

1. Initialize the monorepo structure.
2. Establish a clean Spring Boot and MyBatis-Plus baseline.
3. Implement the order-domain test application.
4. Build the Python agent abstractions and CLI.
5. Add a controlled shell environment and the manual agent loop.
6. Demonstrate the first test-driven automatic bug fix.
7. Add reproducible benchmarks, sandboxing, tracing, and evaluation.

The Phase 2 baseline has passed against local MySQL. Phase 3 begins only after
explicit review and approval.

## Repository status

Use Git as the source of truth for local changes:

```shell
git status
git diff
```

No automatic commit is performed by project tooling.
