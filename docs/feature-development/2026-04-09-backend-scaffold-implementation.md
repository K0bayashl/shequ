# 2026-04-09 - backend 后端脚手架落地

## 背景

根据 [社区 MVP 项目脚手架开发文档](2026-04-08-project-scaffold-development.md)，在 `backend/` 目录下开始落地社区 MVP 后端工程底座。

## 本次完成

- 新增 `backend/pom.xml`，建立 Spring Boot 3.5.x + Maven 构建入口。
- 新增 Spring Boot 主程序、DDD 分层包结构和 `package-info.java` 占位。
- 新增统一返回体、错误码、业务异常和全局异常处理。
- 新增 `GET /api/v1/scaffold/ping` 和 `POST /api/v1/scaffold/echo` 两个最小验证接口。
- 新增安全骨架，包括安全配置、密码编码器、JWT 过滤器和 JWT 工具占位。
- 新增 `application.yml`、`application-local.yml`、`application-dev.yml`，并保留 Flyway 机制接入点但不创建脚手架阶段的数据库表。
- 新增 `backend/README.md` 与 `backend/settings.xml`，补充本地启动和 CLI 验证入口。
- 调整 [ci.yml](../../.github/workflows/ci.yml)，让 CI 指向 `backend/pom.xml`。

## 验证结果

- 在当前机器默认 JDK 17 环境下，执行 `mvn -B -ntp -s backend/settings.xml -f backend/pom.xml "-Djava.version=17" test`，5 条测试全部通过。
- 基线仍保持 Java 21；由于当前机器默认 Java 版本不是 21，本机的完整基线验证仍待补。

## 遗留项

- 需要在 Java 21 环境下补跑一遍完整构建与测试。
- 还没有进入用户、CDK、课程、帖子等业务模块。
- MCP 客户端本地注册仍待补齐。

## 下一步建议

- 先确认第一个业务模块切片。
- 在 `backend/` 上继续按 DDD 边界推进业务代码。
- 每完成一个业务里程碑就补一份 feature 记录。
