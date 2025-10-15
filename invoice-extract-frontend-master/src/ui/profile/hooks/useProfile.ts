import { useState } from "react";
import type { ChangeEvent, FormEvent } from "react";

export type ProfileData = {
    nombre: string;
    apellidos: string;
    celular: string;
    cargo: string;
};

type SubmitFn = (data: ProfileData) => Promise<void> | void;

export function useProfile(opts?: { onSubmit?: SubmitFn }) {
    const [perfil, setPerfil] = useState<ProfileData>({
        nombre: "",
        apellidos: "",
        celular: "",
        cargo: "",
    });

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        const key = name as keyof ProfileData;
        setPerfil((prev) => ({ ...prev, [key]: value }));
    };

    const isValid =
        perfil.nombre.trim() !== "" &&
        perfil.apellidos.trim() !== "" &&
        perfil.celular.trim() !== "" &&
        perfil.cargo.trim() !== "";

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (!isValid) return;
        if (opts?.onSubmit) {
            await opts.onSubmit(perfil);
        } else {
            console.log("Perfil actualizado:", perfil);
            alert("Perfil actualizado correctamente");
        }
    };

    return { perfil, handleChange, handleSubmit, isValid };
}
