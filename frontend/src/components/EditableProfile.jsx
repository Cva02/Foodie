import { useState } from "react";
import { Check, Pencil, X } from "lucide-react";
import { api } from "../api.js";

export default function EditableProfile({ profile, endpoint, fields, onSaved }) {
  const [editing, setEditing] = useState(false);
  const [values, setValues] = useState({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const startEditing = () => {
    setValues(Object.fromEntries(fields.map(({ name }) => [name, profile[name] ?? ""])));
    setError("");
    setEditing(true);
  };

  const save = async (event) => {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await api.patchJson(endpoint, values);
      setEditing(false);
      onSaved();
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="profile-details">
      <div className="profile-details-heading">
        <h3>Personal details</h3>
        {!editing && (
          <button className="btn btn-light" type="button" onClick={startEditing}>
            <Pencil size={15} /> Edit details
          </button>
        )}
      </div>
      {editing ? (
        <form onSubmit={save}>
          <div className="profile-edit-grid">
            {fields.map(({ name, label, type = "text" }) => (
              <label className="profile-field" key={name}>
                {label}
                <input
                  type={type}
                  value={values[name]}
                  onChange={(event) => setValues({ ...values, [name]: event.target.value })}
                  required
                />
              </label>
            ))}
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
          <div className="profile-edit-actions">
            <button className="btn btn-danger" type="submit" disabled={busy}>
              <Check size={16} /> {busy ? "Saving…" : "Save changes"}
            </button>
            <button className="btn btn-light" type="button" onClick={() => setEditing(false)} disabled={busy}>
              <X size={16} /> Cancel
            </button>
          </div>
        </form>
      ) : (
        <div className="info-grid">
          {fields.map(({ name, label, icon: Icon }) => (
            <div className="info-row" key={name}>
              {Icon && <Icon size={16} />}
              <div>
                <div className="info-label">{label}</div>
                <div className="info-value">{profile[name] || "—"}</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
