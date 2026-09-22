# Hợp đồng FE/BE

OpenAPI trên Spring (`/swagger-ui.html`) là hợp đồng. Tách rõ **đã ship trên `main`** và **sẽ thêm**.

Frontend logic dùng `code` (khi có error envelope), không nhánh if theo `message`.

---

## Đã ship — `/api/v1/orders`

Base: `http://localhost:8080`  
Tag Swagger: `Order Controller`

### `POST /api/v1/orders` → 201

Tạo order `PENDING`. **As-built tin giá client** — lệch rule sản phẩm. Giữ chữ ký đến khi có Menu; lúc đó bỏ `price` khỏi request, server snapshot từ `menu_items`.

Request:

```json
{
  "items": [
    { "productId": "PROD-001", "quantity": 2, "price": 150.00 }
  ]
}
```

Validation:

- `items` `@NotEmpty`
- `productId` `@NotBlank`
- `quantity` `@Min(1)`
- `price` `@NotNull`

Response `OrderResponse`:

```json
{
  "id": 1,
  "orderNumber": "uuid",
  "status": "PENDING",
  "totalAmount": 300.00,
  "createdAt": "2026-09-22T15:00:00"
}
```

`totalAmount` = Σ `price * quantity` trong `OrderServiceImpl`.  
`orderNumber` = `UUID.randomUUID()`.  
Response **không** trả danh sách item.

### `GET /api/v1/orders/{id}` → 200 | 404

404: `RuntimeException` → body string `"Order not found with ID: {id}"` (chưa envelope `code`).

### `GET /api/v1/orders` → 200

`List<OrderResponse>`.

### Lỗi as-built

Validation (`@Valid`): **400** body `Map<field, message>` — không có `code` / `requestId`.

```json
{ "items": "Đơn hàng phải có ít nhất 1 sản phẩm" }
```

`RuntimeException`: **404** body **string**, không JSON.

Khi thêm API public/staff: chuyển dần sang envelope dưới đây. Không phá 400 map hiện tại trong cùng PR trừ khi DRI Order chủ động.

---

## Sẽ thêm — public / khách

Chưa implement. Path có thể chỉnh; ranh giới không đổi.

```text
GET    /api/public/qr/{qrToken}
POST   /api/public/sessions/join
GET    /api/public/menu
POST   /api/public/order-previews
POST   /api/public/orders              # PAY_AT_END — khác /api/v1/orders hiện tại
POST   /api/public/checkout-requests
POST   /api/public/payment-drafts      # PAY_WITH_ORDER
POST   /api/public/payments
```

Khi làm `POST /api/public/orders`: **không** nhận `price`. Snapshot `unit_price_snapshot` từ menu. Có thể deprecate `/api/v1/orders` hoặc để nội bộ/dev.

### Join (đích)

Request: `{ "qrToken": "..." }`  
Response gồm restaurant, table, session, sessionGuest, sessionToken.

`GET .../qr/{qrToken}` chỉ resolve. `POST .../sessions/join` mới join/tạo (idempotent theo table + device).

### Preview (đích)

Client gửi `menuItemId` + `quantity` + `note`. Server trả `unitPrice` / `lineTotal` / `total`. UI hiện số này trước confirm.

---

## Sẽ thêm — staff / admin

```text
POST   /api/staff/auth/login

GET    /api/staff/orders
PATCH  /api/staff/orders/{orderId}/status

GET    /api/staff/tables
POST   /api/staff/tables
PATCH  /api/staff/tables/{tableId}
POST   /api/staff/tables/{tableId}/qr/regenerate
POST   /api/staff/tables/{tableId}/release

GET/POST/PATCH /api/staff/menu/categories
GET/POST/PATCH /api/staff/menu/items
POST   /api/staff/menu/items/{itemId}/availability
POST   /api/staff/menu/items/{itemId}/image

POST   /api/staff/payments/{paymentId}/confirm
POST   /api/staff/sessions/{sessionId}/force-close
```

Login → JWT. Confirm cash `{ "method": "CASH" }`. Mọi chuyển trạng thái liên quan (order materialize, session close, table) **một transaction**.

---

## Envelope lỗi (đích, API mới)

```json
{
  "code": "MENU_ITEM_UNAVAILABLE",
  "message": "Món đã hết",
  "details": {},
  "requestId": "req-123"
}
```

Codes: `SESSION_EXPIRED`, `SESSION_LOCKED`, `MENU_ITEM_UNAVAILABLE`, `ORDER_VALIDATION_FAILED`, `PAYMENT_FAILED`, `PAYMENT_ALREADY_PROCESSED`, `FORBIDDEN`, `NOT_FOUND`.

---

## STOMP (chưa có — phase realtime)

Không emit event cho sửa giỏ.

Topic đề xuất:

```text
/topic/sessions/{sessionId}
/topic/restaurants/{restaurantId}/orders
```

Event: `ORDER_CREATED`, `ORDER_STATUS_CHANGED`, `CHECKOUT_REQUESTED`, `PAYMENT_UPDATED`, `SESSION_UPDATED`.

Disconnect: WS drop → reconnect → REST resync → resume STOMP.

---

## Idempotency (chưa có)

Header `Idempotency-Key` cho join, preview-confirm, create order, create/confirm payment, checkout.

`POST /api/v1/orders` hiện **không** idempotent.

---

## Mapping as-built → đích Order

| As-built | Đích |
|---|---|
| `productId` (string tự do) | `menuItemId` FK menu |
| `price` từ client | `unit_price_snapshot` từ server |
| không `note` | `note` optional |
| `status` = `PENDING` string | enum kitchen `PENDING→PREPARING→READY→SERVED` |
| không `sessionId` | bắt buộc thuộc DiningSession |
| response không có items | response có line items khi FE cần |

DRI Order quyết định: evolve entity `Order` hiện có (thêm cột Liquibase `002-`) chứ không tạo bảng song song.