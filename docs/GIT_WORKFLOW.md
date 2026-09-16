# Git 推送与协作标准（Git Push / PR Standard）

> 本文是本仓库 **分支 / commit / push / PR / merge** 的单一事实来源（Single Source of Truth）。  
> 本项目采用业界常见 AI 协作默认：**AI 交付到「功能分支 + PR」；人在 PR 里 Review 后再 Merge 进 `main`。**

硬约束摘要亦见根目录 [`AGENTS.md`](../AGENTS.md)。

---

## 0. 标准交付链路（必须遵守）

```text
按大版本（Phase）从 main 拉分支
  → 在该分支开发与本地验证
  → commit（本地，备注含简易中文）
  → push 功能分支到 origin
  → 开 PR（base = main）
  → 【人】在 PR 里 Review（+ CI）
  → 【人】Merge 进 main
  → 本地切回 main 并 pull
  → 下一重大版本再开新分支
```

| 步骤 | 谁做 | 说明 |
|------|------|------|
| 拉大版本分支 | AI / 人 | 一个大版本一条主工作分支 |
| commit | AI（须用户批准触发） | 只进当前大版本分支 |
| push 功能分支 | AI（须用户批准触发） | **不到 main** |
| 开 PR | AI（须用户批准触发） | AI 默认最远交付点 |
| Review + Merge | **人** | 进 `main` 必须人审 |

**AI 默认停在：commit + push 功能分支 + 开 PR。**  
**不要**让 AI 直接 push / merge 进 `main`。

---

## 1. 四个动作别混淆

| 动作 | 英文 | 发生在哪 | 效果 |
|------|------|----------|------|
| 提交 | **commit** | 本机 Git | 写入本地历史；远程还看不到 |
| 推送 | **push** | 本机 → `origin` **功能分支** | 远程可见该大版本分支；`main` 不变 |
| 开 PR | **Pull Request** | GitHub | 申请合进 `main`，进入人审 |
| 合并 | **merge** | PR 上由人操作 | 进入 `origin/main` |

常见误解：

- 「提交了」≠「推到 GitHub 了」
- 「推送了」≠「已经进 main 了」
- 「开了 PR」≠「已经合并了」

---

## 2. 大版本分支策略（按 Phase 拉分支）

本仓库的「大版本」= **Roadmap 中的 Phase（阶段）**，不是每次小改都新开分支。

### 2.1 规则

| 规则 | 说明 |
|------|------|
| **按大版本拉分支** | 每个 Phase 从最新 `main` 拉一条工作分支 |
| **同版本内复用** | 同一 Phase 的修 bug、补文档、小迭代，继续用该分支多次 commit |
| **升版本再新开** | 进入下一 Phase（须用户显式批准）时，先确保上一版本已 Merge（或明确策略），再从新的 `main` 拉新分支 |
| **禁止** | 为每次微调都拉新分支；禁止未批准 Phase 的代码混进当前大版本 PR |

### 2.2 分支命名

```text
codex/phase-<N>-<short-name>
```

示例：

```text
codex/phase-2-spring-boot-baseline
codex/phase-3-business-domain
codex/phase-4-python-agent-skeleton
```

### 2.3 开分支标准步骤

```bash
git fetch origin
git checkout main
git pull origin main
git checkout -b codex/phase-N-<short-name>
```

- 基线必须是已更新的 `main`（干净基线）。
- 若上一 Phase 尚未合入 `main`，不要假装「新大版本已基于正式主干」；应先合并或由用户指定基线策略。

### 2.4 `main`（主分支）

`main` = **Clean Baseline（干净基线）**：

```text
buildable + testable + clean
```

禁止：直接往 `main` 堆未审查大改、Benchmark 故意 Bug、长期半成品。

MVP 阶段不引入复杂 Git Flow（多级 release/develop 等）。

---

## 3. 谁可以触发什么

| 动作 | 默认 | 条件 |
|------|------|------|
| 拉大版本分支 | 可执行 | 用户批准进入该 Phase / 明确要求开分支 |
| `git commit` | 不自动 | 用户说「提交」或「按标准交付」等（见第 8 节） |
| `git push` 功能分支 | 不自动 | 同上 |
| 开 PR | 不自动 | 同上 |
| Merge 进 `main` | **仅人** | 人在 GitHub Review 后点 Merge |
| force push `main` | **禁止** | — |
| 改 `git config` | **禁止** | — |
| `--no-verify` | **禁止** | 除非用户明确要求 |

用户可要求「只 commit 不推送」——此时不要擅自 push / 开 PR。  
若用户说「按标准提交流程 / 交付到 PR」，则执行：**commit → push 功能分支 → 开 PR**（仍不 Merge）。

---

