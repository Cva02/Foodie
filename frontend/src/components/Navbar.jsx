import { useEffect, useState } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { api } from '../api';
import { useCart, useGeo } from '../context.jsx';
import { BagIcon, BellIcon, ChevronDown, MoonIcon, PinIcon, ReceiptIcon, SearchIcon, SlidersIcon, SunIcon } from '../icons.jsx';

export default function Navbar() {
  const { loc, setOpen } = useGeo();
  const { count } = useCart();
  const [unread, setUnread] = useState(0);
  const [theme, setTheme] = useState(() => localStorage.getItem('foodie_theme') === 'dark' ? 'dark' : 'light');

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem('foodie_theme', theme);
  }, [theme]);

  useEffect(() => {
    let alive = true;
    const load = () => api.get('/notifications/unread-count').then((r) => alive && setUnread(r.count)).catch(() => {});
    load();
    const t = setInterval(load, 8000);
    window.addEventListener('foodie-notifications-changed', load);
    return () => { alive = false; clearInterval(t); window.removeEventListener('foodie-notifications-changed', load); };
  }, []);

  const cls = ({ isActive }) => 'nav-link' + (isActive ? ' active' : '');
  const triggerHomeVisit = () => window.dispatchEvent(new Event('foodie-home-visit'));

  return (
    <header className="navbar">
      <div className="navbar-in">
        <Link to="/customer" className="brand" aria-label="Foodie home" onClick={triggerHomeVisit}>
          <img src="/logo-mark.png" alt="" />
          <span>foodie</span>
        </Link>

        <button className="loc-btn" onClick={() => setOpen(true)} title="Change delivery location">
          <PinIcon size={18} />
          <span className="loc-text"><b>Deliver to</b><em>{loc.label}</em></span>
          <ChevronDown size={16} />
        </button>

        <nav className="nav-right">
          <NavLink to="/search" className={cls}><SearchIcon /><span>Search</span></NavLink>
          <NavLink to="/orders" className={cls}><ReceiptIcon /><span>Orders</span></NavLink>
          <NavLink to="/notifications" className={cls}>
            <span className="icon-wrap"><BellIcon />{unread > 0 && <i className="badge">{unread > 9 ? '9+' : unread}</i>}</span>
            <span>Notifications</span>
          </NavLink>
          <NavLink to="/cart" className={cls}>
            <span className="icon-wrap"><BagIcon />{count > 0 && <i className="badge cart">{count}</i>}</span>
            <span>Cart</span>
          </NavLink>
          <button
            type="button"
            className="theme-toggle"
            onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}
            aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
            title={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
          >
            {theme === 'light' ? <MoonIcon size={18} /> : <SunIcon size={18} />}
          </button>
          <NavLink to="/settings" className={cls}><SlidersIcon /><span>Settings</span></NavLink>
        </nav>
      </div>
    </header>
  );
}
