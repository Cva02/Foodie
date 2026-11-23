import { Navigate, Outlet, Route, Routes, useLocation } from "react-router-dom";
import { useAuth } from "./context.jsx";

// Admin components
import Layout from "./components/Layout.jsx";
import RequireAuth from "./components/RequireAuth.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import LogoutPage from "./pages/LogoutPage.jsx";
import Overview from "./pages/Overview.jsx";
import CustomersPage from "./pages/CustomersPage.jsx";
import CustomerDetail from "./pages/CustomerDetail.jsx";
import SellersPage from "./pages/SellersPage.jsx";
import SellerDetail from "./pages/SellerDetail.jsx";
import RidersPage from "./pages/RidersPage.jsx";
import RiderDetail from "./pages/RiderDetail.jsx";
import PartnerApplicationsPage from "./pages/PartnerApplicationsPage.jsx";
import LiveOrders from "./pages/LiveOrders.jsx";

// Customer components
import Navbar from "./components/Navbar.jsx";
import BottomNav from "./components/BottomNav.jsx";
import LocationDrawer from "./components/LocationDrawer.jsx";
import Login from "./pages/Login.jsx";
import Home from "./pages/Home.jsx";
import Search from "./pages/Search.jsx";
import Restaurant from "./pages/Restaurant.jsx";
import Cart from "./pages/Cart.jsx";
import Orders from "./pages/Orders.jsx";
import Notifications from "./pages/Notifications.jsx";
import Settings from "./pages/Settings.jsx";

function CustomerShell() {
  return (
    <>
      <Navbar />
      <main className="page"><Outlet /></main>
      <BottomNav />
      <LocationDrawer />
    </>
  );
}

function CustomerProtected() {
  const { user, ready } = useAuth();
  const location = useLocation();

  if (!ready) {
    return (
      <div className="splash">
        <img src="/logo-mark.png" alt="Foodie" />
      </div>
    );
  }

  if (!user) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location.pathname }}
      />
    );
  }

  return <CustomerShell />;
}

export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      {/* ADMIN PORTAL */}
      <Route path="/admin/login" element={<LoginPage />} />
      <Route path="/admin/logout" element={<LogoutPage />} />

      <Route path="/admin" element={<RequireAuth />}>
        <Route element={<Layout />}>
          <Route index element={<Overview />} />
          <Route path="customers" element={<CustomersPage />} />
          <Route path="customers/:id" element={<CustomerDetail />} />
          <Route path="sellers" element={<SellersPage />} />
          <Route
            path="sellers/applications"
            element={<PartnerApplicationsPage role="SELLER" />}
          />
          <Route path="sellers/:id" element={<SellerDetail />} />
          <Route path="riders" element={<RidersPage />} />
          <Route
            path="riders/applications"
            element={<PartnerApplicationsPage role="RIDER" />}
          />
          <Route path="riders/:id" element={<RiderDetail />} />
          <Route path="orders" element={<LiveOrders />} />
          <Route path="*" element={<Navigate to="/admin" replace />} />
        </Route>
      </Route>

      {/* CUSTOMER PORTAL */}
      <Route
        path="/login"
        element={user ? <Navigate to="/" replace /> : <Login />}
      />

      <Route element={<CustomerProtected />}>
        <Route index element={<Home />} />
        <Route path="search" element={<Search />} />
        <Route path="restaurant/:id" element={<Restaurant />} />
        <Route path="cart" element={<Cart />} />
        <Route path="orders" element={<Orders />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="settings" element={<Settings />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
