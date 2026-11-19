import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { Bike, Bell, ChevronDown, ClipboardList, LayoutDashboard, LogOut, Menu, Store, Users, X } from "lucide-react";
import FoodieBrand from "./FoodieBrand.jsx";
import Avatar from "./Avatar.jsx";
import { useAuth } from "../auth.js";
import { api } from "../api.js";

const NAV = [
  { to: "/", label: "Overview", icon: LayoutDashboard, end: true },
  { to: "/customers", label: "Customers", icon: Users },
  { to: "/sellers", label: "Sellers", icon: Store, group: "sellers" },
  { to: "/riders", label: "Riders", icon: Bike, group: "riders" },
  { to: "/orders", label: "Live orders", icon: ClipboardList },
];

const TITLES = {
  "/": "Overview",
  "/customers": "Customers",
  "/sellers": "Sellers",
  "/riders": "Riders",
  "/orders": "Live orders",
};

export default function Layout() {
  const { pathname } = useLocation();
  const { username } = useAuth();
  const section = "/" + pathname.split("/")[1];
  const title = pathname.endsWith("/applications")
    ? `${section === "/sellers" ? "Seller" : "Rider"} applications`
    : TITLES[section] || "Dashboard";
  const [expandedGroups, setExpandedGroups] = useState({
    sellers: pathname.startsWith("/sellers"),
    riders: pathname.startsWith("/riders"),
  });
  const [pendingApplications, setPendingApplications] = useState([]);
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [notificationError, setNotificationError] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(true);

  useEffect(() => {
    setExpandedGroups((current) => ({
      sellers: pathname.startsWith("/sellers") || current.sellers,
      riders: pathname.startsWith("/riders") || current.riders,
    }));
  }, [pathname]);

  useEffect(() => {
    let active = true;
    const refresh = async () => {
      try {
        const [sellers, riders] = await Promise.all([
          api.get("/api/seller-applications"),
          api.get("/api/rider-applications"),
        ]);
        if (active) {
          setPendingApplications([
            ...sellers.filter((application) => application.status === "PENDING").map((application) => ({
              id: application.id,
              name: application.restaurantName,
              partner: "Seller",
              submittedAt: application.submittedAt,
              to: "/sellers/applications",
            })),
            ...riders.filter((application) => application.status === "PENDING").map((application) => ({
              id: application.id,
              name: application.name,
              partner: "Rider",
              submittedAt: application.submittedAt,
              to: "/riders/applications",
            })),
          ].sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt)));
          setNotificationError("");
        }
      } catch (error) {
        if (active) setNotificationError(error.message);
      }
    };
    refresh();
    const timer = window.setInterval(refresh, 15000);
    window.addEventListener("foodie-applications-updated", refresh);
    return () => {
      active = false;
      window.clearInterval(timer);
      window.removeEventListener("foodie-applications-updated", refresh);
    };
  }, []);

  const sellerApplications = pendingApplications.filter((application) => application.partner === "Seller").length;
  const riderApplications = pendingApplications.filter((application) => application.partner === "Rider").length;

  return (
    <div className={`shell ${sidebarOpen ? "" : "sidebar-closed"}`}>
      <aside className="sidebar" id="admin-sidebar">
        <div className="brand">
          <FoodieBrand subtitle="Admin Console" />
        </div>

        <nav className="nav">
          {NAV.map(({ to, label, icon: Icon, end, group }) => (
            <div className="nav-item" key={to}>
              <div className="nav-item-row">
                <NavLink
                  to={to}
                  end={end}
                  className={({ isActive }) =>
                    `nav-link ${isActive && !(group && pathname.endsWith("/applications")) ? "nav-active" : ""}`}
                >
                  <Icon size={18} />
                  {label}
                </NavLink>
                {group && (
                  <button type="button" className={`nav-group-toggle ${expandedGroups[group] ? "expanded" : ""}`}
                    aria-label={`${expandedGroups[group] ? "Collapse" : "Expand"} ${label} submenu`}
                    aria-expanded={Boolean(expandedGroups[group])}
                    onClick={() => setExpandedGroups((current) => ({
                      ...current,
                      [group]: !current[group],
                    }))}>
                    <ChevronDown size={15} />
                  </button>
                )}
              </div>
              {group && expandedGroups[group] && (
                <div className="nav-submenu">
                  <NavLink to={`${to}/applications`} className={({ isActive }) =>
                    `nav-sub-link ${isActive ? "nav-sub-active" : ""}`}>
                    Applications
                    {(group === "sellers" ? sellerApplications : riderApplications) > 0 && (
                      <span className="nav-sub-count">
                        {group === "sellers" ? sellerApplications : riderApplications}
                      </span>
                    )}
                  </NavLink>
                </div>
              )}
            </div>
          ))}
        </nav>

        <div className="sidebar-foot">foodie Admin v1.0</div>
      </aside>

      <div className="main">
        <header className="topbar">
          <div className="topbar-heading">
            <button type="button" className="sidebar-toggle"
              aria-label={sidebarOpen ? "Close navigation" : "Open navigation"}
              aria-controls="admin-sidebar"
              aria-expanded={sidebarOpen}
              onClick={() => setSidebarOpen((open) => !open)}>
              {sidebarOpen ? <X size={19} /> : <Menu size={19} />}
            </button>
            <h1>{title}</h1>
          </div>
          <div className="admin-chip">
            <div className="notification-wrap">
              <button type="button" className="notification-button" aria-label="Notifications"
                aria-expanded={notificationOpen} onClick={() => setNotificationOpen((open) => !open)}>
                <Bell size={19} />
                {pendingApplications.length > 0 && <span className="notification-count">{pendingApplications.length}</span>}
              </button>
              {notificationOpen && (
                <section className="notification-menu" aria-label="Pending application notifications">
                  <div className="notification-menu-head">
                    <strong>New applications</strong>
                    <span>{pendingApplications.length} pending</span>
                  </div>
                  {notificationError && <p className="notification-error" role="alert">{notificationError}</p>}
                  {pendingApplications.length === 0 ? (
                    <p className="notification-empty">No pending applications.</p>
                  ) : (
                    <div className="notification-list">
                      {pendingApplications.slice(0, 8).map((application) => (
                        <Link className="notification-item" key={`${application.partner}-${application.id}`}
                          to={application.to} onClick={() => setNotificationOpen(false)}>
                          <span className="notification-dot" />
                          <span><b>New {application.partner} application</b><small>{application.name}</small></span>
                        </Link>
                      ))}
                    </div>
                  )}
                  <Link className="notification-footer" to={pendingApplications[0]?.to || "/sellers/applications"}
                    onClick={() => setNotificationOpen(false)}>Review applications</Link>
                </section>
              )}
            </div>
            <Avatar name={username || "Admin"} size={34} />
            <div>
              <div className="strong">{username || "Admin"}</div>
              <div className="muted small">Administrator</div>
            </div>
            <NavLink to="/logout" className="btn btn-outline btn-sm logout-btn">
              <LogOut size={15} /> Log out
            </NavLink>
          </div>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
