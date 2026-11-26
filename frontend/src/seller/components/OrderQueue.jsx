
import { useState } from "react";
import { Check, Inbox, Loader2, X } from "lucide-react";

function formatINR(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount || 0);
}

function timeAgo(value) {
  if (!value) return "";

  const seconds = Math.max(
    0,
    Math.floor((Date.now() - new Date(value).getTime()) / 1000)
  );

  if (seconds < 60) return "Just now";
  if (seconds < 3600) return `${Math.floor(seconds / 60)} min ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)} hr ago`;

  return `${Math.floor(seconds / 86400)} days ago`;
}

function OrderCard({ order, busy, onAction }) {
  const { status } = order;

  function reject() {
    const reason = window.prompt(
      `Enter a reason for rejecting order #${order.orderNumber}:`,
      "Unable to fulfil this order"
    );

    if (reason === null) return;

    onAction(order, "REJECTED", reason.trim());
  }

  return (
    <article className="seller-order-card">
      <div className="seller-order-header">
        <div>
          <h3>#{order.orderNumber}</h3>
          <p className="seller-muted">{order.customerName}</p>
        </div>

        <div className="seller-order-meta">
          <span className={`seller-status seller-status-${status.toLowerCase()}`}>
            {status.replaceAll("_", " ")}
          </span>
          <span className="seller-muted seller-small">
            {order.paymentMode || "Payment"} · {timeAgo(order.createdAt)}
          </span>
        </div>
      </div>

      <ul className="seller-order-items">
        {(order.items || []).map((item, index) => (
          <li key={`${item.name}-${index}`}>
            <span>
              {item.quantity} × {item.name}
            </span>
            <span>{formatINR(item.price * item.quantity)}</span>
          </li>
        ))}
      </ul>

      {order.cancelReason && ["REJECTED", "CANCELLED"].includes(status) && (
  <p className="seller-order-reason">
    {status === "REJECTED" ? "Rejection reason:" : "Cancellation reason:"}
    {" "}{order.cancelReason}
  </p>
)}

      <div className="seller-order-footer">
        <strong className="seller-order-total">
          {formatINR(order.total)}
        </strong>

        {status === "PENDING" && (
          <div className="seller-order-actions">
            <button
              type="button"
              className="seller-btn seller-btn-danger"
              disabled={busy}
              onClick={reject}
            >
              <X size={16} /> Reject
            </button>

            <button
              type="button"
              className="seller-btn seller-btn-success"
              disabled={busy}
              onClick={() => onAction(order, "ACCEPTED")}
            >
              {busy ? <Loader2 size={16} /> : <Check size={16} />}
              Accept
            </button>
          </div>
        )}

        {status === "ACCEPTED" && (
          <button
            type="button"
            className="seller-btn seller-btn-dark"
            disabled={busy}
            onClick={() => onAction(order, "PREPARING")}
          >
            {busy && <Loader2 size={16} />}
            Mark Preparing
          </button>
        )}

        {status === "PREPARING" && (
          <button
            type="button"
            className="seller-btn seller-btn-primary"
            disabled={busy}
            onClick={() => onAction(order, "READY_FOR_PICKUP")}
          >
            {busy && <Loader2 size={16} />}
            Mark Ready for Pickup
          </button>
        )}

        {status === "READY_FOR_PICKUP" && (
          <div className="seller-pickup-code">
            <span>Pickup code</span>
            <strong>{order.pickupCode || "Not assigned yet"}</strong>
            <small>Share with rider when available</small>
          </div>
        )}
      </div>
    </article>
  );
}

export default function OrderQueue({
  orders = [],
  loading,
  busyId,
  onAction,
}) {
  const [tab, setTab] = useState("pending");

  const pending = orders.filter((order) => order.status === "PENDING");

  const active = orders.filter((order) =>
    ["ACCEPTED", "PREPARING", "READY_FOR_PICKUP"].includes(order.status)
  );

  const list = tab === "pending" ? pending : active;

  return (
    <section className="seller-order-queue">
      <div className="seller-order-tabs">
        <button
          type="button"
          className={tab === "pending" ? "selected" : ""}
          onClick={() => setTab("pending")}
        >
          Pending Orders <span>{pending.length}</span>
        </button>

        <button
          type="button"
          className={tab === "active" ? "selected" : ""}
          onClick={() => setTab("active")}
        >
          Active Orders <span>{active.length}</span>
        </button>
      </div>

      {loading && orders.length === 0 ? (
        <div className="seller-order-empty">
          Loading orders...
        </div>
      ) : list.length === 0 ? (
        <div className="seller-order-empty">
          <Inbox size={36} />
          <strong>
            {tab === "pending"
              ? "No new orders right now"
              : "No active orders"}
          </strong>
          <p>
            {tab === "pending"
              ? "New customer orders will appear here."
              : "Accepted orders will appear here while being prepared."}
          </p>
        </div>
      ) : (
        <div className="seller-order-grid">
          {list.map((order) => (
            <OrderCard
              key={order.id}
              order={order}
              busy={busyId === order.id}
              onAction={onAction}
            />
          ))}
        </div>
      )}
    </section>
  );
}
