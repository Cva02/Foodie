import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api';
import { useCart, useGeo } from '../context.jsx';
import Modal from '../components/Modal.jsx';
import { ChevronDown, SearchIcon, StarIcon, VegMark } from '../icons.jsx';
import { foodPhoto, rupee, tileBg } from '../util.js';

export default function Restaurant() {
  const { id } = useParams();
  const { loc } = useGeo();
  const cart = useCart();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [vegOnly, setVegOnly] = useState(false);
  const [q, setQ] = useState('');
  const [collapsed, setCollapsed] = useState({});
  const [pending, setPending] = useState(null);

  useEffect(() => {
    setData(null);
    api.get(`/restaurants/${id}?lat=${loc.lat}&lon=${loc.lon}`).then(setData).catch((e) => setError(e.message));
  }, [id, loc.lat, loc.lon]);

  const groups = useMemo(() => {
    if (!data) return [];
    const map = new Map();
    data.items
      .filter((i) => (!vegOnly || i.veg) && (!q || i.name.toLowerCase().includes(q.toLowerCase())))
      .forEach((i) => { if (!map.has(i.category)) map.set(i.category, []); map.get(i.category).push(i); });
    return [...map.entries()];
  }, [data, vegOnly, q]);

  if (error) return <div className="container"><p className="error center-text">{error}</p></div>;
  if (!data) return <div className="container narrow"><div className="skeleton tall" /></div>;

  const r = data.restaurant;
  const inCart = cart.cart.restaurant && cart.cart.restaurant.id === r.id;
  const qty = (itemId) => (inCart ? cart.cart.items[itemId]?.qty || 0 : 0);

  const tryAdd = (item) => {
    if (!r.open || !item.available) return;
    if (cart.cart.restaurant && cart.cart.restaurant.id !== r.id) { setPending(item); return; }
    cart.add(r, item);
  };

  return (
    <div className="container narrow rest-page">
      <p className="crumbs"><Link to="/">Home</Link> / <b>{r.name}</b></p>

      <section className="rest-head">
        <div>
          <h1>{r.name}</h1>
          <p className="muted">{r.cuisines}</p>
          <p className="muted">{r.area}{r.distanceKm != null ? ` • ${r.distanceKm} km` : ''}</p>
        </div>
        <div className="rating-box">
          <b><StarIcon size={14} /> {r.rating.toFixed(1)}</b>
          <small>Excellent</small>
        </div>
      </section>
      <p className="rest-line">🕑 {r.deliveryMinutes} mins &nbsp;&nbsp; 💰 ₹{r.costForTwo} for two</p>
      {!r.open && <p className="error">This restaurant is currently closed and not accepting orders.</p>}

      {r.offer && (
        <div className="offer-strip"><b>Deals for you:</b> <span>🏷️ {r.offer}</span></div>
      )}

      <div className="menu-tools">
        <label className="switch">
          <input type="checkbox" checked={vegOnly} onChange={(e) => setVegOnly(e.target.checked)} />
          <span className="slider" /> <span>Veg only</span>
        </label>
        <div className="searchbar slim">
          <SearchIcon size={16} />
          <input placeholder={`Search in ${r.name}`} value={q} onChange={(e) => setQ(e.target.value)} />
        </div>
      </div>

      {groups.length === 0 && <p className="muted center-text pad">No dishes found.</p>}

      {groups.map(([cat, items]) => (
        <section key={cat} className="menu-group">
          <button className="menu-group-h" onClick={() => setCollapsed({ ...collapsed, [cat]: !collapsed[cat] })}>
            <h3>{cat} ({items.length})</h3>
            <span className={'chev' + (collapsed[cat] ? '' : ' up')}><ChevronDown /></span>
          </button>
          {!collapsed[cat] && items.map((item) => (
            <article key={item.id} className="dish">
              <div className="dish-info">
                <VegMark veg={item.veg} />
                {item.bestseller && <span className="tag">★ Bestseller</span>}
                <h4>{item.name}</h4>
                <b>{rupee(item.price)}</b>
                <p>{item.description}</p>
              </div>
              <div className="dish-pic" style={{ background: tileBg(item.id) }}>
                {foodPhoto(item) ? (
                  <img
                    className="dish-image"
                    src={foodPhoto(item)}
                    alt={item.name}
                    loading="lazy"
                    onError={(e) => { e.currentTarget.onerror = null; e.currentTarget.src = 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=400&q=85'; }}
                  />
                ) : <span>{item.emoji}</span>}
                {!item.available ? (
                  <span className="add-btn off">UNAVAILABLE</span>
                ) : qty(item.id) === 0 ? (
                  <button className="add-btn" onClick={() => tryAdd(item)} disabled={!r.open}>ADD</button>
                ) : (
                  <div className="stepper float">
                    <button onClick={() => cart.change(item.id, -1)} aria-label="Remove one">−</button>
                    <span>{qty(item.id)}</span>
                    <button onClick={() => cart.change(item.id, 1)} aria-label="Add one">+</button>
                  </div>
                )}
              </div>
            </article>
          ))}
        </section>
      ))}

      {inCart && cart.count > 0 && (
        <Link to="/cart" className="cart-bar">
          <span>{cart.count} item{cart.count > 1 ? 's' : ''} | {rupee(cart.subtotal)}</span>
          <b>VIEW CART ›</b>
        </Link>
      )}

      {pending && (
        <Modal
          title="Items already in cart"
          onClose={() => setPending(null)}
          actions={<>
            <button className="btn ghost" onClick={() => setPending(null)}>NO</button>
            <button className="btn primary" onClick={() => { cart.add(r, pending); setPending(null); }}>YES, START AFRESH</button>
          </>}
        >
          Your cart contains dishes from <b>{cart.cart.restaurant.name}</b>. Do you want to discard the selection and add dishes from <b>{r.name}</b>?
        </Modal>
      )}
    </div>
  );
}
