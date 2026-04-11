# 2026-04-11 - 内容治理后端最小字段与接口草案

> 目标：给内容系统提供可落地的最小治理能力，覆盖举报、下架、封禁和审计留痕。
> 适用阶段：内容模块第一阶段实现前的接口与数据草案。

## 1. 目标

- 在不引入复杂审核流的前提下，提供发布后处置能力。
- 优先满足快速止损：可举报、可下架、可封禁、可追溯。

## 2. 假设

- 社区采用 CDK 注册制，用户登录后即为会员。
- 当前内容核心对象是课程与章节，后续可复用到帖子与评论。
- 后端沿用 Spring Boot + DDD 分层 + Flyway。

## 3. 范围

- 包含：治理数据模型草案、接口草案、权限边界、最小验收标准。
- 不包含：全量人工审核队列、复杂工单系统、自动风控评分。

## 4. 最小数据字段草案

### 4.1 举报记录 content_report

- id: bigint, 主键
- content_type: varchar(32), 内容类型（course 或 chapter，后续可扩展 post/comment）
- content_id: bigint, 内容主键
- reporter_user_id: bigint, 举报人
- reason_code: varchar(32), 举报原因编码
- reason_detail: varchar(500), 补充说明
- status: tinyint, 0 待处理, 1 已处理, 2 已驳回
- handled_by: bigint, 处理人（管理员）
- handled_at: datetime, 处理时间
- handle_note: varchar(500), 处理备注
- created_at: datetime
- updated_at: datetime

### 4.2 内容治理状态字段（复用到内容表）

- moderation_status: tinyint, 0 正常, 1 已下架
- moderation_reason: varchar(255), 下架原因
- moderated_by: bigint, 操作者
- moderated_at: datetime, 操作时间

说明：课程可直接在 course 表增加上述字段；章节可在 course_chapter 表增加同类字段。

### 4.3 用户治理状态（复用到 user）

- status: 现有用户状态字段继续沿用
- ban_reason: varchar(255), 封禁原因（可选）
- banned_at: datetime, 封禁时间（可选）
- banned_by: bigint, 操作者（可选）

### 4.4 审计日志 action_audit_log

- id: bigint, 主键
- actor_user_id: bigint, 操作者
- action_type: varchar(64), 操作类型（report_handle/content_takedown/user_ban）
- target_type: varchar(32), 目标类型
- target_id: bigint, 目标主键
- action_result: varchar(32), 结果（success/failed）
- detail_json: text, 扩展详情
- created_at: datetime

## 5. 最小接口草案

### 5.1 用户举报

- POST /api/reports
- 权限：登录会员
- 请求体：contentType, contentId, reasonCode, reasonDetail
- 响应：reportId, status

### 5.2 管理员查询举报列表

- GET /api/admin/reports
- 权限：管理员
- 查询参数：status, contentType, page, size
- 响应：分页列表

### 5.3 管理员处理举报

- POST /api/admin/reports/{id}/handle
- 权限：管理员
- 请求体：decision（approve/reject）, handleNote, optionalActions
- optionalActions 可包含：takedownContent, banUser

### 5.4 管理员下架内容

- POST /api/admin/content/{type}/{id}/takedown
- 权限：管理员
- 请求体：reason
- 响应：status

### 5.5 管理员恢复内容

- POST /api/admin/content/{type}/{id}/restore
- 权限：管理员
- 请求体：reason
- 响应：status

### 5.6 管理员封禁用户

- POST /api/admin/users/{id}/ban
- 权限：管理员
- 请求体：reason
- 响应：status

### 5.7 管理员解封用户

- POST /api/admin/users/{id}/unban
- 权限：管理员
- 请求体：reason
- 响应：status

## 6. 权限边界

- 会员：仅可发起举报，不可查看他人举报详情。
- 管理员：可查看与处理举报，可执行下架与封禁。
- 未登录用户：无举报与管理权限。

## 7. DDD 分层建议（候选文件）

- interfaces/rest/moderation: 报告与处置控制器、请求响应 DTO
- application/moderation: 举报提交、举报处理、下架、封禁用例服务
- domain/moderation: 举报实体、处置规则、治理状态值对象
- domain/user: 封禁行为复用用户聚合规则
- infrastructure/persistence/moderation: JPA 实体与仓储适配
- db/migration: 新增治理相关迁移脚本

## 8. 风险

- 若缺少审计日志，后续争议难以追溯。
- 若仅做前端隐藏不做后端鉴权，易绕过。
- 若一次性引入过多自动规则，会增加维护成本。

## 9. 有序实施计划（最小可执行）

1. 先落表：report 与 audit 基础表 + course/course_chapter 治理字段。
2. 再落接口：举报提交、管理员查询、管理员处理。
3. 再补动作：内容下架/恢复、用户封禁/解封。
4. 最后补测试与文档：鉴权、状态流转、审计留痕验证。

## 10. 验收与验证计划

- 权限验证：会员可举报，非管理员不可处理举报。
- 状态验证：举报状态从待处理到已处理或驳回可追踪。
- 处置验证：下架后内容对外不可见，恢复后可见。
- 封禁验证：被封禁用户无法访问受保护接口。
- 审计验证：每次处理动作都有审计记录。