import { Navigate } from "react-router-dom";

export default function RequireAuth({ children }) {
  return sessionStorage.getItem("foodie-admin-session")
    ? children
    : <Navigate to="/login" replace />;
}
