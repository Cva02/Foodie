export function Loading() {
  return <div className="state">Loading…</div>;
}

export function ErrorBox({ message }) {
  return (
    <div className="state state-error">
      Couldn't load data: {message}
      <div className="small">Is the Spring Boot server running on port 8080?</div>
    </div>
  );
}
