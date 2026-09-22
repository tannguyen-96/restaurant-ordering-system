# Kiến trúc — bám `main`

## Stack đã khóa (as-built)

### Backend — source of truth

- Java 21, Spring Boot **4.1.1** (`backend/build.gradle`)
- Gradle Wrapper **9.7.1**
- Spring WebMVC, Validation, Data JPA
- PostgreSQL + **Liquibase** (`classpath:db/changelog/db.changelog-master.yaml`)
- springdoc OpenAPI (`/swagger-ui.html`)
- Lombok
- Dockerfile multi-stage: `eclipse-temurin:21-jdk-alpine` → `21-jre-alpine`, `-Xmx300m`, port 8080

**Không có trên `main`:** Spring Security, JWT, Redis, WebSocket/STOMP, Flyway, Maven/`pom.xml`.

Hibernate `ddl-auto: validate` — schema chỉ do Liquibase.

### Frontend — UI, không chứa business truth

- CRA (`react-scripts` 5.0.1), React **19.3**, `frontend/src/App.js` (JS, không TS)
- Scripts: `start` / `build` / `test` / `eject` — **không** có `dev` (dù `run.txt` ghi `npm run dev`)
- Chưa: React Router, Tailwind, DaisyUI, TanStack Query, STOMP client, gọi API Spring

DaisyUI trong `run.txt` = ý định UI. Thêm bằng PR, không giả đã có.

### Hạ tầng hiện tại vs đích gần

```text
Hiện tại (máy dev):
  Postgres local  :5432
  ./gradlew bootRun          → :8080
  npm start (CRA)            → :3000
  backend/Dockerfile         → image Spring (chưa compose)

Đích Phase 1 (thêm, không thay stack):
  Docker Compose
  ├── frontend   (CRA build/dev)
  ├── backend    (Dockerfile đã có)
  └── postgres
  Redis + STOMP thêm khi tới phase realtime — không nhét Phase 1.
```

## Sơ đồ as-built

```text
                    ┌──────────────────────────┐
                    │   CRA App.js (hello)     │
                    │   chưa gọi API           │
                    └──────────────────────────┘

                    ┌──────────────────────────┐
                    │   Spring Boot :8080      │
                    │   OrderController        │
                    │   OrderServiceImpl       │
                    │   OrderRepository        │
                    │   Liquibase → schema     │
                    └────────────┬─────────────┘
                                 │
                          ┌──────▼──────┐
                          │ PostgreSQL  │
                          │ schema order│
                          │ orders      │
                          │ order_items │
                          └─────────────┘
```

Đích sau này (cùng monolith, thêm package):

```text
Vite không. Vẫn CRA SPA + router:
  /q/:qrToken   khách
  /staff/*      staff
        │ REST (+ STOMP khi có)
        ▼
Spring packages:
  auth, restaurant, menu, table, qr, session, order, payment, audit, notification
```

## Module backend hiện có

```text
backend/src/main/java/com/example/backend/
├── BackendApplication.java
├── config/OpenApiConfig.java
├── controller/OrderController.java     /api/v1/orders
├── dto/request/{OrderRequest,OrderItemRequest}
├── dto/response/OrderResponse
├── exception/GlobalExceptionHandler.java
├── model/{Order,OrderItem}
├── repository/OrderRepository.java     JpaRepository<Order, Long>
└── service/OrderService.java
    └── impl/OrderServiceImpl.java
```

Thêm module mới **cùng package tree**, không đẻ service thứ hai.

- Controller: HTTP + DTO, ủy quyền service
- Service: use case + `@Transactional`
- Entity: không trả ra API (đã map `OrderResponse`)
- Repo: Spring Data

## Frontend structure (đích, từ CRA hiện có)

Không xóa CRA. Thêm dần:

```text
frontend/src/
├── App.js              # hiện hello — sẽ thành router shell
├── index.js
├── features/customer/  # qr, session, menu, cart, review, payment
├── features/staff/     # login, orders, tables, menu
└── lib/api.js          # fetch Spring
```

TypeScript: optional sau khi router + API client ổn. Không chặn Phase 1.

## Dữ liệu — Liquibase (đã apply trên `main`)

Master: `db/changelog/db.changelog-master.yaml`  
Changeset: `changes/001-create-schema-and-tables.yaml`

```text
schema "order"
  orders:       id BIGSERIAL PK, order_number VARCHAR UNIQUE, status VARCHAR,
                total_amount DECIMAL(19,2), created_at TIMESTAMP
  order_items:  id BIGSERIAL PK, product_id VARCHAR, quantity INT,
                price DECIMAL(19,2), order_id BIGINT FK → orders.id
```

Quy tắc: không sửa changeset đã apply. Đổi schema = file mới `002-...yaml`, commit cùng PR.

Không hard-delete lịch sử nghiệp vụ. Order/payment/session/audit giữ history.

### Entity vs Liquibase — rủi ro

`Order` / `OrderItem` dùng `@Table(..., schema = "\"order\"")` (quote trong tên schema). Liquibase tạo schema `"order"`. Nếu JPA/Postgres lệch identifier, boot fail lúc validate — kiểm tra khi chạy `bootRun`. Sửa bằng PR, không đổi tool migration.

### Tiền và giờ

- `NUMERIC` / `BigDecimal`. Cấm float. As-built đã dùng `BigDecimal`.
- `created_at` hiện `LocalDateTime` (không timezone). Đích: timestamptz + display UTC+7.
- Clock abstraction khi làm timeout session.

### Redis

Chưa có. Khi thêm: TTL / cache / rate-limit thôi. **Cấm** làm truth cho order, payment, session, tiền, permission.

## Ảnh

Chưa có. Đích: abstraction `ImageStorage`, MVP local disk. DB metadata, không Base64.

## Audit

Chưa có. Đích append-only, hẹp:

```text
MENU_ITEM_AVAILABILITY_CHANGED
ORDER_STATUS_CHANGED
PAYMENT_CONFIRMED
CHECKOUT_COMPLETED
SESSION_CLOSED_MANUALLY
```

## Auth

Chưa có. Đích: Staff/Admin username + password → JWT. Khách: session token. Bootstrap admin từ env.

`cloud.txt` ghi Auth0 — **không** dùng cho MVP local.

## Concurrency / idempotency

Chưa có idempotency key. `createOrder` luôn insert mới.

Bảo vệ khi thêm QR/session/payment: transaction Postgres, unique constraint (một session active / bàn), idempotency key.

Không distributed lock, không queue, không event sourcing.

## Log / test

As-built: `BackendApplicationTests.contextLoads()` thôi. Rule tiền/session/payment: test fail trước rồi code.

## Decision log

| Quyết định | Lý do |
|---|---|
| Base = `main` | Team đã có Order + Liquibase + Gradle + CRA; scaffold lại tốn công |
| Giữ Liquibase | Đã có changeset; không migrate sang Flyway |
| Giữ CRA JS | Đã thay Next; Vite/TS là rewrite FE lần 2 — không làm trừ blocker |
| STOMP/Redis sau | Chưa có trên `main`; Compose + domain trước realtime |
| Client không gửi giá | Rule sản phẩm; code hiện nhận `price` → sửa khi có Menu |