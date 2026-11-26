import { Search } from "lucide-react";

/** Search box + filter chips used on every list page. */
export default function Toolbar({ query, onQuery, placeholder, filters, active, onFilter }) {
  return (
    <div className="toolbar">
      <div className="search">
        <Search size={16} />
        <input value={query} onChange={(e) => onQuery(e.target.value)} placeholder={placeholder} />
      </div>
      <div className="chips">
        {filters.map((f) => (
          <button
            key={f.value}
            className={`chip ${active === f.value ? "chip-active" : ""}`}
            onClick={() => onFilter(f.value)}
          >
            {f.label}
            {f.count !== undefined && <span className="chip-count">{f.count}</span>}
          </button>
        ))}
      </div>
    </div>
  );
}
