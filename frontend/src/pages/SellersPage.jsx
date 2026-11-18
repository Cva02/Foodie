import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Star } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import Toolbar from "../components/Toolbar.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import Photo from "../components/Photo.jsx";
import { inr, useFetch } from "../utils.js";

export default function SellersPage() {
  const { data, error, loading } = useFetch("/api/sellers");
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState("ALL");
  const navigate = useNavigate();

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();
    return (data || []).filter(
      (s) =>
        (filter === "ALL" || s.status === filter) &&
        (!q || [s.name, s.ownerName, s.cuisine, s.address].some((v) => v.toLowerCase().includes(q)))
    );
  }, [data, query, filter]);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const count = (st) => data.filter((s) => s.status === st).length;

  return (
    <>
      <div className="card">
        <Toolbar
          query={query}
          onQuery={setQuery}
          placeholder="Search restaurant, owner or cuisine"
          active={filter}
          onFilter={setFilter}
          filters={[
            { value: "ALL", label: "All", count: data.length },
            { value: "ACTIVE", label: "Active", count: count("ACTIVE") },
            { value: "PENDING", label: "Pending", count: count("PENDING") },
            { value: "SUSPENDED", label: "Suspended", count: count("SUSPENDED") },
          ]}
        />
      </div>

      {rows.length === 0 ? (
        <div className="empty">No sellers match this filter.</div>
      ) : (
        <div className="cards-grid">
          {rows.map((s) => (
            <div key={s.id} className="entity-card" onClick={() => navigate(`/sellers/${s.id}`)}>
              <Photo src={s.image} alt={s.name} className="cover">
                <div className="cover-badge"><Badge value={s.status} /></div>
                <div className={`open-pill ${s.open ? "open" : "closed"}`}>{s.open ? "Open" : "Closed"}</div>
              </Photo>
              <div className="entity-body">
                <h3>{s.name}</h3>
                <div className="muted small">{s.cuisine}</div>
                <div className="entity-row">
                  <span className="rating"><Star size={14} fill="currentColor" /> {s.rating > 0 ? s.rating.toFixed(1) : "New"}</span>
                  <span className="muted small">{s.totalOrders} orders</span>
                  <span className="strong small">{inr(s.revenue)}</span>
                </div>
                <div className="owner">
                  <Avatar src={s.ownerAvatar} name={s.ownerName} size={26} />
                  <span className="small">{s.ownerName}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
