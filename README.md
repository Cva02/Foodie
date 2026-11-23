# Foodie Customer Portal

Swiggy-style customer web app, branded with the logo and colours from `Foodie_5.apk`
(red `#E23744`, charcoal `#1C1C1C`).

* **Backend:** Java 17, Spring Boot 3.3, Spring Data JPA, **MySQL on localhost**
* **Frontend:** React 18 + Vite + React Router

## What you need
* JDK 17+, Maven 3.9+
* Node.js 18+
* MySQL 8 running on `localhost:3306`

## Run it
1. **MySQL**: nothing to create by hand; the `foodie_customer` database is created on first start.
   Default login is `root` / `root`. If yours differs:
   `set DB_USER=myuser` and `set DB_PASSWORD=mypass` (Windows) or `export ...` (Mac/Linux),
   or edit `backend/src/main/resources/application.properties`.
2. **Backend**
   ```
   cd backend
   mvn spring-boot:run
   ```
   Runs on http://localhost:8080. On first start it seeds 10 restaurants, ~70 dishes and 5 riders.
3. **Frontend**
   ```
   cd frontend
   npm install
   npm run dev
   ```
   Open http://localhost:5173 (Vite proxies `/api` to the backend).

Use `localhost` (not a LAN IP) so the browser allows the "current location" permission.

## Screens
| Screen | Notes |
|---|---|
| Login / Sign up | New users are saved in the MySQL `users` table (BCrypt hashed password) |
| Home | Cuisine circles, top restaurants, filter chips (sort, fast delivery, rating, pure veg, under 300), distance from your location |
| Restaurant | Menu by category, veg-only toggle, dish search, ADD / stepper, sticky cart bar, "start afresh" prompt when mixing restaurants |
| Cart | Address (current location / saved), payment, bill details, place order |
| Orders | Live status tracker, rider details, **Cancel order** (with reason), Reorder |
| Notifications | Order placed / confirmed / preparing / dispatched / delivered / cancelled, unread badge in the navbar |
| Settings | Profile, saved addresses, change password, preferences, logout |
| Navbar | Logo, delivery location, Search, Orders, Notifications, Settings, Cart (mobile gets a bottom tab bar) |

## Use my current location
Navbar location > **Use my current location** (also in Cart and Settings > Addresses).
The browser GPS position is reverse-geocoded with OpenStreetMap Nominatim, saved on the user in MySQL and
used to show distances and to sort restaurants by distance.

## Cancelling orders
`POST /api/orders/{id}/cancel`. Allowed while the order is `PLACED` or `CONFIRMED`
(until the restaurant starts preparing). The restaurant gets a notification as well.

## Order lifecycle / seller + rider side
Placing an order writes a notification for the restaurant (`notifications.restaurant_id`) so the seller/admin
console can show it. Until that console drives the status, `OrderSimulator` advances every order:
PLACED > CONFIRMED (rider assigned) > PREPARING > OUT_FOR_DELIVERY > DELIVERED
(20 / 40 / 60 / 90 seconds; change `foodie.simulation.step-seconds`).
Set `foodie.simulation.enabled=false` once real seller/rider actions update `orders.status`.

## API
```
POST /api/auth/signup | /api/auth/login
GET  /api/restaurants?q=&cuisine=&veg=&sort=&lat=&lon=     GET /api/restaurants/{id}
GET/PUT /api/users/me   PUT /api/users/me/password   PUT /api/users/me/location
GET/POST /api/users/me/addresses   DELETE /api/users/me/addresses/{id}
POST /api/orders/coupons/validate   POST /api/orders   GET /api/orders   GET /api/orders/{id}   POST /api/orders/{id}/cancel
GET /api/notifications   GET /api/notifications/unread-count   POST /api/notifications/{id}/read   POST /api/notifications/read-all
```

## Coupons
- `WELCOME100`: ₹100 off the first order only, when the item total is at least ₹399.
- `FOODIE50`: ₹50 off when the item total is at least ₹299.
- Coupon validation recalculates the cart from the current MySQL menu prices. The order endpoint checks the coupon again before saving the final total.

## Tables (auto-created)
`users`, `addresses`, `restaurants`, `menu_items`, `riders`, `orders`, `order_items`, `notifications`

## Adding your own restaurants and dishes
Insert rows into `restaurants` and `menu_items` (or extend `DataSeeder`). Set `image_url` to show real photos;
without one the cards show an emoji tile.
