import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bike, Star } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import Toolbar from "../components/Toolbar.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import { inr, useFetch } from "../utils.js";

export default function RidersPage() {
  const { data, error, loading } = useFetch("/api/riders");
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState("ALL");
  const navigate = useNavigate();

  const working = (r) => r.status === "ONLINE" || r.status === "ON_DELIVERY";

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();
    return (data || []).filter((r) => {
      const matchesFilter =
        filter === "ALL" ||
        (filter === "WORKING" && working(r)) ||
        (filter === "OFFLINE" && r.status === "OFFLINE") ||
        (filter === "PENDING" && r.status === "PENDING") ||
        (filter === "SUSPENDED" && (r.status === "SUSPENDED" || r.status === "REJECTED"));
      return matchesFilter && (!q || [r.name, r.phone, r.vehicleNumber].some((v) => v.toLowerCase().includes(q)));
    });
  }, [data, query, filter]);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  return (
    <>
      <div className="card">
        <Toolbar
          query={query}
          onQuery={setQuery}
          placeholder="Search by name, phone or vehicle number"
          active={filter}
          onFilter={setFilter}
          filters={[
            { value: "ALL", label: "All", count: data.length },
            { value: "WORKING", label: "Working", count: data.filter(working).length },
            { value: "OFFLINE", label: "Offline", count: data.filter((r) => r.status === "OFFLINE").length },
            { value: "PENDING", label: "Applications", count: data.filter((r) => r.status === "PENDING").length },
            { value: "SUSPENDED", label: "Suspended", count: data.filter((r) => r.status === "SUSPENDED" || r.status === "REJECTED").length },
          ]}
        />
      </div>

      {rows.length === 0 ? (
        <div className="empty">No riders match this filter.</div>
      ) : (
        <div className="cards-grid cards-grid-sm">
          {rows.map((r) => (
            <div key={r.id} className="entity-card rider-card" onClick={() => navigate(`/riders/${r.id}`)}>
              <div className="rider-top">
                <Avatar src={r.avatar} name={r.name} size={64} />
                <div>
                  <h3>{r.name}</h3>
                  <div className="muted small">{r.phone}</div>
                  <Badge value={r.status} />
                </div>
              </div>
              <div className="rider-vehicle">
                <Bike size={16} /> {r.vehicle} · {r.vehicleNumber}
              </div>
              <div className="rider-stats">
                <div><span>Rating</span><strong className="rating"><Star size={13} fill="currentColor" /> {r.rating > 0 ? r.rating.toFixed(1) : "—"}</strong></div>
                <div><span>Deliveries</span><strong>{r.totalDeliveries}</strong></div>
                <div><span>Earnings</span><strong>{inr(r.earnings)}</strong></div>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
