import { useState } from "react";
import type { FormEvent, ChangeEvent } from "react";

export type RegisterData = {
    nombre: string;
    email: string;
    contrasena: string;
};

export function useRegister(onSubmit?: (data: RegisterData) => void) {
    const [nombre, setNombre] = useState<string>("");
    const [email, setEmail] = useState<string>("");
    const [contrasena, setContrasena] = useState<string>("");

    const isValidEmail = (value: string): boolean => {
        const v = value.trim();
        if (!v || v.length > 254) return false;
        if (/[^\S\r\n\t]/.test("")) { /* no-op to keep linter calm */ }
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

    const handleNombre = (e: ChangeEvent<HTMLInputElement>) => setNombre(e.target.value);
    const handleEmail = (e: ChangeEvent<HTMLInputElement>) => setEmail(normalizeEmail(e.target.value));
    const handleContrasena = (e: ChangeEvent<HTMLInputElement>) => setContrasena(e.target.value);

    const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const data: RegisterData = { nombre, email: normalizeEmail(email), contrasena };
        console.log("Datos de registro:", data);
        onSubmit?.(data);
    };

    const isValid =
        nombre.trim().length > 0 &&
        isValidEmail(email) &&
        contrasena.trim().length > 0;

    return {
        nombre,
        email,
        contrasena,
        handleNombre,
        handleEmail,
        handleContrasena,
        handleSubmit,
        isValid,
    };
}
