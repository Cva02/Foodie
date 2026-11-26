import { useCallback, useEffect, useRef, useState } from "react";
import {
  Bell,
  Bike,
  Check,
  CircleHelp,
  Clock3,
  Eye,
  EyeOff,
  History,
  LifeBuoy,
  Lock,
  LogOut,
  Mail,
  MapPinned,
  Moon,
  PackageCheck,
  Pencil,
  Power,
  Send,
  Star,
  Sun,
  X,
  Wallet,
} from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import FoodieBrand from "../components/FoodieBrand.jsx";
import { riderApi, useRiderAuth } from "../riderApi.js";
import { useToast } from "../context.jsx";
import { isValidServicePinCodePair, normalizeOtpInput } from "../utils/riderValidation.js";

const THEME_STORAGE_KEY = "foodie_rider_theme_v3";
const money = (amount) => `₹${Number(amount || 0).toLocaleString("en-IN")}`;
const prettyStatus = (status) => (status || "").replaceAll("_", " ").toLowerCase()
  .replace(/\b\w/g, (letter) => letter.toUpperCase());

function getInitialTheme() {
  try {
    const savedTheme = localStorage.getItem(THEME_STORAGE_KEY);
    if (savedTheme === "light" || savedTheme === "dark") return savedTheme;
  } catch (_) {
    // Fall back to light when storage is unavailable.
  }
  return "light";
}

function ThemeToggle({ theme, onToggle }) {
  const nextTheme = theme === "dark" ? "light" : "dark";
  return (
    <button
      type="button"
      className="rider-theme-toggle"
      onClick={onToggle}
      aria-label={`Switch to ${nextTheme} mode`}
      aria-pressed={theme === "dark"}
    >
      {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
      {theme === "dark" ? "Light" : "Dark"}
    </button>
  );
}

function DeliveryRoute({ order }) {
  const hasCoordinates = [
    order.sellerLatitude,
    order.sellerLongitude,
    order.customerLatitude,
    order.customerLongitude,
  ].every((coordinate) => coordinate != null && Number.isFinite(Number(coordinate)));
  const origin = hasCoordinates
    ? `${order.sellerLatitude},${order.sellerLongitude}`
    : order.sellerAddress;
  const destination = hasCoordinates
    ? `${order.customerLatitude},${order.customerLongitude}`
    : order.address;
  const routeUrl = new URL("https://www.google.com/maps/dir/");
  routeUrl.search = new URLSearchParams({
    api: "1",
    origin,
    destination,
    travelmode: "driving",
  }).toString();
  const embedUrl = new URL("https://maps.google.com/maps");
  embedUrl.search = new URLSearchParams({
    output: "embed",
    saddr: origin,
    daddr: destination,
  }).toString();

  return (
    <div className="rider-route">
      <div className="rider-route-heading">
        <MapPinned size={17} />
        <strong>Route to customer</strong>
      </div>
      {origin && destination ? (
        <>
          <iframe
            title={`Route from ${order.sellerName || "restaurant"} to customer`}
            src={embedUrl.toString()}
            loading="lazy"
            referrerPolicy="no-referrer-when-downgrade"
          />
          <a href={routeUrl.toString()} target="_blank" rel="noreferrer">
            Open directions in Google Maps
          </a>
        </>
      ) : (
        <p>Map directions are unavailable because this order has no pickup or delivery location.</p>
      )}
    </div>
  );
}

function OrderCard({ order, available = false, action, busy, onAction, onView, readOnly = false }) {
  const actionLabels = {
    accept: "Accept delivery",
    "picked-up": "Confirm pickup",
    dispatched: "Mark dispatched",
    delivered: "Mark delivered",
  };
  const nextAction = {
    RIDER_ASSIGNED: "picked-up",
    PICKED_UP: "dispatched",
    DISPATCHED: "delivered",
  }[order.status];
  const currentAction = action || (available ? "accept" : nextAction);

  return (
    <article className="portal-order-card">
      <div className="portal-order-top">
        <div>
          <span className="portal-order-id">{order.id}</span>
          <h3>{order.sellerName || "Restaurant"}</h3>
        </div>
        <div className="portal-order-price">
          <span className="portal-order-status">{prettyStatus(order.status)}</span>
          <span className="portal-order-amount">{money(order.amount)}</span>
        </div>
      </div>
      {order.items && <p className="portal-order-items">{order.items}</p>}
      <div className="portal-order-meta">
        <span><Clock3 size={14} />{order.placedAt || "Recently placed"}</span>
        {order.customerPinCode && <span className="portal-order-pin">PIN {order.customerPinCode}</span>}
        {order.distanceKm != null && <span>{Number(order.distanceKm).toFixed(1)} km away</span>}
      </div>
      <div className={`portal-order-actions portal-order-card-actions${available ? " rider-available-actions" : ""}`}>
        <button type="button" className="rider-view-order" onClick={() => onView(order)}>
          <Eye size={15} /> View
        </button>
        {available ? (
          <>
            <button
              type="button"
              className="portal-primary portal-action"
              onClick={() => onAction(order.id, "accept")}
              disabled={busy}
            >
              {busy ? "Updating…" : "Accept delivery"} <Bike size={16} />
            </button>
            <button
              type="button"
              className="rider-decline-action"
              onClick={() => onAction(order.id, "decline")}
              disabled={busy}
            >
              Decline for me
            </button>
          </>
        ) : currentAction && !readOnly ? (
          <button
            type="button"
            className="portal-primary portal-action"
            onClick={() => onAction(order.id, currentAction)}
            disabled={busy}
          >
            {busy ? "Updating…" : actionLabels[currentAction]}
            {currentAction === "picked-up" || currentAction === "delivered"
              ? <Check size={16} />
              : <Bike size={16} />}
          </button>
        ) : null}
      </div>
    </article>
  );
}

function OrderDetailsDialog({ order, onClose }) {
  return (
    <div className="rider-dialog-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget) onClose();
    }}>
      <section className="rider-dialog rider-order-dialog" role="dialog" aria-modal="true" aria-labelledby="order-dialog-title">
        <header className="rider-dialog-header">
          <div>
            <span className="portal-order-id">{order.id}</span>
            <h2 id="order-dialog-title">Order details</h2>
          </div>
          <button type="button" className="rider-dialog-close" onClick={onClose} aria-label="Close order details">
            <X size={19} />
          </button>
        </header>
        <div className="rider-order-detail-summary">
          <div><span>Restaurant</span><strong>{order.sellerName || "Restaurant"}</strong></div>
          <div><span>Status</span><strong>{prettyStatus(order.status)}</strong></div>
          <div><span>Order total</span><strong>{money(order.amount)}</strong></div>
          {order.payment && <div><span>Payment method</span><strong>{order.payment}</strong></div>}
          {order.placedAt && <div><span>Placed</span><strong>{order.placedAt}</strong></div>}
          {order.expectedDeliveryAt && <div><span>Estimated delivery</span><strong>{order.expectedDeliveryAt}</strong></div>}
          {order.deliveredAt && <div><span>Delivered</span><strong>{order.deliveredAt}</strong></div>}
          {order.customerPinCode && <div><span>Delivery PIN</span><strong>{order.customerPinCode}</strong></div>}
          {order.items && <div className="rider-order-detail-items"><span>Items</span><strong>{order.items}</strong></div>}
          {order.sellerAddress && <div className="rider-order-detail-address"><span>Pickup location</span><strong>{order.sellerAddress}</strong></div>}
          {order.address && <div className="rider-order-detail-address"><span>Delivery address</span><strong>{order.address}</strong></div>}
          {order.customerName && <div><span>Customer</span><strong>{order.customerName}</strong></div>}
          {order.customerPhone && (
            <div><span>Customer phone</span><strong><a href={`tel:${order.customerPhone}`}>{order.customerPhone}</a></strong></div>
          )}
        </div>
        {["PICKED_UP", "DISPATCHED"].includes(order.status) && <DeliveryRoute order={order} />}
      </section>
    </div>
  );
}

