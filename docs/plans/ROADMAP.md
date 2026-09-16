# Development Roadmap（开发路线图）

> 长期阶段规划（Phase 1–13）。  
> **当前正在做什么**见 [`CURRENT_PHASE.md`](CURRENT_PHASE.md) 与根目录 [`AGENTS.md`](../../AGENTS.md)。  
> 本文不写本轮 Codex 临时任务清单。

系统总览：[`../ARCHITECTURE.md`](../ARCHITECTURE.md)

---

## Phase Overview

| Phase | Name | 中文 |
|------:|------|------|
| 1 | Repository Initialization | 仓库初始化 |
| 2 | Spring Boot Baseline | Spring Boot 基线 |
| 3 | Business Domain | 业务领域 |
| 4 | Python Agent Skeleton | Python Agent 骨架 |
| 5 | Shell Environment | Shell 执行环境 |
| 6 | Agent Loop | Agent 循环 |
| 7 | First Automatic Bug Fix | 首次自动修 Bug |
| 8 | Benchmark Framework | Benchmark 框架 |
| 9 | Docker Sandbox | Docker 沙箱 |
| 10 | Context Engineering | 上下文工程 |
| 11 | Tracing | 执行追踪 |
| 12 | Evaluation | 评估 |
| 13 | Advanced Capabilities | 高级能力 |

进入下一阶段须：**用户显式批准** + 更新 `AGENTS.md` / `CURRENT_PHASE.md`。

---

## Phase 1 — Repository Initialization

**目标：** 只创建 Monorepo 基础结构。

**主要产物：** `agent/`、`shop-service/`、`benchmark/tasks/`、`infra/`、`scripts/`、`docs/`、`.github/workflows/`、`.gitignore`、`.env.example`、`README.md`、`Makefile` 等。

**验收：** 结构就绪。**Do not** 在本阶段实现业务 / AI；**Do not** 引入 LangChain / LangGraph / MCP / Multi-Agent。

---

## Phase 2 — Spring Boot Baseline

**目标：** 初始化可启动、可测试的 `shop-service` 基线。

**主要产物：** Java 21、Spring Boot 3.x、MyBatis-Plus、MySQL、Maven、JUnit 5、Mockito、Flyway；包结构与启动测试。

**验收：** `mvn test` → **BUILD SUCCESS**。  
**Do NOT use Spring Data JPA.**

---

## Phase 3 — Business Domain

**目标：** 订单域 MVP。

**主要产物：** User / Product / Order / OrderItem；创建 / 查询 / 分页 / 取消；库存扣减与恢复；真 MySQL 不变量测试。

**验收：** 业务能力落地；干净基线满足全部适用 **Business Invariants**。详见 [`../java/JAVA_ARCHITECTURE.md`](../java/JAVA_ARCHITECTURE.md) 与 [`phase-3-acceptance.md`](phase-3-acceptance.md)。

---

## Phase 4 — Python Agent Skeleton

**目标：** Agent 骨架。

**主要产物：** Model、Environment、State、Agent、CLI。

**验收：** 骨架可运行。**Do not use LangGraph.** 不引入 MCP / Multi-Agent / Redis / Vector DB / RAG。

详见 [`../agent/AGENT_RUNTIME.md`](../agent/AGENT_RUNTIME.md)。

---

## Phase 5 — Shell Environment

**目标：** 在固定 workspace（`shop-service`）内执行命令。

**主要产物：** 能跑 `git status`、`rg`、`find`、`sed`、`mvn test`、`git diff` 等。

**验收：** Environment 绑定固定 cwd；不得默认操作整台机器。

---

## Phase 6 — Agent Loop

**目标：** 手写 Agent Loop。

**主要产物：** Task → LLM → Action → Shell → Observation → LLM；max steps、timeout、停止条件、history。

**验收：** 循环可运行；停止条件含 **Verified Stopping Condition**。

---

## Phase 7 — First Automatic Bug Fix

**目标：** 首次自动修 Bug。

**示例任务：** Insufficient inventory still allows order creation / 库存不足仍然可以创建订单。

**Agent 必须步骤：**

1. Search the repository and locate the relevant implementation.
2. Inspect existing tests and business rules.
3. Reproduce the defect.
4. Run an existing relevant test and confirm failure, or add a regression test that fails for the defect.
5. Make the minimum production-code change required to fix the defect.
6. Run the targeted test and confirm PASS.
7. Run the relevant regression suite and reach `BUILD SUCCESS`.
8. Run `git status` and inspect `git diff`.
9. Finish only when the result satisfies the Verified Stopping Condition.

**必须由 Agent 自己搜索和定位需要修改的文件**（不得直接告知精确文件）。

---

## Phase 8 — Benchmark Framework

**目标：** 可重复工程任务框架。

**主要产物：** ≥10 个任务（TASK-001…010 结构）：apply patch → run agent → evaluate → reset。

详见 [`../benchmark/BENCHMARK.md`](../benchmark/BENCHMARK.md)。

---

## Phase 9 — Docker Sandbox

**目标：** 从 local subprocess 演进到 isolated Docker。

**最终应限制：** CPU、Memory、Timeout、Filesystem、Network、Permissions。

**注意：** Docker Sandbox will be added later — 不是当前能力。

---

## Phase 10 — Context Engineering

**目标：** 按需加载上下文。

**原则：** **Do not send the whole repository to the LLM.**

流程：Search → Locate → Read → Reason → Search More If Needed。

---

## Phase 11 — Tracing

**目标：** 可追溯一次完整 run（LLM I/O、Action、Command、Observation、Exit code、Token、Latency、Error）。

---

## Phase 12 — Evaluation

**目标：** 对任务集打分：Solved Rate、Test Pass Rate、Average Steps、Token Usage、Cost、Latency。

---

## Phase 13 — Advanced Capabilities

**目标：** 仅在 MVP 可用之后：GitHub Issues / PRs、MCP、LangGraph、Multi-Agent 等。

**Only after the MVP works. / 仅在 MVP 可用后。**

---

## Engineering Philosophy（工程理念）

推荐演进顺序：

```text
Clean Java Baseline → Tests → Python Model Abstraction → Shell Environment
→ Agent Loop → Automatic Bug Fixing → Git Diff → Benchmark → Docker Sandbox
→ Context Engineering → Tracing → Evaluation → GitHub Integration → MCP → Multi-Agent
```

目标是：

> **clear, testable, reproducible, explainable Software Engineering Agent**  
> **清晰、可测试、可重复、可解释的软件工程智能体。**

不是构建最复杂的 AI 系统。

### Git / CI / Docker（高层）

- `main` must always be: **buildable / testable / clean**
- MVP 不要引入复杂 Git Flow
- **Do not commit automatically unless explicitly requested**
- **禁止直接把故意制造的 Bug 提交到 `main`**
- CI：Java `mvn test` + 未来 Python `pytest`
- Docker：初期可用 MySQL 容器；Agent Sandbox 在 Phase 9
