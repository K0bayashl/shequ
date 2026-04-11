# MCP 与 Hooks 配置指南

> 这份文档用于把工作流里提到的 MCP 和 hooks 落成可复制、可启用的配置、脚本和安装说明。

## 1. 先决条件

- 使用支持 MCP 和 hooks 的客户端，例如 VS Code Copilot / Claude 相关客户端。
- 本地已安装 Node.js 18+，用于执行示例脚本。
- 你有权限修改仓库中的 `.github/hooks/`、`scripts/hooks/` 和客户端配置文件。

## 2. 目录约定

```text
.github/hooks/
  session-start.json
  pre-tool-use.json
  post-tool-use.json
  pre-compact.json
scripts/hooks/
  session-start.mjs
  pre-tool-use.mjs
  post-tool-use.mjs
  pre-compact.mjs
scripts/mcp/
  community-mvp-mcp.mjs
```

## 3. hooks 的启用方式

### 3.1 仓库内配置

把 hooks 的命令配置放到 `.github/hooks/` 下，每个事件一个 JSON 文件。

### 3.2 客户端启用

如果你的客户端支持读取仓库 hooks，确保它已经允许加载 `.github/hooks/*.json`。
如果你的客户端需要本地显式配置，就把仓库中的示例命令复制到本地设置文件里。

## 4. hooks 示例

### 4.1 SessionStart

用途：新会话开始时，提醒 AI 先读规范、进度和当前任务文档。

### 4.2 PreToolUse

用途：在执行工具前做规则确认或危险操作拦截。

### 4.3 PostToolUse

用途：在改动后做轻量校验或格式化提醒。

### 4.4 PreCompact

用途：在上下文压缩前输出当前摘要，避免丢失状态。

## 5. MCP 接入建议

当前仓库优先推荐的 MCP 类型是：

- GitHub / 文档 / 工作区文件统一 MCP：读取 issue、PR、仓库状态、外部文档和仓库文件。
- 领域 MCP：数据库、支付、对象存储、消息队列等外部系统接入。

### 5.1 最小接入原则

- 如果当前只打算使用 Copilot 和 Codex，就先只给这两个客户端各配一份 MCP 注册，不必一次性接入 Claude / Gemini。
- MCP 服务本体尽量只维护一份；客户端只保留很薄的一层注册配置。
- 第一阶段优先接 GitHub / 文档 / 工作区文件统一 MCP，后续再按需补数据库、浏览器、对象存储等 MCP。

### 5.2 Copilot + Codex 最小接入模板（示意）

下面是一个通用模板，不同客户端的字段名和文件格式可能不同，实际使用时请替换成对应客户端支持的写法。

```text
MCP 服务名：community-mvp
用途：GitHub + 文档检索 + 工作区文件读取

服务端启动命令：node scripts/mcp/community-mvp-mcp.mjs
服务端工作目录：.
环境变量：
  - GITHUB_PERSONAL_ACCESS_TOKEN=<your-github-personal-access-token>
  - COMMUNITY_MVP_GITHUB_REPOSITORY=<owner/repo>
  - COMMUNITY_MVP_MCP_WORKSPACE_ROOT=e:\app

客户端注册要点：
  - Copilot 单独保存一份客户端配置
  - Codex 单独保存一份客户端配置
  - 两者都指向同一个服务启动命令或同一个远程服务地址
```

### 5.3 配置原则

- 先配置最常用、最稳定的 MCP。
- 只要工作流用到 MCP，就要写清楚安装位置、配置文件位置和验证方式。
- 如果不同客户端的配置格式不同，就提供仓库内的示例模板，而不是只写抽象说明。

### 5.4 推荐的第一阶段 MCP 服务

如果你现在只打算接 Copilot 和 Codex，我建议先上 1 个统一服务：

1. `community-mvp`
   - 作用：在同一个服务里同时提供 GitHub、文档检索和工作区文件读取能力。
   - 场景：需求分析、任务分解、代码审查、交付整理、上下文恢复。
   - 工具：`github_repo_info`、`github_list_issues`、`github_list_pull_requests`、`github_get_issue`、`github_get_pull_request`、`fetch_url`、`read_workspace_file`、`list_workspace_files`。
   - 认证：使用 `GITHUB_PERSONAL_ACCESS_TOKEN`，并通过 `COMMUNITY_MVP_GITHUB_REPOSITORY` 指定仓库。

### 5.5 具体示例配置文件

仓库里已经放了一个可以直接复制修改的正式模板：`docs/mcp-services.example.json`，其内容直接对应 `community-mvp` 这一统一服务。

如果你的客户端要求不同格式，可以把这个示例中的服务名、命令、参数和环境变量字段映射成它自己的配置格式，但保留同样的统一服务语义。

## 6. 验证方式

- SessionStart 触发后，AI 应输出当前规则摘要和任务目标。
- PreToolUse 触发前，应能看到规则确认信息或拦截信息。
- PostToolUse 完成后，应能看到简短的验证或格式化提示。
- PreCompact 触发前，应能看到当前任务摘要。

## 7. 你应该如何使用这份指南

- 新增一个依赖外部系统的工作流时，先在这里补配置说明。
- 新增一个 hook 或 MCP 时，优先给出可复制的示例文件。
- 如果暂时无法真正接入，也至少保留模板和替换说明，避免只写“建议”。
