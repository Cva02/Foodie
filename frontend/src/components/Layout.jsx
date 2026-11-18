import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { Bike, LayoutDashboard, LogOut, Store, Users } from "lucide-react";
import Logo from "./Logo.jsx";
import Avatar from "./Avatar.jsx";

const NAV = [
  { to: "/", label: "Overview", icon: LayoutDashboard, end: true },
  { to: "/customers", label: "Customers", icon: Users },
  { to: "/sellers", label: "Sellers", icon: Store },
  { to: "/riders", label: "Riders", icon: Bike },
];

const TITLES = {
  "/": "Overview",
  "/customers": "Customers",
  "/sellers": "Sellers",
  "/riders": "Riders",
};

export default function Layout() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const section = "/" + pathname.split("/")[1];
  const title = TITLES[section] || "Dashboard";

  const logout = () => {
    sessionStorage.removeItem("foodie-admin-session");
    navigate("/login", { replace: true });
  };

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <Logo />
          <div>
            <div className="brand-name">foodie</div>
            <div className="brand-sub">Admin Console</div>
          </div>
        </div>

        <nav className="nav">
          {NAV.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => `nav-link ${isActive ? "nav-active" : ""}`}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-foot">Foodie Admin v1.0</div>
      </aside>

      <div className="main">
        <header className="topbar">
          <h1>{title}</h1>
          <div className="admin-chip">
            <Avatar src="https://randomuser.me/api/portraits/men/75.jpg" name="Admin" size={34} />
            <div>
              <div className="strong">Admin</div>
              <div className="muted small">admin@foodie.in</div>
            </div>
            <button className="logout-button" onClick={logout} aria-label="Log out" title="Log out">
              <LogOut size={17} />
            </button>
          </div>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
