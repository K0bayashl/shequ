# 个人程序员社区 MVP 需求文档

> 文档状态：已确认 | 最后更新：2026-04-08

---

## 1. 基本信息

- 需求名称：个人程序员知识付费社区 MVP
- 提出人：项目负责人（个人开发者）
- 负责人：个人开发者
- 优先级：高
- 当前状态：已确认

---

## 2. 背景说明

- **业务背景：** 面向程序员群体的个人知识付费社区，由单人维护。对标鱼皮、小林 coding 等个人技术博主的社区模式。
- **问题描述：** 现有知识分享平台（B站、公众号）变现链路长、社区沉淀弱，无法形成稳定的付费用户群体。
- **现有方案：** 无，从零搭建。
- **为什么现在要做：** 知识付费赛道成熟，个人技术品牌变现路径清晰，MVP 先验证模式可行性。

---

## 3. 项目定位

**核心模式：**
```
用户免费注册 → 付款给管理员（微信）→ 获得 CDK → 激活会员 → 永久访问所有课程和社区内容
```

**角色定义：**

| 角色 | 说明 |
|---|---|
| 管理员（Admin） | 个人维护者，发布内容、管理用户、生成 CDK |
| 会员（Member） | 付费激活用户，可访问全部内容，可发帖评论 |
| 非会员（Inactive） | 免费注册但未激活用户，仅可浏览部分内容 |
| 游客（Guest） | 未注册用户，仅可看到帖子列表 |

---

## 4. 权限矩阵

| 功能 | 游客 | 非会员 | 会员 | 管理员 |
|---|---|---|---|---|
| 浏览帖子列表 | ✅ | ✅ | ✅ | ✅ |
| 查看帖子详情 + 评论 | ❌ | ✅ | ✅ | ✅ |
| 发帖 / 评论 | ❌ | ❌ | ✅ | ✅ |
| 查看课程列表 + 简介 | ❌ | ✅ | ✅ | ✅ |
| 观看课程内容（章节） | ❌ | ❌ | ✅ | ✅ |
| 发布课程 / 文章 | ❌ | ❌ | ❌ | ✅ |
| 上传资源（图片/视频） | ❌ | ❌ | ❌ | ✅ |
| 访问管理后台 `/admin` | ❌ | ❌ | ❌ | ✅ |

---

## 5. 功能需求

### 5.1 用户系统

**F1 - 用户注册（CDK 激活）**
1. 用户填写用户名、邮箱、密码、CDK 码
2. 系统校验 CDK 是否存在且未使用
3. 注册成功，账号状态直接设为 `active`（激活）
4. CDK 标记为已使用（逻辑删除，保留记录）

**F2 - 登录 / 登出**
1. 用户输入邮箱 + 密码
2. 校验通过后，返回 JWT Token（Payload 含 userId、role、status）
3. 登出时客户端清除 Token

**F3 - 个人主页**
- 展示：用户名、头像（可选）、注册时间、会员状态
- 展示该用户发布的帖子列表

**F4 - 修改密码**
- 输入旧密码 + 新密码（两次确认）
- 校验旧密码正确后更新

---

### 5.2 内容系统

**F5 - 课程发布（管理员）**
1. 管理员在后台创建课程，填写标题、简介、封面图
2. 依次添加章节，每章节有标题、内容（Markdown 格式）、排序号
3. Markdown 内容中可内嵌资源 URL（图片、视频）
4. 课程可保存为草稿或直接发布

**F6 - 课程列表**
- 所有注册用户可见（游客不可见）
- 展示：封面图、课程标题、简介、章节数、发布时间

**F7 - 课程详情 & 章节阅读**
- 仅会员可访问章节内容
- 左侧章节目录，右侧 Markdown 渲染内容
- 视频链接渲染为内嵌播放器

---

### 5.3 讨论区

> 讨论区混合展示管理员文章（type=0）和用户帖子（type=1），用标签区分来源。

