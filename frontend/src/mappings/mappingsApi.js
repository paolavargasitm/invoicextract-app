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
  let body = null;
  const text = await res.text();
  try { body = text ? JSON.parse(text) : null; } catch { body = text; }
  if (!res.ok) {
    const message = body?.error?.message || text || `HTTP ${res.status}`;
    const code = body?.error?.code || res.status;
    const err = new Error(message);
    err.status = res.status;
    err.code = code;
    err.body = body;
    throw err;
  }
  return body;
}

export const mappingsApi = {
  list: (erp, status = 'ACTIVE') => request(`/api/mappings?erp=${encodeURIComponent(erp)}&status=${encodeURIComponent(status)}`),
  create: (payload) => request('/api/mappings', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id, payload) => request(`/api/mappings/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  changeStatus: (id, status) => request(`/api/mappings/${id}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }),
};
