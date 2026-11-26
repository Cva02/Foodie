
import { useEffect, useState } from "react";
import { Store, User, Mail, Phone, MapPin, FileText, RefreshCw, Power } from "lucide-react";
import { sellerApi } from "../api";
import { useToast } from "../../context.jsx";
import "../seller.css";

export default function ProfilePage() {
  const toast = useToast();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [toggling, setToggling] = useState(false);

  async function loadProfile() {
    setLoading(true);
    try {
      const data = await sellerApi.getProfile();
      setProfile(data);
    } catch (error) {
      toast(error.message || "Unable to load seller profile", "error");
    } finally {
      setLoading(false);
    }
  }

  async function toggleOpen() {
    if (!profile) return;
    setToggling(true);
    try {
      const updated = await sellerApi.updateAvailability(!profile.open);
      setProfile(updated);
      toast(
        updated.open
          ? "Restaurant is now OPEN for customer orders"
          : "Restaurant is now CLOSED",
        "success"
      );
    } catch (error) {
      toast(error.message || "Unable to update availability", "error");
    } finally {
      setToggling(false);
    }
  }

  useEffect(() => {
    loadProfile();
  }, []);

  if (loading) {
    return <main className="seller-profile-page">Loading profile...</main>;
  }

  if (!profile) {
    return (
      <main className="seller-profile-page">
        <p>Unable to load your profile.</p>
        <button className="seller-btn seller-btn-dark" onClick={loadProfile}>
          <RefreshCw size={16} /> Try Again
        </button>
      </main>
    );
  }

  const details = [
    { label: "Restaurant Name", value: profile.restaurantName, icon: Store },
    { label: "Owner Name", value: profile.ownerName, icon: User },
    { label: "Email Address", value: profile.email, icon: Mail },
    { label: "Phone Number", value: profile.phone, icon: Phone },
    { label: "Cuisine", value: profile.cuisine, icon: Store },
    { label: "Address", value: profile.address, icon: MapPin },
    { label: "City", value: profile.city, icon: MapPin },
    { label: "FSSAI Number", value: profile.fssaiNumber, icon: FileText },
  ];

  return (
    <main className="seller-profile-page">
      <header className="seller-page-heading">
        <div>
          <h1>Restaurant Profile</h1>
          <p className="seller-muted">View your registered restaurant details.</p>
        </div>
        <button className="seller-btn seller-btn-dark" onClick={loadProfile}>
          <RefreshCw size={16} /> Refresh
        </button>
      </header>

      <section className="seller-profile-card">
        {profile.restaurantImage && (
          <img
            className="seller-profile-cover"
            src={profile.restaurantImage}
            alt={profile.restaurantName || "Restaurant"}
          />
        )}

        <div className="seller-profile-heading">
          <div className="seller-profile-avatar"><Store size={30} /></div>
          <div>
            <h2>{profile.restaurantName || "My Restaurant"}</h2>
            <p className="seller-muted">{profile.ownerName || "Restaurant Owner"}</p>
          </div>
          <span className={`seller-profile-status seller-profile-${(profile.status || "unknown").toLowerCase()}`}>
            {profile.status || "Unknown"}
          </span>
        </div>

        {profile.status === "SUSPENDED" && profile.statusNote && (
          <div className="seller-profile-warning">
            <strong>Account suspended</strong>
            <p>{profile.statusNote}</p>
            <small>Contact Foodie administration for assistance.</small>
          </div>
        )}

        <div className="seller-profile-details">
          {details.map(({ label, value, icon: Icon }) => (
            <div className="seller-profile-detail" key={label}>
              <span className="seller-profile-detail-icon"><Icon size={18} /></span>
              <div>
                <small>{label}</small>
                <strong>{value || "Not provided"}</strong>
              </div>
            </div>
          ))}
        </div>

        <div className="seller-profile-open-status">
          <div>
            <strong>Restaurant availability</strong>
            <span className={`seller-profile-open-pill ${profile.open ? "open" : "closed"}`}>
              {profile.open ? "Open for orders" : "Closed"}
            </span>
          </div>
          <button
            type="button"
            className={`seller-btn ${profile.open ? "seller-btn-dark" : "seller-btn-primary"}`}
            onClick={toggleOpen}
            disabled={toggling || profile.status !== "ACTIVE"}
          >
            <Power size={15} />
            {toggling ? "Updating…" : profile.open ? "Close Restaurant" : "Open Restaurant"}
          </button>
        </div>
      </section>
    </main>
  );
}
