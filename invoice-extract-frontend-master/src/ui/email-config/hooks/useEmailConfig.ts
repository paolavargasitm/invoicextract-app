import { useEffect, useState } from "react";
import type { ChangeEvent } from "react";
import { http } from "../../../lib/http";

export type EmailConfigData = {
    email: string;
    password: string;
};

type ValidateFn = (data: EmailConfigData) => Promise<boolean | string> | boolean | string;
type SaveFn = (data: EmailConfigData) => Promise<void> | void;

export function useEmailConfig(opts?: { onValidate?: ValidateFn; onSave?: SaveFn }) {
    const [email, setEmail] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const [validationResult, setValidationResult] = useState<string>("");
    const [loading, setLoading] = useState<"idle" | "validating" | "saving">("idle");
    const [activeUsername, setActiveUsername] = useState<string>("");
    const [activeConfiguredAt, setActiveConfiguredAt] = useState<string>("");
    const [successMessage, setSuccessMessage] = useState<string>("");

    const isValidEmail = (value: string): boolean => {
        const v = value.trim();
        if (!v || v.length > 254) return false;
        if (/[\r\n\t]/.test(v)) return false;
        if (v.includes(" ")) return false;
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) return false;
        const [local, domain] = v.split("@");
        if (!local || !domain) return false;
        if (local.length === 0 || local.length > 64) return false;
        return true;
    };

    const normalizeEmail = (value: string): string => {
        const v = value.trim().replace(/[\r\n\t]/g, "");
        const parts = v.split("@");
        if (parts.length !== 2) return v;
        return `${parts[0]}@${parts[1].toLowerCase()}`;
    };

    const handleEmail = (e: ChangeEvent<HTMLInputElement>) => setEmail(normalizeEmail(e.target.value));
    const handlePassword = (e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value);

    const isComplete = isValidEmail(email) && password.trim() !== "";

    const validarConexion = async () => {
        if (!email.trim() || !password.trim()) {
            setValidationResult("Por favor, completa ambos campos.");
            return;
        }
        if (!isValidEmail(email)) {
            setValidationResult("Correo inválido.");
            return;
        }
        setLoading("validating");
        try {
            if (opts?.onValidate) {
                const result = await opts.onValidate({ email, password });
                if (result === true) setValidationResult("Conexión válida");
                else if (result === false) setValidationResult("No se pudo validar la conexión");
                else setValidationResult(String(result));
            } else {
                setValidationResult("Conexión válida");
            }
        } catch (err: unknown) {
            setValidationResult("Error al validar la conexión");
        } finally {
            setLoading("idle");
        }
    };

    const guardarCredenciales = async () => {
        if (!email.trim() || !password.trim()) {
            alert("Por favor, completa ambos campos antes de guardar.");
            return;
        }
        if (!isValidEmail(email)) {
            alert("Correo inválido. Verifica el formato (usuario@dominio).");
            return;
        }
        setLoading("saving");
        try {
            if (opts?.onSave) {
                await opts.onSave({ email, password });
            } else {
                // Guardar contra el backend protegido (mismo host de invoices)
                const resp = await http("/api/config/email", {
                    method: "POST",
                    body: JSON.stringify({ username: normalizeEmail(email), password })
                });
                if (!resp.ok) {
                    const txt = await resp.text();
                    throw new Error(txt || "Error al guardar credenciales");
                }
                setSuccessMessage("Credenciales guardadas correctamente.");
                // limpiar formulario
                setEmail("");
                setPassword("");
                // refrescar correo activo
                await fetchActiveEmail();
            }
        } finally {
            setLoading("idle");
        }
    };

    const fetchActiveEmail = async () => {
        try {
            // 1) Obtener config activa desde el backend (mismo host de invoices)
            const resp = await http("/api/config/email/active");
            if (!resp.ok) return;
            const data = await resp.json();
            const username: string = data.username ?? "";
            setActiveUsername(username);

            // 2) Intentar obtener la fecha de configuración consultando el filtro por status ACTIVE
            if (username) {
                const listResp = await http(`/api/config/email/filter?username=${encodeURIComponent(username)}&status=ACTIVE`);
                if (listResp.ok) {
                    const list = await listResp.json();
                    // buscar el más reciente por createdAt
                    if (Array.isArray(list) && list.length > 0) {
                        const sorted = [...list].sort((a, b) => new Date(b.createdAt || b.createdDate || 0).getTime() - new Date(a.createdAt || a.createdDate || 0).getTime());
                        const top = sorted[0];
                        const configuredAt: string = top.createdAt || top.createdDate || "";
                        setActiveConfiguredAt(configuredAt);
                    }
                }
            }
        } catch {
            // ignore errores de consulta
        }
    };

    useEffect(() => {
        // No auto-fetch; el usuario consultará manualmente
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return {
        email,
        password,
        validationResult,
        loading,
        isComplete,
        handleEmail,
        handlePassword,
        validarConexion,
        guardarCredenciales,
        activeUsername,
        activeConfiguredAt,
        successMessage,
        refreshActiveEmail: fetchActiveEmail,
    };
}
