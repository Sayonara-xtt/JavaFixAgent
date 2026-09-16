# JavaFix Agent — Codex 项目启动指南（全量蓝图 / Bootstrap Guide）

> **Archived / 已归档。** 原始全量蓝图，仅供查证。日常请用现行专项文档：
>
> - 文档地图：[`../../README.md`](../../README.md)
> - [`../../ARCHITECTURE.md`](../../ARCHITECTURE.md)
> - [`../../agent/AGENT_RUNTIME.md`](../../agent/AGENT_RUNTIME.md)
> - [`../../java/JAVA_ARCHITECTURE.md`](../../java/JAVA_ARCHITECTURE.md)
> - [`../../benchmark/BENCHMARK.md`](../../benchmark/BENCHMARK.md)
> - [`../../plans/CURRENT_PHASE.md`](../../plans/CURRENT_PHASE.md)
> - [`../../plans/ROADMAP.md`](../../plans/ROADMAP.md)
> - [`../../../AGENTS.md`](../../../AGENTS.md)
>
> **当前硬约束以 `AGENTS.md` 为准**；本文含未开始阶段，不可当作许可。

---

## 中文导读（如何读本文）

| 你想了解… | 优先看章节 |
|-----------|------------|
| 项目到底要做成什么 | §1 项目目标、§1.1 角色边界 |
| 技术选型与禁止事项 | §2 技术栈 |
| 订单业务与不变量 | §4–§8 |
| Agent 怎么跑（概念） | §9–§12 |
| TDD 与验证（先红后绿） | §10.1 TDD 与验证闭环 |
| 干净基线与 Benchmark | §13–§14 |
| 分阶段怎么推进 | §18 路线图 |
| 验收与工程理念 | §20、§24 |

**核心闭环（中文注释）：**

```text
任务 Task
  → Coding Agent 决策
  → LLM 生成下一步
  → Action（多为 Shell）
  → Environment 在 shop-service 执行
  → Maven/JUnit / git 产生 Observation
  → 写回历史，进入下一轮 Agent Loop
  → 直到「可验证成功」或超时/超步数
```

**三层闭环（中文注释）：** 微观 Agent Loop（下一步干什么）→ 中观 TDD（改得对不对）→ 宏观 Benchmark（稳不稳）。

---

## 1. 项目目标（Project Goal）

Build a Python-based Coding Agent that can analyze, modify, test, and verify a Java Spring Boot + MyBatis-Plus project.

构建一个基于 Python 的 Coding Agent（编程智能体），能够自动分析、修改、测试并验证 Java Spring Boot + MyBatis-Plus 项目中的软件工程任务。

The final Agent should be able to:

- Understand software engineering tasks. — 理解软件工程任务
- Search a Java repository. — 搜索 Java 代码仓库
- Read Java, XML, SQL, config, and tests. — 阅读 Java、XML、SQL、配置和测试
- Modify source code. — 修改源代码
- Run Maven and JUnit tests. — 运行 Maven 与 JUnit 测试
- Read failures and retry. — 读取失败信息并继续修复
- Check `git status` and `git diff`. — 检查 Git 状态和代码差异
- Produce a final verified result. — 输出经过验证的最终结果
- Support reproducible benchmark tasks. — 支持可重复 Benchmark（基准测试）任务

Core model:

```text
Task（任务）
    ↓
Coding Agent（编程智能体）
    ↓
LLM（大语言模型）
    ↓
Action（动作）
    ↓
Environment（执行环境）
    ↓
Java Repository（Java 代码仓库）
    ↓
Maven / JUnit
    ↓
Observation（环境反馈）
    ↓
Agent Loop（智能体循环）
```

This is not a chatbot project.
本项目不是聊天机器人项目。

### 1.1 角色边界（Role Boundary）

The roles in this project must remain explicit:

```text
Developer / User
    ↓
Codex
    ↓
Develops and maintains javafix-agent

Future runtime:

User Issue
    ↓
JavaFix Agent
    ↓
Analyzes / modifies / tests shop-service
```

中文说明：

- **Codex** 是当前项目的开发助手，负责按照本指南和 `AGENTS.md` 构建、修改 JavaFix Agent 项目本身。
- **JavaFix Agent** 是本项目最终产物，负责在受控环境中解决 `shop-service` 的软件工程任务。
- 不要把 Codex 的开发规则与 JavaFix Agent 的运行时规则混为一层。


