import { useState } from "react";
import { useParams } from "react-router-dom";
import { Ban, Calendar, CheckCircle2, FileText, Mail, MapPin, Phone, Star, XCircle } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import BackLink from "../components/BackLink.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import Photo from "../components/Photo.jsx";
import EditableProfile from "../components/EditableProfile.jsx";
import InfoRow from "../components/InfoRow.jsx";
import OrdersTable from "../components/OrdersTable.jsx";
import { api } from "../api.js";
import { inr, useFetch } from "../utils.js";

export default function SellerDetail() {
  const { id } = useParams();
  const { data, error, loading, reload } = useFetch(`/api/sellers/${id}`);
  const [busy, setBusy] = useState(false);

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const { profile: s, orders } = data;

  const setStatus = async (value) => {
    setBusy(true);
    try {
      await api.patch(`/api/sellers/${id}/status?value=${value}`);
      reload();
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <BackLink to="/sellers">All sellers</BackLink>

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
            <EditableProfile
              profile={s}
              endpoint={`/api/sellers/${id}/profile`}
              onSaved={reload}
              fields={[
                { name: "name", label: "Restaurant name" },
                { name: "ownerName", label: "Owner name" },
                { name: "phone", label: "Phone", icon: Phone },
                { name: "email", label: "Email", type: "email", icon: Mail },
                { name: "cuisine", label: "Cuisine" },
                { name: "address", label: "Address", icon: MapPin },
                { name: "city", label: "City" },
                { name: "fssaiNumber", label: "FSSAI licence", icon: FileText },
              ]}
            />
            <div className="info-grid">
              <InfoRow icon={Calendar} label="Joined Foodie">{s.joinedOn}</InfoRow>
            </div>
          </div>

          <div className="profile-actions">
            {s.status === "PENDING" && (
              <>
                <button className="btn btn-green" disabled={busy} onClick={() => setStatus("ACTIVE")}>
                  <CheckCircle2 size={16} /> Approve restaurant
                </button>
                <button className="btn btn-danger" disabled={busy} onClick={() => setStatus("SUSPENDED")}>
                  <XCircle size={16} /> Reject
                </button>
              </>
            )}
            {s.status === "ACTIVE" && (
              <button className="btn btn-danger" disabled={busy} onClick={() => setStatus("SUSPENDED")}>
                <Ban size={16} /> Suspend restaurant
              </button>
            )}
            {s.status === "SUSPENDED" && (
              <button className="btn btn-green" disabled={busy} onClick={() => setStatus("ACTIVE")}>
                <CheckCircle2 size={16} /> Reinstate restaurant
              </button>
            )}
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
    </>
  );
}
