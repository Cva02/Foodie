import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff, Lock, Mail } from "lucide-react";
import FoodieBrand from "../../components/FoodieBrand.jsx";
import { sellerApi } from "../api";
import { useToast } from "../../context.jsx";
import "../seller.css";

export default function SellerLogin() {
  const navigate = useNavigate();
  const toast = useToast();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!email.trim() || !password) {
      setError("Please enter your email and password.");
      return;
    }

    setLoading(true);

    try {
      await sellerApi.login(email.trim(), password);
      toast("Seller login successful", "success");
      navigate("/seller", { replace: true });
    } catch (err) {
      const message = err.message || "Login failed. Please try again.";
      setError(message);
      toast(message, "error");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page auth-page-login">
      <div className="auth-side">
        <FoodieBrand className="auth-brand" subtitle="Restaurant Partner" />
        <span className="auth-side-kicker">FOODIE · SELLER</span>
        <h1>Grow your<br />kitchen.</h1>
        <p>Seller console</p>
        <span className="auth-side-note">
          Accept live customer orders, update your menu availability, and track restaurant earnings in one place.
        </span>
      </div>

      <div className="auth-main">
        <form className="auth-card" onSubmit={handleSubmit}>
          <div className="auth-card-eyebrow"><span /> SECURE SIGN IN</div>
          <h2>Seller login</h2>
          <p className="muted">Sign in to manage your restaurant on Foodie.</p>

          <label className="field">
            <span className="field-label">Email address</span>
            <span className="input-icon">
              <Mail size={16} />
              <input
                id="seller-email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="Enter your registered email"
                autoComplete="username"
                autoFocus
                required
              />
            </span>
          </label>

          <label className="field">
            <span className="field-label">Password</span>
            <span className="input-icon">
              <Lock size={16} />
              <input
                id="seller-password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Enter your password"
                autoComplete="current-password"
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

          {error && (
            <div className="form-error" role="alert">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="btn btn-danger btn-block"
            disabled={loading || !email.trim() || !password}
          >
            {loading ? "Signing in…" : "Log in"}
          </button>

          <div className="portal-auth-links">
            <Link to="/seller/register" className="password-reset-link">
              New restaurant? Create seller account
            </Link>
          </div>

          <div className="admin-portal-links">
            <span>Switch portal</span>
            <Link to="/login">Customer</Link>
            <Link to="/rider">Rider</Link>
            <Link to="/admin/login">Admin</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
