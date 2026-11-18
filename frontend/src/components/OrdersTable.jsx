import Badge from "./Badge.jsx";
import { inr } from "../utils.js";

/** Reusable order list. Pass the columns you want to show. */
export default function OrdersTable({ orders, show = [] }) {
  if (!orders.length) {
    return <div className="empty">No orders yet.</div>;
  }
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            <th>Order</th>
            {show.includes("customer") && <th>Customer</th>}
            {show.includes("seller") && <th>Restaurant</th>}
            {show.includes("rider") && <th>Rider</th>}
            <th>Items</th>
            <th>Amount</th>
            <th>Payment</th>
            <th>Status</th>
            <th>Placed</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td className="strong">{o.id}</td>
              {show.includes("customer") && <td>{o.customerName}</td>}
              {show.includes("seller") && <td>{o.sellerName}</td>}
              {show.includes("rider") && <td>{o.riderName || <span className="muted">Not assigned</span>}</td>}
              <td className="items-cell">{o.items}</td>
              <td className="strong">{inr(o.amount)}</td>
              <td>{o.payment}</td>
              <td>
                <Badge value={o.status} />
              </td>
              <td className="muted nowrap">{o.placedAt}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
