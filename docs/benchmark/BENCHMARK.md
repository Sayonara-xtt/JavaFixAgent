# Benchmark and Evaluation Architecture（基准测试与评估架构）

> 只描述 Agent 如何被重复测试和评价。  
> 业务不变量见 [`../java/JAVA_ARCHITECTURE.md`](../java/JAVA_ARCHITECTURE.md)。  
> Agent 运行时见 [`../agent/AGENT_RUNTIME.md`](../agent/AGENT_RUNTIME.md)。

**状态说明：** Benchmark Runner / Evaluation Engine 属于后续阶段。本文定义目标架构；**不得**把故意故障直接提交到 `main`。

---

## Clean Baseline（干净基线）

定义：

```text
main
=
buildable
+
testable
+
clean
```

**The `main` branch must always remain healthy. / `main` 分支必须始终保持健康基线。**

干净基线在引入 Benchmark Bug 之前应满足：

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

稳定后可打标签：

```bash
git tag baseline-v1
```

**Do not commit intentionally broken benchmark code directly to `main`. / 禁止直接把故意制造的 Bug 提交到 `main`。**

Benchmark 故障不得直接污染 `main`；通过 `setup.patch` 临时注入，评测后重置。

---

## Benchmark Task Structure（任务结构）

```text
benchmark/
└── tasks/
    ├── task-001/
    │   ├── issue.md
    │   ├── setup.patch
    │   └── metadata.json
    └── ...
```

---

## Benchmark Lifecycle（生命周期）

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

---

## Initial Tasks（TASK-001 ~ TASK-010）

| ID | Task | 中文说明 |
|----|------|----------|
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

任务通常故意破坏某一 **Business Invariant（业务不变量）**；干净基线必须满足全部适用不变量。

---

## Evaluation Metrics（评估指标）

```text
Solved Rate（任务解决率）
Test Pass Rate（测试通过率）
Average Steps（平均步骤数）
Token Usage（Token 使用量）
Cost（成本）
Latency（延迟）
```

---

## Ground Truth（真实依据）

评估必须以：

```text
Tests
Command Output
Git State
Repository State
```

作为主要依据。

**不能只依赖 LLM 自我声明。**

Treat test output, command output, and Git state as Ground Truth.

与 **Verified Stopping Condition（可验证停止条件）** 一致：模型请求 finish 本身不等于成功。详见 [`../agent/AGENT_RUNTIME.md`](../agent/AGENT_RUNTIME.md)。

---

## Related Documents

- [`../plans/ROADMAP.md`](../plans/ROADMAP.md) — Phase 8 / 12
- [`../plans/CURRENT_PHASE.md`](../plans/CURRENT_PHASE.md) — 当前是否允许实现 Benchmark
- [`../java/JAVA_ARCHITECTURE.md`](../java/JAVA_ARCHITECTURE.md) — 业务不变量
