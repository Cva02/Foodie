import { Navigate, Route, Routes } from "react-router-dom";
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

export default function App() {
  return (
    <Routes>
      {/* public */}
      <Route path="login" element={<LoginPage />} />
      <Route path="logout" element={<LogoutPage />} />

      {/* everything else needs the admin to be logged in */}
      <Route element={<RequireAuth />}>
        <Route element={<Layout />}>
          <Route index element={<Overview />} />
          <Route path="customers" element={<CustomersPage />} />
          <Route path="customers/:id" element={<CustomerDetail />} />
          <Route path="sellers" element={<SellersPage />} />
          <Route path="sellers/applications" element={<PartnerApplicationsPage role="SELLER" />} />
          <Route path="sellers/:id" element={<SellerDetail />} />
          <Route path="riders" element={<RidersPage />} />
          <Route path="riders/applications" element={<PartnerApplicationsPage role="RIDER" />} />
          <Route path="riders/:id" element={<RiderDetail />} />
          <Route path="orders" element={<LiveOrders />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Route>
    </Routes>
  );
}