---

## 2. 技术栈（Technology Stack）

### Python 编程智能体（Python Coding Agent）

- Python 3.12+
- OpenAI API
- Pydantic
- Typer
- Rich
- pytest
- subprocess / Shell
- Git
- Docker in later phases — Docker 放在后续阶段

Initial implementation must NOT use:

- LangChain
- LangGraph
- MCP
- Multi-Agent
- Vector Database
- RAG
- Redis

中文说明：第一版必须优先手写 Agent Loop、Model、Environment、State，不要过早引入复杂框架。

### Java 业务系统（Java Business System）

- Java 21
- Spring Boot 3.x
- MyBatis-Plus
- MySQL 8.x
- Maven
- JUnit 5
- Mockito
- Flyway

Important constraint:

**Do NOT use Spring Data JPA.**

重要约束：

**禁止使用 Spring Data JPA。**

Persistence rules:

- Simple CRUD → MyBatis-Plus
- Conditional query → LambdaQueryWrapper / LambdaUpdateWrapper
- Critical or complex SQL → Custom Mapper + XML SQL

---

## 3. 仓库策略（Repository Strategy）

Use a Monorepo（单仓库多模块）.

Repository name:

```text
javafix-agent
```

Recommended structure:

```text
javafix-agent/
├── AGENTS.md
├── README.md
├── .gitignore
├── .env.example
├── Makefile
│
├── agent/
├── shop-service/
├── benchmark/
├── infra/
├── scripts/
├── docs/
└── .github/
    └── workflows/
```

Responsibilities:

- `agent/` — Python Coding Agent implementation / Python 编程智能体实现
- `shop-service/` — Java Spring Boot + MyBatis-Plus business system / Java 示例业务系统
- `benchmark/` — Reproducible Agent tasks / 可重复 Agent 基准任务
- `infra/` — Docker, MySQL, Sandbox / Docker、MySQL、沙箱
- `scripts/` — Development helper scripts / 开发辅助脚本
- `docs/` — Architecture, business rules, and plans / 架构、业务规则与计划
- `.github/workflows/` — GitHub Actions CI / 持续集成

---

## 4. Java 业务领域（Java Business Domain）

Use a small e-commerce order system.

使用一个小型电商订单系统。

Initial domains:

```text
User（用户）
Product（商品）
Order（订单）
OrderItem（订单项）
```

Initial features:

1. Product query — 商品查询
2. Create order — 创建订单
3. Order detail — 订单详情
4. Order pagination — 订单分页
5. Cancel order — 取消订单
6. Inventory deduction — 库存扣减
7. Inventory restoration — 取消订单后恢复库存

Do not add payment, shipping, warehouse, coupon, or other large modules in the MVP.

MVP 阶段不要加入支付、物流、仓库、优惠券等复杂模块。

---

### 4.1 业务不变量（Business Invariants）

The Java business system is a software-engineering target for the Coding Agent, not a full e-commerce product. Business complexity must serve reproducible engineering tasks.

Java 业务系统是 Coding Agent 的真实软件工程靶场，而不是完整电商产品。业务复杂度必须服务于可重复的软件工程任务。

The following invariants define the minimum business correctness contract:

| Rule | Required behavior |
|---|---|
| Inventory sufficiency / 库存充足性 | An order must not be created when `stock < quantity`. / 当库存不足时禁止创建订单。 |
| Atomic stock deduction / 原子扣库存 | Stock must never become negative because of an order operation. / 订单操作不得导致负库存。 |
| Order amount / 订单金额 | Order total must be derived correctly from order items. / 订单总金额必须由订单项正确计算。 |
| User existence / 用户存在性 | Missing users must produce a controlled business error, not `NullPointerException`. / 用户不存在时必须返回受控业务异常。 |
| Transaction rollback / 事务回滚 | Failure in a critical order-creation step must roll back the whole transaction. / 创建订单关键步骤失败时必须整体回滚。 |
| Cancel idempotency / 取消幂等 | An order must not be successfully cancelled twice. / 同一订单不能成功取消两次。 |
| Inventory restoration / 库存恢复 | Successful cancellation restores inventory exactly once. / 成功取消订单后库存只能恢复一次。 |
| Logical deletion / 逻辑删除 | Logically deleted rows must not participate in normal business queries. / 逻辑删除数据不得进入正常业务查询。 |
| Pagination correctness / 分页正确性 | Page number, size, total and records must remain consistent. / 页码、分页大小、总数和记录必须一致。 |
| Optimistic locking / 乐观锁 | Concurrent updates protected by versioning must reject stale updates. / 受版本控制的并发更新必须拒绝过期版本。 |

