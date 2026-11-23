import { Link } from 'react-router-dom';
import { StarIcon } from '../icons.jsx';
import { restaurantPhoto, tileBg } from '../util.js';

export default function RestaurantCard({ r, compact = false }) {
  const image = restaurantPhoto(r);
  return (
    <Link to={`/restaurant/${r.id}`} className={'rcard' + (r.open ? '' : ' closed') + (compact ? ' compact' : '')}>
      <div className="rcard-img" style={{ background: tileBg(r.id) }}>
        {image ? (
          <img
            src={image}
            alt=""
            loading="lazy"
            onError={(e) => { e.currentTarget.onerror = null; e.currentTarget.src = 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=900&q=85'; }}
          />
        ) : <span className="rcard-emoji">{r.emoji}</span>}
        {r.offer && <div className="rcard-offer">{r.offer}</div>}
        {!r.open && <div className="rcard-closed">Currently closed</div>}
      </div>
      <div className="rcard-body">
        <h3 title={r.name}>{r.name}</h3>
        <div className="rcard-meta">
          <span className="rating"><StarIcon size={11} /> {r.rating.toFixed(1)}</span>
          <span>• {r.deliveryMinutes} mins</span>
          {r.distanceKm != null && <span>• {r.distanceKm} km</span>}
        </div>
        <p className="rcard-cuisines" title={r.cuisines}>{r.cuisines}</p>
        <p className="rcard-area">{r.area} • ₹{r.costForTwo} for two</p>
      </div>
    </Link>
  );
}
