# Architecture

## System
A Spring Boot REST service organized by controller, service, repository, DTO, model, exception, and configuration packages under `com.example.backend`.

## Components
- Controllers expose `/api/v1/branches`, `/api/v1/tables`, and `/api/v1/orders`.
- Services contain business operations.
- Repositories provide Spring Data JPA persistence.
- DTOs define request and response contracts.
- `GlobalExceptionHandler` centralizes API error handling.
- Liquibase manages PostgreSQL schema changes.

## Data Flow
HTTP request -> controller validation -> service business logic -> repository/JPA -> PostgreSQL. Service results are mapped to response DTOs and returned by controllers.

## External Integrations
PostgreSQL is the runtime database. OpenAPI documentation is exposed through springdoc. Docker supports packaging and deployment.

## Infrastructure
Gradle builds the Java 21 application. Spring profiles provide environment-specific configuration. Database credentials and other secrets must come from external configuration.

## Design Overview
The application keeps transport concerns in controllers and DTOs, business rules in focused services, persistence in Spring Data repositories, and schema evolution in Liquibase. Spring Security protects non-public routes with stateless Bearer JWT authentication.
