import React from "react";
import RegisterView from "../components/RegisterView";
import { useRegister } from "../hooks/useRegister";

const RegisterPage: React.FC = () => {
    const {
        nombre,
        email,
        contrasena,
        handleNombre,
        handleEmail,
        handleContrasena,
        handleSubmit,
        isValid,
    } = useRegister();

    return (
        <RegisterView
            nombre={nombre}
            email={email}
            contrasena={contrasena}
            onNombreChange={handleNombre}
            onEmailChange={handleEmail}
            onContrasenaChange={handleContrasena}
            onSubmit={handleSubmit}
            isValid={isValid}
        />
    );
};

export default RegisterPage;
