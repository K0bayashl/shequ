# Community MVP Backend

## Overview

This directory contains the Spring Boot backend scaffold for the Community MVP project.

## Environment

- Java 17
- Maven 3.9+

## Commands

- Start locally: `mvn spring-boot:run`
- Run tests: `mvn test`
- If you want the CLI to use the workspace-local Maven repository: `mvn -s settings.xml test`

## Profiles

- `local`: default profile, can start without a database
- `dev`: enables datasource, Flyway, Redis, JWT placeholders through externalized configuration

## Configuration

The project reads sensitive values from environment variables in `dev` profile.

You can use `src/main/resources/application-dev.example.yml` as a placeholder template reference.
Do not commit real secrets into repository files.

Suggested variables:

- `COMMUNITY_MVP_DB_URL`
- `COMMUNITY_MVP_DB_USERNAME`
- `COMMUNITY_MVP_DB_PASSWORD`
- `COMMUNITY_MVP_REDIS_HOST`
- `COMMUNITY_MVP_REDIS_PORT`
- `COMMUNITY_MVP_JWT_SECRET`

PowerShell example (fill placeholders before running):

```powershell
Set-Location E:\app\backend
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:COMMUNITY_MVP_DB_URL = "jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:COMMUNITY_MVP_DB_USERNAME = "<DB_USERNAME>"
$env:COMMUNITY_MVP_DB_PASSWORD = "<DB_PASSWORD>"
$env:COMMUNITY_MVP_REDIS_HOST = "<REDIS_HOST>"
$env:COMMUNITY_MVP_REDIS_PORT = "<REDIS_PORT>"
$env:COMMUNITY_MVP_JWT_SECRET = "<JWT_SECRET_AT_LEAST_32_CHARS>"
mvn spring-boot:run
```

## Structure

- `src/main/java/.../application`: application layer, grouped by module under `scaffold` and `user`
- `src/main/java/.../domain`: domain layer, grouped by module under `scaffold` and `user`
- `src/main/java/.../interfaces`: interface layer, grouped by module under `rest/scaffold` and `rest/user`
- `src/main/java/.../infrastructure`: infrastructure layer, including shared security and persistence adapters
- `src/main/java/.../common`: shared response, error, and web advice objects
- `src/main/java/.../config`: configuration and properties

## Current Scope

The scaffold currently includes:

- Spring Boot startup entry
- actuator health endpoint
- unified API response wrapper
- global exception handling
- validation-ready example endpoint
- JWT and Redis skeleton configuration
- Flyway is wired for later business modules, but scaffold phase does not create a database table

The backend now also includes the first business module:

- user registration with CDK consumption
- email/password login with JWT issuance
- self profile lookup and protected profile lookup
- password change with token invalidation on password update
- status-aware access checks for disabled accounts
- integration tests for the scaffold and user flows

The scaffold phase still intentionally avoids a standalone scaffold-specific database table.

## Verification Note

The project baseline stays on Java 17.
If the local machine only has JDK 17, you can still use `settings.xml` to isolate the Maven repository, but full baseline validation should be rerun on JDK 17.
For the current user module work, the local verification in this workspace was run with `-Djava.version=17` because only JDK 17 is available here.

