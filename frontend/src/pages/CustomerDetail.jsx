import { useState } from "react";
import { useParams } from "react-router-dom";
import { Ban, CheckCircle2, Calendar, Mail, MapPin, Phone } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import BackLink from "../components/BackLink.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import EditableProfile from "../components/EditableProfile.jsx";
import InfoRow from "../components/InfoRow.jsx";
import OrdersTable from "../components/OrdersTable.jsx";
import { api } from "../api.js";
import { inr, useFetch } from "../utils.js";

export default function CustomerDetail() {
  const { id } = useParams();
  const { data, error, loading, reload } = useFetch(`/api/customers/${id}`);
  const [busy, setBusy] = useState(false);

  if (loading && !data) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const { profile: c, orders } = data;
  const blocked = c.status === "BLOCKED";
  const avg = c.totalOrders ? c.totalSpent / c.totalOrders : 0;

  const toggle = async () => {
    setBusy(true);
    try {
      await api.patch(`/api/customers/${id}/status?value=${blocked ? "ACTIVE" : "BLOCKED"}`);
      reload();
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <BackLink to="/customers">All customers</BackLink>

      <div className="card profile">
        <Avatar src={c.avatar} name={c.name} size={96} />
        <div className="profile-main">
          <div className="profile-title">
            <h2>{c.name}</h2>
            <Badge value={c.status} />
          </div>
          <div className="muted">Customer ID · {c.id}</div>
          <EditableProfile
            profile={c}
            endpoint={`/api/customers/${id}/profile`}
            onSaved={reload}
            fields={[
              { name: "name", label: "Full name" },
              { name: "email", label: "Email", type: "email", icon: Mail },
              { name: "phone", label: "Phone", icon: Phone },
              { name: "address", label: "Delivery address", icon: MapPin },
              { name: "city", label: "City" },
            ]}
          />
          <div className="info-grid">
            <InfoRow icon={Calendar} label="Member since">{c.joinedOn}</InfoRow>
          </div>
        </div>
        <div className="profile-actions">
          <button className={blocked ? "btn btn-green" : "btn btn-danger"} onClick={toggle} disabled={busy}>
            {blocked ? <CheckCircle2 size={16} /> : <Ban size={16} />}
            {blocked ? "Unblock customer" : "Block customer"}
          </button>
        </div>
      </div>

      <div className="tiles">
        <div className="tile"><span>Total orders</span><strong>{c.totalOrders}</strong></div>
        <div className="tile"><span>Total spent</span><strong>{inr(c.totalSpent)}</strong></div>
        <div className="tile"><span>Avg. order value</span><strong>{inr(avg)}</strong></div>
        <div className="tile"><span>City</span><strong>{c.city}</strong></div>
      </div>

      <div className="card">
        <div className="card-head"><h2>Recent orders</h2></div>
        <OrdersTable orders={orders} show={["seller", "rider"]} />
      </div>
    </>
  );
}
