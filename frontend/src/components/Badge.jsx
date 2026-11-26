import { pretty } from "../utils.js";

const TONES = {
  ACTIVE: "green",
  VERIFIED: "green",
  DELIVERED: "green",
  ONLINE: "green",
  PENDING: "amber",
  PREPARING: "amber",
  PLACED: "amber",
  AWAITING_RIDER: "amber",
  ACCEPTED: "amber",
  READY_FOR_PICKUP: "amber",
  RIDER_ASSIGNED: "blue",
  PICKED_UP: "blue",
  ON_THE_WAY: "blue",
  DISPATCHED: "blue",
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