**F8 - 发帖（会员）**
- 填写标题 + Markdown 正文（支持代码块、图片）
- 发布后出现在讨论区列表

**F9 - 管理员发文章**
- 同发帖流程，type 字段由系统标记为 article
- 展示时附带"管理员文章"标签

**F10 - 帖子列表（讨论区）**
- 游客可见列表（仅标题、发帖人、时间、评论数）
- 非会员及以上可进入详情

**F11 - 帖子详情 & 评论**
- 展示完整正文（Markdown 渲染）
- 评论列表（单层，按时间正序）
- 会员可发表评论（纯文本，MVP 阶段）

---

### 5.4 资源管理

**F12 - 文件上传（管理员）**
1. 管理员在编辑器内选择本地文件（图片 / 视频）
2. 后端上传至阿里云 OSS，写入 resource 表
3. 返回 OSS 访问 URL，编辑器自动插入到 Markdown

**F13 - 资源列表（管理员）**
- 后台展示所有已上传资源（文件名、类型、大小、上传时间）
- 支持删除（逻辑删除数据库记录）

---

### 5.5 管理后台（/admin）

**F14 - CDK 管理**
- 批量生成 CDK（指定数量，系统随机生成 CDK 码）
- 查看 CDK 列表（已使用 / 未使用，使用者信息）

**F15 - 用户管理**
- 查看用户列表（用户名、邮箱、状态、注册时间）
- 禁用 / 启用账号（修改 status 字段）

**F16 - 内容管理**
- 课程列表：编辑、下架（草稿化）、删除
- 章节编辑：修改内容、调整排序

**F17 - 帖子 / 评论管理**
- 删除违规帖子（逻辑删除）
- 删除违规评论（逻辑删除）

---

## 6. 业务规则

- CDK 一码一用，使用后立即标记，不可重复使用
- 管理员账号直接在数据库设置 `role=1`，不通过前台注册流程创建
- 课程和文章仅管理员可发布，普通会员只能发帖
- 帖子和评论的删除为逻辑删除（`status=1`），保留数据库记录
- 用户被禁用后（`status=2`），Token 仍在有效期内但接口鉴权应拒绝（需在鉴权逻辑中检查用户状态）

---

## 7. 数据库设计

### 技术说明
- 数据库：MySQL 8.x
- 主键类型：INT（自增），前端无精度丢失问题，容量约 21 亿条，足够个人社区使用
- 外键：逻辑外键（不在数据库层加约束，由业务代码保证）
- 软删除：使用 `status` 字段标记删除，不物理删除

---

### 表结构

#### 用户表 `user`

```sql
CREATE TABLE `user` (
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(50) NOT NULL COMMENT '用户名，唯一',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱，唯一',
    `password`   VARCHAR(255) NOT NULL COMMENT 'bcrypt 加密密码',
    `avatar`     VARCHAR(255) COMMENT '头像 URL',
    `role`       TINYINT     NOT NULL DEFAULT 0 COMMENT '角色：0=普通用户，1=管理员',
    `status`     TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0=未激活，1=已激活，2=已禁用',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) COMMENT='用户表';
```

#### CDK 表 `cdk`

```sql
CREATE TABLE `cdk` (
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`       VARCHAR(50) NOT NULL COMMENT 'CDK 码，唯一',
    `is_used`    TINYINT     NOT NULL DEFAULT 0 COMMENT '0=未使用，1=已使用',
    `used_by`    INT         COMMENT '使用者 user.id',
    `used_at`    DATETIME    COMMENT '使用时间',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) COMMENT='CDK 激活码表';
```

#### 课程表 `course`

```sql
CREATE TABLE `course` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(200) NOT NULL COMMENT '课程标题',
    `description` TEXT         COMMENT '课程简介（非会员可见）',
    `cover_image` VARCHAR(255) COMMENT '封面图 URL',
    `author_id`   INT          NOT NULL COMMENT '作者 user.id',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=草稿，1=已发布',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) COMMENT='课程表';
