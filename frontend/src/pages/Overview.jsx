import { Link } from "react-router-dom";
import { Bike, ChevronRight, IndianRupee, ShoppingBag, Store, Users } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import StatCard from "../components/StatCard.jsx";
import OrdersTable from "../components/OrdersTable.jsx";
import { inr, useFetch } from "../utils.js";

export default function Overview() {
  const { data: d, error, loading } = useFetch("/api/dashboard");
  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const sections = [
    {
      to: "/customers",
      icon: Users,
      title: "Customers",
      text: `${d.customers} registered customers`,
      cta: "View all customers",
    },
    {
      to: "/sellers",
      icon: Store,
      title: "Sellers",
      text: `${d.sellers} restaurants on Foodie`,
      cta: "View all sellers",
    },
    {
      to: "/riders",
      icon: Bike,
      title: "Riders",
      text: `${d.ridersOnline} of ${d.riders} riders working now`,
      cta: "View all riders",
    },
  ];

  return (
    <>
      <div className="stats-grid">
        <StatCard icon={IndianRupee} label="Revenue" value={inr(d.revenue)} hint="Delivered orders" />
        <StatCard icon={ShoppingBag} label="Orders" value={d.totalOrders} hint={`${d.liveOrders} in progress`} tone="blue" />
        <StatCard icon={Users} label="Customers" value={d.customers} tone="green" />
        <StatCard icon={Store} label="Sellers" value={d.sellers} tone="amber" />
        {/* <StatCard
          icon={Bike}
          label="Pending approvals"
          value={d.pendingApprovals}
          hint="Sellers + riders"
          tone="purple"
        /> */}
      </div>

      <div className="section-grid">
        {sections.map(({ to, icon: Icon, title, text, cta }) => (
          <Link key={to} to={to} className="section-card">
            <div className="section-icon">
              <Icon size={24} />
            </div>
            <div className="section-body">
              <h3>{title}</h3>
              <p>{text}</p>
            </div>
            <span className="section-cta">
              {cta} <ChevronRight size={16} />
            </span>
          </Link>
        ))}
      </div>

      <div className="card">
        <div className="card-head">
          <h2>Recent orders</h2>
        </div>
        <OrdersTable orders={d.recentOrders} show={["customer", "seller", "rider"]} />
      </div>
    </>
  );
}
