const make = (paths) =>
  function Icon({ size = 20 }) {
    return (
      <svg viewBox="0 0 24 24" width={size} height={size} fill="none" stroke="currentColor" strokeWidth="2"
           strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths}</svg>
    );
  };

export const SearchIcon = make(<><circle cx="11" cy="11" r="7" /><path d="M21 21l-4.3-4.3" /></>);
export const BagIcon = make(<><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" /><path d="M3 6h18" /><path d="M16 10a4 4 0 01-8 0" /></>);
export const BellIcon = make(<><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.7 21a2 2 0 01-3.4 0" /></>);
export const ReceiptIcon = make(<><path d="M4 2v20l3-2 3 2 3-2 3 2 3-2V2z" /><path d="M8 7h8M8 11h8M8 15h5" /></>);
export const SlidersIcon = make(<path d="M4 21v-7M4 10V3M12 21v-9M12 8V3M20 21v-5M20 12V3M1 14h6M9 8h6M17 16h6" />);
export const SunIcon = make(<><circle cx="12" cy="12" r="4" /><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.65 17.65l1.42 1.42M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.65 6.35l1.42-1.42" /></>);
export const MoonIcon = make(<path d="M20.9 13A9 9 0 1111 3.1 7 7 0 0020.9 13z" />);
export const PinIcon = make(<><path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0118 0z" /><circle cx="12" cy="10" r="3" /></>);
export const ChevronDown = make(<path d="M6 9l6 6 6-6" />);
export const CloseIcon = make(<path d="M18 6L6 18M6 6l12 12" />);
export const HomeIcon = make(<><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" /><path d="M9 22V12h6v10" /></>);
export const CrosshairIcon = make(<><circle cx="12" cy="12" r="3" /><circle cx="12" cy="12" r="8" /><path d="M12 1v4M12 19v4M1 12h4M19 12h4" /></>);

export function StarIcon({ size = 12 }) {
  return (
    <svg viewBox="0 0 24 24" width={size} height={size} fill="currentColor" aria-hidden="true">
      <path d="M12 2l3.1 6.3 6.9 1-5 4.9 1.2 6.8L12 17.8 5.8 21l1.2-6.8-5-4.9 6.9-1z" />
    </svg>
  );
}

export function VegMark({ veg }) {
  const c = veg ? '#0f8a3c' : '#c4281c';
  return (
    <span className="vegmark" style={{ borderColor: c }} title={veg ? 'Veg' : 'Non-veg'}>
      <i style={{ background: c }} />
    </span>
  );
}
