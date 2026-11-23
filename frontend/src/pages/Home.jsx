import { useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { api } from '../api';
import { useGeo } from '../context.jsx';
import RestaurantCard from '../components/RestaurantCard.jsx';

const CUISINES = [
  ['Biryani', 'photo-1563379091339-03246963d51a'],
  ['Pizza', 'photo-1513104890138-7c749659a591'],
  ['South Indian', 'photo-1589301760014-d929f3979dbc'],
  ['Chinese', 'photo-1569718212165-3a8278d5f624'],
  ['Burgers', 'photo-1568901346375-23c9450c58cd'],
  ['Desserts', 'photo-1578985545062-69928b1d9587'],
  ['Kebabs', 'photo-1544025162-d76694265947'],
  ['Seafood', 'photo-1519708227418-c8fd9a32b7a2'],
  ['Healthy', 'photo-1512621776951-a57141f2eefd'],
  ['Street Food', 'photo-1601050690597-df0568f70950'],
];

const cuisinePhoto = (id) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=240&h=240&q=85`;

const SORTS = [
  ['', 'Relevance'], ['time', 'Delivery time'], ['rating', 'Rating'], ['distance', 'Distance'],
  ['costLow', 'Cost: Low to High'], ['costHigh', 'Cost: High to Low'],
];

function CartoonChef3D() {
  return (
    <svg
      className="chef3d-svg"
      viewBox="0 0 180 180"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <defs>
        {/* 3D Pixar-style lighting gradients */}
        <radialGradient id="skin3d" cx="38%" cy="32%" r="65%">
          <stop offset="0%" stopColor="#FFE1C6" />
          <stop offset="55%" stopColor="#F6B88F" />
          <stop offset="100%" stopColor="#D9885B" />
        </radialGradient>
        <radialGradient id="cheek3d" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#FF7A7A" stopOpacity="0.65" />
          <stop offset="100%" stopColor="#FF7A7A" stopOpacity="0" />
        </radialGradient>
        <radialGradient id="nose3d" cx="35%" cy="30%" r="65%">
          <stop offset="0%" stopColor="#FF9E80" />
          <stop offset="100%" stopColor="#D6684B" />
        </radialGradient>
        <linearGradient id="hat3d" x1="20%" y1="0%" x2="80%" y2="100%">
          <stop offset="0%" stopColor="#FFFFFF" />
          <stop offset="62%" stopColor="#F2F4F8" />
          <stop offset="100%" stopColor="#CFD6E3" />
        </linearGradient>
        <linearGradient id="coat3d" x1="15%" y1="10%" x2="85%" y2="95%">
          <stop offset="0%" stopColor="#FFFFFF" />
          <stop offset="58%" stopColor="#EEF1F6" />
          <stop offset="100%" stopColor="#C8D0DF" />
        </linearGradient>
        <linearGradient id="scarf3d" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#FF5A67" />
          <stop offset="55%" stopColor="#E23744" />
          <stop offset="100%" stopColor="#9E1924" />
        </linearGradient>
        <linearGradient id="pants3d" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#3B4254" />
          <stop offset="100%" stopColor="#1E222D" />
        </linearGradient>
        <radialGradient id="shoe3d" cx="35%" cy="25%" r="70%">
          <stop offset="0%" stopColor="#6B442A" />
          <stop offset="55%" stopColor="#3D2414" />
          <stop offset="100%" stopColor="#1F1108" />
        </radialGradient>
        <radialGradient id="beard3d" cx="40%" cy="30%" r="70%">
          <stop offset="0%" stopColor="#8C5A3C" />
          <stop offset="65%" stopColor="#59351F" />
          <stop offset="100%" stopColor="#381E0E" />
        </radialGradient>
        <linearGradient id="wandStick" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#FFE082" />
          <stop offset="50%" stopColor="#F59E0B" />
          <stop offset="100%" stopColor="#B45309" />
        </linearGradient>
        <radialGradient id="wandGlow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="#FFFBEB" />
          <stop offset="45%" stopColor="#FDE047" />
          <stop offset="100%" stopColor="#F59E0B" stopOpacity="0" />
        </radialGradient>
        <filter id="drop3d" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="4" stdDeviation="3.5" floodColor="#181C26" floodOpacity="0.28" />
        </filter>
      </defs>

      {/* Dynamic Floor Shadow */}
      <ellipse className="chef3d-floor-shadow" cx="90" cy="168" rx="34" ry="7" fill="rgba(24, 28, 38, 0.22)" />

      {/* Back Arm (swings during walk) */}
      <g className="chef3d-arm-back">
        <path d="M72 96 C58 100, 52 114, 56 124" stroke="url(#coat3d)" strokeWidth="14" strokeLinecap="round" filter="url(#drop3d)" />
        <circle cx="55" cy="126" r="8" fill="url(#skin3d)" filter="url(#drop3d)" />
      </g>

      {/* Back Leg (strides during walk) */}
      <g className="chef3d-leg-back">
        <path d="M80 128 L74 154" stroke="url(#pants3d)" strokeWidth="14" strokeLinecap="round" />
        <ellipse cx="78" cy="159" rx="13" ry="7.5" fill="url(#shoe3d)" filter="url(#drop3d)" />
        <ellipse cx="75" cy="157" rx="5" ry="2.2" fill="rgba(255,255,255,0.28)" />
      </g>

      {/* Front Leg (strides opposite during walk) */}
      <g className="chef3d-leg-front">
        <path d="M98 128 L104 154" stroke="url(#pants3d)" strokeWidth="14" strokeLinecap="round" />
        <ellipse cx="108" cy="159" rx="13" ry="7.5" fill="url(#shoe3d)" filter="url(#drop3d)" />
        <ellipse cx="105" cy="157" rx="5" ry="2.2" fill="rgba(255,255,255,0.28)" />
      </g>

      {/* 3D Cartoon Torso & Chef Coat */}
      <g className="chef3d-torso" filter="url(#drop3d)">
        <path
          d="M62 92 C60 112, 63 132, 72 135 L108 135 C117 132, 120 112, 118 92 C112 84, 68 84, 62 92 Z"
          fill="url(#coat3d)"
        />
        {/* Apron / Coat Fold & Double-Breasted Buttons */}
        <path d="M82 92 L80 134" stroke="#CBD2E1" strokeWidth="2" />
        <circle cx="87" cy="103" r="2.6" fill="#F59E0B" />
        <circle cx="99" cy="103" r="2.6" fill="#F59E0B" />
        <circle cx="87" cy="115" r="2.6" fill="#F59E0B" />
        <circle cx="99" cy="115" r="2.6" fill="#F59E0B" />
        <circle cx="87" cy="127" r="2.6" fill="#F59E0B" />
        <circle cx="99" cy="127" r="2.6" fill="#F59E0B" />
        {/* Red Chef Neckerchief */}
        <path d="M68 88 Q90 99 112 88 Q106 98 90 100 Q74 98 68 88 Z" fill="url(#scarf3d)" />
        <path d="M90 96 L84 111 L92 108 L97 112 Z" fill="url(#scarf3d)" />
      </g>

      {/* 3D Cartoon Head & Puffy Toque Hat */}
      <g className="chef3d-head-group" filter="url(#drop3d)">
        {/* Ears */}
        <circle cx="63" cy="67" r="7" fill="url(#skin3d)" />
        <circle cx="117" cy="67" r="7" fill="url(#skin3d)" />
        {/* Face */}
        <rect x="65" y="44" width="50" height="44" rx="23" fill="url(#skin3d)" />
        {/* Rosy 3D Cheeks */}
        <circle cx="74" cy="70" r="7" fill="url(#cheek3d)" />
        <circle cx="106" cy="70" r="7" fill="url(#cheek3d)" />
        {/* Expressive 3D Eyes */}
        <ellipse cx="81" cy="61" rx="5.2" ry="6" fill="#FFFFFF" />
        <ellipse cx="99" cy="61" rx="5.2" ry="6" fill="#FFFFFF" />
        <circle cx="82.5" cy="61.5" r="3.2" fill="#231815" />
        <circle cx="100.5" cy="61.5" r="3.2" fill="#231815" />
        <circle cx="83.8" cy="59.8" r="1.3" fill="#FFFFFF" />
        <circle cx="101.8" cy="59.8" r="1.3" fill="#FFFFFF" />
        {/* Eyebrows */}
        <path d="M75 53 Q81 49 86 53" stroke="#4E2D18" strokeWidth="2.8" strokeLinecap="round" />
        <path d="M94 53 Q99 49 105 53" stroke="#4E2D18" strokeWidth="2.8" strokeLinecap="round" />
        {/* Big Cheerful Smile & Mustache */}
        <path d="M81 75 Q90 83 99 75" stroke="#6B201A" strokeWidth="3" strokeLinecap="round" fill="#FFFFFF" />
        <path
          d="M73 72 C78 67, 86 69, 90 72 C94 69, 102 67, 107 72 C103 76, 94 76, 90 73 C86 76, 77 76, 73 72 Z"
          fill="url(#beard3d)"
        />
        {/* 3D Bulbous Cartoon Nose */}
        <ellipse cx="90" cy="66" rx="6.2" ry="5" fill="url(#nose3d)" />
        <ellipse cx="88.5" cy="64.5" rx="2.2" ry="1.4" fill="rgba(255,255,255,0.45)" />

        {/* Puffy 3D Chef Toque Hat */}
        <g className="chef3d-hat">
          <path
            d="M64 38 C52 34, 54 16, 68 16 C72 7, 86 5, 92 12 C99 5, 112 8, 114 17 C126 18, 127 35, 115 38 Z"
            fill="url(#hat3d)"
          />
          <path d="M75 18 Q77 28 78 36" stroke="#D5DCE8" strokeWidth="1.8" strokeLinecap="round" />
          <path d="M90 15 Q90 26 90 36" stroke="#D5DCE8" strokeWidth="1.8" strokeLinecap="round" />
          <path d="M104 18 Q102 28 101 36" stroke="#D5DCE8" strokeWidth="1.8" strokeLinecap="round" />
          <rect x="66" y="35" width="48" height="12" rx="4" fill="url(#hat3d)" stroke="#DCE2EC" strokeWidth="1" />
          <rect x="66" y="43" width="48" height="3" rx="1.5" fill="#E23744" />
        </g>
      </g>

      {/* Front Arm + Magic Wand (swings while walking, raises & casts magic at left) */}
      <g className="chef3d-arm-front" filter="url(#drop3d)">
        <path d="M108 96 C120 100, 126 110, 124 120" stroke="url(#coat3d)" strokeWidth="14" strokeLinecap="round" />
        <circle cx="125" cy="121" r="8.2" fill="url(#skin3d)" />

        {/* Glowing 3D Magic Wand */}
        <g className="chef3d-wand-group">
          <line x1="125" y1="121" x2="156" y2="86" stroke="url(#wandStick)" strokeWidth="4.5" strokeLinecap="round" />
          <line x1="125" y1="121" x2="133" y2="112" stroke="#FFFFFF" strokeWidth="4.5" strokeLinecap="round" />
          <circle className="chef3d-wand-orb" cx="158" cy="84" r="16" fill="url(#wandGlow)" />
          <path
            className="chef3d-wand-star"
            d="M158 73 L161 81 L169 84 L161 87 L158 95 L155 87 L147 84 L155 81 Z"
            fill="#FFFBEB"
            stroke="#F59E0B"
            strokeWidth="1.2"
          />
        </g>
      </g>
    </svg>
  );
}

export default function Home() {
  const { loc } = useGeo();
  const location = useLocation();
  const [list, setList] = useState(null);
  const [error, setError] = useState('');
  const [cuisine, setCuisine] = useState('');
  const [sort, setSort] = useState('');
  const [chips, setChips] = useState({ fast: false, rating: false, veg: false, cheap: false });
  const [animRun, setAnimRun] = useState(0);
  const [stagePhase, setStagePhase] = useState('walking'); // 'walking' -> 'magic' -> 'reveal' -> 'done'

  // Replay the 3D cartoon cook walk -> magic action -> items in same line -> cook disappear sequence every time user visits Home
  useEffect(() => {
    setAnimRun((n) => n + 1);
  }, [location.key, location.pathname]);

  useEffect(() => {
    const replay = () => setAnimRun((n) => n + 1);
    const onVisibility = () => {
      if (document.visibilityState === 'visible') setAnimRun((n) => n + 1);
    };
    window.addEventListener('foodie-home-visit', replay);
    window.addEventListener('pageshow', replay);
    document.addEventListener('visibilitychange', onVisibility);
    return () => {
      window.removeEventListener('foodie-home-visit', replay);
      window.removeEventListener('pageshow', replay);
      document.removeEventListener('visibilitychange', onVisibility);
    };
  }, []);

  useEffect(() => {
    setStagePhase('walking');
    const tMagic = setTimeout(() => setStagePhase('magic'), 1850);
    const tReveal = setTimeout(() => setStagePhase('reveal'), 2550);
    const tDone = setTimeout(() => setStagePhase('done'), 3850);
    return () => {
      clearTimeout(tMagic);
      clearTimeout(tReveal);
      clearTimeout(tDone);
    };
  }, [animRun]);

  useEffect(() => {
    setList(null);
    setError('');
    const qs = `lat=${loc.lat}&lon=${loc.lon}&sort=${sort}` + (cuisine ? `&cuisine=${encodeURIComponent(cuisine)}` : '');
    api.get('/restaurants?' + qs).then(setList).catch((e) => setError(e.message));
  }, [loc.lat, loc.lon, sort, cuisine]);

  const city = loc.label.split(',').pop().trim();
  const top = useMemo(() => (list || []).filter((r) => r.open).slice().sort((a, b) => b.rating - a.rating).slice(0, 6), [list]);
  const shown = useMemo(
    () =>
      (list || []).filter((r) =>
        (!chips.fast || r.deliveryMinutes <= 25) && (!chips.rating || r.rating >= 4.0) &&
        (!chips.veg || r.pureVeg) && (!chips.cheap || r.costForTwo <= 300)),
    [list, chips]
  );
  const toggle = (k) => setChips({ ...chips, [k]: !chips[k] });

  return (
    <div className="container">
      <section className="craving-board">
        <div className="craving-heading">
          <span className="craving-eyebrow">THE FLAVOUR FINDER</span>
          <h2 className="sec-h">Tell me, what you wish!</h2>
          <p>Pick a mood. Find your next favourite.</p>
        </div>

        <div key={animRun} className={`wish-line-stage phase-${stagePhase}`}>
          <div className="chef-scene" aria-hidden="true">
            <div className="chef-walk-direction">
              <div className="chef-walk-body">
                <CartoonChef3D />
              </div>
            </div>
            <span className="magic-ring" />
            <span className="magic-star magic-star-one">✦</span>
            <span className="magic-star magic-star-two">✧</span>
            <span className="magic-star magic-star-three">✨</span>
            <span className="magic-beam" />
            <span className="magic-puff">✨ ✧ ✦</span>
          </div>

          <div className="cuisines" aria-label="Choose a cuisine">
            {CUISINES.map(([name, image], index) => (
              <button
                key={name}
                type="button"
                className={'cuisine' + (cuisine === name ? ' on' : '')}
                onClick={() => setCuisine(cuisine === name ? '' : name)}
                style={{ animationDelay: `${2.55 + index * 0.055}s` }}
              >
                <span className="cuisine-pic">
                  <img
                    src={cuisinePhoto(image)}
                    alt=""
                    loading="lazy"
                    onError={(e) => { e.currentTarget.onerror = null; e.currentTarget.src = 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=240&h=240&q=85'; }}
                  />
                </span>
                <span>{name}</span>
              </button>
            ))}
          </div>
        </div>
      </section>

      <aside className="delivery-highlight">
        <span className="delivery-highlight-icon" aria-hidden="true">🚲</span>
        <div>
          <b>Free delivery on every order</b>
          <span>No minimum spend. Your favourites, delivered for free.</span>
        </div>
      </aside>

      <hr className="sep" />

      {error && <p className="error center-text">{error}</p>}

      {!cuisine && top.length > 0 && (
        <section>
          <h2 className="sec-h">Top restaurant chains in {city}</h2>
          <div className="hscroll">
            {top.map((r) => <RestaurantCard key={r.id} r={r} compact />)}
          </div>
          <hr className="sep" />
        </section>
      )}

      <section>
        <h2 className="sec-h" id="restaurants">{cuisine ? `${cuisine} restaurants near you` : `Restaurants with online food delivery in ${city}`}</h2>
        <div className="chips">
          <select className="chip select" value={sort} onChange={(e) => setSort(e.target.value)} aria-label="Sort by">
            {SORTS.map(([v, l]) => <option key={v} value={v}>Sort by: {l}</option>)}
          </select>
          <button className={'chip' + (chips.fast ? ' on' : '')} onClick={() => toggle('fast')}>Fast Delivery</button>
          <button className={'chip' + (chips.rating ? ' on' : '')} onClick={() => toggle('rating')}>Rating 4.0+</button>
          <button className={'chip' + (chips.veg ? ' on' : '')} onClick={() => toggle('veg')}>Pure Veg</button>
          <button className={'chip' + (chips.cheap ? ' on' : '')} onClick={() => toggle('cheap')}>Less than ₹300</button>
          {cuisine && <button className="chip on" onClick={() => setCuisine('')}>{cuisine} ✕</button>}
        </div>

        {list === null && !error && (
          <div className="grid">{Array.from({ length: 8 }).map((_, i) => <div key={i} className="skeleton" />)}</div>
        )}
        {list && shown.length === 0 && <p className="muted center-text pad">No restaurants match these filters.</p>}
        <div className="grid">{shown.map((r) => <RestaurantCard key={r.id} r={r} />)}</div>
      </section>
    </div>
  );
}
