# Phase 3 Business Domain Implementation Plan
# 阶段 3 — 业务领域实现计划

> **Archived / 已归档。** 施工清单；规格见 `../specs/2026-09-15-phase-3-business-domain-design.md`。  
> 现行入口：[`../../README.md`](../../README.md)。

**Goal / 目标:** Deliver the full order-domain MVP (tables, APIs, inventory invariants, MySQL tests) in `shop-service/`.  
在 `shop-service/` 交付完整订单域 MVP（表、API、库存不变量、MySQL 测试）。

**Architecture / 架构:** Controller → Service/Impl → Mapper；MyBatis-Plus CRUD；库存用 XML 显式 SQL；统一 `ApiResponse`；下单/取消 `@Transactional`。

**Tech Stack / 技术栈:** Java 21, Spring Boot 3.5.16, MyBatis-Plus 3.5.17, Flyway, MySQL 8, JUnit 5, MockMvc, AssertJ.

**Spec / 规格:** [`../specs/2026-09-15-phase-3-business-domain-design.md`](../specs/2026-09-15-phase-3-business-domain-design.md)

## Global Constraints

- Use MyBatis-Plus; never add Spring Data JPA.
- Use real MySQL 8.4 through disposable Testcontainers; never fall back to local MySQL or H2.
- Do not modify applied Flyway `V1`.
- No Python agent / LangChain / MCP / Redis.
- Prefer FAIL → PASS → regression for each capability.
- Do not commit unless the user explicitly asks.

---

### Task 1: Schema + MyBatis infrastructure

**Files:**
- Create: `shop-service/src/main/resources/db/migration/V2__create_business_tables.sql`
- Create: `shop-service/src/main/java/com/javafix/shop/config/MybatisPlusConfig.java`
- Modify: `shop-service/src/main/java/com/javafix/shop/ShopApplication.java` (add `@MapperScan`)
- Modify: `shop-service/src/test/java/com/javafix/shop/ShopApplicationTests.java` (assert Flyway version `2`)

- [x] Add V2 tables `user`, `product`, `orders`, `order_item`
- [x] Register pagination + optimistic-locker interceptors
- [x] `@MapperScan("com.javafix.shop.mapper")`
- [x] Run `.\mvnw.cmd test` — startup test expects version `2`

---

### Task 2: Common API envelope + exceptions

**Files:**
- Create: `common/ApiResponse.java`, `exception/ErrorCode.java`, `exception/BusinessException.java`, `exception/GlobalExceptionHandler.java`

- [x] `ApiResponse.ok(data)` / `ApiResponse.fail(code, message)`
- [x] Map `BusinessException` and validation errors to envelope

---

### Task 3: Entities, enums, mappers

**Files:**
- Create entities: `User`, `Product`, `Order`, `OrderItem`
- Create: `enums/OrderStatus.java`
- Create mappers + `mapper/ProductMapper.xml` with `decreaseStock` / `increaseStock`

- [x] `@TableLogic` on deletable entities; `@Version` on Product
- [x] Stock SQL requires `stock >= quantity` and `deleted = 0`

---

### Task 4: Product query API

**Files:**
- Create: `ProductService` / `impl`, `ProductController`, `ProductVO`
- Test: `ProductControllerTest` or service IT

- [x] FAIL then PASS: get existing product; missing/deleted → business error

---

### Task 5: Create / get / page / cancel order APIs

**Files:**
- Create DTOs: `CreateOrderRequest`, `OrderItemRequest`, VOs
- Create: `OrderService` / `impl`, `OrderController`
- Tests covering success, stock failure, missing user, cancel once, second cancel, pagination, amount derivation

- [x] Implement transactional create + cancel per spec
- [x] Run full `.\mvnw.cmd test`

---

### Task 6: Docs / phase markers

**Files:**
- Modify: `AGENTS.md`, `README.md` to Phase 3 complete / next phase note
- Create: this plan's checkbox updates when done

- [x] Document new APIs and that Phase 3 business code is now in scope for later agent work
- [x] Keep Phase 4+ agent code still out of scope until approved


---

### Task 7: Phase 3 acceptance strengthening / 验收补齐（2026-09-16）

- [x] 新增请求边界、缺失/删除对象与分页边界测试
- [x] 验证多商品/重复商品整单回滚、历史价格快照、乐观锁
- [x] 用真实 MySQL CHECK 约束拒绝第二条明细写入，验证完整事务回滚
- [x] 复现并修复并发取消竞态：条件状态更新 + 影响行数判定
- [x] 还库存零行必须失败，回滚状态与此前恢复的库存
- [x] 并发下单不超卖、并发取消只恢复一次；各重复三次
- [x] 只读独立审查；采纳明细显式排序建议
- [x] 干净构建 `mvnw.cmd clean verify`：40 次测试执行全部通过，JAR 打包成功
- [x] 同步 README / AGENTS / design / 验收记录；Phase 4 仍待批准

验收详情：[`../../plans/phase-3-acceptance.md`](../../plans/phase-3-acceptance.md)。未自动提交 Git。
