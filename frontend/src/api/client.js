import axios from 'axios';

const BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:5000').replace(/\/$/, '');
const SESSION_KEY = 'foodie_seller_session';

// ---------------------------------------------------------------------------
// Session (sellerId, restaurantName, JWT token...) in localStorage
// ---------------------------------------------------------------------------
export function getSession() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY));
  } catch {
    return null;
  }
}
export function saveSession(session) {
  try {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  } catch { /* storage unavailable */ }
}
export function clearSession() {
  try {
    localStorage.removeItem(SESSION_KEY);
  } catch { /* storage unavailable */ }
}

// ---------------------------------------------------------------------------
// Axios instance -> Node.js gateway (port 5000)
// ---------------------------------------------------------------------------
const http = axios.create({ baseURL: BASE_URL, timeout: 8000 });

http.interceptors.request.use((config) => {
  const token = getSession()?.token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || '';
    const isAuthCall = url.startsWith('/api/auth/login') || url.startsWith('/api/auth/register');
    if (err.response?.status === 401 && !isAuthCall) {
      clearSession();
      window.dispatchEvent(new Event('foodie:unauthorized'));
    }
    return Promise.reject(err);
  }
);

const isBackendDown = (err) => !err.response || [502, 503, 504].includes(err.response.status);

function backendUnavailableError() {
  const err = new Error('Seller service is unavailable. Please try again when the backend is online.');
  err.response = { status: 503, data: { message: err.message } };
  return err;
}

const sid = () => getSession()?.sellerId;
const seller = (path = '') => `/api/seller/${sid()}${path}`;
const body = (promise) => promise.then((r) => r.data);

// ---------------------------------------------------------------------------
// Public API used by the UI
// ---------------------------------------------------------------------------
export const api = {
  login: async (identifier, password) => {
    try {
      return await body(http.post('/api/auth/login', { identifier, password }));
    } catch (err) {
      if (isBackendDown(err)) throw backendUnavailableError();
      throw err;
    }
  },

  register: async (payload) => {
    try {
      return await body(http.post('/api/auth/register', payload));
    } catch (err) {
      if (isBackendDown(err)) throw backendUnavailableError();
      throw err;
    }
  },

  requestPasswordOtp: (identifier) => body(http.post('/api/auth/forgot-password/request', { identifier })),

  verifyPasswordOtp: (identifier, otp) => body(http.post('/api/auth/forgot-password/verify', { identifier, otp })),

  resetPassword: (identifier, otp, password) =>
    body(http.post('/api/auth/forgot-password/reset', { identifier, otp, password })),

  logout: () => body(http.post('/api/auth/logout')),

  getSummary: () => body(http.get(seller('/summary'))),

  /** group: pending | active | queue | history | all */
  getOrders: (group = 'all') =>
    body(http.get(seller('/orders'), { params: { group } })),

  updateOrderStatus: (orderId, status) =>
    body(http.put(seller(`/orders/${orderId}/status`), { status })),

  getMenu: () => body(http.get(seller('/menu'))),

  addMenuItem: (item) => body(http.post(seller('/menu'), item)),

  updateMenuItem: (id, item) =>
    body(http.put(seller(`/menu/${id}`), item)),

  setStock: (id, inStock) =>
    body(http.patch(seller(`/menu/${id}/stock`), { inStock })),

  deleteMenuItem: (id) => body(http.delete(seller(`/menu/${id}`))),

  uploadImage: (file) => {
    const form = new FormData();
    form.append('image', file);
    return body(http.post('/api/upload', form, { timeout: 20000 }));
  },

  uploadRegistrationCertificate: (file) => {
    const form = new FormData();
    form.append('certificate', file);
    return body(http.post('/api/auth/upload-certificate', form, { timeout: 20000 }));
  },

  getEarnings: () => body(http.get(seller('/earnings'))),

  requestPayout: () => body(http.post(seller('/payouts/request'))),

  getProfile: () => body(http.get(seller('/profile'))),

  getFeedback: () => body(http.get(seller('/feedback'))),

  updateProfile: (profile) => body(http.put(seller('/profile'), profile)),

  setOnline: (online) => body(http.patch(seller('/online'), { online })),
};
