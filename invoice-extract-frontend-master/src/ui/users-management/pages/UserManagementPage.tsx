import React from "react";
import UserManagementView from "../components/UserManagementView";
import { useUserManagement } from "../hooks/useUserManagement";
import type { Role } from "../hooks/useUserManagement";

async function searchUserApi(query: string) {
    return { usuario: query, rol: "Administrador" as Role };
}

async function updateUserRoleApi(usuario: string, rol: Role) {
    alert(`Rol de usuario ${usuario} actualizado a ${rol}`);
}

const UserManagementPage: React.FC = () => {
    const {
        busqueda,
        usuario,
        rol,
        roles,
        loading,
        handleBusquedaChange,
        handleRolChange,
        buscarUsuario,
        actualizarRol,
    } = useUserManagement({
        onSearch: searchUserApi,
        onUpdateRole: updateUserRoleApi,
    });

    return (
        <UserManagementView
            busqueda={busqueda}
            usuario={usuario}
            rol={rol}
            roles={roles}
            loading={loading}
            onBusquedaChange={handleBusquedaChange}
            onRolChange={handleRolChange}
            onBuscar={buscarUsuario}
            onActualizar={actualizarRol}
        />
    );
};

export default UserManagementPage;
