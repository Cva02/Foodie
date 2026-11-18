import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronRight } from "lucide-react";
import { ErrorBox, Loading } from "../components/State.jsx";
import Toolbar from "../components/Toolbar.jsx";
import Avatar from "../components/Avatar.jsx";
import Badge from "../components/Badge.jsx";
import { inr, useFetch } from "../utils.js";

export default function CustomersPage() {
  const { data, error, loading } = useFetch("/api/customers");
  const [query, setQuery] = useState("");
  const [filter, setFilter] = useState("ALL");
  const navigate = useNavigate();

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase();
    return (data || []).filter(
      (c) =>
        (filter === "ALL" || c.status === filter) &&
        (!q || [c.name, c.email, c.phone, c.address].some((v) => v.toLowerCase().includes(q)))
    );
  }, [data, query, filter]);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const count = (s) => data.filter((c) => c.status === s).length;

  return (
    <div className="card">
      <Toolbar
        query={query}
        onQuery={setQuery}
        placeholder="Search by name, email or phone"
        active={filter}
        onFilter={setFilter}
        filters={[
          { value: "ALL", label: "All", count: data.length },
          { value: "ACTIVE", label: "Active", count: count("ACTIVE") },
          { value: "BLOCKED", label: "Blocked", count: count("BLOCKED") },
        ]}
      />
      {rows.length === 0 ? (
        <div className="empty">No customers match this filter.</div>
      ) : (
        <div className="table-wrap">
          <table className="table table-click">
            <thead>
              <tr>
                <th>Customer</th>
                <th>Phone</th>
                <th>Orders</th>
                <th>Total spent</th>
                <th>Joined</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((c) => (
                <tr key={c.id} onClick={() => navigate(`/customers/${c.id}`)}>
                  <td>
                    <div className="person">
                      <Avatar src={c.avatar} name={c.name} />
                      <div>
                        <div className="strong">{c.name}</div>
                        <div className="muted small">{c.email}</div>
                      </div>
                    </div>
                  </td>
                  <td>{c.phone}</td>
                  <td>{c.totalOrders}</td>
                  <td className="strong">{inr(c.totalSpent)}</td>
                  <td className="muted nowrap">{c.joinedOn}</td>
                  <td>
                    <Badge value={c.status} />
                  </td>
                  <td className="chev">
                    <ChevronRight size={18} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
