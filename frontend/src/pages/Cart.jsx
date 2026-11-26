import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth, useCart, useGeo, useToast } from '../context.jsx';
import { CrosshairIcon, VegMark } from '../icons.jsx';
import { rupee } from '../util.js';

const COUPONS = [
  { code: 'WELCOME100', discount: 100, minimum: 399, detail: 'Flat ₹100 off on orders of ₹399 or above' },
  { code: 'FOODIE50', discount: 50, minimum: 299, detail: 'Flat ₹50 off on orders of ₹299 or above' },
];

const COUPON_STORAGE_KEY = 'foodie_applied_coupon';
const ORDER_DISCOUNT_STORAGE_KEY = 'foodie_order_discounts';

export default function Cart() {
  const { list, count, subtotal, cart, change, clear } = useCart();
  const { loc, detect, detecting } = useGeo();
  const { user } = useAuth();
  const toast = useToast();
  const nav = useNavigate();
  const [saved, setSaved] = useState([]);
  const [addr, setAddr] = useState('current');
  const [door, setDoor] = useState('');
  const [pinCode, setPinCode] = useState('');
  const [pay, setPay] = useState(() => localStorage.getItem('foodie_pay') || 'COD');
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [couponBusy, setCouponBusy] = useState(false);
  const [couponMessage, setCouponMessage] = useState('');
  const [appliedCode, setAppliedCode] = useState(() => localStorage.getItem(COUPON_STORAGE_KEY) || '');

  const baseTaxes = Math.round(subtotal * 0.05);
  const isCouponEligible = (coupon) => subtotal >= coupon.minimum || (subtotal + baseTaxes) >= coupon.minimum;

  useEffect(() => {
    api.get('/users/me/addresses').then(setSaved).catch(() => {});
  }, []);

  useEffect(() => {
    if (!appliedCode) {
      localStorage.removeItem(COUPON_STORAGE_KEY);
      return;
    }
    const offer = COUPONS.find((c) => c.code === appliedCode);
    if (!offer || !isCouponEligible(offer)) {
      setAppliedCode('');
      localStorage.removeItem(COUPON_STORAGE_KEY);
      if (offer && count > 0) {
        setCouponMessage(`${offer.code} was removed because your order total is below ₹${offer.minimum}.`);
      }
      return;
    }
    localStorage.setItem(COUPON_STORAGE_KEY, appliedCode);
  }, [appliedCode, subtotal, baseTaxes, count]);

  if (count === 0) {
    return (
      <div className="empty">
        <div className="empty-ico">🛒</div>
        <h2>Your cart is empty</h2>
        <p className="muted">You can go to home page to view more restaurants</p>
        <Link to="/customer" className="btn primary">SEE RESTAURANTS NEAR YOU</Link>
      </div>
    );
  }

  const deliveryFee = 0;
  const activeCoupon = COUPONS.find((c) => c.code === appliedCode && isCouponEligible(c)) || null;
  const billSubtotal = Number(subtotal) || 0;
  const discount = activeCoupon ? Math.max(0, Math.min(Number(activeCoupon.discount) || 0, billSubtotal)) : 0;
  const discountedSubtotal = Math.max(0, billSubtotal - discount);
  const taxes = Math.round(discountedSubtotal * 0.05);
  const originalTotal = billSubtotal + deliveryFee + baseTaxes;
  const total = Math.max(0, discountedSubtotal + deliveryFee + taxes);
  const totalSaved = Math.max(discount, originalTotal - total);
  const chosen = addr === 'current' ? { line: loc.full || loc.label, lat: loc.lat, lon: loc.lon } : saved.find((a) => String(a.id) === addr);

  const toggleCoupon = async (coupon) => {
    if (activeCoupon?.code === coupon.code) {
      setAppliedCode('');
      localStorage.removeItem(COUPON_STORAGE_KEY);
      setCouponMessage('Coupon removed.');
      return;
    }
    if (!isCouponEligible(coupon)) {
      const needed = Math.max(1, coupon.minimum - subtotal);
      setCouponMessage(`Add ${rupee(needed)} more to apply ${coupon.code}.`);
      return;
    }

    // Apply immediately so the coupon and reduced price take effect right away when criteria are met
    setAppliedCode(coupon.code);
    localStorage.setItem(COUPON_STORAGE_KEY, coupon.code);
    const immediateDiscount = Math.min(coupon.discount, billSubtotal);
    setCouponMessage(`${coupon.code} applied! ₹${immediateDiscount} reduced from your bill.`);

    setCouponBusy(true);
    try {
      await api.post('/customer/orders/coupons/validate', {
        couponCode: coupon.code,
        restaurantId: cart.restaurant.id,
        items: list.map((item) => ({ menuItemId: item.id, qty: item.qty })),
      });
    } catch {
      // Keep local coupon active even if backend validation endpoint is unavailable
    } finally {
      setCouponBusy(false);
    }
  };

  const useGps = async () => {
    try { await detect(); setAddr('current'); toast('Delivery location updated', 'ok'); }
    catch (e) { toast(e.message, 'error'); }
  };

  const place = async () => {
    if (!chosen) { toast('Please choose a delivery address', 'error'); return; }
    if (!/^\d{6}$/.test(pinCode)) {
  toast('Please enter a valid 6-digit delivery PIN code', 'error');
  return;
}
    setBusy(true);
    try {
      localStorage.setItem('foodie_pay', pay);
      const order = await api.post('/customer/orders', {
        restaurantId: cart.restaurant.id,
        items: list.map((i) => ({ menuItemId: i.id, qty: i.qty })),
        address: [door.trim(), chosen.line].filter(Boolean).join(', '),
        pinCode: pinCode.trim(),
        lat: chosen.lat ?? loc.lat,
        lon: chosen.lon ?? loc.lon,
        paymentMethod: pay,
        instructions: note.trim() || null,
        couponCode: activeCoupon ? activeCoupon.code : null,
      });
      if (activeCoupon && order?.id != null) {
        try {
          const map = JSON.parse(localStorage.getItem(ORDER_DISCOUNT_STORAGE_KEY) || '{}');
          map[String(order.id)] = {
            couponCode: activeCoupon.code,
            discount,
            taxes,
            total,
          };
          localStorage.setItem(ORDER_DISCOUNT_STORAGE_KEY, JSON.stringify(map));
        } catch { /* ignore storage errors */ }
      }
      localStorage.removeItem(COUPON_STORAGE_KEY);
      setAppliedCode('');
      clear();
      toast(`Order #${order.id} placed! Total ${rupee(total)}`, 'ok');
      nav('/orders');
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="container cart-layout">
      <div className="cart-left">
        <section className="panel">
          <h3>Delivery address</h3>
          <label className={'radio-card' + (addr === 'current' ? ' on' : '')}>
            <input type="radio" name="addr" checked={addr === 'current'} onChange={() => {
  setAddr('current');
  setPinCode('');
}} />
            <div>
              <b>Current location</b>
              <p>{loc.full || loc.label}</p>
            </div>
          </label>
          {saved.map((a) => (
            <label key={a.id} className={'radio-card' + (addr === String(a.id) ? ' on' : '')}>
              <input
  type="radio"
  name="addr"
  checked={addr === String(a.id)}
  onChange={() => {
    setAddr(String(a.id));
    setPinCode(a.pinCode || '');
  }}
/>
              <div><b>{a.label}</b><p>{a.line}</p></div>
            </label>
          ))}
          <button className="btn outline" onClick={useGps} disabled={detecting}>
            <CrosshairIcon size={16} /> {detecting ? 'Detecting…' : 'Use my current location'}
          </button>
          <input className="input" placeholder="Door / Flat no. & landmark (optional)" value={door} onChange={(e) => setDoor(e.target.value)} />
          <input
          
          className="input"
          placeholder="6-digit delivery PIN code"
          value={pinCode}
          onChange={(e) => setPinCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
          inputMode="numeric"
          maxLength={6}
          />
          <p className="small muted">Delivering to {user?.name}. Add addresses in Settings to see them here.</p>
        </section>

        <section className="panel">
          <h3>Payment</h3>
          {[['COD', 'Pay on delivery (cash / UPI)'], ['UPI', 'UPI'], ['CARD', 'Credit / Debit card']].map(([v, l]) => (
            <label key={v} className={'radio-card slim' + (pay === v ? ' on' : '')}>
              <input type="radio" name="pay" checked={pay === v} onChange={() => setPay(v)} />
              <div><b>{l}</b></div>
            </label>
          ))}
          <p className="small muted">Online payment is simulated in this demo; no money is charged.</p>
        </section>
      </div>

      <aside className="cart-right panel">
        <p className="cart-rest"><b>{cart.restaurant.name}</b><small>{cart.restaurant.area}</small></p>
        {list.map((i) => (
          <div key={i.id} className="cart-line">
            <VegMark veg={i.veg} />
            <span className="cl-name">{i.name}</span>
            <div className="stepper">
              <button onClick={() => change(i.id, -1)} aria-label="Remove one">−</button>
              <span>{i.qty}</span>
              <button onClick={() => change(i.id, 1)} aria-label="Add one">+</button>
            </div>
            <span className="cl-price">{rupee(i.price * i.qty)}</span>
          </div>
        ))}
        <textarea className="input" rows={2} placeholder="Any suggestions? We will pass it on to the restaurant" value={note} onChange={(e) => setNote(e.target.value)} />

        <section className="checkout-coupons" aria-label="Available coupons">
          <h4>Pick a coupon</h4>
          {COUPONS.map((coupon) => {
            const applied = activeCoupon?.code === coupon.code;
            const eligible = isCouponEligible(coupon);
            const shortfall = Math.max(0, coupon.minimum - subtotal);
            return (
              <div
                className={'checkout-coupon' + (applied ? ' applied' : '') + (eligible ? ' eligible' : '')}
                key={coupon.code}
                onClick={() => (eligible || applied) && !couponBusy && toggleCoupon(coupon)}
              >
                <div>
                  <b>{coupon.code} {applied && '✓'}</b>
                  <span>{coupon.detail}</span>
                </div>
                <button
                  type="button"
                  className={'coupon-action' + (applied ? ' on' : '')}
                  onClick={(e) => { e.stopPropagation(); toggleCoupon(coupon); }}
                  disabled={couponBusy || (!eligible && !applied)}
                >
                  {applied ? 'REMOVE' : eligible ? 'APPLY' : `ADD ${rupee(shortfall)}`}
                </button>
              </div>
            );
          })}
          {couponMessage && <p className={'coupon-message' + (discount > 0 ? ' applied' : '')} role="status">{couponMessage}</p>}
          <p>Free delivery on every order. No minimum spend.</p>
        </section>

        <h4>Bill details</h4>
        <div className="bill">
          <span>Item total</span>
          <span>
            {discount > 0 ? (
              <><s className="old-price">{rupee(billSubtotal)}</s> <b>{rupee(discountedSubtotal)}</b></>
            ) : rupee(billSubtotal)}
          </span>
        </div>
        {discount > 0 && (
          <div className="bill coupon-discount">
            <span>Coupon discount ({activeCoupon.code})</span>
            <span>−{rupee(discount)}</span>
          </div>
        )}
        <div className="bill"><span>Delivery fee <em className="free">FREE on every order</em></span><span>{rupee(deliveryFee)}</span></div>
        <div className="bill"><span>Taxes & charges (5%)</span><span>{rupee(taxes)}</span></div>
        <div className="bill total">
          <span>TO PAY</span>
          <span>
            {discount > 0 && <s className="old-price">{rupee(originalTotal)}</s>}
            {rupee(total)}
          </span>
        </div>
        {discount > 0 && (
          <div className="coupon-savings-banner">
            🎉 {activeCoupon.code} applied — you save {rupee(totalSaved)} on this order!
          </div>
        )}

        <button className="btn primary block" onClick={place} disabled={busy}>
          {busy ? 'PLACING…' : `PLACE ORDER • ${rupee(total)}`}
        </button>
        <button className="link small center-link" onClick={() => { setAppliedCode(''); localStorage.removeItem(COUPON_STORAGE_KEY); clear(); }}>Clear cart</button>
      </aside>
    </div>
  );
}
