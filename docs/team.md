# Team, phase, Definition of Done kỹ thuật

Team 4 người, ngoài giờ, ~6–8 giờ / người / tuần. Free: GitHub, Trello (sau), GitHub Actions.

Làm **vertical slice**. Tên chưa gán — form cuối file.

## Vai trò — đúng một DRI / module

### Member 1 — DRI QR + Session

Own: restaurant, table, qr, session, session_guest, timeout. Liquibase changeset module này.

Demo: tạo bàn → QR → quét → create/join → **2 máy cùng session**.

### Member 2 — DRI Order + Payment

Own: evolve `Order`/`OrderItem` **đã có trên `main`**, payment, PAY_AT_END rồi PAY_WITH_ORDER, tiền, idempotency.

Demo: review → (bỏ tin giá client) → payment → order → checkout.

Không viết UI. OpenAPI module mình.

### Member 3 — DRI Customer UI

Own: CRA router `/q/:qrToken`, session UI, menu, cart, review, payment UI. Gọi Spring thật.

Demo: scan → menu → cart → review → pay → status.

Không Done nếu chỉ mock.

### Member 4 — DRI Integration + Staff

Own: staff UI, review OpenAPI, **Docker Compose** (Postgres + backend Dockerfile + CRA), Actions, E2E.

DRI 1–2 tự viết OpenAPI module. Member 4 review, Swagger xanh, Compose chạy mọi máy. Quá tải staff menu → Member 3 nhận sau customer menu.

## Working agreements

1. **Phase 1 serial.** Không feature branch nghiệp vụ khi Compose (hoặc ít nhất `bootRun` + Postgres + CRA) chưa chạy đều mọi máy.
2. Song song hạn chế từ Phase 2. Contract trước phần phụ thuộc.
3. Một DRI / module. Người khác được PR; DRI duyệt nghiệp vụ.
4. Không ship màn FE nếu thiếu Spring endpoint + example OpenAPI.
5. Mock chỉ layout, không tính Done.
6. Tích hợp trước polish.
7. PR nhỏ: một business rule, một test chấp nhận.
8. **Không đổi stack đã khóa** (Gradle, Liquibase, CRA) trong PR feature.

## Lễ nghi ngoài giờ

Không daily nếu không unblock. Trello sau `chốt`.

| Session | Thời lượng | Việc |
|---|---|---|
| 1 Planning | 20–30 phút | Phase, contract blocked, PR |
| 2 Build | 2–3 giờ | Slice |
| 3 Integration | 1.5–2 giờ | Member 4: Compose + contract + FE |
| 4 Review/fix | 1.5–2 giờ | Test fail, review |

Xong local ≠ xong. Xong = chạy được trên môi trường team thống nhất (Compose khi có) + contract + UI nếu user-facing.

## Phase — bám `main`

### Phase 0 — As-built (đã xong trên `main`)

Gradle boot, Liquibase `001`, Order CRUD, Swagger, Dockerfile backend, CRA hello.

### Phase 1 — Foundation (cả 4, serial) — **tiếp theo**

- Root `.gitignore` (hiện **không có**)
- `.env.example` + bỏ hardcode `tannguyen`/`1234` trong `application.yaml`
- `docker-compose.yml`: postgres + backend (Dockerfile sẵn) + frontend
- CRA gọi `GET /api/v1/orders` (một màn list/create tối thiểu) để chứng minh FE↔BE
- GitHub Actions: `./gradlew test` + `npm test -- --watchAll=false` + compose smoke (`/api/v1/orders` hoặc swagger)
- **Không** thêm Redis/STOMP/Vite/Flyway ở phase này

Exit: `docker compose up --build` → CRA mở, Spring Swagger mở, Liquibase chạy, POST tạo order được, CI xanh.

### Phase 2 — Restaurant / Table / QR

Exit: Admin login (JWT lần đầu) → Table 01 → QR → quét → backend đúng bàn.

### Phase 3 — Session + Guest

Exit: 2 máy cùng QR → cùng session. Unique active session / bàn ở Postgres.

**Cổng cứng:** không Phase 5 khi Phase 3+4 chưa chạy 2 máy / 1 bàn.

### Phase 4 — Menu + cart local + review

Exit: QR → menu → cart → review. Server từ chối giá giả, món hết, session hết. **Bỏ `price` trên create order public.**

