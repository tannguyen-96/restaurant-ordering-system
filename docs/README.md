# QR Menu MVP — Tài liệu (bám `main`)

Workspace: `G:\restaurant-ordering-system`  
Remote: `https://github.com/tannguyen-96/restaurant-ordering-system`  
Nhánh SoT code: `main` @ `c9b4506`

Bộ này **viết lại theo code đang có**. Spec cũ trên `docs/discuss` (Vite / Flyway / Maven / STOMP sẵn) **không còn là SoT stack**. Domain sản phẩm (QR, session, 2 mode trả) vẫn giữ.

## Đọc theo thứ tự

| File | Dùng khi |
|---|---|
| [product.md](product.md) | Sản phẩm, lifecycle, DoD, ngoài phạm vi |
| [architecture.md](architecture.md) | Stack thật trên `main`, module hiện có, cách lớn dần |
| [contracts.md](contracts.md) | API **đã ship** vs API **sẽ thêm** |
| [team.md](team.md) | 4 DRI, phase bám `main`, form 4 câu |
| [development.md](development.md) | Gradle, CRA, Postgres, Docker, Git, env |

## Quyết định đã khóa (2026-09-22)

- **Base = `main`.** Không scaffold lại. Không đổi Gradle→Maven, Liquibase→Flyway, CRA→Vite trừ khi team chốt lại bằng PR.
- Backend: Spring Boot **4.1.1**, Java **21**, **Gradle 9.7.1**, JPA, Liquibase, springdoc OpenAPI, PostgreSQL.
- Frontend: **Create React App** (`react-scripts` 5), React 19, **JavaScript** (chưa TypeScript). DaisyUI/Tailwind **chưa** có trong `package.json` — thêm incremental.
- Module đã có: **Order + OrderItem** (`/api/v1/orders`).
- Chưa có: Security/JWT, Redis, WebSocket/STOMP, docker-compose, Flyway, Vite, QR/table/session/menu/payment/audit.
- Realtime (STOMP) và Redis vẫn là **mục tiêu sản phẩm**, thêm **sau** khi Compose + domain lõi chạy. Không giả vờ chúng đã có.
- Client **không được tin cho tiền** — rule sản phẩm. Code hiện tại **đang nhận `price` từ client** → phải sửa khi gắn Menu (xem contracts).

Không mở lại các quyết định trên trừ khi PR nêu blocker cụ thể trong code.