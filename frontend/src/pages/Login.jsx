import { useState } from "react";
import { Link } from "react-router-dom";
import { Eye, EyeOff, Lock, Mail, Phone, User } from "lucide-react";
import FoodieBrand from "../components/FoodieBrand.jsx";
import { useAuth } from "../context.jsx";

export default function Login() {
  const { login, signup } = useAuth();
  const [mode, setMode] = useState("login");
  const [showPassword, setShowPassword] = useState(false);
  const [form, setForm] = useState({ name: "", email: "", phone: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      if (mode === "login") await login(form.email, form.password);
      else await signup(form);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-page auth-page-login">
      <div className="auth-side">
        <FoodieBrand className="auth-brand" subtitle="Food Delivery" />
        <span className="auth-side-kicker">FOODIE · CUSTOMER</span>
        <h1>Hungry?<br />We’ve got you.</h1>
        <p>Order fresh from nearby restaurants</p>
        <span className="auth-side-note">
          Explore curated menus, apply instant offers, and track your order live from kitchen to doorstep.
        </span>
      </div>

      <div className="auth-main">
        <form className="auth-card" onSubmit={submit}>
          <div className="auth-card-eyebrow"><span /> {mode === "login" ? "SECURE SIGN IN" : "CREATE ACCOUNT"}</div>
          <h2>{mode === "login" ? "Customer login" : "Create account"}</h2>
          <p className="muted">
            {mode === "login" ? "Sign in to order food, or " : "Already have an account? "}
            <button
              type="button"
              className="password-reset-link"
              onClick={() => {
                setMode(mode === "login" ? "signup" : "login");
                setError("");
              }}
            >
              {mode === "login" ? "create a new account" : "sign in here"}
            </button>
          </p>

          {mode === "signup" && (
            <>
              <label className="field">
                <span className="field-label">Full name</span>
                <span className="input-icon">
                  <User size={16} />
                  <input placeholder="Full name" value={form.name} onChange={set("name")} required />
                </span>
              </label>
              <label className="field">
                <span className="field-label">Phone number</span>
                <span className="input-icon">
                  <Phone size={16} />
                  <input placeholder="Phone number" inputMode="tel" value={form.phone} onChange={set("phone")} required />
                </span>
              </label>
            </>
          )}

          <label className="field">
            <span className="field-label">Email address</span>
            <span className="input-icon">
              <Mail size={16} />
              <input
                type="email"
                placeholder="you@example.com"
                autoComplete="email"
                value={form.email}
                onChange={set("email")}
                required
              />
            </span>
          </label>

          <label className="field">
            <span className="field-label">Password</span>
            <span className="input-icon">
              <Lock size={16} />
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Password"
                minLength={mode === "signup" ? 6 : undefined}
                autoComplete={mode === "login" ? "current-password" : "new-password"}
                value={form.password}
                onChange={set("password")}
                required
              />
              <button
                type="button"
                className="eye"
                onClick={() => setShowPassword((value) => !value)}
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </span>
          </label>

          {error && <div className="form-error" role="alert">{error}</div>}

          <button className="btn btn-danger btn-block" disabled={busy}>
            {busy ? "Please wait…" : mode === "login" ? "Log in" : "Create account"}
          </button>

          <p className="small muted">By continuing, you agree to Foodie’s Terms &amp; Conditions and Privacy Policy.</p>

          <div className="admin-portal-links">
            <span>Partner or Admin? Switch portal</span>
            <Link to="/seller/login">Seller</Link>
            <Link to="/rider">Rider</Link>
            <Link to="/admin/login">Admin</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
