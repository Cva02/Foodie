
import {
  ShoppingBag,
  BellRing,
  ChefHat,
  CheckCircle,
  XCircle,
  TrendingUp,
  Tag,
  Wallet,
} from "lucide-react";

const ORDER_CARDS = [
  {
    key: "totalOrders",
    label: "Total Orders",
    icon: ShoppingBag,
  },
  {
    key: "pendingOrders",
    label: "Pending Orders",
    
    icon: BellRing,
  },
  {
    key: "activeOrders",
    label: "Active Orders",
    
    icon: ChefHat,
  },
  {
    key: "completedOrders",
    label: "Completed Orders",
    
    icon: CheckCircle,
  },
  {
    key: "cancelledOrders",
    label: "Cancelled Orders",
    
    icon: XCircle,
  },
];

const MONEY_CARDS = [
  {
    key: "grossSales",
    label: "Gross Sales",
    
    icon: TrendingUp,
  },
  {
    key: "discounts",
    label: "Discounts",
    
    icon: Tag,
  },
  {
    key: "estimatedEarnings",
    label: "Estimated Earnings",
    
    icon: Wallet,
  },
];

function formatINR(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount || 0);
}

export default function KpiCards({ summary, loading }) {
  return (
    <section aria-label="Seller performance summary">
      <h2>Order Overview</h2>

      <div className="seller-kpi-grid">
        {ORDER_CARDS.map(({ key, label, hint, icon: Icon }) => (
          <article className="seller-kpi-card" key={key}>
            <div className="seller-kpi-icon">
              <Icon size={22} />
            </div>

            <div className="seller-kpi-content">
              {loading && !summary ? (
                <div className="seller-kpi-loading" />
              ) : (
                <p className="seller-kpi-value">
                  {summary?.[key] ?? 0}
                </p>
              )}

              <p className="seller-kpi-label">{label}</p>
              <p className="seller-kpi-hint">{hint}</p>
            </div>
          </article>
        ))}
      </div>

      <h2 className="seller-earnings-heading">Earnings Overview</h2>

      <div className="seller-kpi-grid">
        {MONEY_CARDS.map(({ key, label, hint, icon: Icon }) => (
          <article className="seller-kpi-card" key={key}>
            <div className="seller-kpi-icon">
              <Icon size={22} />
            </div>

            <div className="seller-kpi-content">
              {loading && !summary ? (
                <div className="seller-kpi-loading" />
              ) : (
                <p className="seller-kpi-value">
                  {formatINR(summary?.[key] ?? 0)}
                </p>
              )}

              <p className="seller-kpi-label">{label}</p>
              <p className="seller-kpi-hint">{hint}</p>
            </div>
          </article>
        ))}
      </div>

      <p className="seller-earnings-disclaimer">
        Earnings are estimates based on delivered orders, after discounts.
        They do not represent confirmed payouts.
      </p>
    </section>
  );
}
