# 2026-04-09 - 用户模块第一阶段实现

> 目标：将 [2026-04-08-user-module-development.md](2026-04-08-user-module-development.md) 中确认的第一阶段用户能力落到 `backend/`，并确保测试可重复执行。

## 1. 本次实现范围

- 用户注册
- CDK 校验与消费
- 邮箱 + 密码登录
- JWT 签发与解析
- 当前用户信息读取
- 受保护个人资料读取
- 修改密码
- 禁用用户访问拦截

## 2. 主要实现

- 按 layer-first DDD 结构重新整理后端包结构：`application`、`domain`、`interfaces`、`infrastructure`。
- 新增用户领域模型与值对象。
  - `UserAccount`
  - `Cdk`
  - `UserRole`
  - `UserStatus`
  - `UserPrincipal`
  - `UserProfile`
- 新增用户应用层命令 / 查询 / 服务。
  - `RegisterUserCommand`
  - `LoginUserCommand`
  - `ChangePasswordCommand`
  - `GetProfileQuery`
  - `GetViewerProfileQuery`
  - `UserModuleService`
- 新增用户领域仓储端口与领域服务。
  - `UserAccountRepository`
  - `CdkRepository`
  - `UserAuthenticationVerifier`
  - `UserAuthenticationVerifierImpl`
- 新增持久化实体、JPA 仓储与适配器。
  - `UserAccountEntity`
  - `CdkEntity`
  - `JpaUserAccountRepository`
  - `JpaCdkRepository`
  - `UserAccountRepositoryAdapter`
  - `CdkRepositoryAdapter`
- 新增用户接口层与 DTO。
  - `UserController`
  - `RegisterUserRequest`
  - `LoginUserRequest`
  - `ChangePasswordRequest`
  - `UserSummaryResponse`
  - `UserProfileResponse`
- 完成 JWT / Security 链路对接。
  - `JwtTokenService`
  - `JwtAuthenticationFilter`
  - `CurrentUserContext`
  - `SecurityConfig`
- 新增用户模块 Flyway 迁移，完成 `user` 与 `cdk` 两张表的基线结构。

## 3. 验证结果

- 已执行并通过：
  - `mvn -B -ntp -s backend/settings.xml -f backend/pom.xml clean test`
- 结果：
  - 12 个测试全部通过
  - 用户注册、登录、个人资料、修改密码、禁用态拦截都已覆盖

## 4. 已知约束

- 本机默认 JDK 是 17，当前项目基线也已经统一到 Java 17。
- 第一阶段未实现第三方登录、找回密码、头像上传、管理员后台、刷新 token、多人会话管理。

## 5. 下一步建议

- 在 Java 17 环境下继续补跑正式构建。
- 基于用户模块继续拆下一个业务模块。
- 如果后续需要扩展登录态能力，再补刷新 token 和会话管理。