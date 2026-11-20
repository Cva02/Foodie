import { useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  Bell,
  Check,
  ClipboardList,
  CircleHelp,
  LayoutDashboard,
  Loader2,
  LogOut,
  MessageSquareText,
  Store,
  UtensilsCrossed,
  Volume2,
  VolumeX,
  Wallet,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { api } from '../api/client';
import { errMsg } from '../utils/format';
import Logo from './Logo';
import Switch from './Switch';

const NAV = [
  { to: '/dashboard', label: 'Home', icon: LayoutDashboard },
  { to: '/orders', label: 'Orders', icon: ClipboardList },
  { to: '/menu', label: 'Menu', icon: UtensilsCrossed },
  { to: '/earnings', label: 'Earnings', icon: Wallet },
  { to: '/feedback', label: 'Feedback', icon: MessageSquareText },
  { to: '/profile', label: 'Profile', icon: Store },
  { to: '/help', label: 'Help & Support', icon: CircleHelp },
];

function Header() {
  const { session, logout, patchSession } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [togglingOnline, setTogglingOnline] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [pendingCount, setPendingCount] = useState(0);
  const [unreadCount, setUnreadCount] = useState(0);
  const [soundEnabled, setSoundEnabled] = useState(() => localStorage.getItem('foodie_notifications_sound') !== 'off');
  const previousPendingCount = useRef(null);
  const audioContext = useRef(null);

  function unlockSound() {
    if (!audioContext.current) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) audioContext.current = new AudioContext();
    }
    if (audioContext.current?.state === 'suspended') audioContext.current.resume();
  }

  function playNewOrderChime(force = false) {
    if (!soundEnabled && !force) return;
    unlockSound();
    const ctx = audioContext.current;
    if (!ctx) return;
    const now = ctx.currentTime;
    [880, 1174].forEach((frequency, index) => {
      const oscillator = ctx.createOscillator();
      const gain = ctx.createGain();
      oscillator.type = 'sine';
      oscillator.frequency.value = frequency;
      gain.gain.setValueAtTime(0.0001, now + index * 0.09);
      gain.gain.exponentialRampToValueAtTime(0.18, now + index * 0.09 + 0.015);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + index * 0.09 + 0.22);
      oscillator.connect(gain);
      gain.connect(ctx.destination);
      oscillator.start(now + index * 0.09);
      oscillator.stop(now + index * 0.09 + 0.23);
    });
  }

  useEffect(() => {
    async function refreshPendingOrders() {
      try {
        const orders = await api.getOrders('pending');
        const nextCount = orders.length;
        if (previousPendingCount.current !== null && nextCount > previousPendingCount.current) {
          setUnreadCount((count) => count + (nextCount - previousPendingCount.current));
          playNewOrderChime();
        }
        previousPendingCount.current = nextCount;
        setPendingCount(nextCount);
      } catch {
        // Dashboard handles visible API errors; header polling stays quiet.
      }
    }

    refreshPendingOrders();
    const timer = setInterval(refreshPendingOrders, 15000);
    window.addEventListener('pointerdown', unlockSound, { once: true });
    window.addEventListener('keydown', unlockSound, { once: true });
    return () => {
      clearInterval(timer);
      window.removeEventListener('pointerdown', unlockSound);
      window.removeEventListener('keydown', unlockSound);
    };
  }, [soundEnabled]);

  function toggleNotifications() {
    setNotificationsOpen((open) => {
      const next = !open;
      if (next) setUnreadCount(0);
      return next;
    });
  }

  function toggleSound() {
    setSoundEnabled((enabled) => {
      const next = !enabled;
      localStorage.setItem('foodie_notifications_sound', next ? 'on' : 'off');
      if (next) {
        unlockSound();
        window.setTimeout(() => playNewOrderChime(true), 20);
      }
      return next;
    });
  }

  async function toggleOnline(next) {
    setTogglingOnline(true);
    patchSession({ online: next }); // optimistic
    try {
      await api.setOnline(next);
      toast.success(next ? 'Store is now online' : 'Store is now offline');
    } catch (err) {
      patchSession({ online: !next });
      toast.error(errMsg(err));
    } finally {
      setTogglingOnline(false);
    }
  }

  async function handleLogout() {
    setLoggingOut(true);
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="sticky top-0 z-30 border-b border-gray-100 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-5xl items-center gap-3 px-4 py-3">
        <div className="min-w-0 flex-1">
          <p className="text-xs font-medium text-gray-500">
            Store ID <span className="font-semibold text-gray-700">{session.storeId || `#${session.sellerId}`}</span>
          </p>
          <h1 className="truncate text-lg font-bold leading-tight sm:text-xl">{session.restaurantName}</h1>
        </div>

        <div className="flex items-center gap-2 rounded-full bg-surface py-1.5 pl-3 pr-2">
          <span
            className={`text-xs font-semibold ${session.online ? 'text-green-600' : 'text-gray-500'}`}
            aria-live="polite"
          >
            {session.online ? 'Online' : 'Offline'}
          </span>
          <Switch
            checked={!!session.online}
            onChange={toggleOnline}
            disabled={togglingOnline}
            label="Store online status"
          />
        </div>

        <div className="relative">
          <button
            type="button"
            onClick={toggleNotifications}
            aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}
            aria-expanded={notificationsOpen}
            className="relative flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-surface text-gray-600 transition hover:bg-gray-100"
          >
            <Bell size={20} />
            {unreadCount > 0 && (
              <span className="absolute -right-0.5 -top-0.5 flex min-w-5 items-center justify-center rounded-full border-2 border-white bg-brand-600 px-1 text-[10px] font-bold leading-4 text-white">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>

          {notificationsOpen && (
            <div className="absolute right-0 top-14 z-40 w-72 overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-lg">
              <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
                <div>
                  <p className="font-bold text-ink">Notifications</p>
                  <p className="text-xs text-gray-500">
                    {pendingCount ? `${pendingCount} pending order${pendingCount === 1 ? '' : 's'}` : 'You are all caught up'}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={toggleSound}
                  aria-label={soundEnabled ? 'Mute notification sound' : 'Enable notification sound'}
                  title={soundEnabled ? 'Mute sound' : 'Enable sound'}
                  className="flex h-9 w-9 items-center justify-center rounded-full bg-surface text-gray-600 hover:bg-gray-100"
                >
                  {soundEnabled ? <Volume2 size={18} /> : <VolumeX size={18} />}
                </button>
              </div>

              {pendingCount > 0 ? (
                <button
                  type="button"
                  onClick={() => {
                    setNotificationsOpen(false);
                    navigate('/dashboard');
                  }}
                  className="flex w-full items-start gap-3 px-4 py-4 text-left transition hover:bg-brand-50"
                >
                  <span className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-50 text-brand-600">
                    <Bell size={18} />
                  </span>
                  <span>
                    <span className="block text-sm font-semibold text-ink">
                      {pendingCount} new order{pendingCount === 1 ? '' : 's'}
                    </span>
                    <span className="mt-0.5 block text-xs text-gray-500">Tap to open the incoming order queue</span>
                  </span>
                </button>
              ) : (
                <div className="flex items-center gap-3 px-4 py-5 text-sm text-gray-500">
                  <Check size={18} className="text-green-600" />
                  No pending orders
                </div>
              )}
            </div>
          )}
        </div>

        <button
          type="button"
          onClick={handleLogout}
          disabled={loggingOut}
          aria-label="Log out"
          title="Log out"
          className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-brand-50 text-brand-600 transition hover:bg-brand-100 disabled:opacity-60"
        >
          {loggingOut ? <Loader2 size={20} className="animate-spin" /> : <LogOut size={20} />}
        </button>
      </div>
    </header>
  );
}

