
import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bike, Pencil, Plus, Star, Trash2 } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import Toolbar from "../components/Toolbar.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import RiderForm from "../components/RiderForm.jsx";
import ConfirmDialog from "../components/ConfirmDialog.jsx";
import { api } from "../api.js";
import { inr, useFetch } from "../utils.js";

export default function RidersPage() {
  const { data, error, loading, reload } = useFetch("/api/riders");
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [form, setForm] = useState(null);
  const [removing, setRemoving] = useState(null);
  const navigate = useNavigate();

  const working = (r) =>
    r.status === "ONLINE" || r.status === "ON_DELIVERY";

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();

    return (data || []).filter((r) => {
      const matchesFilter =
        filter === "ALL" ||
        (filter === "WORKING" && working(r)) ||
        (filter === "OFFLINE" && r.status === "OFFLINE") ||
        (filter === "PENDING" && r.status === "PENDING") ||
        (filter === "SUSPENDED" &&
          (r.status === "SUSPENDED" || r.status === "REJECTED"));

      return (
        matchesFilter &&
        (!q ||
          [r.name, r.phone, r.vehicleNumber].some((v) =>
            (v || "").toLowerCase().includes(q)
          ))
      );
    });
  }, [data, query, filter]);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  return (
    <>
      <div className="page-actions">
        <span className="muted">
          {data.length} rider{data.length === 1 ? "" : "s"} saved in the database
        </span>
        <button className="btn btn-danger" onClick={() => setForm("new")}>
          <Plus size={16} /> Add rider
        </button>
      </div>

      <div className="card">
        <Toolbar
          query={query}
          onQuery={setQuery}
          placeholder="Search by name, phone or vehicle number"
          active={filter}
          onFilter={setFilter}
          filters={[
            { value: "ALL", label: "All", count: data.length },
            {
              value: "WORKING",
              label: "Working",
              count: data.filter(working).length,
            },
            {
              value: "OFFLINE",
              label: "Offline",
              count: data.filter((r) => r.status === "OFFLINE").length,
            },
            {
              value: "PENDING",
              label: "Applications",
              count: data.filter((r) => r.status === "PENDING").length,
            },
            {
              value: "SUSPENDED",
              label: "Suspended",
              count: data.filter(
                (r) => r.status === "SUSPENDED" || r.status === "REJECTED"
              ).length,
            },
          ]}
        />
      </div>

      {rows.length === 0 ? (
        <div className="empty">No riders match this filter.</div>
      ) : (
        <div className="cards-grid cards-grid-sm">
          {rows.map((r) => (
            <div
              key={r.id}
              className="entity-card rider-card"
              onClick={() => navigate(`/admin/riders/${r.id}`)}
            >
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
                <div>
                  <span>Rating</span>
                  <strong className="rating">
                    <Star size={13} fill="currentColor" />{" "}
                    {r.rating > 0 ? r.rating.toFixed(1) : "—"}
                  </strong>
                </div>
                <div>
                  <span>Deliveries</span>
                  <strong>{r.totalDeliveries}</strong>
                </div>
                <div>
                  <span>Earnings</span>
                  <strong>{inr(r.earnings)}</strong>
                </div>
              </div>

              <div
                className="entity-actions"
                onClick={(e) => e.stopPropagation()}
              >
                <button
                  className="btn btn-outline btn-sm"
                  onClick={() => setForm(r)}
                >
                  <Pencil size={14} /> Edit
                </button>
                <button
                  className="btn btn-outline btn-sm btn-delete"
                  onClick={() => setRemoving(r)}
                >
                  <Trash2 size={14} /> Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {form && (
        <RiderForm
          rider={form === "new" ? null : form}
          onClose={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            reload();
          }}
        />
      )}

      {removing && (
        <ConfirmDialog
          title="Delete rider"
          message={`Delete rider "${removing.name}" from the database? This cannot be undone.`}
          onClose={() => setRemoving(null)}
          onConfirm={async () => {
            await api.delete(`/api/riders/${removing.id}`);
            setRemoving(null);
            reload();
          }}
        />
      )}
    </>
  );
}
