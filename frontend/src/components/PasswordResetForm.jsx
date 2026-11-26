import { useState } from "react";
import { api } from "../api.js";

/** Admin password recovery: request an MSG91 OTP, then set a new password. */
export default function PasswordResetForm({ onCancel, onComplete }) {
  const [username, setUsername] = useState("");
  const [requestId, setRequestId] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [requested, setRequested] = useState(false);
  const [complete, setComplete] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const requestOtp = async (event) => {
    if (event) event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const response = await api.post("/api/auth/password-reset/request", { username });
      setRequestId(response.requestId);
      setNotice(response.message);
      setRequested(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const resetPassword = async (event) => {
    event.preventDefault();
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      const response = await api.post("/api/auth/password-reset/confirm",
        { requestId, username, otp, newPassword });
      setNotice(response.message);
      setComplete(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <form className="auth-card password-reset-card"
      onSubmit={requested && !complete ? resetPassword : requestOtp}>
      <h2>Reset admin password</h2>
      <p className="muted">
        {complete ? "Your password has been changed. You can now sign in with it."
          : requested
            ? "Enter the one-time code sent to the configured admin recovery mobile and choose a new password."
            : "We’ll send an OTP to the admin recovery mobile configured on the backend."}
      </p>
      <label className="field"><span className="field-label">Admin username</span>
        <input required type="text" maxLength="60" autoComplete="username" value={username}
          onChange={(event) => setUsername(event.target.value)} placeholder="Admin username"
          disabled={requested} />
      </label>
      {requested && !complete && (
        <>
          <label className="field"><span className="field-label">OTP</span>
            <input required inputMode="numeric" autoComplete="one-time-code" maxLength="8"
              value={otp} onChange={(event) => setOtp(event.target.value.replace(/\D/g, ""))} />
          </label>
          <label className="field"><span className="field-label">New password</span>
            <input required minLength="8" maxLength="100" type="password" autoComplete="new-password"
              value={newPassword} onChange={(event) => setNewPassword(event.target.value)} />
          </label>
          <label className="field"><span className="field-label">Confirm new password</span>
            <input required minLength="8" maxLength="100" type="password" autoComplete="new-password"
              value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} />
          </label>
        </>
      )}
      {notice && <div className="password-reset-note" role="status">{notice}</div>}
      {error && <div className="form-error" role="alert">{error}</div>}
      {!complete && (
        <button className="btn btn-danger btn-block" type="submit" disabled={busy}>
          {busy ? "Please wait…" : requested ? "Reset password" : "Send OTP"}
        </button>
      )}
      {requested && !complete && (
        <>
          <button className="password-reset-link" type="button" disabled={busy} onClick={requestOtp}>
            Resend OTP
          </button>
          <button className="password-reset-link" type="button"
            onClick={() => {
              setRequested(false);
              setRequestId("");
              setUsername("");
              setOtp("");
              setNewPassword("");
              setConfirmPassword("");
              setNotice("");
              setError("");
            }}>
            Change admin username
          </button>
        </>
      )}
      <button className="password-reset-link" type="button" onClick={complete ? onComplete : onCancel}>
        {complete ? "Back to sign in" : "Cancel"}
      </button>
    </form>
  );
}
