import React from "react";
import "../styles/UserManagement.css";
import type { Role } from "../hooks/useUserManagement";

export type UserManagementViewProps = {
    busqueda: string;
    usuario: string | null;
    rol: Role;
    roles: ReadonlyArray<Role>;
    loading: "idle" | "searching" | "updating";
    onBusquedaChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onRolChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    onBuscar: () => void;
    onActualizar: () => void;
};

const UserManagementView: React.FC<UserManagementViewProps> = ({
    busqueda,
    usuario,
    rol,
    roles,
    loading,
    onBusquedaChange,
    onRolChange,
    onBuscar,
    onActualizar,
}) => {
    return (
        <div className="um__container">
            <h2 className="um__title">Gestión de Usuarios</h2>

            <div className="um__search">
                <input
                    className="um__search-input"
                    type="text"
                    placeholder="Buscar por ID o Correo"
                    value={busqueda}
                    onChange={onBusquedaChange}
                />
                <button
                    className="um__btn um__btn--primary"
                    onClick={onBuscar}
                    disabled={loading === "searching"}
                >
                    {loading === "searching" ? "Buscando..." : "Buscar Usuario"}
                </button>
            </div>

            {usuario && (
                <div className="um__edit">
                    <h3 className="um__edit-title">Editar Rol de Usuario</h3>

                    <input
                        className="um__input"
                        type="text"
                        value={usuario}
                        readOnly
                        aria-label="Usuario seleccionado"
                    />

                    <select
                        className="um__select"
                        value={rol}
                        onChange={onRolChange}
                        aria-label="Rol del usuario"
                    >
                        {roles.map((r) => (
                            <option key={r} value={r}>
                                {r}
                            </option>
                        ))}
                    </select>

                    <button
                        className="um__btn um__btn--primary"
                        onClick={onActualizar}
                        disabled={loading === "updating"}
                    >
                        {loading === "updating" ? "Actualizando..." : "Actualizar Rol"}
                    </button>
                </div>
            )}
        </div>
    );
};

export default UserManagementView;
