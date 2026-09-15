# Kiến trúc — QR Menu MVP

## Stack đã khóa

### Frontend — SPA, không chứa business truth

- Vite, React, TypeScript
- Tailwind + DaisyUI
- TanStack Query (server state)
- Context/local state (cart, session UI)
- STOMP client
- i18n: catalog `vi` đơn giản từ đầu. Không gắn framework i18n nặng.

Giao tiếp với Spring: REST + WebSocket/STOMP. Không Next.js, API route, server action, tRPC, Prisma, NextAuth.

### Backend — source of truth

- Java 21, Spring Boot 4.1.x (baseline `backend/pom.xml`)
- Spring Web, Security, WebSocket/STOMP
- PostgreSQL + Flyway
- Redis (TTL / cache / rate-limit — không phải truth)
- OpenAPI/Swagger

Không hạ Boot xuống 3.x trừ khi gặp incompatibility cụ thể, ghi rõ dependency nào gãy.

### Hạ tầng

```text
Docker Compose
├── frontend
├── backend
├── postgres
└── redis
```

CI: GitHub Actions free — backend test, frontend typecheck/lint/build, compose smoke.

## Sơ đồ

```text
                    ┌──────────────────────────┐
                    │     Vite + React SPA     │
                    │  /q/:qrToken  (khách)    │
                    │  /staff/*     (staff)    │
                    └────────────┬─────────────┘
                                 │ REST + STOMP
                    ┌────────────▼─────────────┐
                    │   Spring Boot monolith   │
                    │  auth restaurant table   │
                    │  qr session menu order   │
                    │  payment audit notify    │
                    └─────────┬───────┬────────┘
                              │       │
                       ┌──────▼───┐ ┌─▼──────┐
                       │PostgreSQL│ │ Redis  │
                       │  Truth   │ │ TTL    │
                       └──────────┘ └────────┘
```

Notify module = publisher STOMP, không phải notification center.

## Quy tắc truth

| Thứ | Nguồn |
|---|---|
| Order, payment, session, bàn, menu, quyền, tiền | PostgreSQL |
| TTL, cache, rate limit, coordination nhẹ | Redis, được phép |
| Cart từng lần sửa | Local trên máy khách |
| STOMP | Thông báo; disconnect → REST resync rồi resume |

Không microservice. Modular monolith, ranh giới domain rõ, tách service sau này được chứ MVP không cần.

## Backend modules

```text
backend/src/main/java/.../
├── auth/
├── restaurant/
├── menu/
├── table/
├── qr/
├── session/
├── order/
├── payment/
├── audit/
└── notification/
```

Mỗi module chỉ tách `api / application / domain / infrastructure` khi độ phức tạp đủ. Không đẻ 4 layer cho CRUD tầm thường.

- Controller: HTTP, DTO, ủy quyền application service
- Application: use case, transaction, authz
- Domain: lifecycle, invariant
- Repo/infra: Postgres, Redis, file ảnh, STOMP

Không trả JPA entity ra API.

## Frontend structure

Thay toàn bộ scaffold `frontend/` (T3) bằng:

```text
frontend/
├── src/
│   ├── app/router/  app/providers/
│   ├── features/customer/{qr,session,menu,cart,review,payment,orders}/
│   ├── features/staff/{auth,orders,tables,menu}/
│   ├── components/
│   ├── lib/{api,websocket,i18n}/
│   ├── hooks/  types/  main.tsx
├── public/
├── package.json
└── vite.config.ts
```

State: cart/session UI = local/Context. Server = TanStack Query. STOMP chỉ invalidate/update query. Không Redux.

JWT staff gắn header gọi Spring. Token khách = session-scoped, tách biệt JWT staff.

## Dữ liệu

Bảng chính:

```text
restaurants, restaurant_settings
users, restaurant_memberships
tables, qr_tokens
menu_categories, menu_items
dining_sessions, session_guests
orders, order_items, payments
audit_logs
```

Có thể thêm bảng draft cho PAY_WITH_ORDER nếu state machine cần.

### Flyway

Flyway là chủ schema. Không sửa migration đã apply. Mỗi đổi schema = 1 file, commit cùng PR. CI migrate từ DB sạch.

Không hard-delete lịch sử nghiệp vụ. Dùng `inactive` / `revoked` / `deleted_at`. Order, payment, session, audit giữ history.

### Redis — cấm

Không dùng Redis làm truth cho order, payment, session, tiền, permission.

### Tiền và giờ

- `NUMERIC` / `BigDecimal`. Cấm float.
- Timestamp timezone-aware trong DB. Display MVP: UTC+7.
- Clock abstraction cho timeout, để test deterministic.

## Ảnh

Abstraction `ImageStorage`. MVP local disk (sau này S3/R2/MinIO không đổi API).

```text
Upload → validate → max request 50 MB
  → resize max 1600×1600, giữ tỉ lệ
  → WebP ~quality 80
  → lưu file
```

DB chỉ metadata (path, mime, size), không Base64.

## Audit — append-only, hẹp

Chỉ hành động ảnh hưởng nghiệp vụ:

```text
MENU_ITEM_AVAILABILITY_CHANGED
ORDER_STATUS_CHANGED
PAYMENT_CONFIRMED
CHECKOUT_COMPLETED
SESSION_CLOSED_MANUALLY
```

```text
AuditLog: restaurant_id, actor_member_id, action,
          entity_type, entity_id, metadata, created_at
```

Không dùng audit làm analytics.

## Auth

Staff/Admin: username/email + password → JWT.

Khách: session token, không account.

Bootstrap Admin từ env, không seed demo trên production.

Permission-oriented ngay từ 2 role.

## Concurrency / idempotency

Bảo vệ: tạo session từ QR, confirm order, confirm payment, checkout.

Dùng transaction Postgres, unique constraint, idempotency key, validate server-side.

Không distributed lock, không queue, không event sourcing.

## Log

Structured. `requestId` / `correlationId`. Log: ORDER_CREATED, PAYMENT_FAILED/CONFIRMED, SESSION_CLOSED, WEBSOCKET_ERROR, IMAGE_PROCESSING_FAILED. Không gắn observability platform trong MVP.

## Migrate scaffold

Repo hiện có `backend/`, `frontend/` (T3), `run.txt`. Một commit. Coi như scaffold sạch.

**Xóa/thay:** toàn bộ `frontend/` T3 (Next, tRPC, Prisma, NextAuth, SQLite).

**Giữ/tiến hóa:** `backend/` Java 21 + Boot 4.1 + Maven. Thêm Security, STOMP, Flyway, Redis, OpenAPI, test. Không đẻ backend thứ hai.

**Thêm:** `docker-compose.yml`, `.env.example`, `.github/workflows/`, PR template, `backend/.../db/migration/`, `docs/` (bộ này).

Sau Phase 1, README/CI/env không còn chữ Next/T3/tRPC/Prisma/SQLite.

Không xóa file trên git cho đến khi Phase 1 có PR thay thế (ba confirm trước khi xóa).

## Decision log

| Quyết định | Lý do |
|---|---|
| Bỏ T3/tRPC | Scaffold example; tRPC không realtime; 2 backend = rủi ro team đêm |
| Vite SPA, không Next | App QR + staff, không cần SEO |
| STOMP, không poll làm truth | Codex; disconnect thì REST resync |
| PAY_AT_END trước | Đơn giản hơn PAY_WITH_ORDER |
| i18n = file `vi` | Cần tiếng Việt; không gắn i18next trừ khi đau thật |
