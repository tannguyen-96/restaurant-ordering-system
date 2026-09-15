# QR Menu MVP — Tài liệu

Bộ tài liệu này là nguồn sự thật cho team 4 người. Codex plan gốc đã được chốt stack và gộp lại cho đúng việc dùng.

Repo: `https://github.com/tannguyen-96/restaurant-ordering-system`  
Workspace: `G:\restaurant-ordering-system`  
Trello: setup sau, khi team chốt `docs/team.md`

## Đọc theo thứ tự

| File | Dùng khi |
|---|---|
| [product.md](product.md) | Hiểu sản phẩm, luồng, DoD, những gì **không** làm |
| [architecture.md](architecture.md) | Stack, module, DB, Redis, auth, ảnh, audit, migrate scaffold |
| [contracts.md](contracts.md) | REST + STOMP + error code — FE/BE phải bám |
| [team.md](team.md) | Ai làm gì, phase, lễ nghi ngoài giờ, DoD từng card |
| [development.md](development.md) | Chạy local, Docker, Git, CI, env |

## Quyết định đã khóa

- Bỏ T3 / Next.js / tRPC / Prisma / SQLite / NextAuth
- Backend thật: Spring Boot modular monolith
- Frontend: Vite + React + TypeScript SPA
- REST = thao tác nghiệp vụ; STOMP = thông báo; PostgreSQL = source of truth
- PAY_AT_END trước, PAY_WITH_ORDER sau
- Mục tiêu: E2E chạy trên Docker Compose, không phải platform

Không mở lại các quyết định trên trừ khi phát hiện mâu thuẫn cụ thể trong code.

## Trạng thái hiện tại

- Repo: 1 commit scaffold (`backend/` Spring Boot 4.1 + `frontend/` T3). Chưa có product code.
- Phase: **chưa bắt đầu Phase 1**
- Nhánh: `main` — chưa có nhánh feature
- Vai trò 4 người: chưa gán tên; xem đề xuất trong `team.md`
