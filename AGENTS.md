# JavaFix Agent Project Instructions

## Scope

JavaFix Agent is a monorepo for building a Python coding agent that can analyze,
modify, test, and verify a Java Spring Boot and MyBatis-Plus project.

Keep these roles separate:

- Codex develops and maintains this repository.
- JavaFix Agent is the runtime product developed in `agent/`.
- `shop-service/` is the controlled Java target used by the agent.

## Current phase

The repository is currently in **Phase 1 — Repository Initialization**.

During Phase 1:

- Maintain only the repository structure and project documentation.
- Do not add Spring Boot business code.
- Do not add Python agent implementation code.
- Do not introduce LangChain, LangGraph, MCP, multi-agent orchestration, RAG,
  vector databases, or Redis.

Proceed to Phase 2 only after explicit user approval.

## Architecture constraints

- Python 3.12+ for the coding agent.
- Java 21, Spring Boot 3.x, Maven, JUnit 5, Mockito, and Flyway for the Java target.
- Use MyBatis-Plus; do not use Spring Data JPA.
- Prefer explicit, small, testable designs over framework-heavy abstractions.
- Keep agent runtime concepts separate: Model, Agent, State, Action,
  Observation, Environment, and Config.

## Engineering rules

- Inspect the repository and Git state before making changes.
- Keep changes scoped to the requested development phase.
- For testable features and bug fixes, follow FAIL -> PASS -> regression.
- Never weaken, delete, or bypass a valid test to make a build pass.
- Treat test output, command output, and Git state as ground truth.
- Run relevant verification and inspect `git status` and `git diff` before
  reporting completion.
- Do not commit automatically unless the user explicitly requests it.

## Safety rules

- Bind agent execution to an explicitly configured workspace.
- Do not expose secrets in source code, logs, prompts, or benchmark artifacts.
- Enforce command timeouts and report timeout, cancellation, and fatal errors as
  non-success outcomes.
- Do not report success solely because the model requested to finish.

