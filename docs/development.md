# Development — bám `main`

## Hiện trạng repo (`c9b4506`)

```text
backend/     Spring Boot 4.1.1, Java 21, Gradle, JPA, Liquibase, /api/v1/orders, Dockerfile
frontend/     CRA React 19, JS, App.js hello-world (chưa gọi API)
run.txt       gợi ý chạy — một số lệnh lệch code (xem dưới)
cloud.txt     ghi chú deploy sau (Vercel/Render/Neon/Auth0) — không dùng Phase 1
docs/         bộ spec này (viết trên working tree `main`)
```

**Không có:** `docker-compose.yml`, root `.gitignore`, `.env.example`, `.github/workflows/`, Redis, Security.

Nhánh: `main`. Remote: `https://github.com/tannguyen-96/restaurant-ordering-system.git`  
Nhánh cũ `docs/discuss` = spec **lệch stack** (Vite/Flyway). Đừng merge nguyên nhánh đó vào `main`.

## Yêu cầu máy

- JDK 21
- Node 22 LTS khuyến nghị (`run.txt` ghi node 24 — chốt một version trong Compose)
- Docker (cho Phase 1 Compose)
- PostgreSQL local **hoặc** Compose
- Git

## Chạy local — as-built (hôm nay)

Postgres phải up trước. Default `application.yaml`:

```text
url:      jdbc:postgresql://localhost:5432/mydb          # hoặc POSTGRES_URL
user:     tannguyen                                       # POSTGRES_USER
password: 1234                                            # POSTGRES_PASSWORD
```

Profile `local` (`application-local.yml`):

```text
url:      jdbc:postgresql://localhost:5432/restaurant-ordering-system
user:     postgres
password: admin
```

```text
# Backend
cd backend
./gradlew bootRun
# hoặc
./gradlew bootRun --args='--spring.profiles.active=local'

Swagger:  http://localhost:8080/swagger-ui.html
API:      http://localhost:8080/api/v1/orders

# Frontend
cd frontend
npm install
npm start
# http://localhost:3000
# KHÔNG dùng `npm run dev` — script đó không có trong package.json
```

Dockerfile backend (chưa gắn Compose):

```text
cd backend
docker build -t qr-backend .
docker run --rm -p 8080:8080 --env POSTGRES_URL=... qr-backend
```

JVM container: `-Xmx300m`.

## Mục tiêu lệnh sau Phase 1

```text
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Smoke: `POST /api/v1/orders` tạo được 1 order

Seed demo chỉ khi `APP_SEED=true`. Production cấm auto-seed. Chưa có seed trên `main`.

## Git

```text
main          # PR + 1 review + CI (khi có). Không push thẳng nếu team chốt protect.
feature/*     # một card / một rule
```

Không `develop` dài hạn.

```text
feat(order): snapshot price from menu
feat(session): create or join active session
feat(compose): postgres + backend + frontend
docs: rewrite spec to match main
```

Không commit `.env`, password DB, JWT. Root `.gitignore` **cần tạo** Phase 1 — hiện thiếu; đừng add `.graphify/`, `.repomix/`, `frontend/node_modules`, `backend/build`.

## PR

Mô tả rule, checklist DoD `team.md`, example request/response nếu đụng API.

Review: DRI module. DRI tự author thì Member 4 hoặc PO review.

## CI (chưa có — Phase 1)

```text
backend:  ./gradlew test
frontend: npm test -- --watchAll=false && npm run build
compose smoke: GET swagger hoặc POST /api/v1/orders
```

Fail CI không merge.

## Env tối thiểu (sẽ vào `.env.example`)

```text
POSTGRES_URL
POSTGRES_USER
POSTGRES_PASSWORD
POSTGRES_DB
JWT_SECRET                 # khi có auth
ADMIN_BOOTSTRAP_USERNAME
ADMIN_BOOTSTRAP_PASSWORD
APP_SEED=false
```

Hardcode `tannguyen`/`1234` trong `application.yaml` phải ra env.

## Test local

```text
cd backend && ./gradlew test
cd frontend && npm test -- --watchAll=false
```

Rule tiền/session/payment: test fail trước (TDD). CSS không bắt buộc TDD.

## Lệch `run.txt` vs code

| `run.txt` | Thực tế |
|---|---|
| `npm run dev (node24)` | `npm start`; không có script `dev` |
| daisyui theme link | DaisyUI chưa trong `package.json` |
| `./gradlew bootRun` | Đúng |

Sửa `run.txt` trong PR Phase 1.

## Nguyên tắc implement

- YAGNI trên base `main`
- Evolve `Order` bằng Liquibase mới, không tạo bảng song song
- Client không tin: giá, tổng, session, permission (sửa `price` ở Phase 4)
- Invariant ở DB
- Feature xong = CRA → OpenAPI → Spring → Postgres → rule → (STOMP nếu phase ≥6) → CRA
