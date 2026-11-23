import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";
import FoodieBrand from "../components/FoodieBrand.jsx";
import { api } from "../api.js";
import { auth } from "../auth.js";

export default function RiderLogoutPage() {
  const [error, setError] = useState("");
  const logoutStarted = useRef(false);

  useEffect(() => {
    if (logoutStarted.current) return;
    logoutStarted.current = true;
    const signOut = async () => {
      try {
        if (auth.getToken()) await api.post("/api/rider/auth/logout");
      } catch (requestError) {
        setError(`Your browser session ended, but the server could not confirm logout: ${requestError.message}`);
      } finally {
        auth.clear();
      }
    };
    signOut();
  }, []);

  return (
    <div className="auth-page auth-center">
      <div className="auth-card logout-card">
        <FoodieBrand />
        <CheckCircle2 size={34} className="logout-check" />
        <h2>You've been logged out</h2>
        <p className="muted">Your rider session has ended.</p>
        {error && <div className="portal-error" role="alert">{error}</div>}
        <Link to="/rider" className="btn btn-danger btn-block">Rider sign in</Link>
      </div>
    </div>
  );
}