function RiderProfileDialog({ profile, busy, error, onClose, onSave }) {
  const [form, setForm] = useState(() => ({
    name: profile.name || "",
    phone: profile.phone || "",
    vehicle: profile.vehicle || "",
    vehicleNumber: profile.vehicleNumber || "",
    licenseNumber: profile.licenseNumber || "",
    city: profile.city || "",
    servicePinCodes: profile.servicePinCodes || "",
  }));
  const update = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));

  return (
    <div className="rider-dialog-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget) onClose();
    }}>
      <section className="rider-dialog" role="dialog" aria-modal="true" aria-labelledby="profile-dialog-title">
        <header className="rider-dialog-header">
          <div>
            <span className="portal-kicker">RIDER PROFILE</span>
            <h2 id="profile-dialog-title">Edit profile</h2>
          </div>
          <button type="button" className="rider-dialog-close" onClick={onClose} aria-label="Close profile editor">
            <X size={19} />
          </button>
        </header>
        <form className="rider-profile-form" onSubmit={(event) => {
          event.preventDefault();
          onSave(form);
        }}>
          <label className="portal-field">
            Full name
            <input autoComplete="name" value={form.name} onChange={update("name")} maxLength={100} required />
          </label>
          <label className="portal-field">
            Phone number
            <input type="tel" autoComplete="tel" value={form.phone} onChange={update("phone")} maxLength={20} required />
          </label>
          <label className="portal-field">
            Vehicle type
            <select value={form.vehicle} onChange={update("vehicle")} required>
              <option value="">Select vehicle type</option>
              <option value="Bike">Bike</option>
              <option value="Scooty">Scooty</option>
            </select>
          </label>
          <label className="portal-field">
            Vehicle registration number
            <input value={form.vehicleNumber} onChange={update("vehicleNumber")} maxLength={20} required />
          </label>
          <label className="portal-field">
            Driving licence number
            <input value={form.licenseNumber} onChange={update("licenseNumber")} maxLength={30} required />
          </label>
          <label className="portal-field">
            Service area
            <input value={form.city} onChange={update("city")} maxLength={100} required />
          </label>
          <label className="portal-field rider-profile-form-wide">
            Delivery PIN codes
            <input
              value={form.servicePinCodes}
              onChange={update("servicePinCodes")}
              placeholder="531162, 531163"
              pattern="[0-9]{6}[ ]*,[ ]*[0-9]{6}"
              title="Enter two six-digit PIN codes separated by a comma"
              required
            />
          </label>
          <p className="rider-profile-readonly">Email and registration documents can’t be changed here.</p>
          {error && <div className="portal-error rider-profile-form-wide" role="alert">{error}</div>}
          <div className="rider-dialog-actions rider-profile-form-wide">
            <button type="button" className="rider-secondary-button" onClick={onClose} disabled={busy}>Cancel</button>
            <button type="submit" className="portal-primary" disabled={busy}>
              {busy ? "Saving…" : "Save changes"}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