```

#### 课程章节表 `course_chapter`

```sql
CREATE TABLE `course_chapter` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `course_id`  INT          NOT NULL COMMENT '所属课程 course.id',
    `title`      VARCHAR(200) NOT NULL COMMENT '章节标题',
    `content`    LONGTEXT     COMMENT '章节内容（Markdown），含内嵌视频 URL',
    `sort_order` INT          NOT NULL DEFAULT 1 COMMENT '章节排序，从 1 开始',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) COMMENT='课程章节表';
```

#### 帖子表 `post`

```sql
CREATE TABLE `post` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    INT          NOT NULL COMMENT '发布者 user.id',
    `title`      VARCHAR(200) NOT NULL COMMENT '标题',
    `content`    LONGTEXT     COMMENT '正文（Markdown 格式）',
    `type`       TINYINT      NOT NULL DEFAULT 1 COMMENT '0=管理员文章，1=用户帖子',
    `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0=正常，1=已删除',
    `view_count` INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) COMMENT='帖子表（含管理员文章和用户帖子）';
```

#### 评论表 `comment`

```sql
CREATE TABLE `comment` (
    `id`         INT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`    INT      NOT NULL COMMENT '所属帖子 post.id',
    `user_id`    INT      NOT NULL COMMENT '评论者 user.id',
    `content`    TEXT     NOT NULL COMMENT '评论内容（纯文本，MVP 阶段）',
    `status`     TINYINT  NOT NULL DEFAULT 0 COMMENT '0=正常，1=已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`)
) COMMENT='评论表';
```

#### 资源表 `resource`

```sql
CREATE TABLE `resource` (
    `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `original_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
    `url`           VARCHAR(500) NOT NULL COMMENT '阿里云 OSS 访问 URL',
    `type`          TINYINT      NOT NULL COMMENT '0=图片，1=视频，2=音频，3=其他',
    `size`          BIGINT       NOT NULL COMMENT '文件大小（字节）',
    `uploader_id`   INT          NOT NULL COMMENT '上传者 user.id',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) COMMENT='资源表（图片/视频等 OSS 文件记录）';
```

---

### 表关联关系

```
user (1) ──→ (N) course          author_id → user.id
user (1) ──→ (N) post            user_id   → user.id
user (1) ──→ (N) comment         user_id   → user.id
user (1) ──→ (N) resource        uploader_id → user.id
user (1) ──→ (0..1) cdk          used_by   → user.id

course (1) ──→ (N) course_chapter  course_id → course.id
post   (1) ──→ (N) comment         post_id   → post.id
```

> 无中间表：付费模式为买断会员制（user.status 控制全部权限），无需 user-course 关联表。

---

## 8. 技术栈

| 层 | 选型 |
|---|---|
| 后端框架 | Spring Boot 3.x |
| 数据库 | MySQL 8.x |
| 缓存 | Redis |
| 鉴权 | JWT（Payload：userId、role、status） |
| 文件存储 | 阿里云 OSS |
| 前端框架 | Vue 3 + Element Plus |
| 内容格式 | Markdown（代码高亮 + 图片 + 视频内嵌） |
| 部署 | 阿里云 ECS |

---

## 9. 非目标（MVP 不做）

- 搜索功能
- 站内私信
- 通知 / 消息提醒
- 评论二级嵌套（后续可通过加 `parent_id` 扩展）
- 移动端 APP
- 积分 / 等级体系
- 数据统计后台

---

## 10. 后续可扩展点

| 扩展点 | 说明 |
|---|---|
| 评论嵌套 | comment 表加 `parent_id` 字段 |
| 课程进度 | 加 `user_course_progress` 表，记录已读章节 |
| 内容搜索 | 接入 Elasticsearch 或 MySQL 全文索引 |
| 自动支付 | 注册个体工商户后接入微信/支付宝商户 API |
| 消息通知 | 加 `notification` 表，评论有回复时提醒 |
