# JavaFix Agent

JavaFix Agent 是一个学习与工程项目：用 Python 构建可解释的 Coding Agent，
最终能在受控工作区中对 Java Spring Boot + MyBatis-Plus 应用进行搜索、修改、
测试与验证。

This is not a chatbot project. Its goal is a reproducible software-engineering
loop grounded in source code, command results, automated tests, and Git diffs.

**定位一句话：** 不是聊天机器人，而是「任务 → 改代码 → 跑测试 → 看 diff」的可复现工程闭环。

## Documentation / 文档

完整索引与读取策略：**[`docs/README.md`](docs/README.md)**

常用入口：

- [`AGENTS.md`](AGENTS.md) — Codex 当前硬约束
- [`docs/plans/CURRENT_PHASE.md`](docs/plans/CURRENT_PHASE.md) — 现在做什么
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — 系统总体架构
- [`docs/GIT_WORKFLOW.md`](docs/GIT_WORKFLOW.md) — Git 推送 / PR 标准

## Repository structure / 仓库结构

```text
javafix-agent/
|-- AGENTS.md              Codex 执行约束
|-- README.md              项目概览与上手（本文）
|-- agent/                 Python Coding Agent（占位，Phase 4+）
|-- shop-service/          Java 靶场（Phase 2–3 已实现）
|-- benchmark/tasks/       可重复工程任务（占位）
|-- infra/                 未来 Docker/沙箱（占位）
|-- scripts/               本机辅助脚本
|-- docs/                  现行架构/计划 + archive/ 历史文档
`-- .github/workflows/     CI（占位）
```

## Technology stack / 技术栈

### Coding agent（规划）

- Python 3.12+
- OpenAI API
- Pydantic, Typer, Rich, and pytest
- Git and a controlled shell environment

### Java target（已落地）

- Java 21 and Spring Boot 3.x
- MyBatis-Plus (Spring Data JPA is intentionally excluded)
- Maven, JUnit 5, Mockito, and Flyway
- MySQL 8.x
- Knife4j / OpenAPI（接口文档）

自动测试使用 Testcontainers 临时 MySQL 8.4 容器；Maven 和 Java 测试仍在本机运行。

## Current phase / 当前阶段

**Phase 3 — Business Domain（业务领域，已验收；Phase 4 待批准）**

`shop-service` 已包含订单域 MVP：商品、用户、订单、库存扣减/恢复、REST、
基于真 MySQL 的不变量测试，以及 Knife4j 文档。Python Agent 仍不在本阶段范围。

Phase 3 已补齐请求边界、整单回滚、商品快照、乐观锁与并发库存测试。
最新干净构建通过 40 次测试执行（含参数化及并发重复测试），并成功打包 JAR。
验收范围、错误约定和已知限制见 [`docs/plans/phase-3-acceptance.md`](docs/plans/phase-3-acceptance.md)。

## Automated tests / 容器化测试

先启动 Docker Desktop（Linux containers），再运行下方 Maven 测试命令。
Testcontainers 自动创建临时数据库、选择随机端口、执行 Flyway V1/V2；
测试进程结束后清理容器。首次运行需要联网下载依赖与镜像。
不需要启动本机 MySQL、初始化本机测试库或配置测试数据库密码。
Docker 不可用时测试直接失败，不回退本机数据库或 H2。

跑 Java 测试（Windows）：

```powershell
cd shop-service
.\mvnw.cmd test
```

Unix：

```bash
cd shop-service
./mvnw test
```

## Optional local MySQL / 手动启动应用时可选

以下初始化脚本仅用于手动运行应用，不再是自动测试的前置步骤。

如需手动运行应用，可用管理账号创建脚本中的独立 schema，并通过 DB_* 环境变量配置应用连接：

```text
scripts/mysql/init-test-database.sql
```

或 Windows：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/mysql/init-test-database.ps1
```

仅创建 `javafix_shop_test` 与受限账号 `javafix_test`，**不会**修改 MySQL root 密码。

运行时密钥（如 `DB_PASSWORD`）用环境变量注入，勿提交真实凭据。

应用启动后打开 Knife4j：

```text
http://localhost:8080/doc.html
```

OpenAPI JSON：`/v3/api-docs`。启动日志也会打印上述地址。

## High-level roadmap / 路线图摘要

1. 初始化 monorepo — 已完成  
2. Spring Boot + MyBatis-Plus 基线 — 已完成  
3. 订单域测试应用 — 已验收完成  
4. Python Agent 抽象与 CLI  
5. 受控 Shell 环境与 Agent Loop  
6. 首次测试驱动自动修 bug  
7. Benchmark、沙箱、追踪与评估  

Phase 4 须显式批准后方可开始。Phase 细节见 [`docs/plans/ROADMAP.md`](docs/plans/ROADMAP.md)；当前任务见 [`docs/plans/CURRENT_PHASE.md`](docs/plans/CURRENT_PHASE.md)。

## Repository status / 仓库状态

以 Git 为准：

```shell
git status
git diff
```

项目工具不会自动提交 / 推送 / 开 PR。  
标准链路：**按 Phase 大版本拉分支 → commit → push 功能分支 → 开 PR → 人审 Merge**。详见 [`docs/GIT_WORKFLOW.md`](docs/GIT_WORKFLOW.md)。