### Phase 5 — Order + Payment

PAY_AT_END trước trên entity Order hiện có (thêm `session_id`, kitchen status, snapshot giá). PAY_WITH_ORDER sau.

Exit: cả hai mode từ UI thật, Spring thật.

### Phase 6 — STOMP

5 event + reconnect/resync. Cart không bắn event.

### Phase 7 — Staff + audit

Staff demo không cần vào DB.

### Phase 8 — Hardening

Double-click, duplicate payment, concurrent session, session hết, món vừa hết, giá đổi trước review.

### Phase 9 — Demo

Seed, in QR, 2 khách + 1 staff.

## Test

Backend: session lifecycle, một session/bàn, availability, price snapshot, payment/checkout, permission.  
As-built: chỉ `contextLoads` — Phase 1 thêm test `createOrder` tổng tiền.

E2E tối thiểu khi đủ phase: QR → session → menu → cart → review → pay → order → staff → realtime → checkout.

## DoD từng card

- [ ] OpenAPI cập nhật nếu đụng API
- [ ] Liquibase file **mới** nếu đổi schema (không sửa `001`)
- [ ] Test cho business rule
- [ ] 1 review
- [ ] Chạy được Compose (sau Phase 1) hoặc `bootRun` + CRA (trước Compose)
- [ ] UI cập nhật nếu user-facing
- [ ] Không secret trong git
- [ ] Error code (API mới)
- [ ] STOMP event nếu đụng realtime (từ Phase 6)

## Rủi ro

| Rủi ro | Xử lý |
|---|---|
| Rewrite Vite/Flyway len lút | `architecture.md` decision log; Member 4 reject PR |
| `price` client sống dai | Phase 4 bắt buộc bỏ trên public create |
| Schema `"order"` quote lệch JPA | Kiểm tra `bootRun` Phase 1 |
| Member 4 bottleneck | DRI tự OpenAPI; chia staff menu cho M3 |
| `run.txt` vs CRA (`npm run dev` không tồn tại) | Dùng `npm start`; sửa `run.txt` Phase 1 |

---

## Việc team phải trả lời trước khi mở Trello / song song Phase 2

Đọc `docs/README.md` → `product.md` → `architecture.md` → `contracts.md` → file này.

Trả lời comment PR hoặc sửa ô `_điền_` rồi push.

Không tự gán hộ. Mỗi người đề xuất vai mình + lý do 1–2 câu.

### 1. Ai là Member 1 / 2 / 3 / 4?

| Vai | Own (tóm tắt) | Tên GitHub / tên thật | Lý do ngắn |
|---|---|---|---|
| Member 1 — DRI QR + Session | restaurant, table, qr, session, guest, timeout | _điền_ | _điền_ |
| Member 2 — DRI Order + Payment | evolve Order trên `main`, payment, tiền | _điền_ | _điền_ |
| Member 3 — DRI Customer UI | CRA `/q/:qrToken`, menu, cart, review | _điền_ | _điền_ |
| Member 4 — DRI Integration + Staff | Compose, CI, staff UI, E2E | _điền_ | _điền_ |

- [ ] Tôi nhận Member 1
- [ ] Tôi nhận Member 2
- [ ] Tôi nhận Member 3
- [ ] Tôi nhận Member 4
- [ ] Tôi chưa chắc — sẽ comment trên PR

### 2. Ai Product Owner (chốt ưu tiên)?

- Tên: _điền_
- Có code không? có / không
- [ ] Team đồng ý

### 3. Ai được merge `main`?

- [ ] Chỉ PO
- [ ] PO + Member 4
- [ ] Bất kỳ DRI sau khi DRI module + 1 người khác đã review
- Tên cụ thể: _điền_

### 4. Khung giờ Session 1 (UTC+7)?

- Ngày: _ví dụ T7_
- Giờ bắt đầu: _ví dụ 20:00_
- Kênh: _Discord / Meet / ..._
- [ ] Team đồng ý
- [ ] Không họp được — đề xuất: _điền_

Khi 4 câu đủ, PO comment `chốt` trên PR rồi mới Trello + song song Phase 2. Phase 1 (Compose) có thể bắt đầu ngay vì serial, không cần đợi tên nếu PO đồng ý.