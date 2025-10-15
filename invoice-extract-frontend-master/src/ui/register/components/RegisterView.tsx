import React from "react";
import "../styles/Register.css";

export type RegisterViewProps = {
    nombre: string;
    email: string;
    contrasena: string;
    onNombreChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onEmailChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onContrasenaChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    isValid: boolean;
};

const RegisterView: React.FC<RegisterViewProps> = ({
    nombre,
    email,
    contrasena,
    onNombreChange,
    onEmailChange,
    onContrasenaChange,
    onSubmit,
    isValid,
}) => {
    return (
        <div className="register__container">
            <h2 className="register__title">Registro de Usuario</h2>
            <form className="register__form" onSubmit={onSubmit} noValidate>
                <input
                    type="text"
                    placeholder="Nombre completo"
                    value={nombre}
                    onChange={onNombreChange}
                    className="register__input"
                    autoComplete="name"
                    required
                />
                <input
                    type="email"
                    placeholder="Correo electrónico"
                    value={email}
                    onChange={onEmailChange}
                    className="register__input"
                    autoComplete="email"
                    required
                />
                <input
                    type="password"
                    placeholder="Contraseña temporal"
                    value={contrasena}
                    onChange={onContrasenaChange}
                    className="register__input"
                    autoComplete="new-password"
                    required
                />
                <button type="submit" className="register__button" disabled={!isValid}>
                    Registrarse
                </button>
            </form>
        </div>
    );
};

export default RegisterView;
