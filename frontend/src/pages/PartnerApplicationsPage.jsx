import { useEffect, useMemo, useState } from "react";
import { Download, RefreshCw } from "lucide-react";
import { ErrorBox } from "../components/State.jsx";
import Badge from "../components/Badge.jsx";
import ReasonDialog from "../components/ReasonDialog.jsx";
import { api } from "../api.js";

export default function PartnerApplicationsPage({ role }) {
  const isSeller = role === "SELLER";
  const partnerLabel = isSeller ? "Seller" : "Rider";
  const endpoint = isSeller ? "/api/seller-applications" : "/api/rider-applications";
  const [applications, setApplications] = useState([]);
  const [filter, setFilter] = useState("PENDING");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState("");
  const [downloadBusy, setDownloadBusy] = useState("");
  const [rejectionTarget, setRejectionTarget] = useState(null);

  const loadApplications = async () => {
    setError("");
    try {
      setApplications(await api.get(endpoint));
    } catch (loadError) {
      setError(loadError.message);
    }
  };

  useEffect(() => {
    let active = true;
    const refresh = async () => {
      try {
        const result = await api.get(endpoint);
        if (active) {
          setApplications(result);
          setError("");
        }
      } catch (loadError) {
        if (active) setError(loadError.message);
      }
    };
    refresh();
    const timer = window.setInterval(refresh, 15000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [endpoint]);

  const rows = useMemo(() => applications.filter((application) =>
    filter === "PENDING" ? application.status === "PENDING" : application.status !== "PENDING"),
  [applications, filter]);
  const pendingCount = applications.filter((application) => application.status === "PENDING").length;
  const reviewedCount = applications.length - pendingCount;

  const review = async (application, action, note) => {
    setBusy(application.id);
    setError("");
    setNotice("");
    try {
      const updated = await api.patch(`${endpoint}/${application.id}/${action}`,
        action === "reject" ? { note } : undefined);
      setApplications((current) => current.map((item) => item.id === updated.id ? updated : item));
      window.dispatchEvent(new Event("foodie-applications-updated"));
      setNotice(action === "approve"
        ? `${isSeller ? updated.restaurantName : updated.name} approved. ${partnerLabel} ID: ${isSeller ? updated.sellerId : updated.riderId}.`
        : `${partnerLabel} application rejected.`);
      return updated;
    } catch (reviewError) {
      setError(reviewError.message);
      throw reviewError;
    } finally {
      setBusy("");
    }
  };

  const downloadDocument = async (application, documentType) => {
    const key = `${application.id}:${documentType}`;
    setDownloadBusy(key);
    setError("");
    try {
      const route = isSeller
        ? `${endpoint}/${application.id}/${documentType === "gst" ? "gst-document" : "fssai-document"}`
        : `${endpoint}/${application.id}/${documentType === "photo" ? "photo" : "license-document"}`;
      const file = await api.getBlob(route);
      const extension = file.type === "application/pdf" ? "pdf"
        : file.type === "image/png" ? "png" : "jpg";
      const url = URL.createObjectURL(file);
      const link = document.createElement("a");
      link.href = url;
      link.download = `${partnerLabel.toLowerCase()}-${application.id}-${documentType}.${extension}`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (downloadError) {
      setError(downloadError.message);
    } finally {
      setDownloadBusy("");
    }
  };

  return (
    <>
      <div className="page-actions">
        <span className="muted">{pendingCount} pending {partnerLabel.toLowerCase()} application{pendingCount === 1 ? "" : "s"}</span>
        <button className="btn btn-outline" type="button" onClick={loadApplications}>
          <RefreshCw size={15} /> Refresh
        </button>
      </div>
      {notice && <div className="portal-notice" role="status">{notice}</div>}
      {error && <ErrorBox message={error} />}

      <div className="application-filters" role="tablist" aria-label={`${partnerLabel} application status`}>
        <button type="button" role="tab" aria-selected={filter === "PENDING"}
          className={filter === "PENDING" ? "application-filter selected" : "application-filter"}
          onClick={() => setFilter("PENDING")}>
          Pending review <span>{pendingCount}</span>
        </button>
        <button type="button" role="tab" aria-selected={filter === "REVIEWED"}
          className={filter === "REVIEWED" ? "application-filter selected" : "application-filter"}
          onClick={() => setFilter("REVIEWED")}>
          Reviewed <span>{reviewedCount}</span>
        </button>
      </div>

      {rows.length === 0 ? (
        <div className="empty">
          {filter === "PENDING" ? `No pending ${partnerLabel.toLowerCase()} applications.` : `No reviewed ${partnerLabel.toLowerCase()} applications.`}
        </div>
      ) : (
        <div className="seller-application-list">
          {rows.map((application) => {
            const name = isSeller ? application.restaurantName : application.name;
            const profileId = isSeller ? application.sellerId : application.riderId;
            return (
              <article className="seller-application-card" key={application.id}>
                <div className="seller-application-head">
                  <div>
                    <h3>{name}</h3>
                    <span className="muted small">Submitted {new Date(application.submittedAt).toLocaleString()}</span>
                  </div>
                  <Badge value={application.status} />
                </div>
                <div className="seller-application-details">
                  {isSeller ? (
                    <>
                      <span><b>Owner:</b> {application.ownerName}</span>
                      <span><b>Email:</b> {application.email}</span>
                      <span><b>Phone:</b> {application.phone}</span>
                      <span><b>Cuisine:</b> {application.cuisine}</span>
                      <span><b>Address:</b> {application.address}, {application.city}</span>
                      <span><b>FSSAI:</b> {application.fssaiNumber}</span>
                    </>
                  ) : (
                    <>
                      <span><b>Email:</b> {application.email}</span>
                      <span><b>Phone:</b> {application.phone}</span>
                      <span><b>Vehicle:</b> {application.vehicle} · {application.vehicleNumber}</span>
                      <span><b>Driving licence:</b> {application.licenseNumber}</span>
                      <span><b>Service area:</b> {application.city}</span>
                    </>
                  )}
                </div>
                {application.reviewNote && (
                  <div className="application-review-note">
                    <b>Review note:</b> {application.reviewNote}
                  </div>
                )}

                <div className="application-downloads">
                  {!isSeller && application.photoUrl && (
                    <img src={application.photoUrl} alt={`${name} rider`} />
                  )}
                  {isSeller ? (
                    <div className="application-document-actions">
                      {application.hasFssaiDocument ? (
                        <button type="button" className="btn btn-outline btn-sm"
                          disabled={downloadBusy === `${application.id}:fssai`}
                          onClick={() => downloadDocument(application, "fssai")}>
                          <Download size={14} /> {downloadBusy === `${application.id}:fssai` ? "Downloading…" : "Download FSSAI certificate"}
                        </button>
                      ) : <span className="muted small">FSSAI certificate not attached.</span>}
                      {application.hasGstDocument && (
                        <button type="button" className="btn btn-outline btn-sm"
                          disabled={downloadBusy === `${application.id}:gst`}
                          onClick={() => downloadDocument(application, "gst")}>
                          <Download size={14} /> {downloadBusy === `${application.id}:gst` ? "Downloading…" : "Download GST certificate"}
                        </button>
                      )}
                    </div>
                  ) : (
                    <div className="application-document-actions">
                      {application.hasLicenseDocument && (
                        <button type="button" className="btn btn-outline btn-sm"
                          disabled={downloadBusy === `${application.id}:license`}
                          onClick={() => downloadDocument(application, "license")}>
                          <Download size={14} /> {downloadBusy === `${application.id}:license` ? "Downloading…" : "Download driving licence"}
                        </button>
                      )}
                      {application.photoUrl && (
                        <button type="button" className="btn btn-outline btn-sm"
                          disabled={downloadBusy === `${application.id}:photo`}
                          onClick={() => downloadDocument(application, "photo")}>
                          <Download size={14} /> {downloadBusy === `${application.id}:photo` ? "Downloading…" : "Download rider photo"}
                        </button>
                      )}
                      {!application.hasLicenseDocument && <span className="muted small">Driving licence unavailable.</span>}
                    </div>
                  )}
                </div>

                {profileId && <p className="strong">{partnerLabel} ID: {profileId}</p>}
                {application.status === "PENDING" && (
                  <div className="seller-application-actions">
                    <button className="btn btn-green" disabled={busy === application.id}
                      onClick={() => review(application, "approve").catch(() => {})}>
                      {busy === application.id ? "Processing…" : "Approve and create account"}
                    </button>
                    <button className="btn btn-danger" disabled={busy === application.id}
                      onClick={() => setRejectionTarget(application)}>
                      Reject
                    </button>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
      {rejectionTarget && (
        <ReasonDialog
          title={`Reject ${partnerLabel.toLowerCase()} application`}
          message="Add a note explaining why this application was rejected. The applicant will see it on their application status page."
          confirmLabel="Reject application"
          onClose={() => setRejectionTarget(null)}
          onConfirm={async (note) => {
            await review(rejectionTarget, "reject", note);
            setRejectionTarget(null);
          }}
        />
      )}
    </>
  );
}
