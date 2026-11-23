import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { timeAgo } from '../util.js';

const ICON = {
  WELCOME: '👋', ORDER_PLACED: '🧾', ORDER_CONFIRMED: '✅', ORDER_PREPARING: '👨‍🍳',
  ORDER_DISPATCHED: '🛵', ORDER_DELIVERED: '🎉', ORDER_CANCELLED: '❌',
};

export default function Notifications() {
  const nav = useNavigate();
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');

  const load = useCallback(() => api.get('/notifications').then(setItems).catch((e) => setError(e.message)), []);
  const changed = () => window.dispatchEvent(new Event('foodie-notifications-changed'));

  useEffect(() => {
    load();
    const t = setInterval(load, 8000);
    return () => clearInterval(t);
  }, [load]);

  const open = async (n) => {
    if (!n.read) { await api.post(`/notifications/${n.id}/read`).catch(() => {}); changed(); load(); }
    if (n.orderId) nav('/orders');
  };

  const readAll = async () => { await api.post('/notifications/read-all').catch(() => {}); changed(); load(); };

  if (error && !items) return <div className="container"><p className="error center-text">{error}</p></div>;
  if (!items) return <div className="container narrow"><div className="skeleton tall" /></div>;
  const unread = items.filter((n) => !n.read).length;

  return (
    <div className="container narrow">
      <div className="row-between">
        <h2 className="sec-h">Notifications</h2>
        {unread > 0 && <button className="link" onClick={readAll}>Mark all as read ({unread})</button>}
      </div>

      {items.length === 0 && (
        <div className="empty small-empty"><div className="empty-ico">🔔</div><h3>You’re all caught up</h3>
          <p className="muted">Order updates will show up here.</p></div>
      )}

      <ul className="notif-list">
        {items.map((n) => (
          <li key={n.id} className={'notif' + (n.read ? '' : ' unread')} onClick={() => open(n)}>
            <span className="notif-ico">{ICON[n.type] || '🔔'}</span>
            <div>
              <b>{n.title}</b>
              <p>{n.message}</p>
              <small className="muted">{timeAgo(n.createdAt)}</small>
            </div>
            {!n.read && <i className="unread-dot" />}
          </li>
        ))}
      </ul>
    </div>
  );
}
