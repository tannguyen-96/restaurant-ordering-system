# Development

## Hiện trạng repo

```text
backend/     Spring Boot 4.1.1, Java 21, JPA, Postgres, /api/health
frontend/     T3/Next 13 + Prisma SQLite — sẽ thay bằng Vite ở Phase 1
run.txt       gợi ý chạy cũ (backend :8080, frontend :3000)
docs/         bộ spec này
```

Nhánh: `main`. Remote: `https://github.com/tannguyen-96/restaurant-ordering-system.git`

`run.txt` giữ đến khi Phase 1 có README mới.

## Yêu cầu máy (Phase 1 sẽ chốt script)

- JDK 21
- Node 22 LTS khuyến nghị (scaffold ghi node 24 — chốt một version trong Compose)
- Docker Desktop
- Git

Không cần cài Postgres/Redis host; dùng Compose.

## Mục tiêu lệnh sau Phase 1

```text
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui
- Health: GET http://localhost:8080/api/health → `ok`

Seed demo **chỉ** khi `APP_SEED=true` (dev/demo). Production cấm auto-seed.

## Git

```text
main          # protected: PR + 1 review + CI xanh. Không push thẳng.
feature/*     # một card / một rule
```

Không `develop` dài hạn cho đến khi có lý do thật.

```text
feat(session): create or join active session
feat(menu): item availability
feat(order): snapshot price on confirm
feat(payment): staff confirm cash
feat(realtime): order status events
docs: update contracts for checkout
```

Không commit `.env`, JWT secret, password DB. Chỉ `.env.example`.

## PR

Mỗi PR: mô tả rule, link card Trello (khi có), checklist DoD trong `team.md`, example request/response nếu đụng API.

Review: DRI module + 1 người nữa nếu DRI tự author thì Member 4 hoặc PO.

## CI (GitHub Actions, free)

```text
backend tests
frontend typecheck + lint + build
docker compose smoke (health + swagger hoặc GET /api/health)
```

Fail CI không merge.

## Env tối thiểu (sẽ ghi vào `.env.example` ở Phase 1)

```text
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
REDIS_URL
JWT_SECRET
ADMIN_BOOTSTRAP_USERNAME
ADMIN_BOOTSTRAP_PASSWORD
APP_SEED=false
TZ display: Asia/Ho_Chi_Minh (UTC+7)
```

Hiện `application.yml` đang user `tannguyen` / `1234` — chuyển hết sang env, không hard-code.

## Test local (sau khi có suite)

```text
backend:  ./mvnw test
frontend:  npm test && npm run typecheck && npm run lint && npm run build
```

Rule tiền/session/payment: viết test fail trước (TDD). CSS không bắt buộc TDD.

## Nguyên tắc implement

- YAGNI: đúng rule đã khóa, không làm sẵn platform
- Client không tin: giá, tổng, payment/order/session state, permission
- Invariant quan trọng nằm ở DB (một session active / bàn, QR unique, FK)
- STOMP = notify → FE invalidate → REST resync khi cần
- Feature xong = React → OpenAPI → Spring → Postgres → rule → realtime (nếu có) → React

## Việc không làm ở bước docs này

- Không xóa `frontend/` T3 cho đến PR Phase 1
- Không setup Trello
- Không tạo nhánh, không CI, không đổi pom cho đến khi team chốt `team.md` và bắt đầu Phase 1
