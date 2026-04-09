# Community MVP Backend

## Overview

This directory contains the Spring Boot backend scaffold for the Community MVP project.

## Environment

- Java 21
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

Suggested variables:

- `COMMUNITY_MVP_DB_URL`
- `COMMUNITY_MVP_DB_USERNAME`
- `COMMUNITY_MVP_DB_PASSWORD`
- `COMMUNITY_MVP_REDIS_HOST`
- `COMMUNITY_MVP_REDIS_PORT`
- `COMMUNITY_MVP_JWT_SECRET`

## Structure

- `src/main/java/.../interfaces`: REST controllers, DTOs, HTTP exception mapping
- `src/main/java/.../application`: application services and use case orchestration
- `src/main/java/.../domain`: domain-facing models and contracts
- `src/main/java/.../infrastructure`: framework and external-system adapters
- `src/main/java/.../common`: shared response and error objects
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

The scaffold intentionally does not include business modules yet.

## Verification Note

The project baseline stays on Java 21.
If the local machine only has JDK 17, you can still use `settings.xml` to isolate the Maven repository, but full baseline validation should be rerun on JDK 21.
