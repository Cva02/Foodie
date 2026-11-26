import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { api, getToken, setToken } from './api';

/* ───────────── Auth ───────────── */
const AuthCtx = createContext(null);
export const useAuth = () => useContext(AuthCtx);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!getToken()) { setReady(true); return; }
    api.get('/users/me').then(setUser).catch(() => setToken(null)).finally(() => setReady(true));
  }, []);

  useEffect(() => {
    const onUnauthorized = () => { setToken(null); setUser(null); };
    window.addEventListener('foodie-unauthorized', onUnauthorized);
    return () => window.removeEventListener('foodie-unauthorized', onUnauthorized);
  }, []);

  const finish = (r) => { setToken(r.token); setUser(r.user); return r.user; };
  const login = async (email, password) => finish(await api.post('/customer/auth/login', { email, password }));
  const signup = async (body) => finish(await api.post('/customer/auth/signup', body));
  const logout = () => { setToken(null); setUser(null); localStorage.removeItem('foodie_cart'); };

  return <AuthCtx.Provider value={{ user, setUser, ready, login, signup, logout }}>{children}</AuthCtx.Provider>;
}

/* ───────────── Location ("use my current location") ───────────── */
const GeoCtx = createContext(null);
export const useGeo = () => useContext(GeoCtx);

const DEFAULT_LOC = { label: 'Visakhapatnam, Andhra Pradesh', full: 'Visakhapatnam, Andhra Pradesh', lat: 17.6868, lon: 83.2185 };

async function reverseGeocode(lat, lon) {
  try {
    const r = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&zoom=17&addressdetails=1&lat=${lat}&lon=${lon}`);
    const d = await r.json();
    const a = d.address || {};
    const area = a.neighbourhood || a.suburb || a.road;
    const city = a.city || a.town || a.village || a.county;
    const label = [area, city].filter(Boolean).join(', ') || (d.display_name || '').split(',').slice(0, 2).join(',');
    return { label: label || 'Current location', full: d.display_name || label };
  } catch {
    return { label: 'Current location', full: `${lat.toFixed(4)}, ${lon.toFixed(4)}` };
  }
}

export async function searchPlaces(q) {
  const r = await fetch(`https://nominatim.openstreetmap.org/search?format=jsonv2&limit=6&countrycodes=in&q=${encodeURIComponent(q)}`);
  const list = await r.json();
  return list.map((p) => ({
    label: p.display_name.split(',').slice(0, 2).join(',').trim(),
    full: p.display_name,
    lat: parseFloat(p.lat),
    lon: parseFloat(p.lon),
  }));
}

export function GeoProvider({ children }) {
  const { user } = useAuth();
  const [loc, setLocState] = useState(() => {
    try { return JSON.parse(localStorage.getItem('foodie_loc')) || DEFAULT_LOC; } catch { return DEFAULT_LOC; }
  });
  const [detecting, setDetecting] = useState(false);
  const [open, setOpen] = useState(false); // location drawer

  useEffect(() => {
    if (user && user.lat != null && user.lon != null && !localStorage.getItem('foodie_loc')) {
      setLocState({ label: user.locationLabel || 'Saved location', full: user.locationLabel || '', lat: user.lat, lon: user.lon });
    }
  }, [user]);

  const setLoc = useCallback(async (l) => {
    setLocState(l);
    localStorage.setItem('foodie_loc', JSON.stringify(l));
    if (getToken()) {
      try { await api.put('/users/me/location', { lat: l.lat, lon: l.lon, label: l.label }); } catch { /* non-fatal */ }
    }
  }, []);

  const detect = useCallback(
    () =>
      new Promise((resolve, reject) => {
        if (!navigator.geolocation) { reject(new Error('Your browser does not support location access')); return; }
        setDetecting(true);
        navigator.geolocation.getCurrentPosition(
          async (pos) => {
            const { latitude: lat, longitude: lon } = pos.coords;
            const g = await reverseGeocode(lat, lon);
            const l = { label: g.label, full: g.full, lat, lon };
            await setLoc(l);
            setDetecting(false);
            resolve(l);
          },
          (err) => {
            setDetecting(false);
            reject(new Error(err.code === 1
              ? 'Location permission denied. Allow location access for this site and try again.'
              : 'Could not get your location. Please try again.'));
          },
          { enableHighAccuracy: true, timeout: 15000 }
        );
      }),
    [setLoc]
  );

  return <GeoCtx.Provider value={{ loc, setLoc, detect, detecting, open, setOpen }}>{children}</GeoCtx.Provider>;
}

/* ───────────── Cart ───────────── */
const CartCtx = createContext(null);
export const useCart = () => useContext(CartCtx);

export function CartProvider({ children }) {
  const [cart, setCart] = useState(() => {
    try { return JSON.parse(localStorage.getItem('foodie_cart')) || { restaurant: null, items: {} }; }
    catch { return { restaurant: null, items: {} }; }
  });
  useEffect(() => { localStorage.setItem('foodie_cart', JSON.stringify(cart)); }, [cart]);

  const add = (restaurant, item) =>
    setCart((c) => {
      const base = c.restaurant && c.restaurant.id !== restaurant.id ? { restaurant: null, items: {} } : c;
      const cur = base.items[item.id];
      return {
        restaurant: { id: restaurant.id, name: restaurant.name, area: restaurant.area },
        items: { ...base.items, [item.id]: { id: item.id, name: item.name, price: item.price, veg: item.veg, qty: cur ? cur.qty + 1 : 1 } },
      };
    });

  const change = (id, delta) =>
    setCart((c) => {
      const it = c.items[id];
      if (!it) return c;
      const items = { ...c.items };
      if (it.qty + delta <= 0) delete items[id]; else items[id] = { ...it, qty: Math.min(20, it.qty + delta) };
      return Object.keys(items).length ? { ...c, items } : { restaurant: null, items: {} };
    });

  const replaceWith = (restaurant, lines) =>
    setCart({ restaurant: { id: restaurant.id, name: restaurant.name, area: restaurant.area },
              items: Object.fromEntries(lines.map((l) => [l.id, l])) });

  const clear = () => setCart({ restaurant: null, items: {} });

  const list = Object.values(cart.items);
  const count = list.reduce((s, i) => s + i.qty, 0);
  const subtotal = list.reduce((s, i) => s + i.qty * i.price, 0);

  return <CartCtx.Provider value={{ cart, list, count, subtotal, add, change, replaceWith, clear }}>{children}</CartCtx.Provider>;
}

/* ───────────── Toast ───────────── */
const ToastCtx = createContext(() => {});
export const useToast = () => useContext(ToastCtx);

export function ToastProvider({ children }) {
  const [msg, setMsg] = useState(null);
  const timer = useRef();
  const show = useCallback((text, kind = 'info') => {
    setMsg({ text, kind });
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setMsg(null), 3500);
  }, []);
  return (
    <ToastCtx.Provider value={show}>
      {children}
      {msg && <div className={'toast ' + msg.kind} role="status">{msg.text}</div>}
    </ToastCtx.Provider>
  );
}
