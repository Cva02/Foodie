import { auth } from "./auth.js";

// Adds the rider session token to API calls and clears expired sessions.
async function request(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  const token = auth.getToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(url, { ...options, headers });

  if (res.status === 401 && !url.endsWith("/auth/login") && !url.endsWith("/auth/register")) auth.clear();

  const text = await res.text();
  let body = null;
  try {
    body = text ? JSON.parse(text) : null;
  } catch (_) {
    /* not JSON */
  }

  if (!res.ok) throw new Error((body && body.message) || `Request failed (${res.status})`);
  return body;
}

const withBody = (method) => (url, body) =>
  request(url, { method, body: body === undefined ? undefined : JSON.stringify(body) });

export const api = {
  get: (url) => request(url),
  post: withBody("POST"),
  postForm: (url, body) => request(url, { method: "POST", body }),
  patch: withBody("PATCH"),
};
