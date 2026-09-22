# Sản phẩm — QR Menu MVP

## Mục tiêu

Hệ thống order bằng QR cho quán ăn/cafe, đủ demo E2E.

Khách quét QR cố định tại bàn, xem menu không login, giữ giỏ local, review giá/tình trạng **server**, rồi đặt món. Staff/Admin quản lý bàn, QR, menu, order, thanh toán, checkout.

Team 4 người, ngoài giờ. Tiêu chí thành công:

> Luồng khách → Spring → thanh toán → order → staff → (sau đó) realtime, chạy ổn định trên Docker Compose.

Không xây platform trước khi luồng lõi chạy.

## Đã có trên `main` (as-built)

Chỉ **Order CRUD** + Liquibase schema `order` + Swagger + Dockerfile backend + CRA hello-world.

- `POST /api/v1/orders` tạo order `PENDING`, tính tổng từ item client gửi.
- `GET /api/v1/orders/{id}`, `GET /api/v1/orders`.
- Frontend **chưa** gọi API, **chưa** có màn QR/staff.

Phần còn lại dưới đây là **đích sản phẩm**. Làm incremental trên base này.

## Diễn viên

| Vai | Là ai | Làm gì |
|---|---|---|
| Customer / Guest | Khách tại bàn, không tài khoản | Quét QR, xem menu, giỏ local, review, order/pay |
| Staff | Nhân viên quán | Đổi status order, xác nhận tiền mặt, checkout, availability, nhả bàn |
| Admin | Chủ/quản lý | Mọi quyền Staff + cấu hình quán, menu, bàn/QR, membership, force-close |

MVP 2 role: `ADMIN`, `STAFF`. Không tách Cashier/Waiter/Manager.

Admin bootstrap từ env. Không đăng ký, verify email, reset mật khẩu, social login, guest account.

## Domain (đích)

```text
Restaurant
├── RestaurantSettings
├── User
│   └── RestaurantMembership
├── Table
│   └── QRToken
├── MenuCategory
│   └── MenuItem
└── DiningSession
    ├── SessionGuest
    ├── Order            ← đã có entity trên main (thiếu session_id, status kitchen)
    │   └── OrderItem    ← đang dùng productId + price client; đích = menu_item_id + snapshot server
    └── Payment
```

Có thể thêm `PaymentDraft` nội bộ cho `PAY_WITH_ORDER`.

### Table lifecycle — tách khỏi session

```text
AVAILABLE
OCCUPIED
```

Bàn có thể `OCCUPIED` khi session vừa `CLOSED`. Staff/Admin mới nhả bàn.

### DiningSession lifecycle

```text
ACTIVE
CHECKOUT_REQUESTED
CLOSED
EXPIRED
```

Session = một kỳ order/thanh toán, không phải “người còn ngồi”. Một bàn tối đa **một** session active — constraint PostgreSQL.

## Session

```text
Quét QR → resolve table → tìm session ACTIVE
  ├── có  → join
  └── không → tạo đúng một session
```

Máy mới quét cùng QR → join session đang active.

Guest: `anonymous_device_id → SessionGuest → DiningSession`. Token khách scoped theo session, không quyền staff.

### Timeout

Trước khi có order confirm:

```text
không đụng giỏ  → 15 phút → EXPIRED
có activity giỏ → 1 giờ   → EXPIRED
```

Sau ≥1 order confirm: **không** tự hết hạn. Đóng bằng checkout, thanh toán, hoặc admin force-close.

Không lấy WebSocket làm định nghĩa session còn sống.

## Luồng khách

Route đích: `/q/:qrToken` (CRA, chưa có router trên `main`).

```text
Scan QR → join/create session → Menu → Local cart → Review
  → Backend validate (item, availability, qty, giá hiện tại, session)
  → Confirm → Payment theo mode quán
```

Client không tin cho tiền. Review hiện số Spring trả. Giỏ không sync realtime, không ghi từng lần sửa vào Postgres.

Món hết vẫn hiện, `Out of stock`, không add.

Menu MVP: category + item (name, description, price, image, available, display_order). Không modifier/combo/promotion.

## Luồng staff

Cùng một CRA app, thêm router:

```text
/staff/login
/staff/orders
/staff/tables
/staff/menu
```

Dashboard: Active Orders, Checkout Requests, Tables. Realtime (khi có STOMP): toast + optional sound.

## Thanh toán

Tiền: `BigDecimal` / `NUMERIC`. Currency: `VND`. MVP: tax = 0, service = 0, discount = 0, total = subtotal.

Khách **không** tự đánh dấu cash. Chỉ Staff confirm.

### PAY_AT_END — làm trước

```text
Menu → Cart → Review → Order → thêm order…
  → Request Checkout → session CHECKOUT_REQUESTED (không nhận order mới)
  → Staff payment SUCCESS → session CLOSED → table AVAILABLE
```

Tổng = mọi order đã confirm trong session.

### PAY_WITH_ORDER — làm sau

```text
preview → submit payment-draft → lock session
  → staff confirm → materialize Order → session CLOSED
```

Bàn có thể vẫn `OCCUPIED`. Quét QR lại → session mới.

## Order

Gần như immutable sau tạo. Không sửa/hủy trong MVP. Order thêm = record mới cùng session.

Đích:

```text
OrderItem: menu_item_id, quantity, unit_price_snapshot, note
unit_price_snapshot = server. Client không gửi giá.
status: PENDING → PREPARING → READY → SERVED
```

As-built `main`: `status` là `VARCHAR` (`PENDING` lúc create). Chưa có kitchen flow, chưa có `note`, `price` lấy từ request.

Không `CANCELLED` trong MVP.

## Definition of Done — sản phẩm

MVP xong **chỉ khi** chạy thật (2 máy khách + 1 staff, Postgres, **không mock nghiệp vụ**). Redis + WebSocket là điều kiện **khi phase realtime xong**, không chặn DoD phần order/session nếu team chưa tới phase đó.

```text
ADMIN tạo quán/bàn/menu, generate QR

CUSTOMER A quét QR, join, thêm món, review giá server, order/payment

CUSTOMER B quét cùng QR, cùng session, giỏ độc lập

STAFF đổi status, thấy note, confirm cash

PAY_AT_END: nhiều order → checkout → lock → staff confirm → CLOSED → AVAILABLE

PAY_WITH_ORDER: review → pay → materialize → CLOSED (bàn có thể OCCUPIED)

Khách mới sau CLOSED → session mới
```

## Ngoài phạm vi

Guest account, loyalty, social login, reset password, split bill, sửa/hủy order, modifier/combo, promotion, thuế/VAT, service charge, đa tiền tệ, cổng thanh toán online, POS, app bếp, kho, hóa đơn điện tử, lịch staff, analytics, QR động, geolocation, notification center, push.

Không: microservice, event sourcing, queue, distributed lock, K8s, feature-flag platform, ELK, Prometheus, tracing phân tán.

`cloud.txt` (Vercel / Render / Neon / Auth0) = ghi chú deploy **sau MVP local**. Không phải stack Phase 1.