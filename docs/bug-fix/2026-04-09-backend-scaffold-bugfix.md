# 2026-04-09 - backend 脚手架问题修复单

> 基于 [AI 修 bug 提问模板](../AI修bug提问模板.md) 整理
> 文档状态：已修复
> 最后更新：2026-04-10

## 1. 问题标题

- 问题标题：backend 脚手架中的 JWT、健康检查、异常映射和 Maven 示例配置存在缺口
- 问题类型：功能错误 / 兼容性问题 / 报错 / 其他
- 发现时间：2026-04-09
- 发现环境：本地

## 2. 问题背景

当前 `backend/` 目录已经完成第一版社区 MVP 后端脚手架，但在审查时发现几处会影响后续开发和部署验证的缺口：

- JWT 骨架看起来已接入，但实际没有建立认证态，后续受保护接口会全部落入 401。
- 健康检查放行规则只覆盖了 `/actuator/health` 和 `/actuator/info`，没有覆盖 readiness / liveness 探针路径。
- 全局异常处理没有区分请求体解析错误，非法 JSON 会被当成 500。
- `backend/settings.xml` 使用了当前机器的绝对 Maven 本地仓库路径，可移植性较差。

- 业务背景：社区 MVP 后端脚手架
- 涉及功能：安全骨架、健康检查、统一异常处理、本地 Maven 验证
- 影响范围：后续所有需要认证的接口、部署健康探针、接口错误语义、示例构建命令
- 严重程度：高

## 3. 复现步骤

1. 启动 `backend/` 脚手架。
2. 访问白名单接口 `/actuator/health` 或 `/api/v1/scaffold/ping`，确认当前脚手架可以启动。
3. 向未来的受保护接口发送带 `Authorization: Bearer ...` 的请求，但不把认证信息写入 `SecurityContext`。
4. 访问 `/actuator/health/liveness` 或 `/actuator/health/readiness`。
5. 向 `/api/v1/scaffold/echo` 发送非法 JSON，例如缺少引号或多余逗号。
6. 使用 README 或 `backend/settings.xml` 里的 Maven 示例命令在其他机器、其他路径或其他系统上复用。

如果问题不是稳定复现，也请说明：
- 偶发 / 必现：必现
- 大约几次会出现一次：每次
- 是否和特定数据、账号、环境有关：和环境相关，尤其是 Java 版本、Maven 本地仓库路径和部署探针路径

## 4. 期望结果

- 正确结果：带合法 Bearer Token 的后续受保护接口能够识别身份并通过认证链路。
- 正确提示：非法 JSON 应返回清晰的客户端错误，例如 400 或统一的请求解析错误码。
- 正确状态：健康探针路径应全部处于白名单或显式允许状态；Maven 示例配置应具备可移植性。

## 5. 实际结果

- 实际表现：
  - `JwtAuthenticationFilter` 只读取 `Authorization` 头，不建立认证态。
  - `SecurityConfig` 将非白名单请求全部设为 `authenticated()`。
  - 探针子路径未放行。
  - 非法 JSON 会落入兜底异常处理。
  - `backend/settings.xml` 使用当前机器绝对路径。
- 错误提示：
  - 未来受保护接口会返回 401，即使请求携带合法 Bearer Token。
  - 非法 JSON 会返回 `SYSTEM_ERROR` / 500。
- 错误状态：
  - JWT 骨架不可用。
  - readiness / liveness 探针可能被拦截。
  - 客户端请求错误与服务端错误语义混淆。
  - Maven 示例在不同机器上可复用性差。
- 异常日志：
  - 当前测试阶段未直接触发生产级错误日志；该问题由代码审查和本地验证推断得出。

## 6. 错误日志与截图

- 控制台日志：
  - `mvn -B -ntp -s backend/settings.xml -f backend/pom.xml "-Djava.version=17" test` 当前可以通过，但只验证了脚手架已有的 happy path。
- 后端日志：
  - 当前测试运行时可见 Spring Boot 正常启动日志，但没有覆盖 JWT、探针和非法 JSON 的失败路径。
- 浏览器控制台：
  - 不适用。
- 截图：
  - 不适用。
- 报错堆栈：
  - 当前问题由代码审查发现，尚未在运行时捕获具体堆栈。
- 相关链接：
  - [SecurityConfig.java](/E:/app/backend/src/main/java/com/community/mvp/backend/config/SecurityConfig.java)
  - [JwtAuthenticationFilter.java](/E:/app/backend/src/main/java/com/community/mvp/backend/infrastructure/security/JwtAuthenticationFilter.java)
  - [GlobalExceptionHandler.java](/E:/app/backend/src/main/java/com/community/mvp/backend/interfaces/rest/GlobalExceptionHandler.java)
  - [application.yml](/E:/app/backend/src/main/resources/application.yml)
  - [application-dev.yml](/E:/app/backend/src/main/resources/application-dev.yml)
  - [settings.xml](/E:/app/backend/settings.xml)

## 7. 最近改动

- 最近改了哪些文件：
  - `backend/pom.xml`
  - `backend/src/main/java/com/community/mvp/backend/**`
  - `backend/src/main/resources/application*.yml`
  - 已移除脚手架阶段的 Flyway 基线表迁移
  - `backend/settings.xml`
  - `.github/workflows/ci.yml`
