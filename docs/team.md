# Team, phase, Definition of Done kỹ thuật

Team 4 người, ngoài giờ, ~6–8 giờ tập trung / người / tuần. Công cụ free: GitHub, Trello (sau), GitHub Actions.

Làm **vertical slice**, không chia theo layer rồi ghép cuối tuần.

Tên người chưa gán. File này để team thảo luận rồi reply. Không tự gán.

## Vai trò — đúng một DRI / module

### Member 1 — DRI QR + Session

Own: restaurant, table, qr, session, session_guest, timeout.

Demo: tạo bàn → QR → quét → create/join → **2 máy cùng session**.

### Member 2 — DRI Order + Payment

Own: order, payment, PAY_AT_END, PAY_WITH_ORDER, tiền, idempotency, state payment.

Demo: review → payment → order → checkout.

Làm theo OpenAPI, không bám UI.

### Member 3 — DRI Customer UI

Own: `/q/:qrToken`, session UI, menu, cart, review, payment UI, order status.

Demo: scan → menu → cart → review → pay → status.

Không chốt màn hình chỉ với mock nghiệp vụ.

### Member 4 — DRI Integration + Staff

Own: staff auth/dashboard/orders/tables/menu availability, **review** OpenAPI + STOMP, Compose, Actions, E2E happy path, contract test.

Member 4 là chủ **tích hợp**, không phải người viết hết staff UI + hết contract.

**Giảm tải (khác bản Codex):** DRI 1 và 2 tự viết OpenAPI module mình. Member 4 review, giữ Swagger xanh, chạy E2E. Staff UI Member 4 làm; nếu quá tải, Member 3 nhận `/staff/menu` sau khi customer menu xong.

## Working agreements

1. **Phase 1 serial.** Không feature branch nghiệp vụ khi Compose chưa chạy đều mọi máy.
2. **Song song hạn chế từ Phase 2.** Contract viết trước phần phụ thuộc.
3. **Một DRI / module.** Người khác được PR; DRI duyệt nghiệp vụ.
4. **Không ship màn FE** nếu thiếu Spring endpoint + example OpenAPI.
5. Mock UI chỉ để layout, không tính Done.
6. **Tích hợp trước polish.** Không animation/design system khi E2E gãy.
7. PR nhỏ: một business rule, một test chấp nhận.

## Lễ nghi ngoài giờ (không Scrum đầy đủ)

Không daily 15 phút nếu không ai unblock. Trello setup sau.

| Session | Thời lượng | Việc |
|---|---|---|
| 1 Planning | 20–30 phút | Phase hiện tại, contract blocked, PR, E2E |
| 2 Build | 2–3 giờ | Mỗi người làm slice |
| 3 Integration | 1.5–2 giờ | Member 4: Compose + contract + FE + E2E |
| 4 Review/fix | 1.5–2 giờ | Test fail, review, milestone kế |

Xong local ≠ xong. Xong = Compose + contract + UI (nếu user-facing).

## Phase — entry / exit

### Phase 1 — Foundation (cả 4, serial)

Compose, Postgres, Redis, Flyway, skeleton module Spring, Vite+Tailwind+DaisyUI, OpenAPI, Actions, seed **chỉ** dev/demo.

Exit: `docker compose up` → React gọi Spring, Spring↔Postgres/Redis, Flyway chạy, Actions xanh, Swagger mở được.

### Phase 2 — Restaurant / Table / QR

Exit: Admin login → tạo Table 01 → QR → quét → backend ra đúng bàn.

### Phase 3 — Session + Guest

Exit: 2 máy quét cùng QR → cùng session. Test concurrent: chỉ một session active.

**Cổng cứng:** không vào Phase 5 khi Phase 3+4 chưa chạy 2 máy / 1 bàn.

### Phase 4 — Menu + cart local + review

Exit: QR → menu → add → cart → review. Server từ chối: giá giả, món hết, item sai, session hết. Hai máy hai giỏ.

### Phase 5 — Order

PAY_AT_END trước: order, status, order thêm, checkout, lock, staff pay, close, AVAILABLE.

PAY_WITH_ORDER sau: review → pay → materialize → close → bàn có thể OCCUPIED.

Exit: cả hai mode từ UI thật, Spring thật.

### Phase 6 — STOMP

5 event + reconnect/resync. Exit: 2 browser thấy đổi trạng thái, không poll. Cart không bắn event.

### Phase 7 — Staff + audit

Exit: staff chạy demo không cần vào DB.

### Phase 8 — Hardening

Double-click confirm, duplicate payment, concurrent session, staff lạ, session hết, món vừa hết, giá đổi trước review, limit ảnh, requestId.

