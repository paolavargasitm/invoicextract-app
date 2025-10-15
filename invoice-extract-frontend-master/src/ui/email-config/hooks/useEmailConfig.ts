import { useState } from "react";
import type { ChangeEvent } from "react";

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

    const handleEmail = (e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value);
    const handlePassword = (e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value);

    const isComplete = email.trim() !== "" && password.trim() !== "";

    const validarConexion = async () => {
        if (!isComplete) {
            setValidationResult("Por favor, completa ambos campos.");
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
        if (!isComplete) {
            alert("Por favor, completa ambos campos antes de guardar.");
            return;
        }
        setLoading("saving");
        try {
            if (opts?.onSave) {
                await opts.onSave({ email, password });
            } else {
                alert("Credenciales guardadas correctamente.");
            }
        } finally {
            setLoading("idle");
        }
    };

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
    };
}