Rules must be represented by automated tests wherever practical. Benchmark tasks may intentionally violate one invariant, but the clean baseline must satisfy all applicable invariants.

这些规则应尽可能由自动化测试固化。Benchmark 可以故意破坏某一业务不变量，但干净基线必须满足全部适用规则。

---

## 5. Java 包结构（Java Package Structure）

Recommended package:

```text
com.javafix.shop
```

Recommended structure:

```text
shop-service/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/javafix/shop/
│   │   │   ├── ShopApplication.java
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   │   └── impl/
│   │   │   ├── mapper/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   ├── vo/
│   │   │   ├── enums/
│   │   │   ├── exception/
│   │   │   ├── config/
│   │   │   └── common/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── mapper/
│   │       └── db/migration/
│   └── test/
│       └── java/com/javafix/shop/
```

Layering rule:

```text
Controller
    ↓
Service
    ↓
ServiceImpl
    ↓
Mapper
    ↓
MySQL
```

---

## 6. 初始数据库设计（Initial Database Design）

MVP tables:

```text
user
product
orders
order_item
```

Product fields should include:

```text
id
name
price
stock
version
deleted
created_at
updated_at
```

`version` → optimistic locking / 乐观锁  
`deleted` → logical deletion / 逻辑删除

These fields can later become benchmark tasks using `@Version` and `@TableLogic`.

---

## 7. MyBatis-Plus 规则（MyBatis-Plus Rules）

Simple Mapper:

```java
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
```

Simple CRUD:

```java
Product product = productMapper.selectById(productId);
```

Conditional query:

```java
LambdaQueryWrapper<Order> wrapper =
        Wrappers.lambdaQuery(Order.class)
                .eq(Order::getUserId, userId);
```

Critical inventory deduction should use explicit SQL.

关键库存扣减逻辑使用显式 SQL。

Example:

```java
int decreaseStock(
        @Param("productId") Long productId,
        @Param("quantity") Integer quantity
);
```

```xml
<update id="decreaseStock">
    UPDATE product
       SET stock = stock - #{quantity}
     WHERE id = #{productId}
       AND stock >= #{quantity}
</update>
```

This is intentional so the Coding Agent can reason about Java + SQL together.

这样设计是为了让 Coding Agent 同时理解 Java 与 SQL。

---

## 8. 初始 REST 接口（Initial REST APIs）

```text
GET  /api/products/{id}
POST /api/orders
GET  /api/orders/{id}
GET  /api/orders?page=1&size=20
POST /api/orders/{id}/cancel
```

No real payment integration in the MVP.
MVP 阶段不实现真实支付。

---

## 9. Python Agent 结构（Python Agent Structure）

```text
agent/
├── pyproject.toml
├── src/
│   └── javafix_agent/
│       ├── __init__.py
│       ├── cli.py
│       ├── agent.py
│       ├── model.py
│       ├── environment.py
│       ├── prompt.py
│       ├── state.py
│       └── config.py
└── tests/
    ├── test_agent.py
    └── test_environment.py
```

Initial abstractions:

```text
Model（模型抽象）
Environment（执行环境）
Agent（智能体）
State（状态）
```

Suggested interfaces:

```python
class Model:
    def generate(self, messages):
        ...
```

```python
class Environment:
    def execute(self, command: str):
        ...
```

```python
class Agent:
    def run(self, task: str):
        ...
```

---

### 9.1 Agent Runtime 核心组件规则（Agent Runtime Component Rules）

The MVP runtime should explicitly separate these responsibilities:

```text
Agent Runtime
├── Model        # LLM abstraction / 模型抽象
├── Agent        # loop orchestration / 循环编排
├── State        # current task state / 当前任务状态
├── Action       # structured requested operation / 结构化动作
├── Environment  # real execution boundary / 真实执行环境
└── Config       # limits and runtime configuration / 限制与运行配置
```

