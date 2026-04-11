# 2026-04-10 - 前端收敛（质量基线与重复实现清理）

## 1. 目标

- 收敛前端目录中的重复实现，降低维护噪音。
- 建立可执行的 lint 基线，避免仅依赖 build 通过。
- 验证收敛改动不影响现有路由构建结果。

## 2. 实现范围

- 删除未被引用的重复 hook 文件：
  - `frontend/components/ui/use-mobile.tsx`
  - `frontend/components/ui/use-toast.ts`
- 新增前端 ESLint 配置：
  - `frontend/eslint.config.mjs`
- 补齐 lint 运行依赖：
  - 新增 devDependencies：`eslint`、`eslint-config-next`
- 修复 lint 规则触发点：
  - `frontend/components/ui/sidebar.tsx` 中移除渲染期随机数，改为稳定宽度值。

## 3. 验证结果

在 `frontend/` 下执行：

- `pnpm run lint`：通过
- `pnpm run build`：通过（静态路由 `/`、`/admin`、`/community`、`/docs` 均正常产出）

## 4. 风险与遗留

- 当前前端页面仍以本地 mock 交互为主，尚未与后端接口联调。
- `next.config.mjs` 中存在 `typescript.ignoreBuildErrors: true`，后续建议在联调前移除并完成严格类型校验。
- 本次仅做“收敛与基线”改动，未触及业务行为与视觉设计。

## 5. 下一步建议

- 以页面为单位推进 API 联调（优先：认证、社区列表、后台 CDK 管理）。
- 在 CI 中加入 `pnpm run lint` + `pnpm run build`，将当前基线固化。
