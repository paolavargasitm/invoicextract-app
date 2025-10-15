import { authHeader, keycloak } from "../auth/keycloak";

export async function http(path: string, init: RequestInit = {}): Promise<Response> {
  const base = import.meta.env.VITE_BACKEND_BASE_URL || "http://localhost:8080";
  const url = path.startsWith("http") ? path : `${base}${path}`;
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...authHeader(),
    ...(init.headers as Record<string, string> | undefined),
  };
  try {
    await keycloak.updateToken(30);
  } catch {
    // fallthrough; next call will redirect if needed
  }
  return fetch(url, { ...init, headers });
}
