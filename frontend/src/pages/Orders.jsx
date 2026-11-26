import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Star } from 'lucide-react';
import { api } from '../api';
import { useCart, useToast } from '../context.jsx';
import Modal from '../components/Modal.jsx';
import StatusTracker from '../components/StatusTracker.jsx';
import { VegMark } from '../icons.jsx';
import { STATUS_LABEL, dateTime, rupee } from '../util.js';

const REASONS = ['Ordered by mistake', 'Changed my mind', 'Want to change items or address', 'Taking too long', 'Other'];
const FILTERS = [['ALL', 'All'], ['ONGOING', 'Ongoing'], ['DELIVERED', 'Delivered'], ['CANCELLED', 'Cancelled']];
const ORDER_DISCOUNT_STORAGE_KEY = 'foodie_order_discounts';

function resolveOrderPricing(o) {
  let savedMap = {};
  try {
    savedMap = JSON.parse(localStorage.getItem(ORDER_DISCOUNT_STORAGE_KEY) || '{}');
  } catch { /* ignore */ }
  const local = savedMap[String(o.id)];
  const discount = Number(o.discount) > 0 ? Number(o.discount) : (Number(local?.discount) || 0);
  const couponCode = o.couponCode || local?.couponCode || null;
  const itemTotal = Number(o.itemTotal) || o.items.reduce((s, i) => s + i.price * i.qty, 0);
  const deliveryFee = Number(o.deliveryFee) || 0;
  const discountedSubtotal = Math.max(0, itemTotal - discount);
  const taxes = discount > 0 && Number(o.discount) === 0
    ? Math.round(discountedSubtotal * 0.05)
    : (Number(o.taxes) || Math.round(discountedSubtotal * 0.05));
  const total = discount > 0
    ? Math.min(Number(o.total) || Infinity, Math.max(0, discountedSubtotal + deliveryFee + taxes))
    : (Number(o.total) || (itemTotal + deliveryFee + taxes));
  return { couponCode, discount, itemTotal, taxes, total };
}

