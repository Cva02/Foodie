import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { LockKeyhole } from "lucide-react";
import Logo from "../components/Logo.jsx";

const DEMO_EMAIL = "admin@foodie.in";
const DEMO_PASSWORD = "foodie123";

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState(DEMO_EMAIL);
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const submit = (event) => {
    event.preventDefault();
    if (email.trim().toLowerCase() !== DEMO_EMAIL || password !== DEMO_PASSWORD) {
      setError("That email or password doesn’t match the demo account.");
      return;
    }
    sessionStorage.setItem("foodie-admin-session", "demo");
    navigate("/", { replace: true });
  };

  return (
    <main className="login-page">
      <section className="login-card">
        <div className="login-brand">
          <Logo />
          <div>
            <div className="brand-name">foodie</div>
            <div className="brand-sub">Admin Console</div>
          </div>
        </div>
        <div className="login-heading">
          <span className="login-icon"><LockKeyhole size={19} /></span>
          <div>
            <h1>Welcome back</h1>
            <p className="muted">Sign in to manage your Foodie dashboard.</p>
          </div>
        </div>
        <form className="login-form" onSubmit={submit}>
          <label>
            Email address
            <input type="email" autoComplete="username" value={email}
              onChange={(event) => setEmail(event.target.value)} required />
          </label>
          <label>
            Password
            <input type="password" autoComplete="current-password" value={password}
              onChange={(event) => setPassword(event.target.value)} required />
          </label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="btn btn-danger login-submit" type="submit">Sign in</button>
        </form>
        
      </section>
    </main>
  );
}
