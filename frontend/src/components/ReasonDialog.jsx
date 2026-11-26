import { useState } from "react";
import Modal from "./Modal.jsx";

export default function ReasonDialog({ title, message, confirmLabel, onConfirm, onClose }) {
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const submit = async (event) => {
    event.preventDefault();
    const reason = note.trim();
    if (!reason) {
      setError("Enter a reason before continuing.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      await onConfirm(reason);
    } catch (submitError) {
      setError(submitError.message);
      setBusy(false);
    }
  };

  return (
    <Modal title={title} onClose={busy ? () => {} : onClose}>
      <form onSubmit={submit}>
        <div className="modal-body">
          <p>{message}</p>
          <label className="field">
            <span className="field-label">Reason for the applicant</span>
            <textarea autoFocus required maxLength={1000} rows={4} value={note}
              onChange={(event) => setNote(event.target.value)} />
          </label>
          <div className="muted small">{note.length}/1000</div>
          {error && <div className="form-error">{error}</div>}
        </div>
        <div className="modal-foot">
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button type="submit" className="btn btn-danger" disabled={busy || !note.trim()}>
            {busy ? "Saving…" : confirmLabel}
          </button>
        </div>
      </form>
    </Modal>
  );
}
