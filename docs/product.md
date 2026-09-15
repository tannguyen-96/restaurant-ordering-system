# Sản phẩm — QR Menu MVP

## Mục tiêu

Xây hệ thống order bằng QR cho quán ăn/cafe, đủ để demo E2E.

Khách quét QR cố định tại bàn, xem menu không login, giữ giỏ hàng local, xem lại giá/tình trạng hiện tại, rồi đặt món. Staff/Admin quản lý bàn, QR, menu, order, thanh toán, checkout.

Đây là MVP học/demo của team 4 người, làm ngoài giờ. Tiêu chí thành công:

> Luồng khách → backend → thanh toán → order → staff → realtime chạy ổn định trên Docker Compose.

Không xây platform trước khi luồng lõi chạy được.

## Diễn viên

| Vai | Là ai | Làm gì |
|---|---|---|
| Customer / Guest | Khách tại bàn, không tài khoản | Quét QR, xem menu, giỏ local, review, order/pay |
| Staff | Nhân viên quán | Order, đổi status, xác nhận tiền mặt, checkout, đổi availability, nhả bàn |
| Admin | Chủ/quản lý | Tất cả quyền Staff + cấu hình quán, menu, bàn/QR, membership, force-close |

MVP chỉ 2 role: `ADMIN`, `STAFF`. Authorization theo permission, không tách Cashier/Waiter/Manager.

Admin ban đầu bootstrap từ biến môi trường. Không có đăng ký, verify email, reset mật khẩu, social login, guest account.

## Domain

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
    ├── Order
    │   └── OrderItem
    └── Payment
```

Có thể thêm `PaymentDraft` nội bộ cho `PAY_WITH_ORDER`. Không phải tính năng khách thấy. Chỉ để Order không được tạo trước khi thanh toán thành công.

### Table lifecycle — tách khỏi session

```text
AVAILABLE
OCCUPIED
```

Bàn có thể `OCCUPIED` trong khi session vừa `CLOSED`. Staff/Admin mới được nhả bàn.

### DiningSession lifecycle

```text
ACTIVE
CHECKOUT_REQUESTED
CLOSED
EXPIRED
```

Session = một kỳ order/thanh toán, không phải “người còn ngồi ở bàn”.

Một bàn tối đa **một** DiningSession đang active. Constraint phải nằm ở PostgreSQL, không chỉ check trong code.

## Session

```text
Quét QR
  → resolve table
  → tìm session ACTIVE
      ├── có  → join
      └── không → tạo đúng một session
```

Máy mới quét cùng QR thì join session đang active.

Guest không login:

```text
anonymous_device_id → SessionGuest → DiningSession
```

Token khách: scoped theo session, không có quyền staff, hết hạn/thu hồi khi session đóng, dùng lại được khi reload.

### Timeout

Trước khi có order đã confirm:

```text
không đụng giỏ  → 15 phút → EXPIRED
có activity giỏ → 1 giờ   → EXPIRED
```

Sau khi đã có ít nhất một order confirm: **không** tự hết hạn. Đóng bằng checkout, thanh toán, hoặc admin force-close.

Không lấy trạng thái WebSocket làm định nghĩa session còn sống.

## Luồng khách

Route: `/q/:qrToken`

```text
Scan QR
  → join/create session
  → Menu
  → Local cart (mỗi guest một giỏ)
  → Review
  → Backend validate (item, availability, qty, giá hiện tại, session)
  → Confirm
  → Payment flow theo mode quán
```

Client không được tin cho tiền. Review phải hiện số Spring trả về.

Giỏ **không** sync realtime, **không** ghi từng lần sửa vào Postgres.

Món hết vẫn hiện, gắn `Out of stock`, không cho add vào giỏ.

Menu MVP: category (name, display_order, active) + item (name, description, price, image, available, display_order). Không modifier, variant, combo, promotion, flash sale, scheduled price.

## Luồng staff

Cùng một SPA:

```text
/staff/login
/staff/orders
/staff/tables
/staff/menu
```

Dashboard: Active Orders, Checkout Requests, Tables. Realtime: toast + optional sound. Không notification center.

## Thanh toán

Tiền: `BigDecimal` / `NUMERIC`. Currency: `VND`. MVP: tax = 0, service = 0, discount = 0, total = subtotal.

Khách **không** tự đánh dấu tiền mặt đã trả. Chỉ Staff confirm cash.

### PAY_AT_END — làm trước

Order → ăn → order tiếp → checkout → trả.

```text
Menu → Cart → Review → Order → thêm order…
  → Request Checkout
  → Session = CHECKOUT_REQUESTED (không nhận order mới)
  → Staff xử lý payment
  → SUCCESS → Session CLOSED → Table AVAILABLE
```

Session là ranh giới hóa đơn. Tổng = mọi order đã confirm trong session.

### PAY_WITH_ORDER — làm sau

Order → trả → nhận món → session kết thúc.

```text
Menu → Cart → Review → submit payment
  → lock session, chỉ tính draft đã gửi server (giỏ local chưa submit không vào bill)
  → validate lại, tính tổng
  → payment SUCCESS → materialize Order → Session CLOSED
```

Bàn có thể **vẫn OCCUPIED**. Staff nhả bàn sau. Quét QR lại → session mới. Không gắn activity mới vào kỳ đã trả.

## Order

Order gần như immutable sau khi tạo. Không sửa/hủy trong MVP. Order thêm = record Order mới trong cùng session (khi mode cho phép).

```text
OrderItem: menu_item_id, quantity, unit_price_snapshot, note
```

`unit_price_snapshot` luôn do server. Client không gửi giá.

Status:

```text
PENDING → PREPARING → READY → SERVED
```

Không `CANCELLED` trong MVP.

## Definition of Done — sản phẩm

MVP xong **chỉ khi** kịch bản này chạy thật (2 máy khách + 1 staff, Postgres, Redis, WebSocket, không mock nghiệp vụ):

```text
ADMIN tạo quán/bàn/menu, generate QR

CUSTOMER A quét QR, join, thêm món, review giá server, chạy order/payment

CUSTOMER B quét cùng QR, cùng session, giỏ độc lập, chạy order/payment

STAFF nhận realtime, đổi status, thấy note, confirm cash khi cần

PAY_AT_END: order nhiều lần → checkout → lock → staff confirm
            → session CLOSED → bàn AVAILABLE

PAY_WITH_ORDER: review → payment → order được tạo → session CLOSED
                → bàn có thể OCCUPIED → staff nhả bàn sau

Khách mới quét QR sau khi session cũ CLOSED → session mới
```

## Ngoài phạm vi

Không làm trong MVP:

Guest account, loyalty, nickname, social login, reset password, split bill, sửa/hủy order, modifier/variant/combo, giảm giá/promotion, thuế/VAT, service charge, đa tiền tệ, cổng thanh toán online, POS, app bếp, kho, hóa đơn, lịch staff, analytics, QR động, geolocation, notification center, push.

Không làm: microservice, event sourcing, queue, distributed lock, K8s, feature-flag platform, ELK, Prometheus, tracing phân tán.
