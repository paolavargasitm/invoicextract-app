import { useState } from "react";
import type { FormEvent, ChangeEvent } from "react";

export function useLogin(onSubmit?: (email: string, password: string) => void) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleEmail = (e: ChangeEvent<HTMLInputElement>) => setEmail(e.target.value);
    const handlePassword = (e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value);

    const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        console.log("Email:", email);
        console.log("Contraseña:", password);
        onSubmit?.(email, password);
    };

    const isValid = email.trim().length > 0 && password.trim().length > 0;

    return { email, password, handleEmail, handlePassword, handleSubmit, isValid };
}
