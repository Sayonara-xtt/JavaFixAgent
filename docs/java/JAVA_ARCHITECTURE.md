# Java Business and Engineering Architecture（Java 业务与工程架构）

> 只描述 `shop-service`。  
> Agent 运行时见 [`../agent/AGENT_RUNTIME.md`](../agent/AGENT_RUNTIME.md)。  
> 系统总览见 [`../ARCHITECTURE.md`](../ARCHITECTURE.md)。  
> **Business Invariants（业务不变量）** 以本文为 Single Source of Truth（单一事实来源）。

---

## Technology Stack（技术栈）

```text
Java 21
Spring Boot 3.x
MyBatis-Plus
MySQL 8.x
Maven
JUnit 5
Mockito
Flyway
```

必须明确：

```text
Do NOT use Spring Data JPA.
禁止使用 Spring Data JPA。
```

自动集成测试使用 **Testcontainers** 临时 MySQL；**禁止**静默回退本机数据库或 H2。

---

## Layered Architecture（分层架构）

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

## Package Structure（包结构）

推荐包名：`com.javafix.shop`

```text
controller/
service/
service/impl/
mapper/
entity/
dto/
vo/
enums/
exception/
config/
common/
```

资源：

```text
src/main/resources/
├── application.yml
├── mapper/
└── db/migration/
```

---

## Business Domain（业务领域）

小型电商订单系统。Initial domains：

```text
User（用户）
Product（商品）
Order（订单）
OrderItem（订单项）
```

Initial features：

1. Product query — 商品查询  
2. Create order — 创建订单  
3. Order detail — 订单详情  
4. Order pagination — 订单分页  
5. Cancel order — 取消订单  
6. Inventory deduction — 库存扣减  
7. Inventory restoration — 取消订单后恢复库存  

**MVP 阶段不要加入支付、物流、仓库、优惠券等复杂模块。**  
Do not add payment, shipping, warehouse, coupon, or other large modules in the MVP.

定位：Java 业务系统是 Coding Agent 的真实软件工程靶场，而不是完整电商产品。

---

## Business Invariants（业务不变量）

完整定义（Single Source of Truth）：

| Rule | Required behavior |
|------|-------------------|
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

附加规则：

- Rules must be represented by automated tests wherever practical.
- Benchmark tasks may intentionally violate one invariant, but the **clean baseline must satisfy all applicable invariants**.
- 这些规则应尽可能由自动化测试固化。Benchmark 可以故意破坏某一业务不变量，但干净基线必须满足全部适用规则。

### 下单 / 取消流程（注释）

```text
创建订单（事务）:
  校验用户 → 校验商品 → 原子扣库存 → 写订单+明细（金额由明细汇总）
  任一步失败 → 整单回滚

取消订单（事务）:
  仅 CREATED 可取消 → 改 CANCELLED → 按明细还库存一次
  已取消再取消 → 业务错误（不二次还库存）
```

---

## Database Design（数据库设计）

MVP tables：

```text
user
product
orders
order_item
```

Product 字段应包含：

```text
id, name, price, stock, version, deleted, created_at, updated_at
```

- `version` → optimistic locking / 乐观锁  
- `deleted` → logical deletion / 逻辑删除  

Schema 由 Flyway 管理（如 V1 基线、V2 业务表）。

---

## MyBatis-Plus Rules

```text
Simple CRUD
→ MyBatis-Plus

Conditional Query
→ LambdaQueryWrapper / LambdaUpdateWrapper

Critical SQL
→ Custom Mapper + XML
```

**Critical inventory deduction should use explicit SQL. / 关键库存扣减逻辑使用显式 SQL。**

示例模式：

```java
int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
```

```xml
<update id="decreaseStock">
    UPDATE product
       SET stock = stock - #{quantity}
     WHERE id = #{productId}
       AND stock >= #{quantity}
</update>
```

意图：让 Coding Agent 同时理解 Java 与 SQL。

---

## REST API（MVP）

```text
GET  /api/products/{id}
POST /api/orders
GET  /api/orders/{id}
GET  /api/orders?page=1&size=20
POST /api/orders/{id}/cancel
```

**No real payment integration in the MVP. / MVP 阶段不实现真实支付。**

接口文档（已落地）：Knife4j / OpenAPI — 本地启动后见 `http://localhost:8080/doc.html`。

---

## TDD Rules（Java 验证纪律）

Bug fixing and feature work **must** follow a test-first verification discipline whenever the task can be expressed as an automated test.

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

关键节奏：

```text
FAIL → PASS → Regression
```

Rules：

- Do not claim a bug is fixed only because the implementation looks correct.
- For a reproducible defect, prefer proving `FAIL → PASS`.
- Keep the production-code change minimal until the test is green.
- After the targeted test passes, run the relevant regression suite.
- If a task cannot reasonably be expressed as a test, record the alternative verification command and its output.
- **Do not weaken, delete, or bypass a valid test merely to make the build green.**
- Treat test output, command output, and Git state as **Ground Truth（真实依据）**.

与 Agent Loop 的关系：Agent Loop 决定「下一步干什么」；本 TDD 闭环决定「改动对不对」。  
Verified Stopping Condition 见 [`../agent/AGENT_RUNTIME.md`](../agent/AGENT_RUNTIME.md)。

---

## Related Documents

- [`../plans/CURRENT_PHASE.md`](../plans/CURRENT_PHASE.md) — 当前阶段允许改什么
- [`../plans/phase-3-acceptance.md`](../plans/phase-3-acceptance.md) — Phase 3 验收证据
- [`../benchmark/BENCHMARK.md`](../benchmark/BENCHMARK.md) — 干净基线与故障注入
