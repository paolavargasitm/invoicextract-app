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

    const handleNombre = (e: ChangeEvent<HTMLInputElement>) => setNombre(e.target.value);
    const handleEmail = (e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value);
    const handleContrasena = (e: ChangeEvent<HTMLInputElement>) => setContrasena(e.target.value);

    const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const data: RegisterData = { nombre, email, contrasena };
        console.log("Datos de registro:", data);
        onSubmit?.(data);
    };

    const isValid =
        nombre.trim().length > 0 &&
        email.trim().length > 0 &&
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
