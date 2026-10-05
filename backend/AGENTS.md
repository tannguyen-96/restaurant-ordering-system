# Project Memory

## Project Overview
Restaurant ordering backend API for managing branches, restaurant tables, and orders.

## Business Domain
The service exposes REST endpoints for branch management, table management, and order creation and retrieval.

## Tech Stack
- Java 21
- Spring Boot 4.1.1
- Gradle
- Spring MVC, Spring Data JPA, Bean Validation
- PostgreSQL
- Liquibase
- OpenAPI via springdoc
- JUnit 5, Mockito-capable Spring test starters, and Testcontainers

## Repository Structure
- `src/main/java/com/example/backend/`: application code
- `src/main/resources/`: configuration, Liquibase changelogs, and web resources
- `src/test/java/`: tests mirroring the application package structure
- `.agents/skills/`: project guidance for Java, Spring Boot, and Javadoc
- `.github/`: Copilot instructions and reusable prompts

## Coding Conventions
Use constructor injection, final dependencies, DTOs at API boundaries, Bean Validation, domain-specific unchecked exceptions, and focused services. Prefer clear immutable designs, typed generics, short methods, and JUnit 5 tests with deterministic assertions. Public Java types and members should use concise Javadoc where appropriate.

## Build Commands
- `./gradlew build`
- Windows: `gradlew.bat build`
- Run tests: `gradlew.bat test`
- Run locally: `gradlew.bat local`

## Common Commands
- Focused test: `gradlew.bat test --tests com.example.backend.service.AuthServiceTest`
- List Gradle tasks: `gradlew.bat tasks`
- Build the runnable jar: `gradlew.bat bootJar`

## Repository Conventions
- Keep API contracts in request/response DTOs and map them in the service or controller layer.
- Keep schema changes in ordered Liquibase files under `src/main/resources/db/changelog/changes`.
- Keep tests under the package structure they cover and prefer focused deterministic assertions.

## Deployment Workflow
Build the application with Gradle and package it using the repository Dockerfile. Supply database and environment configuration externally; never commit secrets.

## Testing Workflow
Run the focused test during development, then `gradlew.bat test` and `gradlew.bat build` before handoff. Use `@WebMvcTest` for controller behavior, unit tests for services, `@DataJpaTest` for repositories, and Testcontainers for PostgreSQL integration coverage.

## Important Constraints
- Do not expose JPA entities directly from controllers.
- Keep database changes in Liquibase changelogs.
- Preserve API response and validation contracts.
- Do not store temporary work in permanent memory files.

## Definition of Done
The change is implemented in the owning layer, covered by appropriate tests, validated with Gradle, documented when behavior or setup changes, and reflected in `SESSION.md` when meaningful progress is made.
