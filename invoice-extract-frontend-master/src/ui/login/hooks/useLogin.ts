import { useState } from "react";
import type { FormEvent, ChangeEvent } from "react";

export function useLogin(onSubmit?: (email: string, password: string) => void) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

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

    const handleEmail = (e: ChangeEvent<HTMLInputElement>) => setEmail(normalizeEmail(e.target.value));
    const handlePassword = (e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value);

    const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        console.log("Email:", email);
        console.log("Contraseña:", password);
        onSubmit?.(normalizeEmail(email), password);
    };

    const isValid = isValidEmail(email) && password.trim().length > 0;

    return { email, password, handleEmail, handlePassword, handleSubmit, isValid };
}
