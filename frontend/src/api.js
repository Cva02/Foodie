// Thin wrapper around fetch for the Spring Boot admin API.
async function request(url, options) {
  const res = await fetch(url, options);
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body.message) message = body.message;
    } catch (_) {
      /* ignore */
    }
    throw new Error(message);
  }
  return res.json();
}

export const api = {
  get: (url) => request(url),
  patch: (url) => request(url, { method: "PATCH" }),
  patchJson: (url, body) => request(url, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  }),
};
