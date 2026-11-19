import { Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { useAuth } from './context.jsx';
import Navbar from './components/Navbar.jsx';
import BottomNav from './components/BottomNav.jsx';
import LocationDrawer from './components/LocationDrawer.jsx';
import Login from './pages/Login.jsx';
import Home from './pages/Home.jsx';
import Search from './pages/Search.jsx';
import Restaurant from './pages/Restaurant.jsx';
import Cart from './pages/Cart.jsx';
import Orders from './pages/Orders.jsx';
import Notifications from './pages/Notifications.jsx';
import Settings from './pages/Settings.jsx';

function Shell() {
  return (
    <>
      <Navbar />
      <main className="page"><Outlet /></main>
      <BottomNav />
      <LocationDrawer />
    </>
  );
}

function Protected() {
  const { user, ready } = useAuth();
  const where = useLocation();
  if (!ready) return <div className="splash"><img src="/logo-mark.png" alt="Foodie" /></div>;
  if (!user) return <Navigate to="/login" replace state={{ from: where.pathname }} />;
  return <Shell />;
}

export default function App() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route element={<Protected />}>
        <Route index element={<Home />} />
        <Route path="search" element={<Search />} />
        <Route path="restaurant/:id" element={<Restaurant />} />
        <Route path="cart" element={<Cart />} />
        <Route path="orders" element={<Orders />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="settings" element={<Settings />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
