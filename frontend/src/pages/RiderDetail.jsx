import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Ban, Bike, Calendar, CheckCircle2, CreditCard, FileCheck2, Mail, MapPin, Pencil, Phone, Star, Trash2, XCircle,
} from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import BackLink from "../components/BackLink.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import InfoRow from "../components/InfoRow.jsx";
import OrdersTable from "../components/OrdersTable.jsx";
import RiderForm from "../components/RiderForm.jsx";
import ConfirmDialog from "../components/ConfirmDialog.jsx";
import ReasonDialog from "../components/ReasonDialog.jsx";
import { api } from "../api.js";
import { inr, useFetch } from "../utils.js";

export default function RiderDetail() {
  const { id } = useParams();
  const { data, error, loading, reload } = useFetch(`/api/riders/${id}`);
  const [busy, setBusy] = useState(false);
  const [editing, setEditing] = useState(false);
  const [removing, setRemoving] = useState(false);
  const [statusError, setStatusError] = useState("");
  const [reasonTarget, setReasonTarget] = useState(null);
  const navigate = useNavigate();

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const { profile: r, orders } = data;

  const setStatus = async (value, note) => {
    setBusy(true);
    setStatusError("");
    try {
      await api.patch(`/api/riders/${id}/status?value=${value}`, note ? { note } : undefined);
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
      <BackLink to="/admin/riders">All riders</BackLink>
      {statusError && <ErrorBox message={statusError} />}

      <div className="card profile">
        <Avatar src={r.avatar} name={r.name} size={96} />
        <div className="profile-main">
          <div className="profile-title">
            <h2>{r.name}</h2>
            <Badge value={r.status} />
          </div>
          <div className="muted">
            Rider ID · {r.id}
            {r.rating > 0 && (
              <span className="rating inline-rating"><Star size={14} fill="currentColor" /> {r.rating.toFixed(1)}</span>
            )}
          </div>
          <div className="info-grid">
            <InfoRow icon={Phone} label="Phone">{r.phone}</InfoRow>
            <InfoRow icon={Mail} label="Email">{r.email}</InfoRow>
            <InfoRow icon={MapPin} label="Service area">{r.city}</InfoRow>
            <InfoRow icon={Calendar} label="Joined Foodie">{r.joinedOn}</InfoRow>
            <InfoRow icon={Bike} label="Vehicle">{r.vehicle} · {r.vehicleNumber}</InfoRow>
            <InfoRow icon={CreditCard} label="Driving licence">{r.licenseNumber}</InfoRow>
            {r.statusNote && <InfoRow icon={FileCheck2} label="Status note">{r.statusNote}</InfoRow>}
          </div>
        </div>

        <div className="profile-actions">
          {r.status === "PENDING" && (
            <>
              <button className="btn btn-green" disabled={busy} onClick={() => setStatus("OFFLINE").catch(() => {})}>
                <CheckCircle2 size={16} /> Approve rider
              </button>
              <button className="btn btn-danger" disabled={busy} onClick={() => setReasonTarget({
                status: "REJECTED", title: "Reject rider application", confirmLabel: "Reject rider",
              })}>
                <XCircle size={16} /> Reject application
              </button>
            </>
          )}
          {(r.status === "ONLINE" || r.status === "ON_DELIVERY" || r.status === "OFFLINE") && (
            <button className="btn btn-danger" disabled={busy} onClick={() => setReasonTarget({
              status: "SUSPENDED", title: "Suspend rider", confirmLabel: "Suspend rider",
            })}>
              <Ban size={16} /> Suspend rider
            </button>
          )}
          {(r.status === "SUSPENDED" || r.status === "REJECTED") && (
            <button className="btn btn-green" disabled={busy} onClick={() => setStatus("OFFLINE").catch(() => {})}>
              <CheckCircle2 size={16} /> Unblock &amp; approve
            </button>
          )}
          <button className="btn btn-outline" onClick={() => setEditing(true)}>
            <Pencil size={16} /> Edit rider
          </button>
          <button className="btn btn-outline btn-delete" onClick={() => setRemoving(true)}>
            <Trash2 size={16} /> Delete rider
          </button>
        </div>
      </div>

      <div className="tiles">
        <div className="tile"><span>Total deliveries</span><strong>{r.totalDeliveries}</strong></div>
        <div className="tile"><span>Total earnings</span><strong>{inr(r.earnings)}</strong></div>
        <div className="tile"><span>Vehicle</span><strong>{r.vehicle}</strong></div>
        <div className="tile"><span>Rating</span><strong>{r.rating > 0 ? r.rating.toFixed(1) + " / 5" : "No ratings yet"}</strong></div>
      </div>

      <div className="card">
        <div className="card-head"><h2>Verification documents</h2></div>
        <div className="docs">
          {r.documents.map((d) => (
            <div key={d.type} className="doc">
              <FileCheck2 size={20} />
              <span className="strong">{d.type}</span>
              <Badge value={d.status} />
            </div>
          ))}
        </div>
      </div>

      <div className="card">
        <div className="card-head"><h2>Delivery history</h2></div>
        <OrdersTable orders={orders} show={["customer", "seller"]} />
      </div>

      {reasonTarget && (
        <ReasonDialog
          title={reasonTarget.title}
          message="The rider will see this note when they try to sign in."
          confirmLabel={reasonTarget.confirmLabel}
          onClose={() => setReasonTarget(null)}
          onConfirm={async (note) => {
            await setStatus(reasonTarget.status, note);
            setReasonTarget(null);
          }}
        />
      )}
      {editing && (
        <RiderForm
          rider={r}
          onClose={() => setEditing(false)}
          onSaved={() => {
            setEditing(false);
            reload();
          }}
        />
      )}
      {removing && (
        <ConfirmDialog
          title="Delete rider"
          message={`Delete rider "${r.name}" from the database? This cannot be undone.`}
          onClose={() => setRemoving(false)}
          onConfirm={async () => {
            await api.delete(`/api/riders/${id}`);
            navigate("/admin/riders", { replace: true });
          }}
        />
      )}
    </>
  );
}
