# Known Issues

- The existing `BackendApplicationTests` is disabled, so the Spring application context is not currently validated by the default test suite.
- Database-backed tests require PostgreSQL configuration or a working Docker/Testcontainers environment.
- `HELP.md` contains generated Spring guidance and mentions Maven even though this repository uses Gradle; use `build.gradle` and `RUNBOOK.md` as authoritative.
