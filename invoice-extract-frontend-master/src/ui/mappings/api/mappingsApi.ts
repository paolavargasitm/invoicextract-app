import { authHeader } from "../../../auth/keycloak";

const BASE_URL = (import.meta.env.VITE_MAPPINGS_BASE_URL || 'http://localhost:8082/invoice-mapping');

async function request(path: string, options: RequestInit = {}) {
  const url = path.startsWith('http') ? path : `${BASE_URL}${path}`;
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...authHeader(),
    ...(options.headers || {}),
  } as any;
  const res = await fetch(url, { ...options, headers });
  const text = await res.text();
  let body: any = null;
  try { body = text ? JSON.parse(text) : null; } catch { body = text; }
  if (!res.ok) {
    const message = body?.error?.message || text || `HTTP ${res.status}`;
    const code = body?.error?.code || res.status;
    const err: any = new Error(message);
    err.status = res.status; err.code = code; err.body = body; throw err;
  }
  return body;
}

export const mappingsApi = {
  list: (erp: string, status: string = 'ACTIVE') => request(`/api/mappings?erp=${encodeURIComponent(erp)}&status=${encodeURIComponent(status)}`),
  create: (payload: any) => request('/api/mappings', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: string, payload: any) => request(`/api/mappings/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  changeStatus: (id: string, status: string) => request(`/api/mappings/${id}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }),
};
