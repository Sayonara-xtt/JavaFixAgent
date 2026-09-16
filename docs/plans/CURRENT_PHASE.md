# Current Implementation Plan（当前实施计划）

> 变化频率最高的文档。Codex 执行任务前应优先确认本文 + 根目录 [`AGENTS.md`](../../AGENTS.md)。

长期路线图见 [`ROADMAP.md`](ROADMAP.md)。

---

## Current Phase

```text
Phase 3 — Business Domain（业务领域）
```

状态：**MVP 已验收**；当前仅维护本阶段，**不自动进入 Phase 4**。

验收证据：[`phase-3-acceptance.md`](phase-3-acceptance.md)

---

## Current Goal（当前目标）

维护订单域 MVP 的正确性与可测性：

- User / Product / Order / OrderItem
- REST（查询商品、下单、订单详情/分页、取消）
- 库存扣减与恢复
- 真 MySQL（Testcontainers）不变量测试

---

## Allowed（本阶段允许）

- 维护 / 修复订单域相关 Java 代码、Mapper XML、Flyway、测试
- 维护接口文档（Knife4j / OpenAPI）与相关配置
- 维护文档（尤其是本阶段验收与架构文档）
- 自动集成测试继续使用 Testcontainers 临时 MySQL

---

## Forbidden（本阶段禁止）

- 静默把测试换成 H2，或回退本机数据库冒充通过
- 添加 Python Agent 实现代码（`agent/` 运行时）
- 引入 LangChain、LangGraph、MCP、多智能体编排、RAG、向量库、Redis
- 实现 Docker Sandbox / Benchmark Runner / Evaluation Engine（除非单独批准并更新本文）
- 进入 Phase 4 或更高阶段功能（须显式批准）

硬约束原文亦见 [`AGENTS.md`](../../AGENTS.md)。

---

## Must-read

完整策略见 [`../README.md`](../README.md)。最小集合：`AGENTS.md` → `ARCHITECTURE.md` → 本文 → 任务专项文档。

---

## Task List（本阶段维护清单）

- [x] 订单域 MVP 实现与真 MySQL 不变量测试
- [x] Phase 3 验收（见 `phase-3-acceptance.md`）
- [ ] 按需修复回归 / 文档同步
- [ ] Phase 3 相关未提交改动的整理与提交（仅在用户明确要求时 commit）

历史施工单（归档，仅参考）：

- [`../archive/superpowers/specs/2026-09-15-phase-3-business-domain-design.md`](../archive/superpowers/specs/2026-09-15-phase-3-business-domain-design.md)
- [`../archive/superpowers/plans/2026-09-15-phase-3-business-domain.md`](../archive/superpowers/plans/2026-09-15-phase-3-business-domain.md)

---

## Acceptance Criteria（本阶段验收标准）

已满足（摘要）：

- 创建订单 / 取消 / 分页 / 库存不变量可测
- `mvn test` / `mvn verify` 在 Testcontainers MySQL 上通过
- 不使用 Spring Data JPA；不使用 H2 冒充通过

详见 [`phase-3-acceptance.md`](phase-3-acceptance.md)。

---

## Next Phase Entry（下一阶段入口）

**Phase 4 — Python Agent Skeleton**

进入条件：

1. 用户显式批准
2. 更新 `AGENTS.md` 与本文 `CURRENT_PHASE.md`
3. 按 [`ROADMAP.md`](ROADMAP.md) Phase 4 范围实施；遵守 [`../agent/AGENT_RUNTIME.md`](../agent/AGENT_RUNTIME.md)

在批准前：**禁止**添加 Python Agent 实现代码。

---

## Verification Before Finish（完成前验证）

```text
git status
git diff
```

相关 Java 变更时运行：

```powershell
cd shop-service
.\mvnw.cmd test
```

**Do not commit automatically unless explicitly requested. / 除非明确要求，否则不要自动 commit。**
