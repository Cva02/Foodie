import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { Eye, EyeOff, Lock, User } from "lucide-react";
import FoodieBrand from "../components/FoodieBrand.jsx";
import PasswordResetForm from "../components/PasswordResetForm.jsx";
import { api } from "../api.js";
import { auth, useAuth } from "../auth.js";

export default function LoginPage() {
  const { loggedIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [show, setShow] = useState(false);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [resettingPassword, setResettingPassword] = useState(false);

  const from = location.state?.from || "/admin";
  if (loggedIn) return <Navigate to={from} replace />;

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const { token, username: name } = await api.post("/api/auth/login", {
        username: identifier,
        password,
      });
      auth.setSession(token, name);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <div className="auth-page auth-page-login">
      <div className="auth-side">
        <FoodieBrand className="auth-brand" />
        <span className="auth-side-kicker">FOODIE DELIVERY PLATFORM</span>
        <h1>Good food.<br />Made simple.</h1>
        <p>Admin console</p>
        <span className="auth-side-note">Manage customers, restaurants, riders and live orders from one secure place.</span>
      </div>

      <div className="auth-main">
        {resettingPassword ? (
          <PasswordResetForm
            onCancel={() => setResettingPassword(false)}
            onComplete={() => setResettingPassword(false)} />
        ) : (
          <form className="auth-card" onSubmit={submit}>
            <div className="auth-card-eyebrow"><span /> SECURE SIGN IN</div>
            <h2>Admin login</h2>
            <p className="muted">Sign in to manage Foodie.</p>

            <label className="field">
              <span className="field-label">Username</span>
              <span className="input-icon">
                <User size={16} />
                <input type="text" value={identifier}
                  onChange={(e) => setIdentifier(e.target.value)}
                  autoComplete="username" placeholder="Admin username" autoFocus />
              </span>
            </label>

            <label className="field">
              <span className="field-label">Password</span>
              <span className="input-icon">
                <Lock size={16} />
                <input type={show ? "text" : "password"} value={password}
                  onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
                <button type="button" className="eye" onClick={() => setShow(!show)} aria-label={show ? "Hide password" : "Show password"}>
                  {show ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </span>
            </label>

            {error && <div className="form-error" role="alert">{error}</div>}

            <button className="btn btn-danger btn-block" type="submit" disabled={busy || !identifier || !password}>
              {busy ? "Signing in…" : "Log in"}
            </button>
            <button type="button" className="password-reset-link"
              onClick={() => { setResettingPassword(true); setError(""); }}>
              Forgot password?
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
