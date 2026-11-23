import { Navigate, Route, Routes } from "react-router-dom";
import RiderAccessPage from "./pages/RiderAccessPage.jsx";
import RiderLogoutPage from "./pages/RiderLogoutPage.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/rider" replace />} />
      <Route path="/login" element={<RiderAccessPage />} />
      <Route path="/rider" element={<RiderAccessPage />} />
      <Route path="/logout" element={<RiderLogoutPage />} />
      <Route path="*" element={<Navigate to="/rider" replace />} />
    </Routes>
  );
}