`State` must not be treated as long-term memory. The MVP needs task state, not Redis, vector memory, or cross-task memory.

Recommended minimum state:

```text
AgentState
├── task
├── step
├── max_steps
├── status
├── message_history
├── last_action
├── last_observation
├── files_touched
├── test_status
├── git_status
├── error_count
├── token_usage
└── started_at
```

`Action` and `Observation` should be explicit runtime concepts even if the first implementation remains small.

---

## 10. 初始 Agent Loop（Initial Agent Loop）

The first version must manually implement the loop.

第一版必须手写核心循环。

```python
while step < max_steps:
    response = model.generate(history)
    action = parse_action(response)
    observation = environment.execute(action.command)

    history.append({
        "action": action,
        "observation": observation
    })

    if action.finished:
        break
```

Mental model:

```text
Task（任务）
↓
LLM（大语言模型）
↓
Action（动作）
↓
Shell（命令行）
↓
Environment（执行环境）
↓
Observation（环境反馈）
↓
Agent Loop（智能体循环）
```

---

### 10.1 TDD 与验证闭环（TDD and Verification Loop）

Bug fixing and feature work must follow a test-first verification discipline whenever the task can be expressed as an automated test.

```text
Issue / Task
    ↓
Search and locate relevant code
    ↓
Understand current behavior
    ↓
Existing failing test?
    ├── YES → run it and confirm FAIL
    └── NO  → add a regression test and confirm FAIL
    ↓
Modify the minimum production code
    ↓
Run targeted test
    ↓
PASS
    ↓
Refactor if needed
    ↓
Run regression test suite
    ↓
PASS
    ↓
git diff
    ↓
Verified result
```

Rules:

- Do not claim a bug is fixed only because the implementation looks correct.
- For a reproducible defect, prefer proving `FAIL → PASS`.
- Keep the production-code change minimal until the test is green.
- After the targeted test passes, run the relevant regression suite.
- If a task cannot reasonably be expressed as a test, record the alternative verification command and its output.

### 10.2 可验证停止条件（Verified Stopping Condition）

`action.finished == true` means the model wants to stop; it does **not** by itself mean the task succeeded.

A successful stop must be grounded in the environment:

```text
Model requests finish
AND
Required verification/tests passed
AND
No unresolved fatal command error
AND
Relevant git diff was inspected
=
VERIFIED SUCCESS
```

Recommended behavior:

```python
if action.finished:
    if state.test_status == "passed" and state.verification_passed:
        return SUCCESS
    continue
```

Stopping may also occur because of `max_steps`, timeout, fatal error, or explicit cancellation. Those outcomes must not be reported as successful completion.

---

## 11. Tool 策略（Tool Strategy）

MVP should primarily give the Agent one general-purpose tool:

```text
Shell / Bash
```

Examples:

```bash
git status
find .
rg "OrderService"
sed -n '1,240p' src/main/java/...
mvn test
git diff
```

Do not create many custom tools in the first version.

第一版不要一开始创建大量定制工具。

This helps us clearly understand:

- Tool（工具）
- Action（动作）
- Environment（执行环境）
- Observation（环境反馈）
- Ground Truth（环境真实结果）

---

## 12. 执行环境安全（Environment Safety）

LocalEnvironment must have a fixed workspace.

本地执行环境必须绑定固定工作目录。

```python
class LocalEnvironment:
    def __init__(self, workspace):
        self.workspace = workspace
```

All commands must run with that workspace as `cwd`.

```python
subprocess.run(
    command,
    cwd=self.workspace,
    ...
)
```

The Agent must not freely operate on the whole machine.

Agent 不允许默认自由操作用户整台电脑。

Docker Sandbox will be added later.

---

## 13. 干净基线规则（Clean Baseline Rule）

The `main` branch must always remain healthy.

`main` 分支必须始终保持健康基线。

Before benchmark bugs are introduced:

```text
Spring Boot starts successfully
MySQL starts successfully
Flyway migration succeeds
Order creation works
Inventory deduction works
Order cancellation restores inventory
Pagination works
mvn test → BUILD SUCCESS
```

After the clean baseline is stable:

```bash
git tag baseline-v1
```

Do not commit intentionally broken benchmark code directly to `main`.

