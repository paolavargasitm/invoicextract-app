import { authHeader } from "../../../auth/keycloak";
import { parseBody, toUserError } from "../../../utils/httpError";

const BASE_URL = (import.meta.env.VITE_BACKEND_BASE_URL || "http://localhost:8080/invoicextract");

async function request(path: string, options: RequestInit = {}) {
    const url = path.startsWith("http") ? path : `${BASE_URL}${path}`;
    const headers: HeadersInit = {
        "Content-Type": "application/json",
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

export const invoicesApi = {
    changeStatus: (id: number | string, status: "PENDING" | "APPROVED" | "REJECTED") =>
        request(`/api/invoices/${id}/status?status=${encodeURIComponent(status)}`, {
            method: "PUT",
        }),
};
