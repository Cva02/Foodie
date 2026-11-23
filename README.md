# Foodie Rider

This project contains only the delivery rider experience: registration and login,
availability, ready-order acceptance, pickup and delivery status updates, current
delivery details, delivery history, earnings, profile, and logout. Rider registration
creates an offline account immediately; there is no approval flow.

Registration is a three-step wizard for personal/account information, vehicle/licence
details, and service-area/bank details. It requires contact details, vehicle type and
registration number, a vehicle photo, driving licence number and document, service area,
two different six-digit delivery PIN codes, and bank name, account holder, account number,
and IFSC. Existing riders without configured PIN codes default to `531162` and `531163`.
Vehicle photos accept JPEG, PNG,
or WebP; licence documents accept PDF, JPEG, or PNG. Each upload is limited to 5 MB.
Bank details and documents are stored in the `rider_private_details` table and are not
included in the rider profile response. The interface supports light and dark themes and
remembers the choice.

The rider UI remains rider-only. Twenty-three dummy `READY` orders (`DEMO-1001` through
`DEMO-1023`) are seeded idempotently in Bheemunipatnam, Visakhapatnam, with seller and
customer addresses and delivery PIN codes `531162`, `531163`, `530048`, or `530045`.
Online riders share browser GPS; available orders must match one of the rider's two PIN codes and are sorted by straight-line
distance to the restaurant. Riders can accept or decline an order for themselves. Declining hides
it only for that rider, and a declined order remains available to other riders. Claims
are serialized so only one rider wins.

An order source can submit new orders through `POST /api/order-source/orders`. Set
`ORDER_SOURCE_API_KEY` and send it as the `X-Order-Source-Key` header. The endpoint marks
the order `READY` immediately, so call it when the order should be offered to riders.
It returns a tracking token. A customer-facing app can poll `GET /api/tracking/{token}`;
the response includes the order details, estimated/actual delivery times, GPS route
points, and the next random six-digit OTP: pickup while the order is assigned to a rider,
or delivery after the rider marks it dispatched. Riders must enter the matching code
before confirming each step in an order-specific verification dialog that displays the
order ID and pickup/delivery stage. OTP values are not included in rider order data. The token
is private to the order and should only be shared with that customer.
Demo tokens are `demo-track-1001` through `demo-track-1023`.

Example order-source request:

```json
{
  "orderId": "ORDER-204",
  "customerName": "Ananya Rao",
  "customerPhone": "+919876543210",
  "customerAddress": "Flat 4B, Beach Road, Bheemunipatnam, Visakhapatnam 531163",
  "customerPinCode": "531163",
  "customerLatitude": 17.8902,
  "customerLongitude": 83.4521,
  "sellerId": "REST-01",
  "sellerName": "Bheemunipatnam Breakfast House",
  "sellerAddress": "12 Beach Road, Bheemunipatnam, Visakhapatnam 531163",
  "sellerLatitude": 17.8883,
  "sellerLongitude": 83.4508,
  "itemName": "Masala Dosa",
  "quantity": 1,
  "amount": 180,
  "payment": "UPI"
}
```

The `expectedDeliveryAt` field is optional; when omitted, the API estimates delivery
within 40 minutes. Order statuses are `PLACED`, `ACCEPTED`, `READY`, `RIDER_ASSIGNED`,
`PICKED_UP`, `DISPATCHED`, `DELIVERED`, and `REJECTED`.

Completed rider earnings accrue at the current fixed delivery fee of ₹40 per order.
The rider dashboard shows each rider's stored rating and completed-delivery count.
New riders and existing unrated riders receive a default rating of 5.0/5; stored
positive ratings are preserved.
Registration only accepts Bike or Scooty as the vehicle type. Once an order is picked
up, the active order card displays a route map from the restaurant to the customer's
delivery location when the addresses or coordinates are available.

The rider dashboard is organized into Current Delivery, Available Orders, History
(accepted and delivered), and Help & Support views. Order cards show a compact summary;
the View action opens full order details. Riders can edit their name, phone, vehicle,
vehicle number, licence number, service area, and two delivery PIN codes from the
top-bar profile option. `PATCH /api/rider/profile` persists these changes. Account
email, uploaded registration documents, and bank details are not editable there.

Riders can submit Help & Support requests from the dashboard. Requests are stored with
an `OPEN` status and can be retrieved by the same signed-in rider:

- `POST /api/rider/support` with `subject` (up to 120 characters) and `message` (up to
  2,000 characters)
- `GET /api/rider/support` to list that rider's requests and their current status

Order acceptance and delivery create persistent in-app notifications for the rider who
handled that order. The notification panel links to that rider's accepted-order or
delivered history. Only unread notifications appear in the panel; opening one marks it
read and removes it from the inbox. Notifications are available through
`GET /api/rider/notifications`; use `PATCH /api/rider/notifications/{id}/read` to mark
one as read. Each rider can only see and update their own notifications.

## Run locally

The backend uses Java 17+, Maven, and MySQL. The frontend uses Node 18+.

For an existing database, run `database/schema.sql` to widen rider document columns
before accepting registration uploads.

Start the backend from `backend`:

```powershell
mvn spring-boot:run
```

Start the frontend from `frontend`:

```powershell
npm install
npm run dev
```

Register or sign in at `/rider` (also `/login`). The backend connection can be configured
with `DB_USERNAME` and `DB_PASSWORD`. Set `CORS_ALLOWED_ORIGINS` to the customer app's
origin when it needs to call the tracking API from a browser. Riders must use HTTPS in
production for browser GPS access. Existing legacy status values are normalized on
startup; legacy database tables are not used or deleted.
