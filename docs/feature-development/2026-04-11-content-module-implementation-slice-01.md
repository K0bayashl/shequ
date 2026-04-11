# 2026-04-11 - 内容模块实现切片 01（数据库与核心读写链路）

> 目标：为内容模块第一阶段提供可直接开工的最小实现清单。
> 范围：Flyway 迁移 + DDD 分层的 Controller / Service / Repository 文件级拆解。

## 1. 切片目标

- 完成课程与章节的数据库基线。
- 打通管理员课程创建（含章节）与会员课程读取链路。
- 保持与现有 DDD 目录一致，不做无关重构。

## 2. 切片边界

- 包含：课程/章节核心能力（创建、列表、详情、章节正文）。
- 包含：课程状态三态（草稿/发布/下架）。
- 不包含：举报流程、封禁流程、复杂审核流（放到后续治理切片）。
- 不包含：搜索推荐、学习进度、评论互动。

## 3. Flyway 迁移清单

## 3.1 迁移顺序

1. 新增 `V2__content_module_core.sql`
2. 新增 `V3__content_module_index_refine.sql`（可选，索引拆分时使用）

## 3.2 V2 必做项

- 新建 `course` 表：
  - 主键 id
  - title
  - description
  - cover_image
  - status（0 草稿, 1 已发布, 2 已下架）
  - moderation_status（默认 0 正常）
  - moderation_reason
  - moderated_by
  - moderated_at
  - created_by
  - created_at
  - updated_at
- 新建 `course_chapter` 表：
  - 主键 id
  - course_id（外键）
  - title
  - content（Markdown 原文）
  - sort_order
  - moderation_status（默认 0 正常）
  - moderation_reason
  - moderated_by
  - moderated_at
  - created_at
  - updated_at
- 基础约束：
  - `course_chapter.course_id` 外键
  - 同课程章节排序唯一约束（`course_id`, `sort_order`）
- 最小索引：
  - `course(status, updated_at)`
  - `course_chapter(course_id, sort_order)`

## 3.3 迁移验收

- 空库执行：`V1 -> V2` 可一次性成功。
- 增量执行：已有 V1 的库可升级到 V2。
- 回归检查：用户模块表结构不受影响。

## 4. 文件级实现拆解（DDD）

## 4.1 domain 层

- 新增目录：`backend/src/main/java/com/community/mvp/backend/domain/content/model`
- 新增目录：`backend/src/main/java/com/community/mvp/backend/domain/content/repository`
- 候选文件：
  - `Course.java`
  - `CourseChapter.java`
  - `CourseStatus.java`
  - `CourseRepository.java`
  - `CourseChapterRepository.java`

验收要点：
- 领域对象不依赖 Spring/JPA。
- 状态流转规则在领域或应用层显式表达，不散落在控制器。

## 4.2 application 层

- 新增目录：`backend/src/main/java/com/community/mvp/backend/application/content/command`
- 新增目录：`backend/src/main/java/com/community/mvp/backend/application/content/query`
- 新增目录：`backend/src/main/java/com/community/mvp/backend/application/content/service`
- 候选文件：
  - `CreateCourseCommand.java`
  - `CreateCourseChapterCommand.java`
  - `ListPublishedCoursesQuery.java`
  - `GetCourseDetailQuery.java`
  - `GetChapterContentQuery.java`
  - `ContentCourseService.java`

验收要点：
- `ContentCourseService` 负责用例编排与事务边界。
- 管理员写操作与会员读操作在方法职责上分离。

## 4.3 interfaces 层

- 新增目录：`backend/src/main/java/com/community/mvp/backend/interfaces/rest/content/course`
- 新增目录：`backend/src/main/java/com/community/mvp/backend/interfaces/rest/content/course/dto`
- 候选文件：
  - `AdminCourseController.java`
  - `CourseController.java`
  - `CreateCourseRequest.java`
  - `CreateCourseResponse.java`
  - `CourseListItemResponse.java`
  - `CourseDetailResponse.java`
  - `ChapterContentResponse.java`

接口建议：
- `POST /api/admin/courses`
- `GET /api/courses`
- `GET /api/courses/{id}`
- `GET /api/courses/{id}/chapters/{chapterId}`

验收要点：
- 控制器只做参数校验、鉴权入口、响应映射。
- 不在控制器写业务规则。

## 4.4 infrastructure 层

- 新增目录：`backend/src/main/java/com/community/mvp/backend/infrastructure/persistence/content/entity`
- 新增目录：`backend/src/main/java/com/community/mvp/backend/infrastructure/persistence/content/repository/jpa`
- 新增目录：`backend/src/main/java/com/community/mvp/backend/infrastructure/persistence/content/adapter`
- 候选文件：
  - `CourseEntity.java`
  - `CourseChapterEntity.java`
  - `JpaCourseRepository.java`
  - `JpaCourseChapterRepository.java`
  - `CourseRepositoryAdapter.java`
  - `CourseChapterRepositoryAdapter.java`

验收要点：
- Repository 接口在 domain，适配器在 infrastructure。
- Entity 与 Domain 转换保持显式可测试。

## 5. 测试拆解

## 5.1 接口集成测试

- 新增目录：`backend/src/test/java/com/community/mvp/backend/interfaces/rest/content/course`
- 候选文件：`CourseControllerTests.java`
- 覆盖点：
  - 管理员可创建课程
  - 非管理员创建被拒绝
  - 列表仅返回发布状态课程
  - 章节顺序按 `sort_order`
  - 下架课程不可被普通列表返回

## 5.2 应用层测试

- 新增目录：`backend/src/test/java/com/community/mvp/backend/application/content`
- 候选文件：`ContentCourseServiceTests.java`
- 覆盖点：
  - 状态流转校验
  - 章节归属与排序规则
  - 课程不存在与章节不匹配异常

## 6. 执行顺序（最小可执行）

1. 先做 Flyway V2 迁移与结构验证。
2. 再补 domain + infrastructure 最小读写能力。
3. 再补 application 服务与事务编排。
4. 再补 interfaces 控制器与 DTO。
5. 最后补测试并跑最小验证命令。

## 7. 验证命令建议

- 后端测试：`mvn -B -ntp -s backend/settings.xml -f backend/pom.xml test`
- 定向测试（实现后补）：
  - `-Dtest=CourseControllerTests`
  - `-Dtest=ContentCourseServiceTests`

## 8. 完成定义（DoD）

- 迁移脚本在空库和增量库均可执行。
- 课程创建、列表、详情、章节正文接口可用且鉴权正确。
- 课程状态三态行为符合文档定义。
- 新增测试通过，且不破坏现有用户模块测试。