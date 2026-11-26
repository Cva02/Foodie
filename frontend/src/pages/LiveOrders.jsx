import { useEffect, useState } from "react";
import { api } from "../api.js";
import OrdersTable from "../components/OrdersTable.jsx";
import { ErrorBox, Loading } from "../components/State.jsx";

export default function LiveOrders() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    const load = async () => {
      try {
        const latest = await api.get("/api/orders");
        if (active) {
          setOrders(latest);
          setError("");
        }
      } catch (err) {
        if (active) setError(err.message);
      } finally {
        if (active) setLoading(false);
      }
    };
    load();
    const timer = window.setInterval(load, 6000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, []);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;
  return (
    <div className="card">
      <div className="card-head">
        <h2>Live customer orders</h2>
        <p className="muted small">Live order feed · refreshes every 6 seconds</p>
      </div>
      <OrdersTable orders={orders} show={["customer", "seller", "rider"]} />
    </div>
  );
}
