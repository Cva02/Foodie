export default function Logo({ size = 40 }) {
  return (
    <img
      className="foodie-mark"
      src="/foodie-logo.png"
      alt="Foodie"
      width={size}
      height={size}
      style={{ objectFit: "contain" }}
    />
  );
}
