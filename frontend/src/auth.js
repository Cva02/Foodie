import { useSyncExternalStore } from "react";

// Session token + username are kept in localStorage so a refresh keeps you logged in.
const TOKEN = "foodie_rider_token";
const USER = "foodie_rider_user";
const USER_ID = "foodie_rider_user_id";
const listeners = new Set();

const read = (key) => {
  try {
    return localStorage.getItem(key);
  } catch (_) {
    return null;
  }
};
const notify = () => listeners.forEach((l) => l());

export const auth = {
  getToken: () => read(TOKEN),
  getUser: () => read(USER),
  getRole: () => (read(TOKEN) ? "RIDER" : null),
  getUserId: () => read(USER_ID) || read(USER),
  setSession(token, username, userId = username) {
    try {
      localStorage.setItem(TOKEN, token);
      localStorage.setItem(USER, username);
      localStorage.setItem(USER_ID, userId);
    } catch (_) {
      /* storage blocked – session lasts until reload only */
    }
    notify();
  },
  clear() {
    try {
      localStorage.removeItem(TOKEN);
      localStorage.removeItem(USER);
      localStorage.removeItem(USER_ID);
    } catch (_) {
      /* ignore */
    }
    notify();
  },
  subscribe(fn) {
    listeners.add(fn);
    return () => listeners.delete(fn);
  },
};

/** Re-renders when the user logs in / out (or the server says the token expired). */
export function useAuth() {
  const token = useSyncExternalStore(auth.subscribe, auth.getToken);
  return { loggedIn: !!token, username: auth.getUser(), role: auth.getRole(), userId: auth.getUserId() };
}
