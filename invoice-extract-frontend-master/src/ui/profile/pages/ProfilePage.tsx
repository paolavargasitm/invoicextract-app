import React from "react";
import ProfileView from "../components/ProfileView";
import { useProfile } from "../hooks/useProfile";

const ProfilePage: React.FC = () => {
    const { perfil, handleChange, handleSubmit, isValid } = useProfile();

    return (
        <ProfileView
            nombre={perfil.nombre}
            apellidos={perfil.apellidos}
            celular={perfil.celular}
            cargo={perfil.cargo}
            isValid={isValid}
            onChange={handleChange}
            onSubmit={handleSubmit}
        />
    );
};

export default ProfilePage;
