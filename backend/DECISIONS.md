# Architecture Decisions

## 2026-10-03

Create a repository-first project memory system using root Markdown files and `.github` Copilot prompts.

Reason
Future agent sessions need durable project context that is independent of chat history.

Impact
Agents must read `AGENTS.md`, `ARCHITECTURE.md`, `DECISIONS.md`, and `SESSION.md` before working. Current-session details belong in `SESSION.md`; architectural choices belong here.

## 2026-10-04

`User` gets roles through a `Set<Role> roles` many-to-many field mapped to the existing `user_role` join table (same pattern as `Role.policies` over `role_policy`), not a singular `Role`.

Reason
The Liquibase schema already defines `user_role` as a many-to-many join table, but the `User` entity had no relationship field and `AuthService` called a nonexistent `user.getRole()`. A user can hold multiple roles per the schema.

Impact
`LoginResponse` exposes `List<String> roleNames` instead of a single role. `AuthService` maps `user.getRoles()` to role names for both login and refresh-token responses.
