# System Architecture Overview（系统总体架构）

> 第一次理解本项目时优先阅读本文。  
> 详细规则见专项文档；当前阶段硬约束见根目录 [`AGENTS.md`](../AGENTS.md)。  
> 英文术语首次出现时用「English（中文）」就地备注。

---

## Project Goal（项目目标）

JavaFix Agent 是一个 **Python-based Coding Agent（编程智能体）**，用于分析、修改、测试和验证 **Java Spring Boot + MyBatis-Plus** 项目中的软件工程任务。

最终 Agent 应能：

- 理解软件工程任务
- 搜索 Java 代码仓库
- 阅读 Java、XML、SQL、配置与测试
- 修改源代码
- 运行 Maven / JUnit
- 根据失败信息继续修复
- 检查 `git status` / `git diff`
- 输出可验证的最终结果
- 支持可重复的 Benchmark（基准测试）任务

**This is not a chatbot project. / 本项目不是聊天机器人项目。**

---

## Role Boundary（角色边界）

```text
Developer / User
        ↓
Codex（编码助手）
        ↓
Develops and maintains javafix-agent
```

与未来运行时：

```text
User Issue
        ↓
JavaFix Agent
        ↓
Analyzes / modifies / tests shop-service
```

必须明确：

```text
Codex ≠ JavaFix Agent
```

- **Codex / 编码助手**：按阶段开发与维护本 monorepo。
- **JavaFix Agent**：最终产品，代码在 `agent/`（尚未实现）。
- **`shop-service/`**：受控 Java 靶场，供 Agent 将来分析 / 修改 / 测试。

不要把 Codex 的开发规则与 JavaFix Agent 的运行时规则混为一层。

---

## Repository Architecture（仓库架构）

Monorepo（单仓库多模块）：`javafix-agent`

```text
javafix-agent/
├── AGENTS.md
├── README.md
├── agent/                 Python Coding Agent（Phase 4+）
├── shop-service/          Java Spring Boot + MyBatis-Plus 靶场
├── benchmark/             可重复 Agent 基准任务
├── infra/                 Docker / MySQL / 沙箱（后续阶段）
├── scripts/               开发辅助脚本
├── docs/                  架构、领域规则与计划
└── .github/workflows/     CI（持续集成）
```

| Path | Responsibility / 职责 |
|------|------------------------|
| `agent/` | Python Coding Agent implementation / Python 编程智能体实现 |
| `shop-service/` | Java business system / Java 示例业务系统 |
| `benchmark/` | Reproducible Agent tasks / 可重复基准任务 |
| `infra/` | Docker, MySQL, Sandbox / Docker、MySQL、沙箱 |
| `scripts/` | Development helper scripts / 开发辅助脚本 |
| `docs/` | Architecture, domain rules, plans / 架构、领域规则与计划 |
| `.github/workflows/` | GitHub Actions CI |

---

## System Runtime Flow（系统运行流程）

```text
Task（任务）
 ↓
Agent（智能体调度）
 ↓
Model（模型抽象 / LLM）
 ↓
Action（动作）
 ↓
Environment（执行环境）
 ↓
Observation（环境反馈）
 ↓
Agent Loop（智能体循环）
```

典型落地路径：

```text
任务 Task（任务）
  → Coding Agent（编程智能体）决策
  → LLM 生成下一步
  → Action（动作，多为 Shell）
  → Environment（执行环境）在 shop-service 执行
  → Maven / JUnit / git 产生 Observation（环境反馈）
  → 写回历史，进入下一轮 Agent Loop（智能体循环）
  → 直到 Verified Success（可验证成功）或超时 / 超步数
```

运行时细节见 [`agent/AGENT_RUNTIME.md`](agent/AGENT_RUNTIME.md)。

---

## Three Feedback Loops（三层反馈闭环）

只做高层说明；详细规则见专项文档。

```text
Micro / 微观:  Action → Observation → Replan     → Agent Loop（智能体循环）
Meso  / 中观:  FAIL → Fix → PASS → Regression    → TDD Loop（测试驱动闭环）
Macro / 宏观:  Benchmark Task → Agent → Score    → Evaluation Loop（评估闭环）
```

| 层级 | 回答的问题 | 详见 |
|------|------------|------|
| Agent Loop（智能体循环） | 下一步干什么？ | [`agent/AGENT_RUNTIME.md`](agent/AGENT_RUNTIME.md) |
| TDD Loop（测试驱动闭环） | 改动对不对？ | [`java/JAVA_ARCHITECTURE.md`](java/JAVA_ARCHITECTURE.md) |
| Evaluation Loop（评估闭环） | Agent 稳不稳？ | [`benchmark/BENCHMARK.md`](benchmark/BENCHMARK.md) |

---

## Architecture Principles（架构原则）

```text
Simple before complex
Testable before clever
Verified before finished
Explicit before abstract
```

中文说明：

```text
先简单后复杂
先可测试再追求技巧
先验证再宣布完成
优先显式设计而不是过度抽象
```

冲突时：**never trade verified business correctness for architectural novelty.**  
不要用「更炫的架构」牺牲可验证的业务正确性；优先选择最小、显式、可测试、可复现的设计。

### Rule Hierarchy（规则层级）概览

```text
L1 Project Rules（项目规则）
L2 Business Rules（业务规则）
L3 Engineering Rules（工程规则）
L4 Agent Runtime Rules（智能体运行时规则）
L5 Safety Rules（安全规则）
L6 Verification Rules（验证规则）
```

---

## Technology Stack Snapshot（技术栈快照）

### Coding Agent（规划 / 未实现）

- Python 3.12+
- OpenAI API、Pydantic、Typer、Rich、pytest
- Shell / Git；Docker 在后续阶段

**Initial implementation must NOT use:** LangChain、LangGraph、MCP、Multi-Agent、Vector Database、RAG、Redis。

### Java Target（`shop-service`）

- Java 21、Spring Boot 3.x、MyBatis-Plus、MySQL 8.x
- Maven、JUnit 5、Mockito、Flyway

**Do NOT use Spring Data JPA. / 禁止使用 Spring Data JPA。**

完整 Java 规则见 [`java/JAVA_ARCHITECTURE.md`](java/JAVA_ARCHITECTURE.md)。

---

## Documentation Map

完整索引见 [`README.md`](README.md)。本文只保留系统总览；细则在专项文档。
