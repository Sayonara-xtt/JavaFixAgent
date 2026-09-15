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

Docker-based isolation is planned for a later phase.

## Current phase

**Phase 1 — Repository Initialization**

Only the monorepo structure and project-level documentation exist at this
stage. There is no Java business implementation or Python agent implementation
yet.

## High-level roadmap

1. Initialize the monorepo structure.
2. Establish a clean Spring Boot and MyBatis-Plus baseline.
3. Implement the order-domain test application.
4. Build the Python agent abstractions and CLI.
5. Add a controlled shell environment and the manual agent loop.
6. Demonstrate the first test-driven automatic bug fix.
7. Add reproducible benchmarks, sandboxing, tracing, and evaluation.

The next phase begins only after the Phase 1 structure has been reviewed and
approved.

## Repository status

Use Git as the source of truth for local changes:

```shell
git status
git diff
```

No automatic commit is performed by project tooling.

