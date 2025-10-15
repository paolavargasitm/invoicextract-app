import React from "react";
import "../styles/Login.css";

type Props = {
    email: string;
    password: string;
    onEmailChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onPasswordChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    isValid: boolean;
};

const LoginView: React.FC<Props> = ({
    email,
    password,
    onEmailChange,
    onPasswordChange,
    onSubmit,
    isValid,
}) => {
    return (
        <div className="login__container">
            <form className="login__form" onSubmit={onSubmit} noValidate>
                <h2 className="login__title">Iniciar sesión</h2>

                <label className="login__label" htmlFor="email">Correo electrónico</label>
                <input
                    id="email"
                    type="email"
                    className="login__input"
                    placeholder="Correo electrónico"
                    value={email}
                    onChange={onEmailChange}
                    autoComplete="email"
                    required
                />

                <label className="login__label" htmlFor="password">Contraseña</label>
                <input
                    id="password"
                    type="password"
                    className="login__input"
                    placeholder="Contraseña"
                    value={password}
                    onChange={onPasswordChange}
                    autoComplete="current-password"
                    required
                />

                <button className="login__button" type="submit" disabled={!isValid}>
                    Acceder
                </button>

                <a href="#" className="login__link">¿Olvidaste tu contraseña?</a>
            </form>
        </div>
    );
};

export default LoginView;
