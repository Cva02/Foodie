import { useEffect, useState } from 'react';
import { api } from '../api';
import { useGeo } from '../context.jsx';
import RestaurantCard from '../components/RestaurantCard.jsx';
import { SearchIcon } from '../icons.jsx';

const POPULAR = ['Biryani', 'Pizza', 'Dosa', 'Burger', 'Noodles', 'Cake', 'Kebab', 'Chai'];

export default function Search() {
  const { loc } = useGeo();
  const [q, setQ] = useState('');
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (q.trim().length < 2) { setList([]); return; }
    setLoading(true);
    const t = setTimeout(() => {
      api.get(`/restaurants?q=${encodeURIComponent(q.trim())}&lat=${loc.lat}&lon=${loc.lon}`)
        .then(setList).catch(() => setList([])).finally(() => setLoading(false));
    }, 300);
    return () => clearTimeout(t);
  }, [q, loc.lat, loc.lon]);

  return (
    <div className="container narrow">
      <div className="searchbar">
        <SearchIcon />
        <input autoFocus placeholder="Search for restaurants and food" value={q} onChange={(e) => setQ(e.target.value)} />
      </div>

      {q.trim().length < 2 && (
        <>
          <h3 className="sec-h small-h">Popular cuisines</h3>
          <div className="chips">{POPULAR.map((p) => <button key={p} className="chip" onClick={() => setQ(p)}>{p}</button>)}</div>
        </>
      )}

      {loading && <p className="muted pad">Searching…</p>}
      {q.trim().length >= 2 && !loading && list.length === 0 && <p className="muted center-text pad">No results for “{q}”.</p>}
      <div className="grid">{list.map((r) => <RestaurantCard key={r.id} r={r} />)}</div>
    </div>
  );
}
