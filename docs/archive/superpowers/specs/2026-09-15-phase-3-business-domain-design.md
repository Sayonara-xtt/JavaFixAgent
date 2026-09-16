# Phase 3 Business Domain Design
# 阶段 3 — 业务领域设计规格

**Date / 日期:** 2026-09-15  
**Status / 状态:** Implemented and verified（已实施并验收，2026-09-16）  
**Scope / 范围:** Full MVP order-domain in `shop-service/`（订单域完整 MVP）

> **Archived / 已归档。** 本文回答「本阶段做什么」。怎么做见同目录 plans。  
> 现行规则见 [`../../plans/CURRENT_PHASE.md`](../../plans/CURRENT_PHASE.md) 与 [`../../../AGENTS.md`](../../../AGENTS.md)。  
> 验收证据见 [`../../plans/phase-3-acceptance.md`](../../plans/phase-3-acceptance.md)。

## 1. Goal / 目标

Implement the controlled e-commerce order MVP used as the JavaFix Agent
target: products, users, orders, inventory deduction/restoration, and the
documented REST APIs — without payment, shipping, coupons, or Python agent
code.

实现作为 JavaFix Agent 靶场的电商订单 MVP：商品、用户、订单、库存扣减/恢复及约定 REST。  
**不包含**支付、物流、优惠券，也**不包含** Python Agent。

## 2. Decisions / 已拍板决策

| Topic / 主题 | Choice / 选择 | 中文注释 |
|--------------|---------------|----------|
| Delivery scope | Full MVP in one phase | 一次做完完整 MVP |
| Response envelope | `{ "code", "message", "data" }` | 统一包装，业务错误看 code |
| Create-order body | Multi-item: `userId` + `items[]` | 支持多商品明细 |
| Architecture | Controller → Service/Impl → Mapper (+ XML for stock) | 经典分层；库存用显式 SQL |
| Persistence | MyBatis-Plus; no Spring Data JPA | 禁止 JPA |
| Database | Testcontainers MySQL 8.4 (`javafix_container_test`) | 真库测试，禁止静默 H2 |

## 3. Data model / 数据模型

Flyway `V2__create_business_tables.sql`（**禁止修改已应用的 V1**）。

### `user`（用户）
`id`, `username`, `deleted`, `created_at`, `updated_at`

### `product`（商品）
`id`, `name`, `price`, `stock`, `version`, `deleted`, `created_at`, `updated_at`

### `orders`（订单头）
`id`, `user_id`, `total_amount`, `status`（`CREATED` 已创建 / `CANCELLED` 已取消）,
`deleted`, `created_at`, `updated_at`

### `order_item`（订单明细）
`id`, `order_id`, `product_id`, `product_name`, `product_price`, `quantity`,
`subtotal`

Notes / 注释:
- 表名用 `orders`，避开 MySQL 关键字 `order`。
- 明细快照 `product_name` / `product_price`，避免事后改价影响历史单。
- 金额用 `DECIMAL` / `BigDecimal`。
- Product：`version` = 乐观锁；`deleted` = 逻辑删除。

## 4. APIs / 接口

| Method | Path | Behavior / 行为 |
|--------|------|-----------------|
| GET | `/api/products/{id}` | 返回未删除商品 |
| POST | `/api/orders` | 多明细下单 |
| GET | `/api/orders/{id}` | 订单详情（含明细） |
| GET | `/api/orders?page&size` | 分页（默认 1 / 20） |
| POST | `/api/orders/{id}/cancel` | 取消一次并还库存一次 |

### Create order request / 下单请求体

```json
{
  "userId": 1,
  "items": [{ "productId": 10, "quantity": 2 }]
}
```

### Response envelope / 统一响应

成功：`code = 0`，`message = "ok"`，`data` = 载荷。  
业务失败：非 0 `code` + message；业务错误倾向 HTTP 200；参数校验失败用 HTTP 400。

Suggested business codes / 建议业务码:
- `40001` 用户不存在
- `40002` 商品不存在或已删除
- `40003` 库存不足
- `40004` 订单不存在
- `40005` 订单已取消 / 不可取消

## 5. Core flows / 核心流程

### Create order / 创建订单（`@Transactional`）

```text
校验请求 → 查用户 → 逐行查商品并原子扣库存 → 汇总金额 → 写 orders + order_item
任一步失败 → 事务回滚（库存也不会半扣）
```

1. Validate request（明细非空、每个元素非 null、quantity > 0）；非法 JSON 同样返回 HTTP 400 / `40000`。
2. Load user；缺失 → `40001`。
3. 每行 load product；缺失/已删 → `40002`。
4. `decreaseStock`：`UPDATE ... WHERE stock >= #{quantity} AND deleted = 0`；影响行 ≠ 1 → `40003`。
5. Insert `orders`（`total_amount` = 明细小计之和）与带价格快照的 `order_item`。
6. 失败整单回滚。

### Cancel order / 取消订单（`@Transactional`）

```text
查订单 → 已 CANCELLED 则失败（幂等）→ 仅 CREATED 可取消 → 改状态 → 按明细还库存一次
```

1. Load order；缺失 → `40004`。
2. 已是 `CANCELLED` → `40005`（禁止二次还库存）。
3. 用条件 SQL 原子迁移 `CREATED → CANCELLED`；影响行数不为 1 → `40005`，不得还库存。
4. 按明细 id 升序执行 `increaseStock`；每行必须影响 1 行。商品缺失/已删除 → `40002`，回滚状态及此前恢复的库存。
5. 状态与库存同事务提交一次。

## 6. Testing / 测试要求

使用 Testcontainers 真 MySQL test profile（临时容器、随机端口、自动清理；无需本机测试库）。至少覆盖:
- Context + Flyway 含版本 `2`
- 查商品
- 下单成功与金额正确
- 库存不足被拒且库存不变
- 用户缺失受控错误
- 取消还库存一次；二次取消失败
- 分页字段一致
- 逻辑删除商品不可普通查询

### 2026-09-16 补齐验收

- 空明细、null 元素、非法数量、缺失字段及错误 JSON 均受控拒绝，无订单写入。
- 不存在/已删除用户与商品、不存在订单、非 CREATED 取消、分页边界。
- 多商品/重复商品明细；后续明细库存不足或商品缺失时回滚前序库存。
- 第二条明细数据库写入失败时回滚订单头、明细及全部库存。
- 商品改名/改价不改变历史快照；过期版本更新不得覆盖已扣库存。
- 并发下单不超卖；强制两个取消事务读取旧状态后竞争，只有一个成功。
- 两类并发场景各重复三次；不代表全场景压力测试。

验收证据见 [`../../plans/phase-3-acceptance.md`](../../plans/phase-3-acceptance.md)。Phase 4 未批准。

## 7. Out of scope / 范围外

- 支付 / 物流 / 仓库 / 优惠券
- Python agent / LangChain / MCP / Redis
- 修改 V1 migration
- 静默 H2 fallback
