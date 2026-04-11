# 社区 MVP - v0.dev UI 设计提示词

本文档整理了用于 v0.dev 生成本项目前端 UI 的专属英文提示词。

## ⚠️ 使用说明

1. **分次生成**：请**千万不要**将所有提示词一次性全发给 v0.dev。每次请新建一个对话（New Chat），只发送其中**一个页面**的提示词。
2. **渐进式微调**：生成初始版本后，可以直接在对话框里告诉它如何修改（中英文皆可）。例如：
   - *"Make the theme dark mode"* (改成深色模式)
   - *"Make the code block look more like VS Code"* (让代码块看起来更像 VS Code)
   - *"Change the primary color to a glowing neon green"* (把主色调换成赛博朋克绿)
3. **导出代码**：调整满意后，可以在 v0 右上角直接复制代码（React + Tailwind CSS + shadcn/ui 组件）。

---

## 1. 登录与 CDK 激活页 (Login & CDK Activation)
*这是整个买断制社区的入口，需要体现“高级感”和极简风。*

```text
Create a modern, minimalist authentication page for a premium, invite-only developer community. Use a clean, centered card layout in a sleek digital style (similar to Vercel or Linear). 
Tech stack: React, Tailwind CSS, shadcn/ui, Lucide icons.

The card should have two tabs: "Login" and "Activate".
1. "Login" tab: Basic Email and Password fields with a "Sign In" button.
2. "Activate" tab (The most important part): Fields for Username, Email, Password, and a highly prominent, visually distinct input field for a "CDK License Key" (Activation Code). Add a small tooltip or badge saying "Unlock Lifetime Access".
Use a monochrome color palette (black, white, grays) with a subtle tech blue or emerald green as the primary accent color.
```

---

## 2. 课程阅读页 (Course Reading/Learning UI)
*这是最强调沉浸式阅读和代码展示的区域。*

```text
Design a course reading and documentation interface for a developer learning platform. Style it closely to Notion, GitHub Docs, or Tailwind Docs. Clean, spacious, and highly legible. 
Tech stack: React, Tailwind CSS, shadcn/ui.

Layout requirements:
- A top navigation bar with breadcrumbs and user avatar.
- Left Sidebar: A scrollable, collapsible chapter navigation tree with active state highlighting.
- Main Content Area: A wide, immersive Markdown reading zone.
In the main area, simulate a beautifully rendered markdown article. Include:
1. A large H1 Title and some meta info (Author, Date).
2. A beautiful dark-themed code block with syntax highlighting structure.
3. A blockquote.
4. Clean typography with ample whitespace for reading comfort.
Make the layout fully responsive.
```

---

## 3. 交流社区与帖子列表 (Community Forum)
*混合了官方文章和普通用户互动的区域。*

```text
Create a modern community forum layout tailored for developers. It should look like a cleaner, more minimalist version of Reddit or Hacker News.
Tech stack: React, Tailwind CSS, shadcn/ui.

Layout:
- Left Main Column (70%): A list of discussion threads. Each thread card should show: Title, Author name, Avatar, time posted, and comment count.
Crucially, use visual tags/badges to distinguish between "Official Article" (styled prominently, maybe with a distinct background or icon) and "User Post" (standard style).
- Right Sidebar (30%): A "New Post" primary CTA button, a small profile summary card (showing "Pro Member" status), and a "Trending Topics" widget.
Keep the styling flat, bordered, and minimalist.
```

---

## 4. 管理员总览面板 (Admin Dashboard)
*管理课程、用户和 CDK 的后台入口。*

```text
Design a minimalist Admin Dashboard for a SaaS platform. 
Tech stack: React, Tailwind CSS, shadcn/ui components (Data tables, Cards, Buttons).

Layout:
- A fixed left sidebar with navigation links: Overview, Users, Courses, Content Moderation, and CDK Management.
- Provide a mock UI for the "CDK Management" view in the main content area.
- Add a top section with 3 metric cards (Total Users, Active Members, Remaining CDKs).
- Below that, present a clean Data Table showing a list of generated CDKs, their status (Used/Unused), who used them, and an action column with a "Generate New CDKs" button at the top of the table.
```
