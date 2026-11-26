
const SESSION_KEY = "foodie_seller_session";

// Seller authentication is independent of Admin and Customer.
export function getSellerSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY));
  } catch {
    return null;
  }
}

export function saveSellerSession(session) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSellerSession() {
  localStorage.removeItem(SESSION_KEY);
}

function getApiUrl(url) {
  return url.startsWith("/api/") ? url : `/api${url}`;
}

async function request(url, options = {}, authenticated = true) {
  const headers = { ...(options.headers || {}) };

  if (authenticated) {
    const token = getSellerSession()?.token;
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  if (
    options.body !== undefined &&
    !(options.body instanceof FormData)
  ) {
    headers["Content-Type"] = "application/json";
  }

  let response;

  try {
    response = await fetch(getApiUrl(url), {
      ...options,
      headers,
    });
  } catch {
    throw new Error(
      "Cannot reach the Foodie server. Please check whether the backend is running on port 8080."
    );
  }

  const responseText = await response.text();
  let data = null;

  try {
    data = responseText ? JSON.parse(responseText) : null;
  } catch {
    // Response was not JSON.
  }

  if (
    authenticated &&
    (response.status === 401 || response.status === 403)
  ) {
    clearSellerSession();
    window.dispatchEvent(new Event("foodie-seller-unauthorized"));
  }

  if (!response.ok) {
    const message = [data?.message, data?.reason]
      .filter(Boolean)
      .join(": ");

    throw new Error(
      message || `Request failed (${response.status})`
    );
  }

  return data;
}

const jsonBody = (method, url, body) =>
  request(url, {
    method,
    body: JSON.stringify(body),
  });

export const sellerApi = {
  // Authentication
  login: async (email, password) => {
    const session = await request(
      "/api/seller/auth/login",
      {
        method: "POST",
        body: JSON.stringify({ email, password }),
      },
      false
    );

    saveSellerSession(session);
    return session;
  },

  logout: () => clearSellerSession(),
  getSession: getSellerSession,

    // Public seller registration
  uploadApplicationDocument: (type, file) => {
    const form = new FormData();
    form.append("type", type);
    form.append("file", file);

    return request(
      "/api/seller-applications/upload-document",
      {
        method: "POST",
        body: form,
      },
      false
    );
  },

  registerApplication: (data) =>
    request(
      "/api/seller-applications/register",
      {
        method: "POST",
        body: JSON.stringify(data),
      },
      false
    ),

  // Dashboard summary
  getSummary: () => request("/api/seller/summary"),
  getProfile: () => request("/api/seller/profile"),
  updateAvailability: (open) =>
    jsonBody("PATCH", "/api/seller/profile/availability", { open }),

  // Orders
  getOrders: (group = "all") =>
    request(`/api/seller/orders?group=${encodeURIComponent(group)}`),

  updateOrderStatus: (orderId, status, reason) =>
    jsonBody("PUT", `/api/seller/orders/${orderId}/status`, {
      status,
      ...(reason ? { reason } : {}),
    }),

  // Menu management
  getMenu: () => request("/api/seller/menu"),

  addMenuItem: (item) =>
    jsonBody("POST", "/api/seller/menu", item),

  updateMenuItem: (id, item) =>
    jsonBody("PUT", `/api/seller/menu/${id}`, item),

  setStock: (id, inStock) =>
    jsonBody("PATCH", `/api/seller/menu/${id}/availability`, {
      inStock,
    }),

  deleteMenuItem: (id) =>
    request(`/api/seller/menu/${id}`, {
      method: "DELETE",
    }),

  // Dish image upload
  uploadImage: (file) => {
    const form = new FormData();
    form.append("file", file);

    return request("/api/seller/menu/upload-image", {
      method: "POST",
      body: form,
    });
  },
};
