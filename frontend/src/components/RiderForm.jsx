import { useState } from "react";
import Modal from "./Modal.jsx";
import Field from "./Field.jsx";
import { api } from "../api.js";

const EMPTY = {
  name: "", phone: "", email: "", vehicle: "Bike", vehicleNumber: "", licenseNumber: "",
  city: "Visakhapatnam", avatar: "", status: "OFFLINE", statusNote: "",
};

/** Add (no `rider`) or edit (`rider` given) a delivery rider. Saves to MySQL through the API. */
export default function RiderForm({ rider, onSaved, onClose }) {
  const editing = !!rider;
  const [f, setF] = useState(() => (rider ? { ...EMPTY, ...rider } : EMPTY));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    const body = {
      name: f.name, avatar: f.avatar, phone: f.phone, email: f.email, vehicle: f.vehicle,
      vehicleNumber: f.vehicleNumber, licenseNumber: f.licenseNumber, city: f.city, status: f.status,
      statusNote: f.statusNote,
    };
    try {
      const saved = editing ? await api.put(`/api/riders/${rider.id}`, body) : await api.post("/api/riders", body);
      onSaved(saved);
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <Modal title={editing ? `Edit ${rider.name}` : "Add new rider"} onClose={onClose} wide>
      <form onSubmit={submit}>
        <div className="modal-body form-grid">
          <Field label="Full name" required><input value={f.name} onChange={set("name")} autoFocus /></Field>
          <Field label="Phone" required><input value={f.phone} onChange={set("phone")} placeholder="+91 98765 43210" /></Field>
          <Field label="Email" required><input type="email" value={f.email} onChange={set("email")} /></Field>
          <Field label="Service area / city" required><input value={f.city} onChange={set("city")} /></Field>
          <Field label="Vehicle" required>
            <select value={f.vehicle} onChange={set("vehicle")}>
              {["Bike", "Scooter", "Electric Bike", "Bicycle"].map((v) => <option key={v}>{v}</option>)}
              {!["Bike", "Scooter", "Electric Bike", "Bicycle"].includes(f.vehicle) && <option>{f.vehicle}</option>}
            </select>
          </Field>
          <Field label="Vehicle number" required><input value={f.vehicleNumber} onChange={set("vehicleNumber")} placeholder="AP 31 XX 0000" /></Field>
          <Field label="Driving licence no." required><input value={f.licenseNumber} onChange={set("licenseNumber")} /></Field>
          <Field label="Status">
            <select value={f.status} onChange={set("status")}>
              <option value="OFFLINE">Offline (approved)</option>
              <option value="ONLINE">Online</option>
              <option value="ON_DELIVERY">On delivery</option>
              <option value="PENDING">Pending approval</option>
              <option value="REJECTED">Rejected</option>
              <option value="SUSPENDED">Suspended</option>
            </select>
          </Field>
          {["REJECTED", "SUSPENDED"].includes(f.status) && (
            <Field label="Status note" hint="Shown to the rider when they try to sign in" full>
              <textarea required maxLength={1000} rows={3} value={f.statusNote || ""} onChange={set("statusNote")} />
            </Field>
          )}
          <Field label="Photo URL" hint="Optional – initials are shown if empty" full>
            <input value={f.avatar || ""} onChange={set("avatar")} placeholder="https://…" />
          </Field>
          {error && <div className="form-error field-full">{error}</div>}
        </div>
        <div className="modal-foot">
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button type="submit" className="btn btn-danger" disabled={busy}>
            {busy ? "Saving…" : editing ? "Save changes" : "Add rider"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
