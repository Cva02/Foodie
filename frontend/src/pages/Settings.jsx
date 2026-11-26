import { useEffect, useState } from 'react';
import { api } from '../api';
import { useAuth, useGeo, useToast } from '../context.jsx';
import { CrosshairIcon } from '../icons.jsx';

const TABS = [['profile', 'Profile'], ['addresses', 'Addresses'], ['security', 'Password'], ['prefs', 'Preferences']];

export default function Settings() {
  const { user, logout } = useAuth();
  const [tab, setTab] = useState('profile');

  return (
    <div className="container account">
      <div className="account-head">
        <h2>{user.name}</h2>
        <p className="muted">{user.phone} • {user.email}</p>
      </div>
      <div className="account-body">
        <nav className="account-nav">
          {TABS.map(([k, l]) => <button key={k} className={tab === k ? 'on' : ''} onClick={() => setTab(k)}>{l}</button>)}
          <button className="logout" onClick={logout}>Logout</button>
        </nav>
        <section className="account-pane">
          {tab === 'profile' && <Profile />}
          {tab === 'addresses' && <Addresses />}
          {tab === 'security' && <Password />}
          {tab === 'prefs' && <Prefs />}
        </section>
      </div>
    </div>
  );
}

function Profile() {
  const { user, setUser } = useAuth();
  const toast = useToast();
  const [name, setName] = useState(user.name);
  const [phone, setPhone] = useState(user.phone || '');
  const [busy, setBusy] = useState(false);

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    try { setUser(await api.put('/users/me', { name, phone })); toast('Profile updated', 'ok'); }
    catch (err) { toast(err.message, 'error'); }
    finally { setBusy(false); }
  };

  return (
    <form onSubmit={save}>
      <h3>Edit profile</h3>
      <label className="field">Name<input className="input" value={name} onChange={(e) => setName(e.target.value)} required /></label>
      <label className="field">Phone<input className="input" value={phone} onChange={(e) => setPhone(e.target.value)} /></label>
      <label className="field">Email<input className="input" value={user.email} disabled /></label>
      <button className="btn primary" disabled={busy}>{busy ? 'SAVING…' : 'SAVE CHANGES'}</button>
    </form>
  );
}

function Addresses() {
  const toast = useToast();
  const { detect, detecting } = useGeo();
  const [list, setList] = useState([]);
  const [form, setForm] = useState({ label: 'Home', line: '', pinCode: '', lat: null, lon: null });

  const load = () => api.get('/users/me/addresses').then(setList).catch(() => {});
  useEffect(() => { load(); }, []);

  const fillFromGps = async () => {
    try { const l = await detect(); setForm((f) => ({ ...f, line: l.full || l.label, lat: l.lat, lon: l.lon })); }
    catch (e) { toast(e.message, 'error'); }
  };

  const add = async (e) => {
    e.preventDefault();
    try { await api.post('/users/me/addresses', form); setForm({ label: 'Home', line: '', pinCode: '', lat: null, lon: null }); toast('Address saved', 'ok'); load(); }
    catch (err) { toast(err.message, 'error'); }
  };

  const remove = async (id) => { await api.del(`/users/me/addresses/${id}`).catch(() => {}); load(); };

  return (
    <div>
      <h3>Manage addresses</h3>
      {list.length === 0 && <p className="muted">No saved addresses yet.</p>}
      {list.map((a) => (
        <div key={a.id} className="addr-card">
          <div>
  <b>{a.label}</b>
  <p>{a.line}</p>
  <p className="small muted">PIN: {a.pinCode || 'Not saved'}</p>
</div>
          <button className="link danger-text" onClick={() => remove(a.id)}>DELETE</button>
        </div>
      ))}

      <form onSubmit={add} className="addr-form">
        <h4>Add new address</h4>
        <div className="seg">
          {['Home', 'Work', 'Other'].map((l) => (
            <button type="button" key={l} className={form.label === l ? 'on' : ''} onClick={() => setForm({ ...form, label: l })}>{l}</button>
          ))}
        </div>
        <textarea className="input" rows={3} placeholder="Flat no., building, street, area" value={form.line}
                  onChange={(e) => setForm({ ...form, line: e.target.value })} required />
                  <input
  className="input"
  placeholder="6-digit PIN code"
  value={form.pinCode}
  onChange={(e) => setForm({ ...form, pinCode: e.target.value.replace(/\D/g, '').slice(0, 6) })}
  inputMode="numeric"
  maxLength={6}
  required
/>
        <div className="row-gap">
          <button type="button" className="btn outline" onClick={fillFromGps} disabled={detecting}>
            <CrosshairIcon size={16} /> {detecting ? 'Detecting…' : 'Use my current location'}
          </button>
          <button className="btn primary">SAVE ADDRESS</button>
        </div>
      </form>
    </div>
  );
}

function Password() {
  const toast = useToast();
  const [form, setForm] = useState({ currentPassword: '', newPassword: '' });
  const [busy, setBusy] = useState(false);

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    try { await api.put('/users/me/password', form); setForm({ currentPassword: '', newPassword: '' }); toast('Password changed', 'ok'); }
    catch (err) { toast(err.message, 'error'); }
    finally { setBusy(false); }
  };

  return (
    <form onSubmit={save}>
      <h3>Change password</h3>
      <label className="field">Current password<input className="input" type="password" value={form.currentPassword}
        onChange={(e) => setForm({ ...form, currentPassword: e.target.value })} required /></label>
      <label className="field">New password<input className="input" type="password" minLength={6} value={form.newPassword}
        onChange={(e) => setForm({ ...form, newPassword: e.target.value })} required /></label>
      <button className="btn primary" disabled={busy}>{busy ? 'SAVING…' : 'UPDATE PASSWORD'}</button>
    </form>
  );
}

function Prefs() {
  const toast = useToast();
  const [prefs, setPrefs] = useState(() => {
    try { return JSON.parse(localStorage.getItem('foodie_prefs')) || { orders: true, offers: false }; }
    catch { return { orders: true, offers: false }; }
  });
  const [pay, setPay] = useState(localStorage.getItem('foodie_pay') || 'COD');

  const toggle = (k) => {
    const next = { ...prefs, [k]: !prefs[k] };
    setPrefs(next);
    localStorage.setItem('foodie_prefs', JSON.stringify(next));
  };

  return (
    <div>
      <h3>Preferences</h3>
      <label className="switch row"><input type="checkbox" checked={prefs.orders} onChange={() => toggle('orders')} /><span className="slider" /> Order status updates</label>
      <label className="switch row"><input type="checkbox" checked={prefs.offers} onChange={() => toggle('offers')} /><span className="slider" /> Offers and promotions</label>
      <label className="field">Default payment method
        <select className="input" value={pay} onChange={(e) => { setPay(e.target.value); localStorage.setItem('foodie_pay', e.target.value); toast('Saved', 'ok'); }}>
          <option value="COD">Pay on delivery</option>
          <option value="UPI">UPI</option>
          <option value="CARD">Credit / Debit card</option>
        </select>
      </label>
      <p className="small muted">These preferences are stored in this browser.</p>
    </div>
  );
}
