# AI 驱动的 Java 项目工作区

这个仓库当前同时保存了两类内容：

- 一套从 0 到 1 开发 Java 项目的 AI 工作流模板。
- `backend/` 下已经落地的社区 MVP 后端脚手架。

## 目录说明

- `AGENTS.md`：仓库级开发规范，所有 AI 任务都应该先遵守它。
- `docs/ai-workflow.md`：Java 项目的自主开发流程说明。
- `.github/agents/`：推荐的子代理角色定义。
- `.github/skills/`：可复用的工作流技能。
- `.github/prompts/`：可直接调用的任务入口。
- `.github/instructions/`：按文件类型自动加载的规则。
- `.github/workflows/ci.yml`：GitHub Actions 持续集成模板。
- `.github/skills/window-context-init/`：新窗口接手项目时恢复上下文的技能。
- `.github/skills/github-actions/`：GitHub Actions workflow 编写、审查和排障技能。
- `.github/prompts/initialize-window-context.prompt.md`：新窗口上下文初始化入口。
- `.github/prompts/fix-bug.prompt.md`：启动 bug 分析和修复流程的入口。
- `.github/prompts/close-task.prompt.md`：任务完成或 bug 修复完成后的收尾入口。
- `.github/instructions/ddd.instructions.md`：DDD 领域建模规则。
- `.github/instructions/flyway.instructions.md`：Flyway 数据库迁移规则。
- `.github/instructions/frontend.instructions.md`：React + Next.js + Tailwind CSS 前端规则。
- `.github/hooks/`：MCP / hooks 示例配置。
- `scripts/hooks/`：hooks 示例脚本。
- `scripts/mcp/`：统一 MCP 服务入口脚本。
- `backend/README.md`：后端脚手架的运行、配置和验证说明。
- `backend/`：Spring Boot 3.x + Maven 后端脚手架。
- `frontend/`：前端项目预留目录。
- `docs/需求开发文档模板.md`：需求分析前使用的标准需求文档模板。
- `docs/AI修bug提问模板.md`：修复 bug 前使用的标准问题描述模板。
- `docs/项目进度.md`：记录当前项目真实状态的进度文档。
- `docs/mcp-hooks-setup.md`：MCP 和 hooks 的配置与启用指南。
- `docs/mcp-services.example.json`：Copilot / Codex 共用的 MCP 服务示例配置。
- `docs/bug-fix/`：bug 修复记录目录。
- `docs/feature-development/`：功能开发记录目录。
- `scripts/mcp/community-mvp-mcp.mjs`：统一 MCP 服务入口脚本。

## 推荐使用方式

1. 先让总协调子代理读取需求并拆解任务。
2. 让侦察子代理做只读调查。
3. 让实现子代理只处理一个最小任务切片。
4. 让审查子代理在合并前找风险和测试缺口。
5. 通过 GitHub Actions 保持 CI 通过。

## 默认建议

- Java 21
- Maven
- JUnit 5
- Mockito
- GitHub Actions

如果你想继续，我可以下一步直接在现有 `backend/` 脚手架上推进第一个业务模块，比如用户、CDK 或帖子模块。