禁止直接把故意制造的 Bug 提交到 `main`。

---

## 14. Benchmark 基准设计（Benchmark Design）

Recommended structure:

```text
benchmark/
└── tasks/
    ├── task-001/
    │   ├── issue.md
    │   ├── setup.patch
    │   └── metadata.json
    ├── task-002/
    │   ├── issue.md
    │   ├── setup.patch
    │   └── metadata.json
    └── ...
```

Lifecycle:

```text
Clean Baseline（干净基线）
    ↓
Apply setup.patch（应用故障补丁）
    ↓
Buggy Repository（带 Bug 的仓库）
    ↓
Run Coding Agent（运行智能体）
    ↓
Run Tests（运行测试）
    ↓
Evaluate Result（评估结果）
    ↓
Reset Repository（重置仓库）
```

Recommended initial tasks:

| ID | Task | 中文说明 |
|---|---|---|
| TASK-001 | Insufficient inventory still allows order creation | 库存不足仍可创建订单 |
| TASK-002 | Wrong total amount calculation | 订单总金额计算错误 |
| TASK-003 | NullPointerException when user does not exist | 用户不存在触发空指针 |
| TASK-004 | Cancel order does not restore stock | 取消订单不恢复库存 |
| TASK-005 | Order can be cancelled twice | 订单可重复取消 |
| TASK-006 | Add order pagination | 增加订单分页 |
| TASK-007 | Wrong query condition | 查询条件错误 |
| TASK-008 | Logical deletion does not work | 逻辑删除失效 |
| TASK-009 | Optimistic lock does not work | 乐观锁失效 |
| TASK-010 | Transaction does not rollback | 事务未正确回滚 |

---

## 15. Git 策略（Git Strategy）

Use Git locally and GitHub as the remote platform.

本地使用 Git，远程使用 GitHub。

`main` must always be:

```text
buildable
testable
clean
```

Suggested branches:

```text
feature/init-shop-service
feature/order-domain
feature/agent-loop
feature/docker-sandbox
feature/evaluation
```

Do not introduce complex Git Flow in the MVP.

MVP 阶段不要引入复杂 Git Flow。

Before finishing any task:

```bash
git status
git diff
```

Do not commit automatically unless explicitly requested.

除非明确要求，否则 Codex 不要自动执行 Git commit。

---

## 16. CI 策略（CI Strategy）

Create:

```text
.github/workflows/
├── java-test.yml
└── python-test.yml
```

Java CI:

```text
Checkout
↓
Setup Java 21
↓
Start required services
↓
mvn test
↓
PASS / FAIL
```

Python CI:

```text
Checkout
↓
Setup Python
↓
Install dependencies
↓
pytest
↓
PASS / FAIL
```

---

## 17. Docker 策略（Docker Strategy）

Recommended infrastructure:

```text
infra/
├── docker-compose.yml
└── docker/
    └── agent-sandbox.Dockerfile
```

Initial stage:

```text
Local Python Agent
    ↓
Local Java Project
    ↓
MySQL Docker
```

Later:

```text
Python Agent
    ↓
Docker Sandbox
    ↓
Java Workspace
    ↓
Maven / JDK / Git
    ↓
MySQL
```

Sandbox should eventually limit:

- CPU
- Memory
- Timeout
- Filesystem
- Network
- Permissions

---

## 18. 开发路线图（Development Roadmap）

### 阶段 1 — 仓库初始化（Phase 1 — Repository Initialization）

Create the monorepo structure only.

只创建 Monorepo 基础结构。

Do not implement business logic or AI logic yet.

---

### 阶段 2 — Spring Boot 基线（Phase 2 — Spring Boot Baseline）

Initialize `shop-service`.

Requirements:

- Java 21
- Spring Boot 3.x
- MyBatis-Plus
- MySQL
- Maven
- JUnit 5
- Mockito
- Flyway

Acceptance:

```bash
mvn test
```

must succeed.

---

### 阶段 3 — 业务领域（Phase 3 — Business Domain）

Implement:

- Product
- User
- Order
- OrderItem
- Create order
- Query order
- Pagination
- Cancel order
- Inventory deduction
- Inventory restoration

---

### 阶段 4 — Python Agent 骨架（Phase 4 — Python Agent Skeleton）

Implement:

