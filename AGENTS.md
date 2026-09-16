# JavaFix Agent Project Instructions
# JavaFix Agent 项目指令（给 AI / 编码助手）

> **Codex Execution Contract / Codex 执行约束**  
> 本文只保存当前必须遵守的规则。完整架构见专项文档，不要把系统蓝图整份复制到这里。

## Before implementation / 动手前

1. Read this file (`AGENTS.md`)
2. Read [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
3. Read [`docs/plans/CURRENT_PHASE.md`](docs/plans/CURRENT_PHASE.md)
4. Read the task-specific document:
   - Java → [`docs/java/JAVA_ARCHITECTURE.md`](docs/java/JAVA_ARCHITECTURE.md)
   - Agent Runtime → [`docs/agent/AGENT_RUNTIME.md`](docs/agent/AGENT_RUNTIME.md)
   - Benchmark → [`docs/benchmark/BENCHMARK.md`](docs/benchmark/BENCHMARK.md)
5. Inspect repository; run `git status`
6. Confirm current phase matches `CURRENT_PHASE.md`

文档地图（唯一）：[`docs/README.md`](docs/README.md)

## Scope / 范围

JavaFix Agent is a monorepo for building a Python coding agent that can analyze,
modify, test, and verify a Java Spring Boot and MyBatis-Plus project.

保持角色分离（勿混淆）：

- **Codex / 编码助手**：开发与维护本仓库本身。
- **JavaFix Agent**：运行时产品，代码在 `agent/`（尚未实现）。
- **`shop-service/`**：受控 Java 靶场，供 Agent 将来分析/修改/测试。

```text
Codex ≠ JavaFix Agent
```

## Current phase / 当前阶段

**Phase 3 — Business Domain（业务领域）**

Phase 3 MVP 已验收；当前仅维护本阶段。  
允许 / 禁止清单以 [`docs/plans/CURRENT_PHASE.md`](docs/plans/CURRENT_PHASE.md) 为准（避免两处重复）。  
验收证据：[`docs/plans/phase-3-acceptance.md`](docs/plans/phase-3-acceptance.md)。

本阶段硬禁令摘要：

- 禁止静默改用 H2 / 回退本机库冒充通过
- 禁止添加 Python Agent 实现
- 禁止引入 LangChain、LangGraph、MCP、多智能体、RAG、向量库、Redis
- 自动测试必须用 Testcontainers 临时 MySQL

进入 Phase 4 须用户显式批准，并同步更新本文与 `CURRENT_PHASE.md`。

## Architecture constraints / 架构约束

- Python 3.12+ for the coding agent.（Coding Agent 侧）
- Java 21, Spring Boot 3.x, Maven, JUnit 5, Mockito, Flyway.（靶场侧）
- Use MyBatis-Plus; **Do NOT use Spring Data JPA.**（用 MP，禁止 JPA）
- Prefer explicit, small, testable designs over framework-heavy abstractions.
- Keep agent runtime concepts separate: Model（模型抽象）, Agent（智能体调度器）, State（状态）, Action（动作）,
  Observation（环境反馈）, Environment（执行环境）, and Config（运行配置）.（见 `docs/agent/AGENT_RUNTIME.md`）

业务不变量完整定义：[`docs/java/JAVA_ARCHITECTURE.md`](docs/java/JAVA_ARCHITECTURE.md)

## Engineering rules / 工程规则

- Inspect the repository and Git state before making changes.
- Keep changes scoped to the requested development phase.
- For testable features and bug fixes, follow **FAIL → PASS → Regression**.
- Never weaken, delete, or bypass a valid test to make a build pass.
- Treat test output, command output, and Git state as **Ground Truth（真实依据）**.
- Run relevant verification and inspect `git status` and `git diff` before
  reporting completion.
- **Do not commit automatically unless the user explicitly requests it.**

## Safety rules / 安全规则

- Bind agent execution to an explicitly configured workspace.
- Do not expose secrets in source code, logs, prompts, or benchmark artifacts.
- Enforce command timeouts and report timeout, cancellation, and fatal errors as
  non-success outcomes.
- Do not report success solely because the model requested to finish.
  （Verified Stopping Condition（可验证停止条件）见 [`docs/agent/AGENT_RUNTIME.md`](docs/agent/AGENT_RUNTIME.md)）
