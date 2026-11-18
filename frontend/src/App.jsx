import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout.jsx";
import Overview from "./pages/Overview.jsx";
import CustomersPage from "./pages/CustomersPage.jsx";
import CustomerDetail from "./pages/CustomerDetail.jsx";
import SellersPage from "./pages/SellersPage.jsx";
import SellerDetail from "./pages/SellerDetail.jsx";
import RidersPage from "./pages/RidersPage.jsx";
import RiderDetail from "./pages/RiderDetail.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import RequireAuth from "./components/RequireAuth.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="login" element={<LoginPage />} />
      <Route element={<RequireAuth><Layout /></RequireAuth>}>
        <Route index element={<Overview />} />
        <Route path="customers" element={<CustomersPage />} />
        <Route path="customers/:id" element={<CustomerDetail />} />
        <Route path="sellers" element={<SellersPage />} />
        <Route path="sellers/:id" element={<SellerDetail />} />
        <Route path="riders" element={<RidersPage />} />
        <Route path="riders/:id" element={<RiderDetail />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
