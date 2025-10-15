import { useState } from "react";
import type { ChangeEvent } from "react";

export const ROLES = ["Administrador", "Editor", "Lector"] as const;
export type Role = typeof ROLES[number];

export type UserManagementOptions = {
    onSearch?: (query: string) => Promise<{ usuario: string; rol: Role } | null> | { usuario: string; rol: Role } | null;
    onUpdateRole?: (usuario: string, rol: Role) => Promise<void> | void;
};

export function useUserManagement(opts?: UserManagementOptions) {
    const [busqueda, setBusqueda] = useState<string>("");
    const [usuario, setUsuario] = useState<string | null>(null);
    const [rol, setRol] = useState<Role>("Administrador");
    const [loading, setLoading] = useState<"idle" | "searching" | "updating">("idle");

    const handleBusquedaChange = (e: ChangeEvent<HTMLInputElement>) => setBusqueda(e.target.value);
    const handleRolChange = (e: ChangeEvent<HTMLSelectElement>) => setRol(e.target.value as Role);

    const buscarUsuario = async () => {
        if (busqueda.trim() === "") {
            alert("Ingrese ID o correo para buscar");
            return;
        }
        setLoading("searching");
        try {
            if (opts?.onSearch) {
                const result = await opts.onSearch(busqueda.trim());
                if (result) {
                    setUsuario(result.usuario);
                    setRol(result.rol);
                } else {
                    setUsuario(null);
                    alert("Usuario no encontrado");
                }
            } else {
                setUsuario(busqueda.trim());
                setRol("Administrador");
            }
        } finally {
            setLoading("idle");
        }
    };

    const actualizarRol = async () => {
        if (!usuario) return;
        setLoading("updating");
        try {
            if (opts?.onUpdateRole) {
                await opts.onUpdateRole(usuario, rol);
            } else {
                alert(`Rol de usuario ${usuario} actualizado a ${rol}`);
            }
        } finally {
            setLoading("idle");
        }
    };

    return {
        busqueda,
        usuario,
        rol,
        roles: ROLES,
        loading,
        handleBusquedaChange,
        handleRolChange,
        buscarUsuario,
        actualizarRol,
    };
}
