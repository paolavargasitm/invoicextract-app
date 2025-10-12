import { authHeader } from '../keycloak';

const BASE_URL = import.meta.env.VITE_MAPPINGS_BASE_URL || 'http://localhost:8082/invoice-mapping';

async function request(path, options = {}) {
  const url = path.startsWith('http') ? path : `${BASE_URL}${path}`;
  const headers = {
    'Content-Type': 'application/json',
    ...authHeader(),
    ...(options.headers || {}),
  };
  const res = await fetch(url, { ...options, headers });
  const text = await res.text();
  let body = null;
  try { body = text ? JSON.parse(text) : null; } catch { body = text; }
  if (!res.ok) {
    const message = body?.error?.message || text || `HTTP ${res.status}`;
    const code = body?.error?.code || res.status;
    const err = new Error(message);
    err.status = res.status; err.code = code; err.body = body; throw err;
  }
  return body;
}

export const erpsApi = {
  list: () => request('/api/erps'),
  create: (payload) => request('/api/erps', { method: 'POST', body: JSON.stringify(payload) }),
  changeStatus: (id, status) => request(`/api/erps/${id}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }),
};
