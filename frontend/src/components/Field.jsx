/** Label + input / select used by the seller & rider forms. */
export default function Field({ label, required, hint, full, children }) {
  return (
    <label className={`field ${full ? "field-full" : ""}`}>
      <span className="field-label">
        {label}
        {required && <b className="req"> *</b>}
      </span>
      {children}
      {hint && <span className="field-hint">{hint}</span>}
    </label>
  );
}
