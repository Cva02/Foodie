import { useEffect } from "react";
import { Link } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";
import FoodieBrand from "../components/FoodieBrand.jsx";
import { api } from "../api.js";
import { auth } from "../auth.js";

/** Visiting /logout ends the session on the server and in the browser, then says goodbye. */
export default function LogoutPage() {
  useEffect(() => {
    api.post("/api/auth/logout").catch(() => {}).finally(() => auth.clear());
  }, []);

  return (
    <div className="auth-page auth-center">
      <div className="auth-card logout-card">
        <FoodieBrand className="logout-brand" />
        <CheckCircle2 size={34} className="logout-check" />
        <h2>You've been logged out</h2>
        <p className="muted">Your admin session has ended safely.</p>
        <Link to="/login" className="btn btn-danger btn-block">Log in again</Link>
      </div>
    </div>
  );
}