function OrderOtpDialog({ otpDialog, value, error, busy, onClose, onChange, onSubmit }) {
  const isPickup = otpDialog.action === "picked-up";
  const step = isPickup ? "pickup" : "delivery";

  return (
    <div className="rider-dialog-backdrop otp-dialog-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget && !busy) onClose();
    }}>
      <section className="rider-dialog rider-otp-dialog" role="dialog" aria-modal="true" aria-labelledby="otp-dialog-title">
        <header className="rider-dialog-header">
          <div>
            <span className="portal-kicker">ORDER VERIFICATION</span>
            <h2 id="otp-dialog-title">Enter {step} OTP</h2>
          </div>
          <button type="button" className="rider-dialog-close" onClick={onClose} aria-label="Close OTP dialog" disabled={busy}>
            <X size={19} />
          </button>
        </header>
        <div className="rider-otp-order-reference">
          <span>For this order</span>
          <strong>{otpDialog.orderId}</strong>
          {otpDialog.sellerName && <small>{otpDialog.sellerName}</small>}
        </div>
        <p className="rider-otp-instructions">
          Enter the six-digit {step} code for order <strong>{otpDialog.orderId}</strong>.
        </p>
        <form className="rider-otp-form" onSubmit={onSubmit}>
          <label className="portal-field">
            {isPickup ? "Pickup OTP" : "Delivery OTP"}
            <input
              type="text"
              inputMode="numeric"
              autoComplete="one-time-code"
              pattern="[0-9]{6}"
              maxLength={6}
              value={value}
              onChange={onChange}
              aria-describedby={error ? "rider-otp-error" : undefined}
              autoFocus
              required
            />
          </label>
          {error && <div id="rider-otp-error" className="portal-error" role="alert">{error}</div>}
          <div className="rider-dialog-actions">
            <button type="button" className="rider-secondary-button" onClick={onClose} disabled={busy}>Cancel</button>
            <button type="submit" className="portal-primary" disabled={busy || value.length !== 6}>
              {busy ? "Verifying…" : `Verify ${step}`}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

