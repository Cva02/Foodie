import { useState } from "react";
import Modal from "./Modal.jsx";
import Field from "./Field.jsx";
import { api } from "../api.js";

const EMPTY = {
  name: "", ownerName: "", cuisine: "", phone: "", email: "", address: "",
  city: "Visakhapatnam", fssaiNumber: "", image: "", ownerAvatar: "",
  status: "ACTIVE", statusNote: "", open: true,
};

/** Add (no `seller`) or edit (`seller` given) a restaurant. Saves to MySQL through the API. */
export default function SellerForm({ seller, onSaved, onClose }) {
  const editing = !!seller;
  const [f, setF] = useState(() => (seller ? { ...EMPTY, ...seller } : EMPTY));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const set = (k) => (e) => setF({ ...f, [k]: e.target.type === "checkbox" ? e.target.checked : e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    const body = {
      name: f.name, ownerName: f.ownerName, ownerAvatar: f.ownerAvatar, image: f.image,
      cuisine: f.cuisine, phone: f.phone, email: f.email, address: f.address, city: f.city,
      fssaiNumber: f.fssaiNumber, status: f.status, statusNote: f.statusNote, open: f.open,
    };
    try {
      const saved = editing ? await api.put(`/api/sellers/${seller.id}`, body) : await api.post("/api/sellers", body);
      onSaved(saved);
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  return (
    <Modal title={editing ? `Edit ${seller.name}` : "Add new seller"} onClose={onClose} wide>
      <form onSubmit={submit}>
        <div className="modal-body form-grid">
          <Field label="Restaurant name" required><input value={f.name} onChange={set("name")} autoFocus /></Field>
          <Field label="Cuisine" required><input value={f.cuisine} onChange={set("cuisine")} placeholder="e.g. Italian • Pizza" /></Field>
          <Field label="Owner name" required><input value={f.ownerName} onChange={set("ownerName")} /></Field>
          <Field label="Phone" required><input value={f.phone} onChange={set("phone")} placeholder="+91 98765 43210" /></Field>
          <Field label="Email" required><input type="email" value={f.email} onChange={set("email")} /></Field>
          <Field label="FSSAI licence no." required><input value={f.fssaiNumber} onChange={set("fssaiNumber")} /></Field>
          <Field label="Address" required full><input value={f.address} onChange={set("address")} /></Field>
          <Field label="City" required><input value={f.city} onChange={set("city")} /></Field>
          <Field label="Status">
            <select value={f.status} onChange={set("status")}>
              <option value="ACTIVE">Active</option>
              <option value="PENDING">Pending approval</option>
              <option value="SUSPENDED">Suspended</option>
            </select>
          </Field>
          {f.status === "SUSPENDED" && (
            <Field label="Status note" hint="Shown to the seller when they try to sign in" full>
              <textarea required maxLength={1000} rows={3} value={f.statusNote || ""} onChange={set("statusNote")} />
            </Field>
          )}
          <Field label="Cover image URL" hint="Optional – a gradient is shown if empty" full>
            <input value={f.image || ""} onChange={set("image")} placeholder="https://…" />
          </Field>
          <Field label="Owner photo URL" hint="Optional" full>
            <input value={f.ownerAvatar || ""} onChange={set("ownerAvatar")} placeholder="https://…" />
          </Field>
          <label className="check field-full">
            <input type="checkbox" checked={!!f.open} onChange={set("open")} /> Restaurant is open for orders
          </label>
          {error && <div className="form-error field-full">{error}</div>}
        </div>
        <div className="modal-foot">
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={busy}>Cancel</button>
          <button type="submit" className="btn btn-danger" disabled={busy}>
            {busy ? "Saving…" : editing ? "Save changes" : "Add seller"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
