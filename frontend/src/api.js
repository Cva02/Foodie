const TOKEN_KEY = 'foodie_token';
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (t) => (t ? localStorage.setItem(TOKEN_KEY, t) : localStorage.removeItem(TOKEN_KEY));

async function request(method, url, body) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = 'Bearer ' + token;
  let res;
  try {
    res = await fetch('/api' + url, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch {
    throw new Error('Cannot reach the server. Is the Spring Boot backend running on port 8080?');
  }
  const text = await res.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { /* not json */ }
  }
  if (!res.ok) {
    if (res.status === 401 && token) window.dispatchEvent(new Event('foodie-unauthorized'));
    throw new Error((data && data.message) || `Something went wrong (${res.status})`);
  }
  return data;
}

export const api = {
  get: (u) => request('GET', u),
  post: (u, b) => request('POST', u, b === undefined ? {} : b),
  put: (u, b) => request('PUT', u, b),
  del: (u) => request('DELETE', u),
};