export default function RiderAccessPage() {
  const session = useRiderAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const accessFormRef = useRef(null);
  const [theme, setTheme] = useState(getInitialTheme);
  const [showPassword, setShowPassword] = useState(false);
  const [registering, setRegistering] = useState(false);
  const [registrationStep, setRegistrationStep] = useState(0);
  const [registrationStepError, setRegistrationStepError] = useState("");
  const [form, setForm] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
    vehicle: "",
    vehicleNumber: "",
    licenseNumber: "",
    city: "",
    servicePinCodes: "",
    bankName: "",
    accountHolderName: "",
    bankAccountNumber: "",
    bankIfsc: "",
  });
  const [documents, setDocuments] = useState({ vehiclePhoto: null, licenseDocument: null });
  const [profile, setProfile] = useState(null);
  const [orders, setOrders] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [dashboardPage, setDashboardPage] = useState("current");
  const [historyView, setHistoryView] = useState("accepted");
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [profileEditorOpen, setProfileEditorOpen] = useState(false);
  const [profileSaveBusy, setProfileSaveBusy] = useState(false);
  const [profileEditError, setProfileEditError] = useState("");
  const [otpDialog, setOtpDialog] = useState(null);
  const [otpValue, setOtpValue] = useState("");
  const [otpError, setOtpError] = useState("");
  const [supportTickets, setSupportTickets] = useState([]);
  const [supportSubject, setSupportSubject] = useState("");
  const [supportMessage, setSupportMessage] = useState("");
  const [supportBusy, setSupportBusy] = useState(false);
  const [supportError, setSupportError] = useState("");
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [busyAction, setBusyAction] = useState("");
  const [error, setError] = useState("");
  const [locationNotice, setLocationNotice] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const isRider = session.loggedIn;

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try {
      localStorage.setItem(THEME_STORAGE_KEY, theme);
    } catch (_) {
      // Theme still applies for this session when storage is unavailable.
    }
  }, [theme]);

  const refresh = useCallback(async () => {
    try {
      const [rider, riderOrders, tickets, riderNotifications] = await Promise.all([
        riderApi.getProfile(),
        riderApi.getOrders(),
        riderApi.getSupportTickets(),
        riderApi.getNotifications(),
      ]);
      setProfile(rider);
      setOrders(riderOrders);
      setSupportTickets(tickets);
      setNotifications(riderNotifications);
      setError("");
    } catch (requestError) {
      setError(requestError.message);
    }
  }, []);

  useEffect(() => {
    if (!isRider) {
      setProfile(null);
      setOrders([]);
      setSupportTickets([]);
      setNotifications([]);
      setNotificationsOpen(false);
      setDashboardPage("current");
      setSelectedOrder(null);
      setProfileEditorOpen(false);
      setOtpDialog(null);
      setLocationNotice("");
      return undefined;
    }

    let active = true;
    const initialLoad = async () => {
      if (active) setLoading(true);
      await refresh();
      if (active) setLoading(false);
    };
    initialLoad();
    const timer = window.setInterval(() => {
      if (active) refresh();
    }, 8000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [isRider, refresh]);

  useEffect(() => {
    if (!isRider || !profile || !["ONLINE", "ON_DELIVERY"].includes((profile.status || "").toUpperCase())) {
      setLocationNotice("");
      return undefined;
    }
    if (!navigator.geolocation) {
      setLocationNotice("Browser GPS is unavailable; nearby order sorting uses your saved PIN codes.");
      return undefined;
    }

    let active = true;
    let lastSentAt = 0;
    const watchId = navigator.geolocation.watchPosition((position) => {
      const now = Date.now();
      if (now - lastSentAt < 10000) return;
      lastSentAt = now;
      riderApi.updateLocation(position.coords.latitude, position.coords.longitude)
        .then(() => { if (active) setLocationNotice(""); })
        .catch(() => {});
    }, (positionError) => {
      if (active) {
        setLocationNotice(positionError.code === 1
          ? "Location access is off — allow location access to share live GPS tracking."
          : "GPS signal unavailable — using your registered service PIN codes.");
      }
    }, { enableHighAccuracy: true, maximumAge: 10000, timeout: 20000 });

    return () => {
      active = false;
      navigator.geolocation.clearWatch(watchId);
    };
  }, [isRider, profile?.status]);

  const updateForm = (field) => (event) => {
    setForm((current) => ({ ...current, [field]: event.target.value }));
    if (registrationStepError && accessFormRef.current?.checkValidity()) {
      setRegistrationStepError("");
    }
  };

  const updateDocument = (field) => (event) => {
    const file = event.target.files?.[0] || null;
    if (file && file.size > 5 * 1024 * 1024) {
      setError("Each uploaded file must be 5 MB or smaller.");
      event.target.value = "";
      return;
    }
    setError("");
    setDocuments((current) => ({ ...current, [field]: file }));
    if (registrationStepError && accessFormRef.current?.checkValidity()) {
      setRegistrationStepError("");
    }
  };

  const toggleTheme = () => setTheme((current) => (current === "dark" ? "light" : "dark"));

  const goToNextRegistrationStep = () => {
    const formElement = accessFormRef.current;
    if (!formElement) return;
    if (!formElement.reportValidity()) {
      setRegistrationStepError("Please correct the invalid or missing fields in this step.");
      return;
    }
    setError("");
    setRegistrationStepError("");
    setRegistrationStep((current) => Math.min(current + 1, 2));
  };

  const submitAccess = async (event) => {
  event.preventDefault();
  setBusy(true);
  setError("");
  setSuccessMessage("");

  try {
    if (registering) {
      if (!documents.vehiclePhoto || !documents.licenseDocument) {
        throw new Error("Upload a vehicle photo and driving licence to register.");
      }

      const registration = new FormData();
      registration.append(
        "rider",
        new Blob([JSON.stringify(form)], { type: "application/json" }),
      );
      registration.append("vehiclePhoto", documents.vehiclePhoto);
      registration.append("licenseDocument", documents.licenseDocument);

      const result = await riderApi.register(registration);

      setSuccessMessage(
        result.message || "Your Rider application has been submitted successfully. Please wait for Admin approval before signing in.",
      );
      toast("Rider application submitted! Wait for Admin approval.", "ok");
      setRegistering(false);
      setRegistrationStep(0);
      setDocuments({ vehiclePhoto: null, licenseDocument: null });
      setForm((current) => ({
        ...current,
        password: "",
        bankName: "",
        accountHolderName: "",
        bankAccountNumber: "",
        bankIfsc: "",
      }));
    } else {
      await riderApi.login(form.email, form.password);
      toast("Signed in to Rider portal", "ok");
      setForm((current) => ({ ...current, password: "" }));
      setSuccessMessage("");
    }
  } catch (requestError) {
    setError(requestError.message);
  } finally {
    setBusy(false);
  }
};

  const runOrderAction = async (orderId, action, providedOtp) => {
    const requiresOtp = action === "picked-up" || action === "delivered";
    if (requiresOtp && providedOtp === undefined) {
      const order = orders.find((item) => item.id === orderId);
      setOtpDialog({
        orderId,
        action,
        sellerName: order?.sellerName || "",
      });
      setOtpValue("");
      setOtpError("");
      return;
    }

    const otp = providedOtp?.trim();
    if (requiresOtp && !/^\d{6}$/.test(otp || "")) {
      setOtpError("Enter a valid six-digit OTP.");
      return;
    }

    setBusyAction(orderId);
    setError("");
    try {
      await riderApi.performOrderAction(orderId, action, requiresOtp ? otp : undefined);
      if (requiresOtp) {
        setOtpDialog(null);
        setOtpValue("");
        setOtpError("");
      }
      toast("Delivery status updated", "ok");
      await refresh();
    } catch (requestError) {
      if (requiresOtp) setOtpError(requestError.message);
      else setError(requestError.message);
    } finally {
      setBusyAction("");
    }
  };

  const submitOrderOtp = (event) => {
    event.preventDefault();
    if (otpDialog) runOrderAction(otpDialog.orderId, otpDialog.action, otpValue);
  };

  const openNotification = async (notification) => {
    setDashboardPage("history");
    setHistoryView(notification.type === "ORDER_DELIVERED" ? "delivered" : "accepted");
    setNotificationsOpen(false);

    if (!notification.readAt) {
      try {
        const updated = await riderApi.markNotificationRead(notification.id);
        setNotifications((current) => current.filter((item) => item.id !== updated.id));
      } catch (requestError) {
        setError(requestError.message);
      }
    }
  };

  const toggleAvailability = async () => {
    if (!profile) return;
    const isCurrentlyOnline = (profile.status || "").trim().toUpperCase() === "ONLINE";
    const nextStatus = isCurrentlyOnline ? "OFFLINE" : "ONLINE";
    setBusyAction("availability");
    setError("");
    try {
      const updated = await riderApi.updateStatus(nextStatus);
      setProfile(updated);
      toast(
        nextStatus === "ONLINE"
          ? "You are now ONLINE and ready to accept orders."
          : "You are now OFFLINE.",
        "ok"
      );
      await refresh();
    } catch (requestError) {
      const msg = requestError.message || "Unable to update status.";
      setError(msg);
      toast(msg, "error");
    } finally {
      setBusyAction("");
    }
  };

  const saveRiderProfile = async (updatedProfile) => {
    setProfileSaveBusy(true);
    setProfileEditError("");
    try {
      const savedProfile = await riderApi.updateProfile(updatedProfile);
      setProfile(savedProfile);
      setProfileEditorOpen(false);
      toast("Rider profile updated", "ok");
    } catch (requestError) {
      setProfileEditError(requestError.message);
    } finally {
      setProfileSaveBusy(false);
    }
  };

  const submitSupportTicket = async (event) => {
    event.preventDefault();
    setSupportBusy(true);
    setSupportError("");
    try {
      const ticket = await riderApi.createSupportTicket(supportSubject, supportMessage);
      setSupportTickets((current) => [ticket, ...current]);
      setSupportSubject("");
      setSupportMessage("");
      toast("Support request submitted", "ok");
    } catch (requestError) {
      setSupportError(requestError.message);
    } finally {
      setSupportBusy(false);
    }
  };

  if (isRider) {
    const currentDeliveries = orders.filter((order) =>
      order.riderId === profile?.id && !["DELIVERED", "REJECTED"].includes(order.status));
    const availableOrders = orders.filter((order) => order.status === "READY" && !order.riderId);
    const acceptedHistory = orders.filter((order) =>
      order.riderId === profile?.id
      && ["RIDER_ASSIGNED", "PICKED_UP", "DISPATCHED", "DELIVERED"].includes(order.status));
    const deliveredHistory = orders.filter((order) =>
      order.riderId === profile?.id && order.status === "DELIVERED");
    const online = (profile?.status || "").toUpperCase() === "ONLINE";
    const onDelivery = (profile?.status || "").toUpperCase() === "ON_DELIVERY";
    const unreadNotifications = notifications.filter((notification) => !notification.readAt).length;
    const historyOrders = historyView === "accepted" ? acceptedHistory : deliveredHistory;
    const dashboardNavigation = [
      { id: "current", label: "Current delivery", icon: PackageCheck, count: currentDeliveries.length },
      { id: "available", label: "Available orders", icon: Bike, count: availableOrders.length },
      { id: "history", label: "History", icon: History, count: acceptedHistory.length },
      { id: "support", label: "Help & support", icon: LifeBuoy },
    ];

    return (
      <div className="portal-app rider-app">
        <header className="portal-topbar">
          <div className="rider-topbar-brand">
            <FoodieBrand logoSize={32} />
            <span className="rider-sidebar-role">Rider</span>
          </div>
          <div className="portal-top-right">
            <span className="portal-user">{profile?.name || session.name}</span>
            <button
              type="button"
              className="rider-profile-trigger"
              onClick={() => {
                setProfileEditError("");
                setProfileEditorOpen(true);
              }}
              disabled={!profile}
            >
              <Pencil size={15} /> <span>Edit profile</span>
            </button>
            <div className="rider-notification-wrap">
              <button
                type="button"
                className="rider-notification-trigger"
                onClick={() => setNotificationsOpen((open) => !open)}
                aria-label={`Notifications${unreadNotifications ? `, ${unreadNotifications} unread` : ""}`}
                aria-expanded={notificationsOpen}
                aria-controls="rider-notification-panel"
              >
                <Bell size={17} />
                <span className="rider-notification-label">Notifications</span>
                {unreadNotifications > 0 && <span className="rider-notification-count">{unreadNotifications}</span>}
              </button>
              {notificationsOpen && (
                <div id="rider-notification-panel" className="rider-notification-panel" role="region" aria-label="Notifications">
                  <strong className="rider-notification-heading">Notifications</strong>
                  {notifications.length > 0 ? notifications.map((notification) => (
                    <button
                      key={notification.id}
                      type="button"
                      className={`rider-notification-item${notification.readAt ? "" : " is-unread"}`}
                      onClick={() => openNotification(notification)}
                    >
                      <span>{notification.type === "ORDER_DELIVERED" ? "Delivery completed" : "Order accepted"}</span>
                      <span>{notification.message}</span>
                      <time dateTime={notification.createdAt}>{new Date(notification.createdAt).toLocaleString()}</time>
                    </button>
                  )) : <p className="rider-notification-empty">No notifications yet.</p>}
                </div>
              )}
            </div>
            <ThemeToggle theme={theme} onToggle={toggleTheme} />
            <button type="button" className="portal-logout" onClick={() => {
              riderApi.logout();
              toast("Signed out of Rider portal", "ok");
              navigate("/rider", { replace: true });
            }}>
              <LogOut size={15} /> Log out
            </button>
          </div>
        </header>
        <div className="rider-shell-body">
        <aside className="rider-sidebar">
          <nav className="rider-sidebar-nav" aria-label="Rider dashboard">
            {dashboardNavigation.map(({ id, label, icon: Icon, count }) => (
              <button
                key={id}
                type="button"
                className={`rider-sidebar-link${dashboardPage === id ? " is-active" : ""}`}
                onClick={() => setDashboardPage(id)}
                aria-current={dashboardPage === id ? "page" : undefined}
              >
                <Icon size={18} />
                <span>{label}</span>
                {count > 0 && <span className="rider-sidebar-count">{count}</span>}
              </button>
            ))}
          </nav>
          <div className="rider-sidebar-profile">
            <div className="rider-sidebar-avatar"><Bike size={18} /></div>
            <div className="rider-sidebar-profile-name">{profile?.name || session.name}</div>
            <span>{profile?.city || "Service area not set"}</span>
            {profile?.servicePinCodes && <small>PINs {profile.servicePinCodes}</small>}
          </div>
        </aside>
        <main className="portal-content rider-dashboard">
          <section className="portal-welcome">
            <div>
              <span className="portal-kicker">RIDER DASHBOARD</span>
              <h1>Ready when you are, {profile?.name || session.name}</h1>
              <p>Stay on top of deliveries, order history and support.</p>
            </div>
            <div className="portal-welcome-art"><Bike size={31} /></div>
          </section>

          {error && <div className="portal-error portal-inline-error" role="alert">{error}</div>}
          {locationNotice && <div className="rider-location-notice" role="status">{locationNotice}</div>}

          <section className="rider-dashboard-summary">
            <div className={`rider-status-card ${online ? "is-online" : onDelivery ? "is-busy" : "is-offline"}`}>
              <span className={`rider-status-dot ${online ? "is-online" : onDelivery ? "is-busy" : ""}`} />
              <div>
                <span className="rider-summary-label">Your status</span>
                <strong>{prettyStatus(profile?.status || "OFFLINE")}</strong>
              </div>
              <button
                type="button"
                className={`portal-primary rider-status-toggle ${online ? "is-online" : ""}`}
                onClick={toggleAvailability}
                disabled={!profile || onDelivery || busyAction === "availability"}
                title={onDelivery ? "Complete your active delivery before going offline" : undefined}
              >
                <Power size={16} />
                {busyAction === "availability"
                  ? "Updating…"
                  : onDelivery
                    ? "On delivery"
                    : online
                      ? "Go offline"
                      : "Go online"}
              </button>
            </div>
            <div className="rider-earnings-card">
              <div className="portal-login-icon"><Wallet size={21} /></div>
              <div><span className="rider-summary-label">Total earnings</span><strong>{money(profile?.earnings)}</strong></div>
            </div>
            <div className="rider-stat-card">
              <div className="portal-login-icon"><Star size={20} /></div>
              <div>
                <span className="rider-summary-label">Rider rating</span>
                <strong>
                  {Number(profile?.rating) > 0 ? `${Number(profile.rating).toFixed(1)} / 5` : "Not rated yet"}
                </strong>
              </div>
            </div>
            <div className="rider-stat-card">
              <div className="portal-login-icon"><PackageCheck size={20} /></div>
              <div>
                <span className="rider-summary-label">Total deliveries</span>
                <strong>{profile?.totalDeliveries ?? 0}</strong>
              </div>
            </div>
          </section>

          {loading && !profile ? (
            <p className="rider-loading">Loading your rider dashboard…</p>
          ) : (
            <div className="rider-dashboard-panel">
                {dashboardPage === "current" && (
                  <section className="rider-page-section">
                    <div className="portal-section-head">
                      <div><h2>Current delivery</h2><p>Manage the pickup and delivery steps for your active orders.</p></div>
                      <span className="portal-count">{currentDeliveries.length} active</span>
                    </div>
                    {currentDeliveries.length > 0 ? (
                      <div className="portal-order-grid is-scrollable">
                        {currentDeliveries.map((order) => (
                          <OrderCard
                            key={order.id}
                            order={order}
                            busy={busyAction === order.id}
                            onAction={runOrderAction}
                            onView={setSelectedOrder}
                          />
                        ))}
                      </div>
                    ) : <div className="rider-empty">You don’t have an active delivery.</div>}
                  </section>
                )}

                {dashboardPage === "available" && (
                  <section className="rider-page-section">
                    <div className="portal-section-head">
                      <div><h2>Available orders</h2><p>Nearby orders are sorted by restaurant distance and your service PIN codes.</p></div>
                      <span className="portal-count">{availableOrders.length} available</span>
                    </div>
                    {!online && !onDelivery ? (
                      <div className="rider-empty">Go online to see available orders.</div>
                    ) : onDelivery ? (
                      <div className="rider-empty">Complete your current delivery to accept another order.</div>
                    ) : availableOrders.length > 0 ? (
                      <div className="portal-order-grid is-scrollable">
                        {availableOrders.map((order) => (
                          <OrderCard
                            key={order.id}
                            order={order}
                            available
                            busy={busyAction === order.id}
                            onAction={runOrderAction}
                            onView={setSelectedOrder}
                          />
                        ))}
                      </div>
                    ) : <div className="rider-empty">No ready orders right now.</div>}
                  </section>
                )}

                {dashboardPage === "history" && (
                  <section className="rider-page-section">
                    <div className="portal-section-head">
                      <div><h2>Order history</h2><p>Review orders you have accepted or delivered.</p></div>
                    </div>
                    <div className="rider-history-tabs" role="tablist" aria-label="Order history">
                      <button
                        type="button"
                        role="tab"
                        aria-selected={historyView === "accepted"}
                        className={historyView === "accepted" ? "is-active" : ""}
                        onClick={() => setHistoryView("accepted")}
                      >
                        Accepted orders <span>{acceptedHistory.length}</span>
                      </button>
                      <button
                        type="button"
                        role="tab"
                        aria-selected={historyView === "delivered"}
                        className={historyView === "delivered" ? "is-active" : ""}
                        onClick={() => setHistoryView("delivered")}
                      >
                        Delivered orders <span>{deliveredHistory.length}</span>
                      </button>
                    </div>
                    {historyOrders.length > 0 ? (
                      <div className="portal-order-grid is-scrollable">
                        {historyOrders.map((order) => (
                          <OrderCard key={order.id} order={order} readOnly onView={setSelectedOrder} />
                        ))}
                      </div>
                    ) : (
                      <div className="rider-empty">
                        {historyView === "accepted"
                          ? "Orders you accept will appear here."
                          : "Completed deliveries will appear here."}
                      </div>
                    )}
                  </section>
                )}

                {dashboardPage === "support" && (
                  <section className="rider-support-card" aria-labelledby="rider-support-title">
                    <div className="portal-section-head">
                      <div>
                        <h2 id="rider-support-title"><CircleHelp size={20} /> Help &amp; support</h2>
                        <p>Send a request to the support team and track its status here.</p>
                      </div>
                    </div>
                    <div className="rider-support-layout">
                      <form className="rider-support-form" onSubmit={submitSupportTicket}>
                        {supportError && <div className="portal-error" role="alert">{supportError}</div>}
                        <label className="portal-field">
                          Subject
                          <input
                            value={supportSubject}
                            onChange={(event) => setSupportSubject(event.target.value)}
                            maxLength={120}
                            required
                          />
                        </label>
                        <label className="portal-field">
                          How can we help?
                          <textarea
                            value={supportMessage}
                            onChange={(event) => setSupportMessage(event.target.value)}
                            maxLength={2000}
                            rows={4}
                            required
                          />
                        </label>
                        <button type="submit" className="portal-primary" disabled={supportBusy}>
                          <Send size={15} /> {supportBusy ? "Submitting…" : "Submit support request"}
                        </button>
                      </form>
                      <div className="rider-support-history">
                        <h3>Your requests</h3>
                        {supportTickets.length ? supportTickets.map((ticket) => (
                          <article className="rider-support-ticket" key={ticket.id}>
                            <div><strong>{ticket.subject}</strong><span>{ticket.status}</span></div>
                            <p>{ticket.message}</p>
                            <time dateTime={ticket.createdAt}>
                              {new Date(ticket.createdAt).toLocaleString()}
                            </time>
                          </article>
                        )) : <p className="portal-muted">You haven’t submitted a support request yet.</p>}
                      </div>
                    </div>
                  </section>
                )}
            </div>
          )}
        </main>
        </div>
        {selectedOrder && <OrderDetailsDialog order={selectedOrder} onClose={() => setSelectedOrder(null)} />}
        {profileEditorOpen && profile && (
          <RiderProfileDialog
            profile={profile}
            busy={profileSaveBusy}
            error={profileEditError}
            onClose={() => setProfileEditorOpen(false)}
            onSave={saveRiderProfile}
          />
        )}
        {otpDialog && (
          <OrderOtpDialog
            otpDialog={otpDialog}
            value={otpValue}
            error={otpError}
            busy={busyAction === otpDialog.orderId}
            onClose={() => {
              setOtpDialog(null);
              setOtpValue("");
              setOtpError("");
            }}
            onChange={(event) => {
              setOtpValue(normalizeOtpInput(event.target.value));
              if (otpError) setOtpError("");
            }}
            onSubmit={submitOrderOtp}
          />
        )}
      </div>
    );
  }

  return (
    <div className={`auth-page auth-page-login rider-auth ${registering ? "is-registering" : ""}`}>
      <aside className="auth-side portal-rider">
        <FoodieBrand className="auth-brand" subtitle="Delivery Partner" />
        <span className="auth-side-kicker">FOODIE · RIDER</span>
        <h1>Welcome,<br />Rider.</h1>
        <p>Rider delivery console</p>
        <span className="auth-side-note">Go online, accept nearby orders, verify OTP pickups and track your daily earnings.</span>
      </aside>
      <main className="auth-main rider-auth-main">
        <div className="portal-auth-toolbar">
          <ThemeToggle theme={theme} onToggle={toggleTheme} />
        </div>
        <form
          ref={accessFormRef}
          className={`auth-card ${registering ? "rider-registration-card" : ""}${registrationStepError ? " has-step-error" : ""}`}
          onSubmit={submitAccess}
        >
          <div className="auth-card-eyebrow"><span /> {registering ? "RIDER REGISTRATION" : "SECURE SIGN IN"}</div>
          <h2>{registering ? "Create rider account" : "Rider login"}</h2>
          <p className="muted">
            {registering
              ? "Complete the three steps to apply as a delivery partner."
              : "Sign in to go online and manage deliveries."}
          </p>
          {registering ? (
            <>
              <ol className="rider-registration-progress" aria-label="Registration steps">
                {["Personal", "Vehicle", "Area & bank"].map((label, index) => (
                  <li
                    key={label}
                    className={index === registrationStep ? "is-current" : index < registrationStep ? "is-complete" : ""}
                    aria-current={index === registrationStep ? "step" : undefined}
                  >
                    <span>{index + 1}</span>{label}
                  </li>
                ))}
              </ol>
              <p className="rider-registration-required-note">All fields are required. Invalid values are checked before moving to the next step.</p>
              <div className="rider-registration-fields">
                {registrationStep === 0 && (
                  <>
                    <label className="portal-field">
                      Full name
                      <input
                        autoComplete="name"
                        value={form.name}
                        onChange={updateForm("name")}
                        maxLength={100}
                        required
                      />
                    </label>
                    <label className="portal-field">
                      Email address
                      <input
                        type="email"
                        autoComplete="username"
                        value={form.email}
                        onChange={updateForm("email")}
                        maxLength={255}
                        required
                      />
                    </label>
                    <label className="portal-field">
                      Phone number
                      <input
                        type="tel"
                        autoComplete="tel"
                        value={form.phone}
                        onChange={updateForm("phone")}
                        pattern="[+0-9 ()-]{7,20}"
                        title="Enter a phone number using 7 to 20 digits or phone symbols."
                        maxLength={20}
                        required
                      />
                    </label>
                    <label className="portal-field">
                      Password
                      <input
                        type="password"
                        autoComplete="new-password"
                        minLength={8}
                        maxLength={100}
                        value={form.password}
                        onChange={updateForm("password")}
                        required
                      />
                    </label>
                  </>
                )}
                {registrationStep === 1 && (
                  <>
                    <label className="portal-field">
                      Vehicle type
                      <select value={form.vehicle} onChange={updateForm("vehicle")} required>
                        <option value="">Select vehicle type</option>
                        <option value="Bike">Bike</option>
                        <option value="Scooty">Scooty</option>
                      </select>
                    </label>
                    <label className="portal-field">
                      Vehicle registration number
                      <input
                        value={form.vehicleNumber}
                        onChange={updateForm("vehicleNumber")}
                        maxLength={20}
                        autoCapitalize="characters"
                        pattern="[A-Za-z0-9 -]{4,20}"
                        title="Enter 4 to 20 letters, numbers, spaces, or hyphens."
                        required
                      />
                    </label>
                    <label className="portal-field">
                      Vehicle photo (max 5 MB)
                      <input
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        onChange={updateDocument("vehiclePhoto")}
                        required={!documents.vehiclePhoto}
                      />
                    </label>
                    <label className="portal-field">
                      Driving licence number
                      <input
                        value={form.licenseNumber}
                        onChange={updateForm("licenseNumber")}
                        maxLength={30}
                        pattern="[A-Za-z0-9/-]{5,30}"
                        title="Enter 5 to 30 letters, numbers, slashes, or hyphens."
                        required
                      />
                    </label>
                    <label className="portal-field">
                      Licence document (max 5 MB)
                      <input
                        type="file"
                        accept="application/pdf,image/jpeg,image/png"
                        onChange={updateDocument("licenseDocument")}
                        required={!documents.licenseDocument}
                      />
                    </label>
                  </>
                )}
                {registrationStep === 2 && (
                  <>
                    <label className="portal-field">
                      Service area
                      <input value={form.city} onChange={updateForm("city")} maxLength={100} required />
                    </label>
                    <label className="portal-field">
                      Delivery PIN codes (enter two different codes, separated by a comma)
                      <input
                        value={form.servicePinCodes}
                        placeholder="531162, 531163"
                        maxLength={14}
                        pattern="[0-9]{6}[ ]*,[ ]*[0-9]{6}"
                        title="Enter two different six-digit PIN codes separated by a comma."
                        onChange={(event) => {
                          event.target.setCustomValidity(
                            event.target.value && !isValidServicePinCodePair(event.target.value)
                              ? "Enter two different delivery PIN codes."
                              : "",
                          );
                          updateForm("servicePinCodes")(event);
                        }}
                        required
                      />
                    </label>
                    <label className="portal-field">
                      Bank name
                      <input value={form.bankName} onChange={updateForm("bankName")} maxLength={100} required />
                    </label>
                    <label className="portal-field">
                      Account holder name
                      <input
                        value={form.accountHolderName}
                        onChange={updateForm("accountHolderName")}
                        maxLength={100}
                        autoComplete="name"
                        required
                      />
                    </label>
                    <label className="portal-field">
                      Bank account number
                      <input
                        type="text"
                        inputMode="numeric"
                        autoComplete="off"
                        value={form.bankAccountNumber}
                        onChange={updateForm("bankAccountNumber")}
                        maxLength={20}
                        pattern="[0-9]{6,20}"
                        title="Enter a bank account number using 6 to 20 digits."
                        required
                      />
                    </label>
                    <label className="portal-field">
                      IFSC code
                      <input
                        value={form.bankIfsc}
                        onChange={updateForm("bankIfsc")}
                        maxLength={11}
                        autoCapitalize="characters"
                        autoComplete="off"
                        pattern="[A-Za-z]{4}0[A-Za-z0-9]{6}"
                        title="Enter a valid 11-character IFSC code."
                        required
                      />
                    </label>
                  </>
                )}
              </div>
              {registrationStepError && (
                <div className="portal-error rider-registration-step-error" role="alert">
                  {registrationStepError}
                </div>
              )}
              <div className="rider-registration-actions">
                {registrationStep > 0 && (
                  <button
                    type="button"
                    className="rider-registration-back"
                    onClick={() => {
                      setError("");
                      setRegistrationStepError("");
                      setRegistrationStep((current) => current - 1);
                    }}
                  >
                    Back
                  </button>
                )}
                {registrationStep < 2 ? (
                  <button
                    type="button"
                    className="portal-primary rider-registration-next"
                    onClick={goToNextRegistrationStep}
                  >
                    Next
                  </button>
                ) : (
                  <button type="submit" className="btn btn-danger btn-block rider-registration-next" disabled={busy}>
                    {busy ? "Submitting…" : "Create account"}
                  </button>
                )}
              </div>
              {successMessage && <div className="portal-notice" role="status">{successMessage}</div>}
              {error && <div className="form-error" role="alert">{error}</div>}
              <button
                type="button"
                className="password-reset-link"
                onClick={() => {
                  setRegistering(false);
                  setRegistrationStep(0);
                  setRegistrationStepError("");
                  setError("");
                }}
              >
                Already registered? Sign in
              </button>
            </>
          ) : (
            <>
              <label className="field">
                <span className="field-label">Email address</span>
                <span className="input-icon">
                  <Mail size={16} />
                  <input
                    type="email"
                    autoComplete="username"
                    placeholder="Rider email"
                    value={form.email}
                    onChange={updateForm("email")}
                    required
                  />
                </span>
              </label>
              <label className="field">
                <span className="field-label">Password</span>
                <span className="input-icon">
                  <Lock size={16} />
                  <input
                    type={showPassword ? "text" : "password"}
                    autoComplete="current-password"
                    placeholder="Password"
                    value={form.password}
                    onChange={updateForm("password")}
                    required
                  />
                  <button
                    type="button"
                    className="eye"
                    onClick={() => setShowPassword((value) => !value)}
                    aria-label={showPassword ? "Hide password" : "Show password"}
                  >
                    {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </span>
              </label>
              {successMessage && <div className="portal-notice" role="status">{successMessage}</div>}
              {error && <div className="form-error" role="alert">{error}</div>}
              <button type="submit" className="btn btn-danger btn-block" disabled={busy || !form.email || !form.password}>
                {busy ? "Signing in…" : "Log in"}
              </button>
              <div className="portal-auth-links">
                <button
                  type="button"
                  className="password-reset-link"
                  onClick={() => {
                    setRegistering(true);
                    setRegistrationStep(0);
                    setRegistrationStepError("");
                    setError("");
                  }}
                >
                  New rider? Create an account
                </button>
              </div>
              <div className="admin-portal-links">
                <span>Switch portal</span>
                <Link to="/login">Customer</Link>
                <Link to="/seller/login">Seller</Link>
                <Link to="/admin/login">Admin</Link>
              </div>
            </>
          )}
        </form>
      </main>
    </div>
  );
}



