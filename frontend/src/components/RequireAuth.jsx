
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth.js";

export default function RequireAuth() {
  const { loggedIn, role } = useAuth();
  const location = useLocation();

  if (loggedIn && role !== "ADMIN") {
    return <Navigate to={`/${role.toLowerCase()}`} replace />;
  }

  if (!loggedIn) {
    return (
      <Navigate
        to="/admin/login"
        replace
        state={{ from: location.pathname }}
      />
    );
  }

  return <Outlet />;
}
