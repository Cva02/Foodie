import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth.js";

/** Everything inside this route needs a logged-in admin; others are sent to /login. */
export default function RequireAuth() {
  const { loggedIn, role } = useAuth();
  const location = useLocation();
  if (loggedIn && role !== "ADMIN") return <Navigate to={`/${role.toLowerCase()}`} replace />;
  if (!loggedIn) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <Outlet />;
}
