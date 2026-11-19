import { useEffect, useState } from 'react';
import { api } from '../api';
import { searchPlaces, useGeo, useToast } from '../context.jsx';
import { CloseIcon, CrosshairIcon, PinIcon } from '../icons.jsx';

export default function LocationDrawer() {
  const { open, setOpen, setLoc, detect, detecting, loc } = useGeo();
  const toast = useToast();
  const [q, setQ] = useState('');
  const [results, setResults] = useState([]);
  const [saved, setSaved] = useState([]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    if (open) api.get('/users/me/addresses').then(setSaved).catch(() => {});
  }, [open]);

  useEffect(() => {
    if (q.trim().length < 3) { setResults([]); return; }
    setSearching(true);
    const t = setTimeout(() => {
      searchPlaces(q.trim()).then(setResults).catch(() => setResults([])).finally(() => setSearching(false));
    }, 500);
    return () => clearTimeout(t);
  }, [q]);

  if (!open) return null;

  const close = () => { setOpen(false); setQ(''); setResults([]); };

  const useCurrent = async () => {
    try { await detect(); toast('Showing restaurants near your current location', 'ok'); close(); }
    catch (e) { toast(e.message, 'error'); }
  };

  const pick = (p) => { setLoc({ label: p.label, full: p.full, lat: p.lat, lon: p.lon }); close(); };

  const pickSaved = (a) => {
    setLoc({ label: `${a.label}: ${a.line.split(',')[0]}`, full: a.line, lat: a.lat ?? loc.lat, lon: a.lon ?? loc.lon });
    close();
  };

  return (
    <div className="overlay left" onClick={close}>
      <aside className="drawer" onClick={(e) => e.stopPropagation()}>
        <button className="icon-btn" onClick={close} aria-label="Close"><CloseIcon /></button>
        <input className="input" autoFocus placeholder="Search for area, street name…" value={q} onChange={(e) => setQ(e.target.value)} />

        <button className="loc-row" onClick={useCurrent} disabled={detecting}>
          <CrosshairIcon size={22} />
          <div>
            <b>{detecting ? 'Detecting your location…' : 'Use my current location'}</b>
            <small>Using GPS</small>
          </div>
        </button>

        {searching && <p className="muted pad">Searching…</p>}
        {results.map((p, i) => (
          <button key={i} className="loc-row" onClick={() => pick(p)}>
            <PinIcon size={20} />
            <div><b>{p.label}</b><small>{p.full}</small></div>
          </button>
        ))}

        {saved.length > 0 && <h4 className="drawer-h">Saved addresses</h4>}
        {saved.map((a) => (
          <button key={a.id} className="loc-row" onClick={() => pickSaved(a)}>
            <PinIcon size={20} />
            <div><b>{a.label}</b><small>{a.line}</small></div>
          </button>
        ))}

        <p className="muted pad small">Currently delivering to: {loc.label}</p>
      </aside>
    </div>
  );
}
