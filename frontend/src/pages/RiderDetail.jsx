import { useState } from "react";
import { useParams } from "react-router-dom";
import {
  Ban, Bike, Calendar, CheckCircle2, CreditCard, FileCheck2, Mail, MapPin, Phone, Star, XCircle,
} from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import BackLink from "../components/BackLink.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import EditableProfile from "../components/EditableProfile.jsx";
import InfoRow from "../components/InfoRow.jsx";
import OrdersTable from "../components/OrdersTable.jsx";
import { api } from "../api.js";
import { inr, useFetch } from "../utils.js";

export default function RiderDetail() {
  const { id } = useParams();
  const { data, error, loading, reload } = useFetch(`/api/riders/${id}`);
  const [busy, setBusy] = useState(false);

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const { profile: r, orders } = data;

  const setStatus = async (value) => {
    setBusy(true);
    try {
      await api.patch(`/api/riders/${id}/status?value=${value}`);
      reload();
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <BackLink to="/riders">All riders</BackLink>

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
          <EditableProfile
            profile={r}
            endpoint={`/api/riders/${id}/profile`}
            onSaved={reload}
            fields={[
              { name: "name", label: "Full name" },
              { name: "phone", label: "Phone", icon: Phone },
              { name: "email", label: "Email", type: "email", icon: Mail },
              { name: "city", label: "Service area", icon: MapPin },
              { name: "vehicle", label: "Vehicle", icon: Bike },
              { name: "vehicleNumber", label: "Vehicle registration" },
              { name: "licenseNumber", label: "Driving licence", icon: CreditCard },
            ]}
          />
          <div className="info-grid">
            <InfoRow icon={Calendar} label="Joined Foodie">{r.joinedOn}</InfoRow>
          </div>
        </div>

        <div className="profile-actions">
          {r.status === "PENDING" && (
            <>
              <button className="btn btn-green" disabled={busy} onClick={() => setStatus("OFFLINE")}>
                <CheckCircle2 size={16} /> Approve rider
              </button>
              <button className="btn btn-danger" disabled={busy} onClick={() => setStatus("REJECTED")}>
                <XCircle size={16} /> Reject application
              </button>
            </>
          )}
          {(r.status === "ONLINE" || r.status === "ON_DELIVERY" || r.status === "OFFLINE") && (
            <button className="btn btn-danger" disabled={busy} onClick={() => setStatus("SUSPENDED")}>
              <Ban size={16} /> Suspend rider
            </button>
          )}
          {(r.status === "SUSPENDED" || r.status === "REJECTED") && (
            <button className="btn btn-green" disabled={busy} onClick={() => setStatus("OFFLINE")}>
              <CheckCircle2 size={16} /> Unblock &amp; approve
            </button>
          )}
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
    </>
  );
}
