import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Ban, Calendar, CheckCircle2, FileText, Mail, MapPin, Pencil, Phone, Star, Trash2, XCircle } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import BackLink from "../components/BackLink.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import Photo from "../components/Photo.jsx";
import InfoRow from "../components/InfoRow.jsx";
import OrdersTable from "../components/OrdersTable.jsx";
import SellerForm from "../components/SellerForm.jsx";
import ConfirmDialog from "../components/ConfirmDialog.jsx";
import ReasonDialog from "../components/ReasonDialog.jsx";
import { api } from "../api.js";
import { inr, useFetch } from "../utils.js";

export default function SellerDetail() {
  const { id } = useParams();
  const { data, error, loading, reload } = useFetch(`/api/sellers/${id}`);
  const [busy, setBusy] = useState(false);
  const [editing, setEditing] = useState(false);
  const [removing, setRemoving] = useState(false);
  const [statusError, setStatusError] = useState("");
  const [reasonTarget, setReasonTarget] = useState(null);
  const navigate = useNavigate();

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const { profile: s, orders } = data;

  const setStatus = async (value, note) => {
    setBusy(true);
    setStatusError("");
    try {
      await api.patch(`/api/sellers/${id}/status?value=${value}`, note ? { note } : undefined);
      reload();
    } catch (statusUpdateError) {
      setStatusError(statusUpdateError.message);
      throw statusUpdateError;
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <BackLink to="/sellers">All sellers</BackLink>
      {statusError && <ErrorBox message={statusError} />}

      <div className="card banner-card">
        <Photo src={s.image} alt={s.name} className="banner" />
        <div className="profile banner-profile">
          <div className="profile-main">
            <div className="profile-title">
              <h2>{s.name}</h2>
              <Badge value={s.status} />
              <span className={`open-pill inline ${s.open ? "open" : "closed"}`}>{s.open ? "Open now" : "Closed"}</span>
            </div>
            <div className="muted">
              {s.cuisine} · Seller ID {s.id}
              {s.rating > 0 && (
                <span className="rating inline-rating"><Star size={14} fill="currentColor" /> {s.rating.toFixed(1)}</span>
              )}
            </div>
            <div className="info-grid">
              <InfoRow icon={Phone} label="Phone">{s.phone}</InfoRow>
              <InfoRow icon={Mail} label="Email">{s.email}</InfoRow>
              <InfoRow icon={MapPin} label="Address">{s.address}, {s.city}</InfoRow>
              <InfoRow icon={FileText} label="FSSAI licence">{s.fssaiNumber}</InfoRow>
              <InfoRow icon={Calendar} label="Joined Foodie">{s.joinedOn}</InfoRow>
              {s.statusNote && <InfoRow icon={Ban} label="Status note">{s.statusNote}</InfoRow>}
            </div>
          </div>

          <div className="profile-actions">
            {s.status === "PENDING" && (
              <>
                <button className="btn btn-green" disabled={busy} onClick={() => setStatus("ACTIVE").catch(() => {})}>
                  <CheckCircle2 size={16} /> Approve restaurant
                </button>
                <button className="btn btn-danger" disabled={busy} onClick={() => setReasonTarget({
                  status: "SUSPENDED", title: "Reject seller application", confirmLabel: "Reject seller",
                })}>
                  <XCircle size={16} /> Reject
                </button>
              </>
            )}
            {s.status === "ACTIVE" && (
              <button className="btn btn-danger" disabled={busy} onClick={() => setReasonTarget({
                status: "SUSPENDED", title: "Suspend seller", confirmLabel: "Suspend seller",
              })}>
                <Ban size={16} /> Suspend restaurant
              </button>
            )}
            {s.status === "SUSPENDED" && (
              <button className="btn btn-green" disabled={busy} onClick={() => setStatus("ACTIVE").catch(() => {})}>
                <CheckCircle2 size={16} /> Reinstate restaurant
              </button>
            )}
            <button className="btn btn-outline" onClick={() => setEditing(true)}>
              <Pencil size={16} /> Edit seller
            </button>
            <button className="btn btn-outline btn-delete" onClick={() => setRemoving(true)}>
              <Trash2 size={16} /> Delete seller
            </button>
          </div>
        </div>
      </div>

      <div className="tiles">
        <div className="tile"><span>Total orders</span><strong>{s.totalOrders}</strong></div>
        <div className="tile"><span>Revenue</span><strong>{inr(s.revenue)}</strong></div>
        <div className="tile"><span>Menu items</span><strong>{s.menu.length}</strong></div>
        <div className="tile">
          <span>Owner</span>
          <strong className="owner-tile"><Avatar src={s.ownerAvatar} name={s.ownerName} size={28} /> {s.ownerName}</strong>
        </div>
      </div>

      <div className="card">
        <div className="card-head"><h2>Menu</h2></div>
        <div className="menu-grid">
          {s.menu.map((m) => (
            <div key={m.name} className="menu-item">
              <Photo src={m.image} alt={m.name} className="menu-photo" />
              <div className="menu-info">
                <div className="menu-name">
                  <span className={`veg-dot ${m.veg ? "veg" : "nonveg"}`} />
                  {m.name}
                </div>
                <div className="muted small">{m.category}</div>
                <div className="strong">{inr(m.price)}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="card">
        <div className="card-head"><h2>Recent orders</h2></div>
        <OrdersTable orders={orders} show={["customer", "rider"]} />
      </div>

      {reasonTarget && (
        <ReasonDialog
          title={reasonTarget.title}
          message="The seller will see this note when they try to sign in."
          confirmLabel={reasonTarget.confirmLabel}
          onClose={() => setReasonTarget(null)}
          onConfirm={async (note) => {
            await setStatus(reasonTarget.status, note);
            setReasonTarget(null);
          }}
        />
      )}
      {editing && (
        <SellerForm
          seller={s}
          onClose={() => setEditing(false)}
          onSaved={() => {
            setEditing(false);
            reload();
          }}
        />
      )}
      {removing && (
        <ConfirmDialog
          title="Delete seller"
          message={`Delete "${s.name}" and its menu from the database? This cannot be undone.`}
          onClose={() => setRemoving(false)}
          onConfirm={async () => {
            await api.delete(`/api/sellers/${id}`);
            navigate("/sellers", { replace: true });
          }}
        />
      )}
    </>
  );
}