- Model
- Environment
- State
- Agent
- CLI

Do not use LangGraph.

---

### 阶段 5 — Shell 执行环境（Phase 5 — Shell Environment）

Agent must execute inside `shop-service`:

```bash
git status
rg
find
sed
mvn test
git diff
```

---

### 阶段 6 — Agent Loop（Phase 6 — Agent Loop）

Implement:

```text
Task
→ LLM
→ Action
→ Shell
→ Observation
→ LLM
```

Add:

- max steps
- timeout
- stopping condition
- history

---

### 阶段 7 — 第一个自动 Bug 修复（Phase 7 — First Automatic Bug Fix）

Create:

```text
Insufficient inventory still allows order creation.
库存不足仍然可以创建订单。
```

Agent must:

1. Search the repository and locate the relevant implementation.
2. Inspect existing tests and business rules.
3. Reproduce the defect.
4. Run an existing relevant test and confirm failure, or add a regression test that fails for the defect.
5. Make the minimum production-code change required to fix the defect.
6. Run the targeted test and confirm PASS.
7. Run the relevant regression suite and reach `BUILD SUCCESS`.
8. Run `git status` and inspect `git diff`.
9. Finish only when the result satisfies the Verified Stopping Condition.

---

### 阶段 8 — Benchmark 框架（Phase 8 — Benchmark Framework）

Create at least 10 reproducible engineering tasks.

---

### 阶段 9 — Docker 沙箱（Phase 9 — Docker Sandbox）

Move execution from local subprocess to isolated Docker.

---

### 阶段 10 — 上下文工程（Phase 10 — Context Engineering）

Do not send the whole repository to the LLM.

不要把整个仓库一次性发送给 LLM。

Use:

```text
Task
↓
Search
↓
Locate
↓
Read
↓
Reason
↓
Search More If Needed
```

Measure:

- files read
- tokens used
- steps
- task success

---

### 阶段 11 — 执行追踪（Phase 11 — Tracing）

Record:

- LLM input
- LLM output
- Action
- Command
- Observation
- Exit code
- Token usage
- Latency
- Error

---

### 阶段 12 — 评估（Phase 12 — Evaluation）

Measure:

- Solved Rate（任务解决率）
- Test Pass Rate（测试通过率）
- Average Steps（平均步骤数）
- Token Usage（Token 使用量）
- Cost（成本）
- Latency（延迟）

---

### 阶段 13 — 高级能力（Phase 13 — Advanced Capabilities）

Only after the MVP works:

- GitHub Issues
- Pull Requests
- MCP
- LangGraph
- Multi-Agent

---

## 19. 推荐提交顺序（Recommended Commit Sequence）

```text
initial commit
chore: initialize project structure
feat: initialize shop service
feat: add database schema
feat: add product domain
feat: add order domain
feat: implement order creation
feat: implement order cancellation
test: add order service tests
feat: initialize coding agent
feat: add local shell environment
feat: implement basic agent loop
feat: add benchmark framework
```

Avoid a single huge commit.

避免一次性提交整个项目。

---

## 20. MVP 验收标准（MVP Acceptance Criteria）

The first meaningful MVP is:

```text
User Task
    ↓
Python Coding Agent
    ↓
Search Java Repository
    ↓
Read Relevant Code + Tests + Business Rules
    ↓
Reproduce Defect / Create Failing Regression Test
    ↓
Confirm TEST FAIL
    ↓
Modify Minimum Java / XML / SQL
    ↓
Run Targeted Test
    ↓
TEST PASS
    ↓
Run Regression Tests
    ↓
BUILD SUCCESS
    ↓
git status + git diff
    ↓
Verified Stopping Condition
    ↓
Final Result
```

Example task:

```text
Fix the issue where an order can still be created when product inventory is insufficient.

修复商品库存不足时仍然可以创建订单的问题。
```

The Agent must solve it without being told which exact file to edit.

必须由 Agent 自己搜索和定位需要修改的文件。

---

## 21. Codex 执行规则（Rules for Codex）

Before modifying code:

1. Read this document.
2. Read `AGENTS.md` if it exists.
3. Inspect repository structure.
4. Run `git status`.
5. Confirm the current development phase.

During implementation:

