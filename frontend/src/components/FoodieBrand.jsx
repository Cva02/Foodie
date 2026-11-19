import Logo from "./Logo.jsx";

export default function FoodieBrand({ subtitle, className = "" }) {
  return (
    <span className={`foodie-brand ${className}`.trim()}>
      <Logo />
      <span className="foodie-brand-copy">
        <strong className="foodie-wordmark">foodie</strong>
        {subtitle && <small>{subtitle}</small>}
      </span>
    </span>
  );
}
