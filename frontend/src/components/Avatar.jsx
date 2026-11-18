import { useState } from "react";
import { initials } from "../utils.js";

/** Image with a graceful initials fallback if the dummy image fails to load. */
export default function Avatar({ src, name, size = 44, square = false }) {
  const [failed, setFailed] = useState(false);
  const style = {
    width: size,
    height: size,
    borderRadius: square ? 12 : "50%",
    fontSize: size * 0.38,
  };
  if (!src || failed) {
    return (
      <div className="avatar avatar-fallback" style={style}>
        {initials(name)}
      </div>
    );
  }
  return (
    <img
      className="avatar"
      style={style}
      src={src}
      alt={name}
      loading="lazy"
      onError={() => setFailed(true)}
    />
  );
}