- Keep changes scoped to the requested phase.
- Do not skip directly to advanced architecture.
- Do not add unnecessary frameworks.
- Do not use Spring Data JPA.
- Prefer simple and explicit designs.
- Keep tests runnable.
- For bug fixes and testable features, follow `FAIL → PASS → Regression`.
- Do not weaken, delete, or bypass a valid test merely to make the build green.
- Treat test output, command output, and Git state as Ground Truth.
- Preserve a clean baseline.

Before finishing:

```bash
git status
git diff
```

Run relevant tests.

Do not commit automatically unless explicitly requested.

---

## 22. Codex 第一个任务（Codex First Task）

After reading this document, perform only:

**Phase 1 — Repository Initialization**

阅读本文档后，只执行：

**Phase 1 — Repository Initialization（仓库初始化）**

Tasks:

1. Inspect current Git repository status.
2. Create:

```text
agent/
shop-service/
benchmark/tasks/
infra/
scripts/
docs/
.github/workflows/
```

3. Create:

```text
.gitignore
.env.example
README.md
Makefile
```

4. README should describe:

- Project purpose
- Repository structure
- Technology stack
- Current phase
- High-level roadmap

5. Do not create Spring Boot business code yet.
6. Do not create Python Agent implementation yet.
7. Do not introduce LangChain, LangGraph, MCP, or Multi-Agent.
8. After creating the structure:
   - Run `git status`
   - Run `git diff`
   - Summarize created files
   - Explain the directory design
   - Recommend the next step
9. Do not commit automatically.

---

## 23. Phase 1 审核后的下一任务（Next Task After Phase 1 Approval）

Proceed to:

```text
Phase 2 — Spring Boot Baseline
```

Initialize `shop-service` with:

- Java 21
- Spring Boot 3.x
- MyBatis-Plus
- MySQL
- Maven
- JUnit 5
- Mockito
- Flyway

Create first:

- `pom.xml`
- `application.yml`
- database configuration
- Flyway migration framework
- Java package structure
- basic startup test

Then run:

```bash
mvn test
```

Expected result:

```text
BUILD SUCCESS
```

---

## 24. 工程理念（Engineering Philosophy）

Development order:

```text
Clean Java Baseline（干净 Java 基线）
        ↓
Tests（测试）
        ↓
Python Model Abstraction（模型抽象）
        ↓
Shell Environment（Shell 执行环境）
        ↓
Agent Loop（智能体循环）
        ↓
Automatic Bug Fixing（自动修 Bug）
        ↓
Git Diff
        ↓
Benchmark（基准测试）
        ↓
Docker Sandbox（沙箱）
        ↓
Context Engineering（上下文工程）
        ↓
Tracing（执行追踪）
        ↓
Evaluation（评估）
        ↓
GitHub Integration
        ↓
MCP
        ↓
Multi-Agent（多智能体）
```

### 24.1 三层反馈闭环（Three Feedback Loops）

JavaFix Agent should be understood as three nested feedback systems:

```text
Micro / 微观:
Action → Observation → Replan
Agent Loop

Meso / 中观:
Test FAIL → Fix → Test PASS → Regression
TDD Loop

Macro / 宏观:
Benchmark Task → Agent → Result → Evaluator → Score
Evaluation Loop
```

The Agent Loop decides **how to act**.
The TDD loop decides **whether the change is correct**.
The Evaluation loop decides **whether the Agent reliably solves a task set**.

### 24.2 规则层级（Rule Hierarchy）

Project rules should remain separated by responsibility:

```text
L1 Project Rules
   Project goal, scope, phase

L2 Business Rules
   Order / Product / Inventory invariants

L3 Engineering Rules
   Spring Boot / MyBatis-Plus / TDD / Git

L4 Agent Runtime Rules
   Model / State / Action / Environment / Agent Loop

L5 Safety Rules
   Workspace / Timeout / Sandbox / Permissions

L6 Verification Rules
   Tests / Git Diff / Benchmark / Tracing / Evaluation
```

When rules conflict, never trade verified business correctness for architectural novelty. Prefer the smallest explicit design that can be tested and reproduced.

The goal is not to build the most complex AI system.

目标不是构建最复杂的 AI 系统。

The goal is to build a:

**clear, testable, reproducible, explainable Software Engineering Agent**

即：

**清晰、可测试、可重复、可解释的软件工程智能体。**
