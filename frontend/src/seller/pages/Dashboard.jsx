
import { useCallback, useEffect, useState } from "react";
import { sellerApi } from "../api";
import { useToast } from "../../context.jsx";
import KpiCards from "../components/KpiCards";
import OrderQueue from "../components/OrderQueue";
import "../seller.css";

const SUCCESS_MESSAGES = {
  ACCEPTED: "Order accepted successfully",
  REJECTED: "Order rejected",
  PREPARING: "Order marked as preparing",
  READY_FOR_PICKUP: "Order is ready for rider pickup",
};

export default function Dashboard() {
  const toast = useToast();

  const [summary, setSummary] = useState(null);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);

  const loadDashboard = useCallback(
    async (silent = false) => {
      try {
        const [summaryData, orderData] = await Promise.all([
          sellerApi.getSummary(),
          sellerApi.getOrders("queue"),
        ]);

        setSummary(summaryData);
        setOrders(Array.isArray(orderData) ? orderData : []);
      } catch (error) {
        if (!silent) {
          toast(
            error.message || "Unable to load Seller dashboard",
            "error"
          );
        }
      } finally {
        setLoading(false);
      }
    },
    [toast]
  );

  useEffect(() => {
    loadDashboard();

    const timer = window.setInterval(() => {
      loadDashboard(true);
    }, 15000);

    return () => window.clearInterval(timer);
  }, [loadDashboard]);

  async function handleOrderAction(order, status, reason) {
    setBusyId(order.id);

    try {
      await sellerApi.updateOrderStatus(order.id, status, reason);

      toast(
        SUCCESS_MESSAGES[status] || "Order updated successfully",
        "success"
      );

      await loadDashboard(true);
    } catch (error) {
      toast(
        error.message || "Unable to update order",
        "error"
      );

      await loadDashboard(true);
    } finally {
      setBusyId(null);
    }
  }

  return (
    <main className="seller-dashboard">
      <header>
        <h1>Dashboard</h1>
        <p className="seller-muted">
          Manage your restaurant orders and performance.
        </p>
      </header>

      <KpiCards summary={summary} loading={loading} />

      <section>
        <h2>Incoming Orders</h2>

        <OrderQueue
          orders={orders}
          loading={loading}
          busyId={busyId}
          onAction={handleOrderAction}
        />
      </section>
    </main>
  );
}
