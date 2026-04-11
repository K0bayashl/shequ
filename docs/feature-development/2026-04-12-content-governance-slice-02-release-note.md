# 2026-04-12 - 内容治理切片 02 发布说明

## 发布范围

- 后端新增内容治理最小闭环：举报提交、举报处理、课程下架/恢复、用户封禁/解封、审计留痕。
- 前端完成治理同步：
  - 管理端治理页接入举报列表与处理动作。
  - 管理端新增课程治理与用户治理按钮。
  - 文档阅读页新增课程举报入口。

## 关键接口

- `POST /api/reports`
- `GET /api/admin/moderation/reports`
- `POST /api/admin/moderation/reports/{reportId}/handle`
- `POST /api/admin/moderation/courses/{courseId}/takedown`
- `POST /api/admin/moderation/courses/{courseId}/restore`
- `POST /api/admin/moderation/users/{userId}/ban`
- `POST /api/admin/moderation/users/{userId}/unban`

## 验证结果

- 后端：`ModerationControllerTests` + `CourseControllerTests` + `ContentCourseServiceTests`，共 9/9 通过。
- 前端：`pnpm lint` 无 error；`pnpm build` 成功。

## 已知事项

- 前端仍存在 1 条历史 lint warning（`profile-view.tsx`），与本次发布无直接关联。
- 当前发布为最小治理闭环，复杂审核流与自动风控能力不在本次范围内。
