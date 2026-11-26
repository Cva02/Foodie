
import { useCallback, useEffect, useState } from "react";
import { RefreshCw, Package, Clock3 } from "lucide-react";
import { sellerApi } from "../api";
import { useToast } from "../../context.jsx";
import "../seller.css";

function formatINR(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount || 0);
}

function formatDate(value) {
  if (!value) return "Date unavailable";
  return new Date(value).toLocaleString("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

const FILTERS = [
  { value: "all", label: "All Orders" },
  { value: "pending", label: "Pending" },
  { value: "active", label: "Active" },
  { value: "history", label: "History" },
];

export default function OrdersPage() {
  const toast = useToast();
  const [filter, setFilter] = useState("all");
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadOrders = useCallback(async (silent = false) => {
    if (silent) setRefreshing(true);
    else setLoading(true);

    try {
      const data = await sellerApi.getOrders(filter);
      setOrders(Array.isArray(data) ? data : []);
    } catch (error) {
      toast(error.message || "Unable to load orders", "error");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [filter, toast]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  return (
    <main className="seller-orders-page">
      <header className="seller-page-heading">
        <div>
          <h1>Orders</h1>
          <p className="seller-muted">
            Review customer orders and track their progress.
          </p>
        </div>
        <button type="button" className="seller-btn seller-btn-dark"
          onClick={() => loadOrders(true)} disabled={refreshing}>
          <RefreshCw size={16} /> {refreshing ? "Refreshing..." : "Refresh"}
        </button>
      </header>

      <div className="seller-order-filter-tabs">
        {FILTERS.map((item) => (
          <button type="button" key={item.value}
            className={filter === item.value ? "selected" : ""}
            onClick={() => setFilter(item.value)}>
            {item.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="seller-order-empty">Loading orders...</div>
      ) : orders.length === 0 ? (
        <div className="seller-order-empty">
          <Package size={38} />
          <strong>No orders found</strong>
          <p>Orders matching this filter will appear here.</p>
        </div>
      ) : (
        <div className="seller-history-list">
          {orders.map((order) => (
            <article className="seller-history-card" key={order.id}>
              <div className="seller-history-header">
                <div>
                  <strong>Order #{order.orderNumber}</strong>
                  <p className="seller-muted">{order.customerName}</p>
                </div>
                <span className={`seller-status seller-status-${(order.status || "").toLowerCase()}`}>
                  {(order.status || "UNKNOWN").replaceAll("_", " ")}
                </span>
              </div>

              <ul className="seller-order-items">
                {(order.items || []).map((item, index) => (
                  <li key={`${item.name}-${index}`}>
                    <span>{item.quantity} × {item.name}</span>
                    <span>{formatINR(item.price * item.quantity)}</span>
                  </li>
                ))}
              </ul>

              {order.cancelReason && ["REJECTED", "CANCELLED"].includes(order.status) && (
  <p className="seller-order-reason">
    {order.status === "REJECTED" ? "Rejection reason:" : "Cancellation reason:"}
    {" "}{order.cancelReason}
  </p>
)}

              <div className="seller-history-footer">
                <span className="seller-muted">
                  <Clock3 size={14} /> {formatDate(order.createdAt)}
                </span>
                <strong>{formatINR(order.total)}</strong>
              </div>
            </article>
          ))}
        </div>
      )}
    </main>
  );
}
