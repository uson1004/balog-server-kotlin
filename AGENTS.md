# Repository Guidelines

## Project Structure & Module Organization
This is a Gradle-based Spring Boot 3 backend. Application code lives under `src/main/java/org/example/bankramenserver` and is grouped by domain feature such as `auth`, `user`, `category`, `report`, and `transaction`. Follow the layered package style already in use: `domain`, `presentation`, `presentation/dto`, `service`, `facade`, and `domain/repository` where persistence is needed. Keep shared infrastructure in `global`, including `config`, `error`, `jwt`, and Swagger document interfaces in `global/document`. Tests belong under `src/test/java/org/example/bankramenserver`.

## Build, Test, and Development Commands
Use the Gradle wrapper through Bash.

- `bash ./gradlew test` — run the JUnit 5 test suite.
- `bash ./gradlew build` — compile, test, and package the app.
- `bash ./gradlew bootRun` — start the service locally.
- `bash ./gradlew clean` — remove Gradle build outputs.

The project uses Java toolchain **17**. Export values required by `application.yaml`, such as MySQL, Redis, and JWT settings, before local runtime testing.

## Coding Style & Naming Conventions
Use standard Java/Spring conventions with 4-space indentation. Class names use `PascalCase`; methods and fields use `camelCase`; constants use `UPPER_SNAKE_CASE`. Prefer constructor injection with Lombok `@RequiredArgsConstructor`. Keep controllers thin and delegate business logic to services. Use record DTOs for immutable request/response shapes and builders when construction readability improves.

## API, Security & Error Handling
Protected APIs should identify the current user from the JWT security context through `UserFacade`; do not accept `userId` path parameters for user-owned report or transaction data. Keep auth rules explicit in `SecurityConfig`. Define Swagger/OpenAPI annotations in `global/document` interfaces and have controllers implement them. Use `GlobalException` plus `ErrorCode`; avoid hard-coded `IllegalArgumentException` for domain/API failures.

## Persistence & Querying
Use Spring Data JPA repositories and Querydsl custom repositories for complex reads. Name custom contracts like `TransactionRepositoryCustom` and implement with `TransactionRepositoryImpl`. Keep query projection records close to the repository package.

## Testing Guidelines
Use `spring-boot-starter-test`, Mockito, and `MockMvcBuilders.standaloneSetup()` for focused controller tests. Name test classes `*Test`. Verify response JSON contracts, status codes, and service invocation parameters. Add integration tests only when framework wiring or persistence must be proven.

## Commit & Pull Request Guidelines
Follow the existing `type(#issue): summary` commit style, e.g. `feat(#9): 리포트 거래 API 인증 명시`. Keep commits focused and include rationale plus verification details in PRs. For API or security changes, document changed endpoints, auth requirements, example requests/responses, and required environment variables.
