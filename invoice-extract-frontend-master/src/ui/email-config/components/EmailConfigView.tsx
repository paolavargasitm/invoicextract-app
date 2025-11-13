import React from "react";
import "../styles/EmailConfig.css";

export type EmailConfigViewProps = {
    email: string;
    password: string;
    loading: "idle" | "validating" | "saving";
    isComplete: boolean;
    onEmailChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onPasswordChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSaveClick: () => void;
    activeUsername: string;
    activeConfiguredAt: string;
    successMessage: string;
    errorMessage: string;
    onRefreshActive: () => void;
};

const EmailConfigView: React.FC<EmailConfigViewProps> = ({
    email,
    password,
    loading,
    isComplete,
    onEmailChange,
    onPasswordChange,
    onSaveClick,
    activeUsername,
    activeConfiguredAt,
    successMessage,
    errorMessage,
    onRefreshActive,
}) => {
    return (
        <div className="ec__container">
            <h2 className="ec__title">Configuración de Correo para Automatización</h2>
            <p className="ec__subtitle">
                Esta sección permite registrar las credenciales del correo que usará el proceso RPA.
                Asegúrate de que los datos sean válidos y estén actualizados.
            </p>

            {/* Se removió el resumen superior del correo activo */}

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

            {/* La validación no está disponible por ahora */}

            {errorMessage && (
                <div className="ec__error" role="alert" aria-live="assertive" style={{
                    marginTop: 8,
                    padding: "8px 10px",
                    borderRadius: 8,
                    background: "#fef2f2",
                    color: "#991b1b",
                    border: "1px solid #fecaca"
                }}>
                    {errorMessage}
                </div>
            )}

            {successMessage && (
                <div className="ec__success" role="status" aria-live="polite" style={{
                    marginTop: 8,
                    padding: "8px 10px",
                    borderRadius: 8,
                    background: "#ecfdf5",
                    color: "#065f46",
                    border: "1px solid #a7f3d0"
                }}>
                    {successMessage}
                </div>
            )}

            <button
                type="button"
                className="ec__btn ec__btn--save"
                onClick={onSaveClick}
                disabled={loading === "saving" || !isComplete}
            >
                {loading === "saving" ? "Guardando..." : "Guardar Credenciales"}
            </button>

            <section style={{ marginTop: 16 }}>
                <div style={{
                    background: "#ffffff",
                    border: "1px solid var(--border, #e5e7eb)",
                    borderRadius: 12,
                    padding: 16,
                    boxShadow: "0 1px 2px rgba(0,0,0,0.04)",
                    display: "grid",
                    gap: 8
                }}>
                    <h3 style={{ margin: 0, color: "#111827", fontSize: 16 }}>Correo activo</h3>
                    <div style={{ display: "grid", gridTemplateColumns: "140px 1fr", gap: 8, alignItems: "center" }}>
                        <div style={{ color: "#6b7280", fontSize: 12 }}>Usuario</div>
                        <div style={{ color: "#111827" }}>{activeUsername || "—"}</div>
                        <div style={{ color: "#6b7280", fontSize: 12 }}>Configurado</div>
                        <div style={{ color: "#111827" }}>{activeConfiguredAt ? new Date(activeConfiguredAt).toLocaleString() : "—"}</div>
                    </div>
                    <div style={{ marginTop: 8 }}>
                        <button
                            type="button"
                            className="ec__btn ec__btn--validate"
                            onClick={onRefreshActive}
                            disabled={loading !== "idle"}
                        >
                            Consultar correo activo
                        </button>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default EmailConfigView;
