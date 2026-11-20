# Foodie Seller Panel

Multi-vendor food delivery **Seller Panel**, 3-tier:

```
React + Vite + Tailwind (5173)  →  Node.js/Express gateway (5000)  →  Spring Boot 3 / Java 21 (8080)  →  MySQL foodie_db
```

| Folder      | What it is                                                                 |
|-------------|----------------------------------------------------------------------------|
| `frontend/` | React UI (login, dashboard, menu, orders history, earnings, profile)        |
| `gateway/`  | Express BFF: JWT login/logout, Multer image upload, authenticated proxy     |
| `backend/`  | Spring Boot core: entities, repositories, service, `SellerPanelController`  |

## Run it

**Full stack**

1. MySQL 8 running locally. The app creates `foodie_db` itself. Set `DB_USERNAME` and `DB_PASSWORD`
   in the environment.
2. Spring Boot (needs JDK 21 + Maven):
   ```bash
   cd backend && mvn spring-boot:run              # :8080
   ```
3. Gateway (Node 18+):
   ```bash
   cd gateway && npm install && npm start         # :5000
   ```
4. Frontend:
   ```bash
   cd frontend && npm install && npm run dev      # http://localhost:5173
   ```
5. Create a seller account from `/login`; seller accounts and application data are stored in MySQL.

## Screens

- **/login**: mobile/email + password, seller registration, and password recovery. Session
  (`sellerId`, `restaurantName`, `storeId`, JWT) is stored in `localStorage`.
- **Header** (all pages): restaurant name, Store ID, **Online/Offline** switch, **Logout**.
- **/dashboard**: 4 KPI cards (Total Amount, Pending Amount, Active Orders, Pending Orders), the live
  order queue (Pending: Accept/Reject, Active: Mark Preparing → Mark Ready for Pickup; auto-refresh
  every 15 s), and the restaurant menu below it with In-Stock switches, Veg/Non-Veg badges, category
  filter and **+ Add New Dish**.
- **/orders**: completed / cancelled history. **/menu**: full menu with search + edit/delete.
- **/earnings**: Total vs Pending amount, payout history, *Request payout*.
- **/profile**: details, opening hours, packaging charge, Logout.

## Order flow and KPI definitions

```
PENDING ─accept→ ACCEPTED ─→ PREPARING ─→ READY_FOR_PICKUP ─(rider)→ DELIVERED
   └─reject→ REJECTED            └─cancel→ CANCELLED
```

- **Pending Orders** = `PENDING`. **Active Orders** = `ACCEPTED`, `PREPARING`, `READY_FOR_PICKUP`.
- **Total Amount** = sum of `DELIVERED` orders already settled in a payout.
- **Pending Amount** = sum of `DELIVERED` orders not yet settled (what "Request payout" clears).
- Invalid transitions are rejected with HTTP 409.

## API (all via the gateway; `/api/seller/{id}/…` requires `Authorization: Bearer <jwt>`)

| Gateway                            | Purpose                                                  |
|------------------------------------|----------------------------------------------------------|
| `POST /api/auth/login`             | `{identifier, password}` → `{token, sellerId, restaurantName, storeId, online}` |
| `POST /api/auth/register`          | Create seller + log in                                   |
| `POST /api/auth/logout`            | Revokes the JWT                                          |
| `POST /api/upload`                 | multipart field `image` (JPG/PNG/WebP ≤ 5 MB) → `{url}`; files served at `/uploads/*` |
| `GET  /api/seller/{id}/summary`    | `{totalAmount, pendingAmount, activeOrders, pendingOrders}` |
| `GET  /api/seller/{id}/orders?group=pending\|active\|queue\|history\|all` | order lists |
| `PUT  /api/seller/{id}/orders/{oid}/status` | `{status}` transition                           |
| `GET/POST /api/seller/{id}/menu`, `PUT/DELETE …/menu/{itemId}`, `PATCH …/menu/{itemId}/stock` | menu CRUD |
| `GET /api/seller/{id}/earnings`, `POST …/payouts/request` | earnings & payouts              |
| `GET/PUT /api/seller/{id}/profile`, `PATCH …/online`       | profile & store status          |

## Security notes

- Passwords are BCrypt-hashed. The gateway verifies the JWT and that `{sellerId}` in the URL matches the
  token, so a seller can only reach their own data.
- Spring Boot only accepts calls carrying the gateway's `X-Internal-Key`.
- **Before deploying, set** `JWT_SECRET`, `INTERNAL_KEY` (same value on gateway and `foodie.internal-key`),
  `DB_USERNAME`, `DB_PASSWORD`, `CORS_ORIGIN`, and `PUBLIC_URL`.
  The gateway refuses to start in `NODE_ENV=production` with the default secrets.
- Logout revocation and the login rate limiter are in-memory; use Redis if you run several gateway instances.
- `ddl-auto=update` is for development; use Flyway/Liquibase in production.
