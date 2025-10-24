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

export const erpsApi = {
  list: () => request('/api/erps'),
  create: (payload: any) => request('/api/erps', { method: 'POST', body: JSON.stringify(payload) }),
  changeStatus: (id: string, status: string) => request(`/api/erps/${id}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }),
};