export default function Orders() {
  const toast = useToast();
  const cart = useCart();
  const nav = useNavigate();
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [cancelling, setCancelling] = useState(null);
  const [reason, setReason] = useState(REASONS[0]);
  const [busy, setBusy] = useState(false);
  const [selectedRatings, setSelectedRatings] = useState({});
  const [ratingOrderId, setRatingOrderId] = useState(null);

  const load = useCallback(() => api.get('/customer/orders').then(setOrders).catch((e) => setError(e.message)), []);

  useEffect(() => {
    load();
    const t = setInterval(load, 5000);   // live status updates
    return () => clearInterval(t);
  }, [load]);

  const confirmCancel = async () => {
    setBusy(true);
    try {
      await api.post(`/customer/orders/${cancelling.id}/cancel`, { reason });
      toast(`Order #${cancelling.id} cancelled`, 'ok');
      window.dispatchEvent(new Event('foodie-notifications-changed'));
      setCancelling(null);
      load();
    } catch (e) {
      toast(e.message, 'error');
      setCancelling(null);
      load();
    } finally {
      setBusy(false);
    }
  };

  const submitRiderRating = async (order) => {
    const rating = selectedRatings[order.id];
    if (!rating) return;

    setRatingOrderId(order.id);
    try {
      await api.post(`/customer/orders/${order.id}/rider-rating`, { rating });
      toast('Thanks for rating your rider!', 'ok');
      setSelectedRatings((current) => {
        const next = { ...current };
        delete next[order.id];
        return next;
      });
      await load();
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      setRatingOrderId(null);
    }
  };

  const reorder = async (o) => {
    try {
      const d = await api.get(`/restaurants/${o.restaurantId}`);
      const avail = new Map(d.items.filter((i) => i.available).map((i) => [i.id, i]));
      const lines = o.items.filter((i) => avail.has(i.menuItemId)).map((i) => {
        const m = avail.get(i.menuItemId);
        return { id: m.id, name: m.name, price: m.price, veg: m.veg, qty: i.qty };
      });
      if (!lines.length) { toast('Items from this order are not available right now', 'error'); return; }
      cart.replaceWith(d.restaurant, lines);
      nav('/cart');
    } catch (e) { toast(e.message, 'error'); }
  };

  if (error && !orders) return <div className="container"><p className="error center-text">{error}</p></div>;
  if (!orders) return <div className="container narrow"><div className="skeleton tall" /></div>;

  const shown = orders.filter((o) =>
    filter === 'ALL' ? true : filter === 'ONGOING' ? !['DELIVERED', 'CANCELLED'].includes(o.status) : o.status === filter);

  return (
    <div className="container narrow">
      <h2 className="sec-h">My orders</h2>
      <div className="chips">
        {FILTERS.map(([v, l]) => <button key={v} className={'chip' + (filter === v ? ' on' : '')} onClick={() => setFilter(v)}>{l}</button>)}
      </div>

      {shown.length === 0 && (
        <div className="empty small-empty">
          <div className="empty-ico">🧾</div>
          <h3>No orders here yet</h3>
          <Link to="/customer" className="btn primary">ORDER NOW</Link>
        </div>
      )}

      {shown.map((o) => {
        const pricing = resolveOrderPricing(o);
        return (
          <article key={o.id} className="order">
            <header>
              <div>
                <h3>{o.restaurantName}</h3>
                <p className="muted small">ORDER #{o.id} | {dateTime(o.createdAt)}</p>
              </div>
              <span className={'status ' + o.status.toLowerCase()}>{STATUS_LABEL[o.status]}</span>
            </header>

            {o.status !== 'CANCELLED' && <StatusTracker status={o.status} />}
            {o.status === 'CANCELLED' && <p className="cancel-note">❌ {o.cancelReason || 'Cancelled'}</p>}

            {o.rider && o.status !== 'CANCELLED' && (
              <p className="rider">
                🛵 <b>{o.status === 'DELIVERED' ? 'Delivered by' : 'Your rider'} {o.rider.name}</b>
                {o.status !== 'DELIVERED' && <> • <a href={'tel:' + o.rider.phone.replace(/\s/g, '')}>{o.rider.phone}</a></>}
                <small> ({o.rider.vehicle})</small>
              </p>
            )}

            {o.status === 'DELIVERED' && o.rider && (
              <section className="rider-rating" aria-label="Rate your rider">
                <div>
                  <strong>{o.riderRating ? 'Your rating' : 'Rate your rider'}</strong>
                  <p className="small muted">
                    {o.riderRating ? 'Thanks for sharing your feedback.' : 'How was your delivery experience?'}
                  </p>
                </div>
                <div className="rider-rating-controls">
                  <div className="rider-rating-stars" role="group" aria-label="Choose a rating from 1 to 5 stars">
                    {[1, 2, 3, 4, 5].map((rating) => {
                      const value = o.riderRating || selectedRatings[o.id] || 0;
                      return (
                        <button
                          key={rating}
                          type="button"
                          className={`rider-rating-star${rating <= value ? ' selected' : ''}`}
                          aria-label={`${rating} star${rating === 1 ? '' : 's'}`}
                          aria-pressed={value === rating}
                          disabled={Boolean(o.riderRating) || ratingOrderId === o.id}
                          onClick={() => setSelectedRatings((current) => ({ ...current, [o.id]: rating }))}
                        >
                          <Star size={20} fill={rating <= value ? 'currentColor' : 'none'} />
                        </button>
                      );
                    })}
                  </div>
                  {!o.riderRating && (
                    <button
                      type="button"
                      className="btn primary rider-rating-submit"
                      disabled={!selectedRatings[o.id] || ratingOrderId === o.id}
                      onClick={() => submitRiderRating(o)}
                    >
                      {ratingOrderId === o.id ? 'SAVING…' : 'SUBMIT RATING'}
                    </button>
                  )}
                </div>
              </section>
            )}

            <ul className="order-items">
              {o.items.map((i) => (
                <li key={i.menuItemId}><VegMark veg={i.veg} /> {i.name} × {i.qty} <span>{rupee(i.price * i.qty)}</span></li>
              ))}
            </ul>
            {pricing.couponCode && pricing.discount > 0 && (
              <p className="order-coupon">🏷️ {pricing.couponCode} applied • Discount −{rupee(pricing.discount)}</p>
            )}
            <p className="small muted">Delivering to: {o.address}</p>
            {o.deliveryCode && !['DELIVERED', 'CANCELLED'].includes(o.status) && (
  <div className="order-delivery-otp">
    <b>Delivery OTP</b>
    <strong>{o.deliveryCode}</strong>
    <p className="small muted">Share this OTP with your rider only when your order arrives.</p>
  </div>
)}

            <footer>
              <div>
                <b>Total {rupee(pricing.total)}</b>
                <small className="muted"> • {o.paymentMethod === 'COD' ? 'Pay on delivery' : o.paymentMethod}</small>
              </div>
              <div className="actions">
                {o.cancellable && <button className="btn danger" onClick={() => { setReason(REASONS[0]); setCancelling(o); }}>CANCEL ORDER</button>}
                {(o.status === 'DELIVERED' || o.status === 'CANCELLED') && <button className="btn outline" onClick={() => reorder(o)}>REORDER</button>}
              </div>
            </footer>
            {o.cancellable && <p className="small muted">You can cancel until the restaurant starts preparing your food.</p>}
          </article>
        );
      })}

      {cancelling && (
        <Modal
          title={`Cancel order #${cancelling.id}?`}
          onClose={() => setCancelling(null)}
          actions={<>
            <button className="btn ghost" onClick={() => setCancelling(null)}>KEEP ORDER</button>
            <button className="btn danger solid" onClick={confirmCancel} disabled={busy}>{busy ? 'CANCELLING…' : 'YES, CANCEL'}</button>
          </>}
        >
          <p>Please tell us why you want to cancel:</p>
          {REASONS.map((r) => (
            <label key={r} className="radio-line"><input type="radio" name="reason" checked={reason === r} onChange={() => setReason(r)} /> {r}</label>
          ))}
        </Modal>
      )}
    </div>
  );
}
