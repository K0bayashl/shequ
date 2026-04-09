---
name: github-actions
description: "用于编写、审查和排查 GitHub Actions workflow（.github/workflows/*.yml）；适用于触发器、权限、缓存、矩阵、Secrets、CI 故障和 Java/Maven 构建。"
argument-hint: "workflow 文件、CI 报错或目标分支"
---
# GitHub Actions

## 适用场景

- 新增或修改 `.github/workflows/*.yml`
- 调整 `push`、`pull_request`、`workflow_dispatch`、`schedule` 等触发器
- 为 Java / Maven 项目配置 build、test、cache、artifact 上传和发布流水线
- 排查 CI 失败、权限不足、secret 缺失、缓存失效、条件判断错误或工作流语法问题
- 设计 PR 校验、分支保护、矩阵构建、手动发布或自动化交付流程

## 工作流程

1. 先确认目标 workflow、触发事件和受影响分支。
2. 对照仓库结构确认构建入口、命令和环境要求。
3. 检查 `permissions`、`env`、`secrets`、`if` 条件和缓存配置是否最小且明确。
4. 优先使用官方 Action 的稳定版本，减少不必要的步骤。
5. 对 Java 项目优先验证 `actions/checkout@v4`、`actions/setup-java@v4`、Maven 命令和缓存策略。
6. 如果 workflow 依赖远端仓库、环境变量或 secret，要明确写出前置条件和验证方式。
7. 修改后检查 workflow 在 push、PR 和手动触发下的行为是否一致。

## 检查清单

- `on:` 是否覆盖需要的事件和分支
- job `runs-on` 是否合理
- `permissions` 是否最小化
- `checkout` 是否在第一步
- 依赖安装和缓存是否正确
- 构建命令是否与本地一致
- `if:` 条件是否会误跳过关键步骤
- secret 名称是否和仓库设置一致
- 路径判断是否兼容当前仓库结构
- 失败输出是否足够清楚，便于排障

## 本仓库的常见约定

- 如果仓库包含 `backend/pom.xml`，优先把 CI 指向 `mvn -B -ntp -f backend/pom.xml test`
- 如果 workflow 只在特定分支执行，要与 GitHub 仓库的默认分支和分支保护保持一致
- 如果 workflow 会上传制品或执行发布动作，要先确认仓库权限和 secret 已配置
- 如果只是在准备 PR 文本或发布说明，继续使用 `java-release`；本 skill 只处理 workflow YAML 和 CI 行为

## 输出格式

- 目标
- 影响范围
- 变更建议
- 验证方式
- 风险与待确认项