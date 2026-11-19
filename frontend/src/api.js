import { auth } from "./auth.js";

// Thin wrapper around fetch for the Spring Boot admin API.
// Adds the admin's session token to every call and logs the admin out if the server says it expired.
async function request(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  const token = auth.getToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(url, { ...options, headers });

  // Session expired / server restarted -> back to the login page (RequireAuth does the redirect).
  if (res.status === 401 && !url.startsWith("/api/auth/login")) auth.clear();

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

async function requestBlob(url) {
  const headers = {};
  const token = auth.getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(url, { headers });
  if (res.status === 401) auth.clear();
  if (!res.ok) {
    const text = await res.text();
    let body = null;
    try {
      body = text ? JSON.parse(text) : null;
    } catch (_) {
      /* not JSON */
    }
    throw new Error((body && body.message) || `Request failed (${res.status})`);
  }
  return res.blob();
}

const withBody = (method) => (url, body) =>
  request(url, { method, body: body === undefined ? undefined : JSON.stringify(body) });

export const api = {
  get: (url) => request(url),
  getBlob: requestBlob,
  post: withBody("POST"),
  postForm: (url, body) => request(url, { method: "POST", body }),
  put: withBody("PUT"),
  patch: withBody("PATCH"),
  delete: (url) => request(url, { method: "DELETE" }),
};
