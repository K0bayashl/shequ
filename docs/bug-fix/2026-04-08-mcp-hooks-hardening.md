# 2026-04-08 - MCP / hooks 基础加固

## 问题背景

仓库中已经有 MCP 与 hooks 的工作流说明，但此前存在几个高优先问题：

- MCP 只有客户端侧示例，没有自维护服务本体。
- hooks 的 `PreToolUse` 只是默认放行，没有危险操作拦截逻辑。
- MCP 和 hooks 的说明只有原则，没有实际可落地配置。

## 修复目标

- 落地一个可执行的 MCP 服务入口。
- 为 hooks 提供可运行的示例脚本。
- 给出仓库内可查看的配置模板和启用说明。

## 本次完成

- 新增 `scripts/mcp/community-mvp-mcp.mjs` 作为统一 MCP 服务入口。
- 新增 `.github/hooks/*.json` 示例配置。
- 新增 `scripts/hooks/*.mjs` 示例脚本。
- 新增 `docs/mcp-services.example.json` 作为 Copilot / Codex 共用的 MCP 服务模板。
- 新增 `docs/mcp-hooks-setup.md` 作为启用指南。
- 将 `PreToolUse` 从默认放行改为基于常见危险命令模式的拦截示例。

## 验证方式

- 文档检查：确认 README、MCP 说明、hooks 说明和项目进度之间的引用完整。
- 代码检查：确认 MCP 服务脚本具备 stdio JSON-RPC 基本流程、工具清单和工具调用处理。
- 规则检查：确认 `PreToolUse` 不再是无条件 allow。
- 运行检查：对 `scripts/hooks/pre-tool-use.mjs` 输入普通工具调用和危险命令样例，分别验证 allow 和 deny。

## 风险与遗留项

- 不同客户端的 MCP 配置格式可能略有差异，需要在各自客户端里补注册。
- GitHub 权限和仓库名需要在实际启用时填写真实值。
- 由于本仓库尚未引入 Node 依赖清单，脚本的运行环境需要本地具备 Node.js 18+。

## 下一步

- 在 Copilot 和 Codex 中分别注册这份 MCP 服务模板。
- 根据实际客户端格式补各自的本地配置示例。
- 后续如果需要，再把危险命令拦截规则收紧到更严格的白名单机制。
