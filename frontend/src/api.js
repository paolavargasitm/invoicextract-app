// Simple API client that always sends Authorization: Bearer <token>
// Usage:
//   import { apiFetch, api } from './api';
//   apiFetch('/api/invoices').then(...)
//   api.get('/api/configs').then(...)

import { authHeader } from './keycloak';

const BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL || 'http://localhost:8080/invoicextract';

// fetch wrapper
export function apiFetch(path, options = {}) {
  const url = path.startsWith('http') ? path : `${BASE_URL}${path}`;
  const headers = {
    'Content-Type': 'application/json',
    ...authHeader(),
    ...(options.headers || {}),
  };
  return fetch(url, { ...options, headers });
}

// convenience helpers
export const api = {
  get: (path) => apiFetch(path, { method: 'GET' }),
  post: (path, body) => apiFetch(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => apiFetch(path, { method: 'PUT', body: JSON.stringify(body) }),
  del: (path) => apiFetch(path, { method: 'DELETE' }),
};

// Optional: quick smoke test you can call from anywhere
export async function testBackendAccess() {
  try {
    const res = await api.get('/api/invoices');
    return { status: res.status, ok: res.ok };
  } catch (e) {
    return { error: String(e) };
  }
}