### Phase 9 — Demo

Seed staging, in QR, E2E đủ 2 mode, 2 máy khách + 1 staff.

## Test — đúng rule, không phủ hết

Backend: session lifecycle/timeout, tách bàn/session, availability, price snapshot, order/payment/checkout, permission.

Integration: một session/bàn, payment+order trong transaction, idempotency, Flyway, authz. Postgres/Testcontainers hoặc Compose.

WS: 5 event + reconnect.

FE: QR, cart, review, pay, order state, checkout, staff. Không test hết component tĩnh.

E2E tối thiểu: QR → session → menu → cart → review → pay → order → staff → realtime → checkout. Cả hai mode.

TDD cho rule tiền/session/payment: test fail trước, rồi code. Không bắt TDD cho CSS.

## DoD từng card (kỹ thuật)

Card không Done nếu thiếu phần áp dụng:

- [ ] OpenAPI đã cập nhật
- [ ] Flyway nếu đổi schema
- [ ] Test tự động cho business rule
- [ ] 1 người review PR
- [ ] Chạy được trên Docker Compose
- [ ] UI khách hoặc staff cập nhật nếu API user-facing
- [ ] Không secret trong git
- [ ] Error code đã ghi
- [ ] STOMP event nếu đụng realtime

DoD sản phẩm: `product.md`.

## Rủi ro

| Rủi ro | Xử lý |
|---|---|
| Member 4 thành bottleneck | DRI tự viết OpenAPI; M4 review; chia staff menu cho M3 nếu cần |
| Làm song song quá sớm | Phase 1 serial; cổng 2 máy trước Phase 5 |
| Polish UI trước E2E | Rule 6 |
| 2 backend / T3 len lút | `architecture.md` decision log |

## Việc team phải trả lời trước Phase 1

Đọc `docs/README.md` → `product.md` → `architecture.md` → `contracts.md` → file này.

Trả lời bằng comment trên PR của nhánh `docs/discuss`, hoặc sửa trực tiếp các ô `[ ]` / `_điền_` rồi push lên nhánh này.

Không tự gán hộ người khác. Mỗi người đề xuất vai trò mình + lý do (1–2 câu).

### 1. Ai là Member 1 / 2 / 3 / 4?

Gán **đúng một người** mỗi vai. Một người có thể nhận 1 vai. Nếu muốn đổi phạm vi module, ghi rõ — đừng để “ai cũng own order”.

| Vai | Own (tóm tắt) | Tên GitHub / tên thật | Lý do ngắn |
|---|---|---|---|
| Member 1 — DRI QR + Session | restaurant, table, qr, session, guest, timeout | _điền_ | _điền_ |
| Member 2 — DRI Order + Payment | order, payment, 2 mode trả, tiền, idempotency | _điền_ | _điền_ |
| Member 3 — DRI Customer UI | `/q/:qrToken`, menu, cart, review, pay UI | _điền_ | _điền_ |
| Member 4 — DRI Integration + Staff | staff UI, review OpenAPI, Compose, CI, E2E | _điền_ | _điền_ |

Checklist cá nhân (mỗi member đánh dấu vai mình đề xuất):

- [ ] Tôi nhận Member 1
- [ ] Tôi nhận Member 2
- [ ] Tôi nhận Member 3
- [ ] Tôi nhận Member 4
- [ ] Tôi chưa chắc — sẽ comment trên PR

### 2. Ai Product Owner (chốt ưu tiên)?

PO không nhất thiết code. PO chốt: cái gì vào sprint/phase, cái gì **không** làm, khi nào DoD sản phẩm đạt.

- Tên: _điền_
- Có code không? có / không
- [ ] Team đồng ý

### 3. Ai được merge `main`?

`main` protected: PR + 1 review + CI xanh. Không push thẳng.

Ai được bấm merge sau khi review xong?

- [ ] Chỉ PO
- [ ] PO + Member 4 (integration)
- [ ] Bất kỳ DRI sau khi DRI module + 1 người khác đã review
- Tên cụ thể: _điền_

### 4. Khung giờ Session 1 trong tuần (UTC+7)?

Session 1 = planning 20–30 phút. Chọn **một** khung cố định. Session 2–4 xếp quanh khung này.

- Ngày: _ví dụ T7_
- Giờ bắt đầu (UTC+7): _ví dụ 20:00_
- Kênh: _Discord / Meet / ..._
- [ ] Team đồng ý khung này
- [ ] Không họp được giờ đó — đề xuất khác: _điền_

Khi 4 câu đã điền đủ, PO (hoặc người mở PR) comment `chốt` trên PR rồi mới mở Trello và bắt Phase 1.
