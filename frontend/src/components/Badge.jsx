import { pretty } from "../utils.js";

const TONES = {
  ACTIVE: "green",
  VERIFIED: "green",
  DELIVERED: "green",
  ONLINE: "green",
  PENDING: "amber",
  PREPARING: "amber",
  AWAITING_RIDER: "amber",
  ON_THE_WAY: "blue",
  ON_DELIVERY: "blue",
  OFFLINE: "gray",
  BLOCKED: "red",
  SUSPENDED: "red",
  REJECTED: "red",
  CANCELLED: "red",
};

export default function Badge({ value, label }) {
  return <span className={`badge badge-${TONES[value] || "gray"}`}>{label || pretty(value)}</span>;
}
