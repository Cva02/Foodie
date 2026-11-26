import { NavLink } from 'react-router-dom';
import { useCart } from '../context.jsx';
import { BagIcon, HomeIcon, ReceiptIcon, SearchIcon, SlidersIcon } from '../icons.jsx';

/* Mobile tab bar, same destinations as the top navbar. */
export default function BottomNav() {
  const { count } = useCart();
  const cls = ({ isActive }) => 'bn-link' + (isActive ? ' active' : '');
  const triggerHomeVisit = () => window.dispatchEvent(new Event('foodie-home-visit'));
  return (
    <nav className="bottom-nav">
      <NavLink to="/customer" end className={cls} onClick={triggerHomeVisit}><HomeIcon /><span>Home</span></NavLink>
      <NavLink to="/search" className={cls}><SearchIcon /><span>Search</span></NavLink>
      <NavLink to="/cart" className={cls}>
        <span className="icon-wrap"><BagIcon />{count > 0 && <i className="badge cart">{count}</i>}</span><span>Cart</span>
      </NavLink>
      <NavLink to="/orders" className={cls}><ReceiptIcon /><span>Orders</span></NavLink>
      <NavLink to="/settings" className={cls}><SlidersIcon /><span>Settings</span></NavLink>
    </nav>
  );
}
