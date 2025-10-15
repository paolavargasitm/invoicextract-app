import React from "react";
import "../styles/EmailConfig.css";

export type EmailConfigViewProps = {
    email: string;
    password: string;
    validationResult: string;
    loading: "idle" | "validating" | "saving";
    isComplete: boolean;
    onEmailChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onPasswordChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onValidateClick: () => void;
    onSaveClick: () => void;
};

const EmailConfigView: React.FC<EmailConfigViewProps> = ({
    email,
    password,
    validationResult,
    loading,
    isComplete,
    onEmailChange,
    onPasswordChange,
    onValidateClick,
    onSaveClick,
}) => {
    return (
        <div className="ec__container">
            <h2 className="ec__title">Configuración de Correo para Automatización</h2>
            <p className="ec__subtitle">
                Esta sección permite registrar las credenciales del correo que usará el proceso RPA.
                Asegúrate de que los datos sean válidos y estén actualizados.
            </p>

            <label className="ec__label" htmlFor="email">Correo electrónico</label>
            <input
                id="email"
                type="email"
                className="ec__input"
                placeholder="usuario@empresa.com"
                value={email}
                onChange={onEmailChange}
                autoComplete="email"
                required
            />

            <label className="ec__label" htmlFor="password">Contraseña</label>
            <input
                id="password"
                type="password"
                className="ec__input"
                placeholder="********"
                value={password}
                onChange={onPasswordChange}
                autoComplete="current-password"
                required
            />

            <div className="ec__actions">
                <button
                    type="button"
                    className="ec__btn ec__btn--validate"
                    onClick={onValidateClick}
                    disabled={loading === "validating" || !isComplete}
                >
                    {loading === "validating" ? "Validando..." : "Validar Conexión"}
                </button>
                <span className="ec__result" aria-live="polite">
                    [{validationResult || "Resultado de validación aparecerá aquí"}]
                </span>
            </div>

            <button
                type="button"
                className="ec__btn ec__btn--save"
                onClick={onSaveClick}
                disabled={loading === "saving" || !isComplete}
            >
                {loading === "saving" ? "Guardando..." : "Guardar Credenciales"}
            </button>
        </div>
    );
};

export default EmailConfigView;
