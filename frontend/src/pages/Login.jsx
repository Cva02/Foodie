import { useState } from 'react';
import { useAuth } from '../context.jsx';

export default function Login() {
  const { login, signup } = useAuth();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      if (mode === 'login') await login(form.email, form.password);
      else await signup(form);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth">
      <section className="auth-hero">
        <img src="/logo-full.png" alt="Foodie" />
        <h1>Hungry? We’ve got you.</h1>
        <p>Order from the best restaurants around you and track every step, from kitchen to doorstep.</p>
      </section>

      <section className="auth-card">
        <h2>{mode === 'login' ? 'Login' : 'Sign up'}</h2>
        <p className="muted">
          {mode === 'login' ? 'or ' : 'or '}
          <button type="button" className="link" onClick={() => { setMode(mode === 'login' ? 'signup' : 'login'); setError(''); }}>
            {mode === 'login' ? 'create a new account' : 'login to your account'}
          </button>
        </p>

        <form onSubmit={submit}>
          {mode === 'signup' && (
            <>
              <input className="input" placeholder="Full name" value={form.name} onChange={set('name')} required />
              <input className="input" placeholder="Phone number" inputMode="tel" value={form.phone} onChange={set('phone')} required />
            </>
          )}
          <input className="input" type="email" placeholder="Email" value={form.email} onChange={set('email')} required />
          <input className="input" type="password" placeholder="Password" minLength={mode === 'signup' ? 6 : undefined}
                 value={form.password} onChange={set('password')} required />
          {error && <p className="error">{error}</p>}
          <button className="btn primary block" disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'LOGIN' : 'CREATE ACCOUNT'}
          </button>
        </form>
        <p className="small muted">By continuing, you agree to Foodie’s Terms & Conditions and Privacy Policy.</p>
      </section>
    </div>
  );
}
