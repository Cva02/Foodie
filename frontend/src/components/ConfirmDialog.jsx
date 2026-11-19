import { useState } from "react";
import Modal from "./Modal.jsx";

/** "Are you sure?" box for deletes. onConfirm may be async; errors are shown inside the box. */
export default function ConfirmDialog({ title, message, confirmLabel = "Delete", onConfirm, onClose }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const run = async () => {
    setBusy(true);
    setError("");
    try {
      await onConfirm();
    } catch (e) {
      setError(e.message);
      setBusy(false);
    }
  };

  return (
    <Modal title={title} onClose={busy ? () => {} : onClose}>
      <div className="modal-body">
        <p>{message}</p>
        {error && <div className="form-error">{error}</div>}
      </div>
      <div className="modal-foot">
        <button className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
        <button className="btn btn-danger" onClick={run} disabled={busy}>
          {busy ? "Deleting…" : confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
