# Current Session

## Current Goal
Maintain the repository memory system and keep the backend service tested.

## Completed Work
- Added and maintained root memory files plus reusable Copilot prompts.
- Completed branch, table, order, product, auth, user, role, and policy API/service coverage.
- Added refresh-token rotation, JWT request authentication, OpenAPI auth documentation, and Liquibase migrations through role-policy assignments.
- Refreshed memory documentation and removed the obsolete controller-test limitation.
- Updated `AuthControllerTest` to verify the refresh token is returned as an HTTP-only cookie rather than response JSON.
- Verified `gradlew.bat test` successfully.
- Fixed `AuthService`/`User`/`LoginResponse`: added the missing `User.roles` many-to-many relationship (via the existing `user_role` join table, mirroring `Role.policies`) and replaced the broken single-`Role` usage with a `List<String> roleNames` on `LoginResponse`.
- Added focused `AuthService.authenticate` unit coverage for missing credentials, an absent user, an invalid password, and successful authentication; verified with `gradlew.bat test --tests com.example.backend.service.AuthServiceTest`.

## Current Status
The service uses focused controller and service layers, DTO API boundaries, Liquibase migrations, and stateless Bearer JWT security. JWT signing uses the external `JWT_SECRET_KEY` configuration.

## Known Blockers
`BackendApplicationTests` remains disabled. Database-backed integration tests require PostgreSQL configuration or Docker/Testcontainers.

## Next Tasks
Add integration coverage where database behavior matters. Keep this file under 250 words and update it when meaningful progress is made.
