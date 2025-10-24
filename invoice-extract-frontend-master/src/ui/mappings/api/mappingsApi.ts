import { authHeader } from "../../../auth/keycloak";
import { parseBody, toUserError } from "../../../utils/httpError";

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
  const body: any = parseBody(text);
  if (!res.ok) {
    throw toUserError(res, body);
  }
  return body;
}

export const mappingsApi = {
  list: (erp: string, status: string = 'ACTIVE') => request(`/api/mappings?erp=${encodeURIComponent(erp)}&status=${encodeURIComponent(status)}`),
  create: (payload: any) => request('/api/mappings', { method: 'POST', body: JSON.stringify(payload) }),
  update: (id: string, payload: any) => request(`/api/mappings/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  changeStatus: (id: string, status: string) => request(`/api/mappings/${id}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }),
};
