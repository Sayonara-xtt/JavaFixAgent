# Agent Runtime Architecture（智能体运行时架构）

> 只描述 Python Coding Agent 本身。  
> 订单业务规则见 [`../java/JAVA_ARCHITECTURE.md`](../java/JAVA_ARCHITECTURE.md)。  
> 系统总览见 [`../ARCHITECTURE.md`](../ARCHITECTURE.md)。

**状态说明：** Agent Runtime 实现属于 Phase 4+。本文定义目标架构与硬约束，**不是**当前已实现能力。

---

## Agent Runtime Components（运行时组件）

组件必须职责分离：

```text
Agent Runtime（智能体运行时）
├── Model（模型抽象）
├── Agent（智能体调度器）
├── State（状态）
├── Action（动作）
├── Environment（执行环境）
└── Config（运行配置）
```

建议接口：

- `Model.generate(messages)`
- `Environment.execute(command: str)`
- `Agent.run(task: str)`

建议目录（未来）：`agent/src/javafix_agent/` — `cli.py`, `agent.py`, `model.py`, `environment.py`, `prompt.py`, `state.py`, `config.py`

---

## Model（模型抽象）

负责：

```text
messages
 ↓
LLM API
 ↓
model response
```

Model **不能**直接执行系统操作（改文件、跑 shell、碰网络文件系统等）。

---

## Agent（智能体调度器）

负责 **Loop Orchestration（循环编排）**：调模型 → 解析 Action → 交给 Environment → 记录 Observation → 决定是否继续。

---

## State（状态）

负责 **Current Task State（当前任务状态）**。

必须明确：

```text
State ≠ Long-term Memory
State 不等于长期记忆
```

MVP **不需要**：

```text
Redis
Vector Database
Cross-task Memory
```

推荐最小 `AgentState` 字段：

```text
task, step, max_steps, status, message_history,
last_action, last_observation, files_touched,
test_status, git_status, error_count, token_usage, started_at
```

---

## Action（动作）

Agent 请求执行的**结构化操作**。MVP 以 Shell 命令为主。

---

## Environment（执行环境）

真实执行边界。例如：

```text
Shell
Git
Maven
Filesystem
```

### Environment Safety（执行环境安全）

- **LocalEnvironment must have a fixed workspace. / 本地执行环境必须绑定固定工作目录。**
- All commands must run with that workspace as `cwd`.
- **The Agent must not freely operate on the whole machine. / Agent 不允许默认自由操作用户整台电脑。**
- Docker Sandbox（沙箱）will be added later（后续阶段）。

---

## Observation（环境反馈）

命令执行后的真实输出（stdout / stderr / exit code、测试结果、Git 状态等），再写回历史供下一轮推理。

Observation 与测试输出、命令输出、Git 状态共同构成 **Ground Truth（真实依据）**。

---

## Agent Loop（智能体循环）

第一版必须坚持：

```text
Manual Agent Loop
手写 Agent Loop
```

**The first version must manually implement the loop. / 第一版必须手写核心循环。**

MVP **不引入**：

```text
LangChain
LangGraph
MCP
Multi-Agent
```

同时禁止过早引入：Vector Database、RAG、Redis。

示意：

```python
while step < max_steps:
    response = model.generate(history)
    action = parse_action(response)
    observation = environment.execute(action.command)
    history.append({"action": action, "observation": observation})
    if action.finished:
        break
```

心智模型：

```text
Task
 ↓
LLM
 ↓
Action
 ↓
Environment
 ↓
Observation
 ↓
LLM
```

---

## Tool Strategy（工具策略）

MVP 主要使用：

```text
Shell / Bash
```

示例：`git status`、`find`、`rg`、`sed`、`mvn test`、`git diff`

**Do not create many custom tools in the first version. / 第一版不要一开始创建大量定制工具。**

目的是清晰理解：Tool / Action / Environment / Observation / Ground Truth。

---

## Verified Stopping Condition（可验证停止条件）

`action.finished == true` means the model wants to stop; it does **not** by itself mean the task succeeded.

模型说「做完了」≠ 真的做完。

完整公式：

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

推荐行为：

```python
if action.finished:
    if state.test_status == "passed" and state.verification_passed:
        return SUCCESS
    continue
```

Stopping may also occur because of `max_steps`, timeout, fatal error, or explicit cancellation.  
**Those outcomes must not be reported as successful completion. / 超时、超步数、致命错误或取消不得报成功。**

---

## Context Engineering（上下文工程）

**Do not send the whole repository to the LLM. / 不要把整个仓库一次性发送给 LLM。**

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

可度量：files read、tokens used、steps、task success。  
实现阶段见 [`../plans/ROADMAP.md`](../plans/ROADMAP.md) Phase 10。

---

## Tracing（执行追踪）

记录：

```text
LLM Input
LLM Output
Action
Command
Observation
Exit Code
Token Usage
Latency
Error
```

实现阶段见 Roadmap Phase 11。

---

## Related Documents

- [`../ARCHITECTURE.md`](../ARCHITECTURE.md) — 系统总览
- [`../benchmark/BENCHMARK.md`](../benchmark/BENCHMARK.md) — 如何评测 Agent
- [`../plans/CURRENT_PHASE.md`](../plans/CURRENT_PHASE.md) — 当前是否允许实现 Runtime
