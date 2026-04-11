# 2026-04-10 - 前后端契约同步（admin/community/docs 最小可用）

## 1. 目标

- 在不引入新依赖的前提下，补齐 admin/community/docs 的最小可用后端只读接口。
- 将前端三个页面从本地 mock 数据切换为优先读取后端接口。
- 保留原页面结构与交互样式，确保改动可验证、可回退。

## 2. 后端实现范围

- 新增内容契约控制器：`/api/v1/content`
  - `GET /api/v1/content/community/feed`（支持 `filter`、`sort`）
  - `GET /api/v1/content/admin/cdks`（支持 `search`、`status`）
  - `GET /api/v1/content/docs/chapters`
- 新增对应 DTO（community/admin/docs）以对齐前端现有数据结构。
- 安全配置补充 CORS：允许本地前端来源（localhost/127.0.0.1 任意端口）跨域调用。

## 3. 前端实现范围

- 扩展统一 API 客户端：新增 content 契约类型与请求函数。
- 社区视图改为调用 `community/feed`，同步帖子列表与热门话题。
- 管理视图改为调用 `admin/cdks`，同步统计与列表筛选结果。
- 文档视图改为调用 `docs/chapters`，动态驱动左侧章节导航。
- 路由页 `app/admin`、`app/community`、`app/docs` 统一复用 `components/views`，避免双轨实现。

## 4. 测试与验证

- 后端：`mvn -q -Dtest=ContentSyncControllerTests test` 通过。
  - 结果：Tests run: 4, Failures: 0, Errors: 0, Skipped: 0。
- 前端：
  - `pnpm run lint` 通过。
  - `pnpm run build` 通过。

## 5. 风险与后续

- 当前新增接口为“最小可用契约”，数据仍是后端内置样例，尚未接入真实业务表。
- 若后续进入业务化阶段，建议按同一路径逐步替换为 application/domain/infrastructure 的真实查询实现。
- 当前 admin/community/docs 的写操作（发帖、生成 CDK、撤销等）仍未开放后端接口。