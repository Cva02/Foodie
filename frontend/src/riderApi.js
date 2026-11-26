import { useSyncExternalStore } from "react";

const RIDER_SESSION_KEY = "foodie_rider_session";
const listeners = new Set();

function notify() {
  listeners.forEach((listener) => listener());
}

export function getRiderSession() {
  try {
    return JSON.parse(localStorage.getItem(RIDER_SESSION_KEY));
  } catch {
    return null;
  }
}

export function saveRiderSession(session) {
  try {
    localStorage.setItem(RIDER_SESSION_KEY, JSON.stringify(session));
  } catch {
    // Session persists in memory if storage is blocked.
  }
  notify();
}

export function clearRiderSession() {
  try {
    localStorage.removeItem(RIDER_SESSION_KEY);
  } catch {
    // Ignore storage errors.
  }
  notify();
}

export function useRiderAuth() {
  const session = useSyncExternalStore(
    (listener) => {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    () => localStorage.getItem(RIDER_SESSION_KEY)
  );
  const parsed = (() => {
    try {
      return session ? JSON.parse(session) : null;
    } catch {
      return null;
    }
  })();

  return {
    loggedIn: Boolean(parsed?.token),
    session: parsed,
    name: parsed?.name || "",
    riderId: parsed?.riderId || "",
  };
}

function getApiUrl(url) {
  return url.startsWith("/api/") ? url : `/api${url}`;
}

async function request(url, options = {}, authenticated = true) {
  const headers = { ...(options.headers || {}) };

  if (authenticated) {
    const token = getRiderSession()?.token;
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  let response;
  try {
    response = await fetch(getApiUrl(url), { ...options, headers });
  } catch {
    throw new Error(
      "Cannot reach the Foodie server. Please check whether the backend is running on port 8080."
    );
  }

  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    // Non-JSON response.
  }

  if (authenticated && (response.status === 401 || response.status === 403)) {
    const msg = data?.message || "";
    if (response.status === 401 || /not active|suspended|not approved/i.test(msg)) {
      clearRiderSession();
    }
  }

  if (!response.ok) {
    const message = [data?.message, data?.reason].filter(Boolean).join(": ");
    throw new Error(message || `Request failed (${response.status})`);
  }

  return data;
}

export const riderApi = {
  login: async (email, password) => {
    const session = await request(
      "/api/rider/auth/login",
      {
        method: "POST",
        body: JSON.stringify({ email, password }),
      },
      false
    );
    saveRiderSession(session);
    return session;
  },
  logout: () => clearRiderSession(),
  register: (formData) =>
    request(
      "/api/rider-applications/register",
      { method: "POST", body: formData },
      false
    ),
  getProfile: () => request("/api/rider/profile"),
  updateProfile: (body) =>
    request("/api/rider/profile", {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  updateStatus: (status) =>
    request(`/api/rider/status?value=${encodeURIComponent(status)}`, {
      method: "PATCH",
    }),
  updateLocation: (latitude, longitude) =>
    request("/api/rider/location", {
      method: "POST",
      body: JSON.stringify({ latitude, longitude }),
    }),
  getOrders: () => request("/api/rider/orders"),
  performOrderAction: (orderId, action, otp) =>
    request(`/api/rider/orders/${orderId}/${action}`, {
      method: "PATCH",
      body: otp ? JSON.stringify({ otp }) : undefined,
    }),
  getSupportTickets: () => request("/api/rider/support"),
  createSupportTicket: (subject, message) =>
    request("/api/rider/support", {
      method: "POST",
      body: JSON.stringify({ subject, message }),
    }),
  getNotifications: () => request("/api/rider/notifications"),
  markNotificationRead: (id) =>
    request(`/api/rider/notifications/${id}/read`, { method: "PATCH" }),
};
