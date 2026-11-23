
import { auth } from "./auth.js";

const CUSTOMER_TOKEN_KEY = "foodie_token";

export const getToken = () => localStorage.getItem(CUSTOMER_TOKEN_KEY);

export const setToken = (token) => {
  if (token) {
    localStorage.setItem(CUSTOMER_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(CUSTOMER_TOKEN_KEY);
  }
};

function isCustomerRequest(url) {
  return !url.startsWith("/api/") ||
    url.startsWith("/api/customer/") ||
    url.startsWith("/api/users/") ||
    url.startsWith("/api/notifications");
}

function getApiUrl(url) {
  return url.startsWith("/api/") ? url : `/api${url}`;
}

async function request(url, options = {}) {
  const customerRequest = isCustomerRequest(url);
  const token = customerRequest ? getToken() : auth.getToken();

  const headers = { ...(options.headers || {}) };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  if (options.body !== undefined && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  let res;

  try {
    res = await fetch(getApiUrl(url), { ...options, headers });
  } catch {
    throw new Error("Cannot reach the server. Is the Spring Boot backend running on port 8080?");
  }

  if (res.status === 401 && token) {
    if (customerRequest) {
      window.dispatchEvent(new Event("foodie-unauthorized"));
    } else if (!url.startsWith("/api/auth/login")) {
      auth.clear();
    }
  }

  const text = await res.text();
  let body = null;

  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    // Response is not JSON.
  }

  if (!res.ok) {
    throw new Error(
      (body && body.message) || `Request failed (${res.status})`
    );
  }

  return body;
}

async function requestBlob(url) {
  const token = auth.getToken();
  const headers = {};

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(getApiUrl(url), { headers });

  if (res.status === 401) {
    auth.clear();
  }

  if (!res.ok) {
    const text = await res.text();
    let body = null;

    try {
      body = text ? JSON.parse(text) : null;
    } catch {
      // Response is not JSON.
    }

    throw new Error(
      (body && body.message) || `Request failed (${res.status})`
    );
  }

  return res.blob();
}

const withBody = (method) => (url, body) =>
  request(url, {
    method,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

export const api = {
  get: (url) => request(url),

  getBlob: requestBlob,

  post: (url, body) =>
    request(url, {
      method: "POST",
      body: body === undefined ? undefined : JSON.stringify(body),
    }),

  postForm: (url, body) =>
    request(url, { method: "POST", body }),

  put: withBody("PUT"),

  patch: withBody("PATCH"),

  delete: (url) => request(url, { method: "DELETE" }),

  del: (url) => request(url, { method: "DELETE" }),
};
