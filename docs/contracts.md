# Hợp đồng FE/BE — REST + STOMP

OpenAPI trên Spring là hợp đồng. Màn hình user-facing không ship nếu thiếu endpoint + example trong file này / Swagger.

Tên path có thể chỉnh khi design chi tiết, **ranh giới trách nhiệm không đổi**.

DRI module viết contract của module mình. Member 4 review/merge, không viết hộ toàn bộ.

Frontend logic dùng `code`, không dùng `message`.

## REST — public / khách

```text
GET    /api/public/qr/{qrToken}
POST   /api/public/sessions/join
GET    /api/public/menu
POST   /api/public/order-previews
POST   /api/public/orders              # PAY_AT_END: tạo order ngay
POST   /api/public/checkout-requests   # PAY_AT_END: khóa session, chờ staff
POST   /api/public/payment-drafts      # PAY_WITH_ORDER: nộp giỏ để vào bill
POST   /api/public/payments            # khởi tạo payment (CASH → PENDING)
```

### QR join

Request:

```json
{ "qrToken": "7f83a1..." }
```

Response:

```json
{
  "restaurant": { "id": "restaurant-01", "name": "Demo Restaurant", "currency": "VND" },
  "table": { "id": "table-05", "name": "05" },
  "session": { "id": "session-101", "status": "ACTIVE" },
  "sessionGuest": { "id": "guest-abc" },
  "sessionToken": "signed-token"
}
```

`GET /api/public/qr/{qrToken}` chỉ resolve quán/bàn/session hiện có. `POST .../sessions/join` mới join hoặc tạo (idempotent theo table + device).

### Order preview

Request:

```json
{
  "sessionGuestId": "guest-abc",
  "items": [
    { "menuItemId": "coffee-01", "quantity": 2, "note": "Ít đá" }
  ]
}
```

Response:

```json
{
  "sessionId": "session-101",
  "items": [
    {
      "menuItemId": "coffee-01",
      "name": "Cà phê sữa",
      "quantity": 2,
      "unitPrice": 35000,
      "lineTotal": 70000,
      "note": "Ít đá"
    }
  ],
  "subtotal": 70000,
  "total": 70000,
  "currency": "VND",
  "expiresAt": "2026-09-15T16:00:00Z"
}
```

UI phải hiện số này trước confirm/pay. Client không gửi `unitPrice`.

### PAY_AT_END — tạo order

Sau preview: `POST /api/public/orders` (kèm idempotency key). Server snapshot giá, tạo Order `PENDING`.

Checkout: `POST /api/public/checkout-requests` `{ "sessionId": "session-101" }` → session `CHECKOUT_REQUESTED`, tạo Payment PENDING = tổng order đã confirm. Không nhận order mới.

### PAY_WITH_ORDER — draft rồi trả

Giỏ chưa submit không vào bill.

```text
preview → POST payment-drafts → lock session
  → tính tổng draft đã nhận → POST payments (CASH = PENDING)
  → staff confirm → materialize Order → session CLOSED
```

## REST — staff / admin

```text
POST   /api/staff/auth/login

GET    /api/staff/orders
PATCH  /api/staff/orders/{orderId}/status

GET    /api/staff/tables
POST   /api/staff/tables
PATCH  /api/staff/tables/{tableId}
POST   /api/staff/tables/{tableId}/qr/regenerate
POST   /api/staff/tables/{tableId}/release

GET    /api/staff/menu/categories
POST   /api/staff/menu/categories
PATCH  /api/staff/menu/categories/{categoryId}
GET    /api/staff/menu/items
POST   /api/staff/menu/items
PATCH  /api/staff/menu/items/{itemId}
POST   /api/staff/menu/items/{itemId}/availability
POST   /api/staff/menu/items/{itemId}/image

POST   /api/staff/payments/{paymentId}/confirm
POST   /api/staff/sessions/{sessionId}/force-close
```

Login: username/email + password → JWT. Không body `paymentId` trùng path.

Staff confirm cash:

```json
{ "method": "CASH" }
```

Response:

```json
{
  "paymentId": "payment-88",
  "status": "SUCCESS",
  "amount": 120000,
  "currency": "VND"
}
```

Mọi chuyển trạng thái liên quan (order materialize, session close, table) nằm trong **một transaction**.

Khởi tạo payment (public hoặc sau checkout):

```json
{
  "sessionId": "session-101",
  "paymentMode": "PAY_AT_END",
  "method": "CASH"
}
```

```json
{
  "paymentId": "payment-88",
  "status": "PENDING",
  "amount": 120000,
  "currency": "VND"
}
```

## Lỗi thống nhất

```json
{
  "code": "MENU_ITEM_UNAVAILABLE",
  "message": "Món đã hết",
  "details": {},
  "requestId": "req-123"
}
```

Codes:

```text
SESSION_EXPIRED
SESSION_LOCKED
MENU_ITEM_UNAVAILABLE
ORDER_VALIDATION_FAILED
PAYMENT_FAILED
PAYMENT_ALREADY_PROCESSED
FORBIDDEN
NOT_FOUND
```

Có thể thêm code; không dùng `message` làm nhánh if.

## STOMP

Thông báo, không phải truth. Không emit event cho từng lần sửa giỏ.

Đề xuất topic (chốt cùng OpenAPI trước khi code FE realtime):

```text
/topic/sessions/{sessionId}
/topic/restaurants/{restaurantId}/orders
```

Event bắt buộc:

```text
ORDER_CREATED
ORDER_STATUS_CHANGED
CHECKOUT_REQUESTED
PAYMENT_UPDATED
SESSION_UPDATED
```

```json
{
  "eventId": "evt-1001",
  "eventType": "ORDER_STATUS_CHANGED",
  "occurredAt": "2026-09-15T15:30:00Z",
  "sessionId": "session-101",
  "orderId": "order-501",
  "payload": { "status": "PREPARING" }
}
```

Disconnect:

```text
WS drop → auto reconnect → REST resync → resume STOMP
```

## Idempotency

Header đề xuất: `Idempotency-Key` cho join, preview-confirm, create order, create payment, confirm payment, checkout.

Retry cùng key không nhân order/payment.

## Việc còn chốt lúc Phase 5 (không chặn Phase 1–4)

DRI Order/Payment quyết định shape cuối của `payment-drafts` vs `orders` khi implement PAY_WITH_ORDER. Phase 1–4 chỉ cần join, menu, preview.