## 4. 标准执行清单

### Step A — 确认大版本分支

1. 读 [`plans/CURRENT_PHASE.md`](plans/CURRENT_PHASE.md)，确认当前 Phase。
2. 应在 `codex/phase-N-...` 上工作，**不要**在 `main` 上直接堆功能。
3. 若新 Phase 已批准且尚无分支：按 §2.3 从 `main` 拉新分支。

### Step B — 开发与本地验证

1. 只改当前 Phase 允许范围。
2. 验证示例：`shop-service` 下 `.\mvnw.cmd test` / `./mvnw test`。
3. 始终查看 `git status`、`git diff`。
4. 测试 / 命令 / Git 状态 = **Ground Truth（真实依据）**。

### Step C — Commit

1. `git status` / `git diff` / `git log`
2. 暂存相关文件；禁止密钥（`.env`、真实密码、token）
3. Commit message **必须含简易中文备注**（见第 6 节）
4. `git commit` 后复查 `git status`

### Step D — Push 功能分支

```bash
git push -u origin HEAD
```

- 推的是大版本功能分支，**不是** `main`
- 首次用 `-u` 设置 upstream

### Step E — 开 PR（AI 交付终点）

- **base：** `main`
- **head：** 当前 `codex/phase-N-...`
- **Title：** 标明大版本（如 Phase 3 …）
- **Summary：** 至少含简明中文（做了什么 / 为什么）
- **Test plan：** 如何验证

```bash
gh pr create --base main --title "..." --body "..."
```

或在 GitHub 网页 Compare & pull request。

### Step F — 人审与 Merge（仅人）

1. 人在 PR 查看 Files changed  
2. 确认描述、中文备注、测试 / CI  
3. 人点击 **Merge**  
4. 本地同步：

```bash
git checkout main
git pull origin main
```

合并后可删已合功能分支（须用户同意）。下一 Phase 再按大版本新开分支。

---

## 5. PR 合并标准（人审检查单）

- [ ] 属于当前大版本 Phase 范围
- [ ] 相关测试通过（或替代验证已写明）
- [ ] 无密钥、无故意故障进入 `main`
- [ ] 文档与约定已同步（如需要）
- [ ] PR 说明含简明中文，后人能看懂

冲突时：**可验证的业务正确性** > 赶进度合并。

---

## 6. Commit Message 约定（提交备注）

- 前缀：`feat` / `fix` / `docs` / `test` / `chore` / `refactor`
- **必须含简易中文**（一两句说清做什么 / 为什么）

推荐：

```text
feat: deliver Phase 3 order-domain MVP and restructure docs

交付 Phase 3 订单域 MVP，并拆分重组项目文档。
```

反例：`feat: update stuff`（无中文、无意图）。

---

## 7. 禁止清单

```text
✗ 未经用户触发自动 commit / push / 开 PR
✗ AI 直接 Merge 或 push 到 main
✗ 不按大版本、每次小改都乱开长期分支
✗ 下一 Phase 未批准就混进当前大版本分支/PR
✗ force push 到 main / master
✗ 提交密钥；为变绿削弱测试
✗ Benchmark 故意 Bug 进入 main
✗ 用「模型说做完了」代替测试与 git 状态
```

---

## 8. 用户常用口令

| 你说 | AI 应做 |
|------|---------|
| 开 Phase N 分支 / 拉大版本分支 | 从最新 `main` 创建 `codex/phase-N-...` |
| 提交 / commit | 仅本地 commit |
| 推送 / push | push 当前功能分支 |
| 开 PR | 必要时先 push，再开 PR（不 Merge） |
| **按标准提交流程 / 交付到 PR** | **commit → push 功能分支 → 开 PR**（停在人审前） |
| 合并到 main | 提示在 GitHub 由人 Merge；AI 不代合 |

---

## 9. 状态怎么理解

```text
仅 commit           → 只在本机大版本分支
已 push             → 远程有功能分支，main 未变
已开 PR             → 等待人审（AI 标准交付完成）
已 Merge            → 进入 origin/main，大版本落地
```

```bash
git fetch origin
git log --oneline origin/main -5
git merge-base --is-ancestor <commit> origin/main
```

退出码 0 表示该 commit 已合入 `origin/main`。

---

## 10. 相关文档

- [`../AGENTS.md`](../AGENTS.md) — Codex 执行约束
- [`plans/CURRENT_PHASE.md`](plans/CURRENT_PHASE.md) — 当前大版本（Phase）
- [`plans/ROADMAP.md`](plans/ROADMAP.md) — Phase 1–13
- [`benchmark/BENCHMARK.md`](benchmark/BENCHMARK.md) — main 干净基线
- [`README.md`](README.md) — 文档地图
