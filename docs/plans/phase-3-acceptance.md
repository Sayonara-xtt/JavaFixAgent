# Phase 3 验收记录

日期：2026-09-16。状态：已完成已批准的订单域 MVP 与补强范围；Phase 4 尚未批准。

## 已交付流程

查询商品 → 校验下单请求与用户/商品 → 原子扣库存 → 汇总金额并保存订单和明细。
查询订单详情与分页；仅 CREATED 订单可取消，成功后按明细恢复库存一次。
用户/商品主要通过测试夹具准备，不包含管理后台、支付、物流或登录权限。

取消资格以数据库条件更新判定，不以先前读取的状态为准。
库存恢复目标不存在或已逻辑删除时，返回业务错误 40002，整次取消回滚：
订单仍为 CREATED，此前恢复的其他库存也回滚。不会静默成功或重复还库存。
订单明细按 id 升序读取，保留商品名、价格及小计快照。

## 验收证据

在 Windows Java 21 / Spring Boot 3.5.16 / Testcontainers 1.21.4 / MySQL 8.4 上执行：

```powershell
cd D:\codework\JavaFixAgent\shop-service
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

2026-09-16 15:49:42（Asia/Shanghai）：BUILD SUCCESS，退出码 0。
40 次测试执行，失败 0、错误 0、跳过 0；含参数化与重复执行，并非 40 个独立测试方法。

| 测试类 | 执行次数 | 验证内容 |
|---|---:|---|
| ShopApplicationTests | 1 | 实际容器 JDBC 地址/数据库、Flyway V1/V2、MyBatis |
| OpenApiDocsTest | 2 | OpenAPI 业务路径与文档页面 |
| OrderDomainIntegrationTest | 7 | 基础订单/库存流程、受控业务错误、分页、逻辑删除 |
| Phase3AcceptanceTest | 30 | 请求边界、整单回滚、历史快照、乐观锁、并发不变量 |

JAR：`shop-service/target/shop-service-0.0.1-SNAPSHOT.jar`。
本次仅验证打包成功，不代表已部署为常驻后台服务。
原始测试报告位于 `shop-service/target/surefire-reports/`，下次 clean 会重新生成。

## 红 → 绿记录

首批新增 24 次执行有 5 个失败，归因于 4 类问题：
并发取消双成功、还库存零行未检查、null 明细漏校验、非法 JSON 未统一包装。
修复后 24 次全部通过；随后追加写入失败/乐观锁测试并重复并发场景，最终共 40 次通过。

数据库失败注入采用测试库内的临时 CHECK 约束，拒绝第二条明细写入，
验证订单头、前一条明细及全部库存回滚，finally 中移除约束。
不修改 Flyway V1/V2，不提升数据库账号权限。
并发取消用测试专用 MyBatis 拦截器同步真实查询后的时序，不伪造 SQL 返回值。
并发下单为 8 个请求争抢 3 件库存；并发取消为两个真实事务竞争同一订单。
两类并发测试各重复三次，有等待超时及线程退出断言。

只读独立审查未发现关键问题；明细排序建议已采纳。

## 约束与已知限制

- 自动测试只使用临时 MySQL 容器、随机宿主端口、不复用容器；不回退本机 MySQL 或 H2。
- 未运行本机数据库初始化脚本，未重置密码或变更本机 MySQL 配置。
- Flyway 仍提示其测试支持范围落后于 MySQL 8.4；本次 V1/V2 迁移已成功，未隐藏警告。
- Mockito 动态加载 Java agent 仍有警告，不影响本次 Java 21 测试结果。
- 本验收不是生产完整性认证或全面压测；未覆盖所有多商品锁竞争、死锁/重试及故障恢复组合。
- 无 Python Agent、Redis、支付、物流、登录权限、CI 部署或自动改代码闭环。
- 当前维持 Phase 3 维护；进入 Phase 4 需显式批准并更新 AGENTS。
- 未自动提交 Git；仓库内已有 Phase 3 未提交改动一并保留。