- 最近加了什么逻辑：
  - Spring Boot 后端脚手架、统一返回体、全局异常处理、JWT/Redis 骨架、Flyway 基线、健康检查与示例接口
- 最近合并了什么分支：
  - 不适用，当前仓库尚未初始化 Git
- 问题是改完之后才出现的吗：
  - 是 / 否 / 不确定：是

## 8. 影响范围

- 影响页面 / 接口：
  - 后续所有受保护 API
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
  - 所有 JSON 请求接口
  - Maven 示例执行入口
- 影响用户类型：
  - 后续开发者、部署环境、测试环境、未来业务接口调用方
- 是否阻塞业务：
  - 是，尤其是认证接口、探针和错误处理链路
- 是否有临时绕过方案：
  - 当前仅能依赖白名单接口和手工调整本地 Maven 配置

## 9. 已尝试过的排查

- 已尝试方案 1：
  - 使用 `mvn -B -ntp -s backend/settings.xml -f backend/pom.xml "-Djava.version=17" test` 验证脚手架
- 已尝试方案 2：
  - 对 `backend/` 核心实现和配置文件做逐文件审查
- 已确认不是：
  - 不是脚手架无法启动
  - 不是基础测试没有通过
  - 不是所有接口都失效
- 已排除的文件或模块：
  - 当前问题不在前端、MCP hooks 或 Git 目录

## 10. 相关文件

- 前端文件：
  - 无
- 后端文件：
  - [SecurityConfig.java](/E:/app/backend/src/main/java/com/community/mvp/backend/config/SecurityConfig.java)
  - [JwtAuthenticationFilter.java](/E:/app/backend/src/main/java/com/community/mvp/backend/infrastructure/security/JwtAuthenticationFilter.java)
  - [JwtTokenService.java](/E:/app/backend/src/main/java/com/community/mvp/backend/infrastructure/security/JwtTokenService.java)
  - [GlobalExceptionHandler.java](/E:/app/backend/src/main/java/com/community/mvp/backend/interfaces/rest/GlobalExceptionHandler.java)
  - [ScaffoldController.java](/E:/app/backend/src/main/java/com/community/mvp/backend/interfaces/rest/ScaffoldController.java)
  - [application.yml](/E:/app/backend/src/main/resources/application.yml)
  - [application-local.yml](/E:/app/backend/src/main/resources/application-local.yml)
  - [application-dev.yml](/E:/app/backend/src/main/resources/application-dev.yml)
  - [application-test.yml](/E:/app/backend/src/main/resources/application-test.yml)
  - [settings.xml](/E:/app/backend/settings.xml)
- 测试文件：
  - [CommunityMvpBackendApplicationTests.java](/E:/app/backend/src/test/java/com/community/mvp/backend/CommunityMvpBackendApplicationTests.java)
  - [ScaffoldControllerTests.java](/E:/app/backend/src/test/java/com/community/mvp/backend/interfaces/rest/ScaffoldControllerTests.java)
- 配置文件：
  - [pom.xml](/E:/app/backend/pom.xml)
  - [ci.yml](/E:/app/.github/workflows/ci.yml)
- 数据表 / 接口：
  - `/actuator/health`
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
  - `/api/v1/scaffold/ping`
  - `/api/v1/scaffold/echo`

## 11. 修改约束

- 不能改的模块：
  - 不能重写整个脚手架结构
  - 不能把后端脚手架扩展成业务功能
- 不能变的接口：
  - `/api/v1/scaffold/ping`
  - `/api/v1/scaffold/echo`
  - `/actuator/health`
- 不能新增的依赖：
  - 不要为了修复而引入过重的额外框架
- 不能影响的行为：
  - local profile 无数据库可启动
  - 统一返回体与基础测试继续可用

## 12. 修复目标

- 修复目标：
  - 让 JWT 骨架真正可用于后续受保护接口
  - 让健康检查探针路径保持可用
  - 让非法 JSON 返回客户端错误
  - 让 Maven 示例配置具有更好的可移植性
- 验收标准：
  - 后续受保护接口能够根据 Bearer Token 建立认证态
  - `/actuator/health/liveness` 和 `/actuator/health/readiness` 不被安全配置误拦截
  - 非法 JSON 返回 400 或统一请求解析错误码
  - Maven 示例配置不依赖当前机器绝对路径，或明确标注为本机专用
- 是否需要补测试：是
- 是否需要补文档：是

## 13. 给 AI 的输出要求

- 请先分析根因，不要直接猜。
- 请给出最小修复方案。
- 请说明需要修改哪些文件。
- 请补充或更新测试。
- 请在修改后说明如何验证。
- 如果信息不够，请先问最少必要问题。


## 14. 修复状态

- 修复结论：已完成
- 验证结论：`mvn -B -ntp -s backend/settings.xml -f backend/pom.xml "-Djava.version=17" test` 通过
- 当前残留项：Java 21 环境下的完整构建尚未在本机复跑，但本次提交的功能修复和测试已经闭环
