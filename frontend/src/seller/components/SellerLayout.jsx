
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  ShoppingBag,
  UtensilsCrossed,
  UserCircle,
  LogOut,
} from "lucide-react";
import FoodieBrand from "../../components/FoodieBrand.jsx";
import { sellerApi } from "../api";
import { useToast } from "../../context.jsx";
import "../seller.css";

export default function SellerLayout() {
  const navigate = useNavigate();
  const toast = useToast();
  const session = sellerApi.getSession();

  function handleLogout() {
    sellerApi.logout();
    toast("Seller logged out successfully", "success");
    navigate("/seller/login", { replace: true });
  }

  const links = [
    {
      to: "/seller",
      label: "Dashboard",
      icon: LayoutDashboard,
      end: true,
    },
    {
      to: "/seller/orders",
      label: "Orders",
      icon: ShoppingBag,
    },
    {
      to: "/seller/menu",
      label: "Menu",
      icon: UtensilsCrossed,
    },
    {
      to: "/seller/profile",
      label: "Profile",
      icon: UserCircle,
    },
  ];

  return (
    <div className="seller-app-layout">
      <aside className="seller-sidebar">
        <div className="seller-brand">
          <FoodieBrand subtitle="Seller Portal" logoSize={36} />
        </div>

        <div className="seller-restaurant-info">
          <span>RESTAURANT</span>
          <strong>
            {session?.restaurantName || "My Restaurant"}
          </strong>
          <small>
            {session?.ownerName || "Seller Account"}
          </small>
        </div>

        <nav className="seller-navigation">
          <span className="seller-nav-heading">WORKSPACE</span>

          {links.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `seller-nav-link ${isActive ? "active" : ""}`
              }
            >
              <Icon size={19} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <button
          type="button"
          className="seller-logout-button"
          onClick={handleLogout}
        >
          <LogOut size={18} />
          <span>Logout</span>
        </button>
      </aside>

      <div className="seller-main-area">
        <header className="seller-topbar">
          <div>
            <strong>
              {session?.restaurantName || "Seller Dashboard"}
            </strong>
            <small>Manage your restaurant on Foodie</small>
          </div>

          <div className="seller-account-chip">
            <UserCircle size={21} />
            <span>{session?.ownerName || "Seller"}</span>
          </div>
        </header>

        <main className="seller-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
