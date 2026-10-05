# Runbook

## Local Setup
Install JDK 21 and use the Gradle wrapper. Configure a PostgreSQL database and copy or adapt `src/main/resources/application-local.yml.example` to local configuration without committing secrets.

## Build
- Windows build: `gradlew.bat build`
- Clean build: `gradlew.bat clean build`
- Run application with local profile: `gradlew.bat local`

## Test
- All tests: `gradlew.bat test`
- Focused test class: `gradlew.bat test --tests com.example.backend.BackendApplicationTests`
- Test report: `build/reports/tests/test/index.html`

## Deploy
Build the application and use the repository `Dockerfile`. Provide database settings through environment-specific configuration.

## Debug
Run the `local` Gradle task from the IDE or attach a debugger to the Spring Boot process. Check application logs and `build/reports` for Gradle results.

## Useful Commands
- `gradlew.bat tasks`
- `gradlew.bat dependencies`
- `gradlew.bat bootJar`
