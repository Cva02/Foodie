export default function Logo({ size = 36 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" aria-label="Foodie">
      <path
        fill="#E23744"
        fillRule="evenodd"
        d="M50 14a36 36 0 1 0 33 21.5A21 21 0 0 1 55 17.5 36 36 0 0 0 50 14z"
      />
      <circle cx="40" cy="48" r="2.8" fill="#1C1C1C" />
      <circle cx="56" cy="56" r="2.8" fill="#1C1C1C" />
      <circle cx="44" cy="66" r="2.8" fill="#1C1C1C" />
    </svg>
  );
}
