# Current Status（当前状态）

> 仅记录继续开发所必需的稳定事实；详细历史以 Git、验收记录和专项架构文档为准。

## Current Goal（当前目标）

维护 Phase 3 订单业务领域 MVP 的正确性、可测性及相关文档；未经用户明确批准，不进入 Phase 4。

## Completed（已完成）

- Phase 3 订单域 MVP 已验收：商品查询、创建订单、订单详情与分页、取消订单、库存扣减与恢复。
- 真 MySQL Testcontainers 验收已在 2026-09-16 通过：40 次测试执行，0 失败、0 错误、0 跳过。
- OpenAPI / Knife4j 文档已落地。

## Important Decisions（重要决策）

- Java 靶场使用 Java 21、Spring Boot、MyBatis-Plus、MySQL、Flyway；禁止 Spring Data JPA。
- 自动集成测试必须使用临时 MySQL Testcontainers，不得回退 H2 或本机数据库冒充通过。
- Phase 3 禁止新增 Python Agent Runtime、LangChain、LangGraph、MCP、多智能体、RAG、向量库与 Redis。
- 上下文与 Token 使用遵循 [`CONTEXT_EFFICIENCY.md`](CONTEXT_EFFICIENCY.md)。


## Changed Files（主要改动文件）
- 2026-09-21：`shop-service` 执行 `mvnw.cmd --batch-mode --no-transfer-progress clean verify`，40 次测试执行，0 失败、0 错误、0 跳过；JAR 已生成。

- 订单领域实现与测试：`shop-service/`。
- 当前阶段约束与验收：[`../plans/CURRENT_PHASE.md`](../plans/CURRENT_PHASE.md)、[`../plans/phase-3-acceptance.md`](../plans/phase-3-acceptance.md)。
## Tests（测试状态）

最近的正式验收记录见 [`../plans/phase-3-acceptance.md`](../plans/phase-3-acceptance.md)。后续涉及 Java 代码时，按任务范围运行目标测试，并在需要时运行 `shop-service` 的 Maven 测试。

## Known Issues（已知事项）

- Phase 3 仅维护；没有待实现的 Agent Runtime 功能。
- Flyway 对 MySQL 8.4 的测试支持范围会产生已知警告，但验收迁移已成功；详见验收记录。

## Next Task（下一步）

按用户请求处理 Phase 3 回归或文档维护；进入 Phase 4 前必须先取得用户明确批准并更新 `AGENTS.md` 与 `CURRENT_PHASE.md`。

## Required Context（继续任务必读）

1. [`../../AGENTS.md`](../../AGENTS.md)
2. [`../ARCHITECTURE.md`](../ARCHITECTURE.md)
3. [`../plans/CURRENT_PHASE.md`](../plans/CURRENT_PHASE.md)
4. 与任务相符的专项架构文档；并按需阅读 [`CONTEXT_EFFICIENCY.md`](CONTEXT_EFFICIENCY.md)。
