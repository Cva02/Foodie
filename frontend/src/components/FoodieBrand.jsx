import Logo from "./Logo.jsx";

export default function FoodieBrand({ subtitle, className = "", logoSize = 40 }) {
  return (
    <span className={`foodie-brand ${className}`.trim()}>
      <Logo size={logoSize} />
      <span className="foodie-brand-copy">
        <strong className="foodie-wordmark">foodie</strong>
        {subtitle && <small>{subtitle}</small>}
      </span>
    </span>
  );
}
