# 文档地图（Documentation Map）

> **唯一入口索引。** 其他地方只链到这里，不要再维护第二份「文档怎么读」长表。

## 现行文档（日常只用这些）

```text
docs/
├── README.md                 ← 你在这里
├── ARCHITECTURE.md           系统总体架构
├── agent/AGENT_RUNTIME.md    Agent 怎么运行
├── java/JAVA_ARCHITECTURE.md Java / 业务不变量（SSOT）
├── benchmark/BENCHMARK.md    Benchmark / 评估
└── plans/
    ├── CURRENT_PHASE.md      现在做什么（高频）
    ├── ROADMAP.md            长期 Phase 1–13
    └── phase-3-acceptance.md Phase 3 验收证据
```

根目录：

| 文件 | 用途 |
|------|------|
| [`../README.md`](../README.md) | 给人看的简介与启动 |
| [`../AGENTS.md`](../AGENTS.md) | Codex 当前硬约束 |

## 按任务读什么（最小集合）

```text
任何任务:     AGENTS.md → ARCHITECTURE.md → plans/CURRENT_PHASE.md
改 Java:      + java/JAVA_ARCHITECTURE.md
改 Agent:     + agent/AGENT_RUNTIME.md   （须已批准 Phase 4+）
改 Benchmark: + benchmark/BENCHMARK.md   （须已批准对应阶段）
```

英文专业术语在各文档中采用「English（中文）」就地备注，不再维护独立 glossary。

## 归档（不要当现行规则）

| 路径 | 说明 |
|------|------|
| [`archive/bootstrap/`](archive/bootstrap/CODEX_PROJECT_BOOTSTRAP_JAVAFIX_AGENT.md) | 原始大蓝图，已拆分 |
| [`archive/superpowers/`](archive/superpowers/) | Phase 2/3 历史 construction 单据 |
| [`bootstrap/`](bootstrap/README.md) | 空壳重定向（可忽略） |

## 冲突时谁说了算

```text
AGENTS.md
  → plans/CURRENT_PHASE.md
  → 专项架构（java / agent / benchmark）
  → ARCHITECTURE.md
  → archive/*（仅参考）
  → README.md（摘要）
```
