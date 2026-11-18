export default function InfoRow({ icon: Icon, label, children }) {
  return (
    <div className="info-row">
      <Icon size={16} />
      <div>
        <div className="info-label">{label}</div>
        <div className="info-value">{children}</div>
      </div>
    </div>
  );
}