export default function Layout() {
  return (
    <div className="min-h-screen md:flex">
      {/* Desktop sidebar */}
      <aside className="sticky top-0 hidden h-screen w-60 shrink-0 flex-col bg-ink p-5 md:flex">
        <div className="mb-8 flex items-center gap-3">
          <Logo size={36} />
          <span className="text-2xl font-bold tracking-tight text-white" style={{ letterSpacing: '-0.03em' }}>
            foodie
          </span>
          <span className="rounded-md bg-white/10 px-1.5 py-0.5 text-[10px] font-semibold uppercase text-gray-300">
            Seller
          </span>
        </div>
        <nav className="space-y-1">
          {NAV.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-2xl px-4 py-3 text-[15px] font-semibold transition ${
                  isActive ? 'bg-brand-600 text-white' : 'text-gray-300 hover:bg-white/10'
                }`
              }
            >
              <Icon size={20} />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <Header />
        <main className="mx-auto w-full max-w-5xl flex-1 px-4 pb-28 pt-5 md:pb-10">
          <Outlet />
        </main>
      </div>

      {/* Mobile bottom navigation */}
      <nav
        className="fixed inset-x-0 bottom-0 z-30 border-t border-gray-100 bg-white pb-[env(safe-area-inset-bottom)] md:hidden"
        aria-label="Main"
      >
        <ul className="mx-auto flex max-w-md justify-around">
          {NAV.map(({ to, label, icon: Icon }) => (
            <li key={to} className="flex-1">
              <NavLink
                to={to}
                className={({ isActive }) =>
                  `flex flex-col items-center gap-0.5 py-2.5 text-[11px] font-semibold ${
                    isActive ? 'text-brand-600' : 'text-gray-400'
                  }`
                }
              >
                <Icon size={22} />
                {label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </div>
  );
}
