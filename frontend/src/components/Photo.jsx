import { useState } from "react";

/** Food / cover photo with a red-gradient fallback. */
export default function Photo({ src, alt, className = "", children }) {
  const [failed, setFailed] = useState(false);
  return (
    <div className={`photo ${className}`}>
      {!failed && src ? (
        <img src={src} alt={alt} loading="lazy" onError={() => setFailed(true)} />
      ) : (
        <div className="photo-fallback" />
      )}
      {children}
    </div>
  );
}
