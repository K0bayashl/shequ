---
description: "用于编辑社区 MVP 的前端代码。覆盖 React、Next.js、TypeScript、Tailwind CSS、shadcn/ui 和前端工程约定。"
applyTo:
  - "**/frontend/**/*.tsx"
  - "**/frontend/**/*.ts"
  - "**/frontend/**/*.css"
---
# 前端技术栈与开发规范

## 默认技术栈
- 框架：React 18+ (App Router)
- SSR/路由：Next.js
- 语言：TypeScript
- 样式：Tailwind CSS
- UI 组件库：shadcn/ui
- 图标：Lucide React

## 适用场景
- 维护 React 组件、Next.js 页面级路由、Server Components 和 Client Components。
- 编写前端业务交互、权限展示、内容管理和后台页面。
- 处理 Markdown 渲染、资源上传入口和管理后台视图。

## 基本原则
- 优先使用函数式组件与 React Hooks。
- 尽量使用 TypeScript 显式描述 props、state、接口返回和组件边界。
- 明确划分 Server Components 和 Client Components (`"use client"` 指令)。
- 优先使用 Tailwind CSS 类名控制样式，避免编写额外的 CSS/SCSS 文件。
- 优先使用现成的 shadcn/ui 组件与设计模式，在此基础上定制。
- 所有异步请求必须提供可感知的状态反馈（至少包含 loading 与 error），禁止请求失败后页面无提示。
- 请求失败时优先透传并展示后端返回的 `message`，若无可用信息再降级为统一兜底提示。

## 结构建议
- 页面入口（`page.tsx`）负责组装和数据获取（优先在服务端获取）。
- 复杂表单、富文本编辑、上传和列表查询分别抽取为独立的客户端组件（Client Components）。
- Markdown 渲染、代码高亮、图片和视频内嵌等能力应统一封装并在需要的地方复用。
- 业务状态管理优先使用简单的 Context、URL 参数或是 SWR/React Query，避免不必要的全局状态库。

## 代码约束
- 不要在服务端组件中使用浏览器专有的 API（如 `window`、`localStorage` 或 `useState`/`useEffect`）。
- 组件命名使用 PascalCase，自定义 Hooks 使用 camelCase 且以 `use` 开头。
- 文件目录命名统一采用连字符风格（kebab-case）。
- 表单提交按钮在请求进行中应禁用或进入 loading 状态，防止重复提交造成误判。

## 输出要求
- 如果某段前端实现偏离 React + Next.js + Tailwind + shadcn/ui 的默认栈，请先指出偏离点。
- 如果需要增加第三方依赖，先说明原因、替代方案和体积影响。
- 优先给出最小改动方案，不要顺手重构无关页面。---
description: "用于编辑社区 MVP 的前端代码。覆盖 Vue 3、TypeScript、Vite、Vue Router、Pinia、Element Plus 和前端工程约定。"
applyTo:
  - "**/src/**/*.vue"
  - "**/src/**/*.ts"
  - "**/src/**/*.tsx"
  - "**/src/**/*.css"
  - "**/src/**/*.scss"
---
# 前端技术栈与开发规范

## 默认技术栈
- 框架：Vue 3
- 语言：TypeScript
- 构建工具：Vite
- 路由：Vue Router
- 状态管理：Pinia
- UI 组件库：Element Plus

## 适用场景
- 编写和维护 Vue 3 页面、组件、布局、路由、状态和表单逻辑
- 编写前端业务交互、权限展示、内容管理和后台页面
- 处理 Markdown 渲染、资源上传入口和管理后台视图

## 基本原则
- 优先使用 Vue 3 Composition API 和 `<script setup>`。
- 尽量使用 TypeScript 显式描述 props、emit、state、接口返回和组件边界。
- 页面逻辑优先拆分为可复用的 composables、stores 和子组件，不要把业务逻辑塞进单个大组件。
- 路由、权限和状态管理要显式组织，不要散落在各个页面里。
- UI 优先使用 Element Plus 组件和一致的设计模式，避免重复造轮子。

## 结构建议
- 页面级组件负责组装，不要承担过多业务判断。
- 领域状态放到 Pinia store，复用逻辑放到 composables。
- 复杂表单、富文本编辑、上传和列表查询分别封装，不要写成一个超大页面文件。
- Markdown 渲染、代码高亮、图片和视频内嵌等能力应统一封装，避免每个页面各写一套。

## 代码约束
- 不要为了“新潮”随意更换前端框架或状态库。
- 不要在页面中直接堆大量接口调用和字段转换逻辑。
- 组件命名、目录结构和路由命名要一致、可预测。
- 如果某个交互需要复杂状态迁移，优先先抽 store 或 composable，再考虑继续堆在页面里。

## 测试与验证
- 前端行为变更时，应至少验证关键交互、表单提交和权限展示逻辑。
- 如果项目后续引入前端测试框架，再为关键页面、组合式函数和状态管理补测试。
- 资源上传、权限切换、Markdown 渲染和管理后台操作属于高风险路径，修改后要重点验证。

## 输出要求
- 如果某段前端实现偏离 Vue 3 + TypeScript + Vite + Pinia + Element Plus 的默认栈，请先指出偏离点。
- 如果需要临时破例，先说明原因、替代方案和影响范围。
- 优先给出最小改动方案，不要顺手重构无关页面。
