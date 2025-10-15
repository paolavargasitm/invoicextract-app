import React from "react";
import "../styles/Profile.css";

export type ProfileViewProps = {
    nombre: string;
    apellidos: string;
    celular: string;
    cargo: string;
    isValid: boolean;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
};

const ProfileView: React.FC<ProfileViewProps> = ({
    nombre,
    apellidos,
    celular,
    cargo,
    isValid,
    onChange,
    onSubmit,
}) => {
    return (
        <div className="pf__container">
            <h2 className="pf__title">Gestión de Mi Perfil</h2>

            <form className="pf__form" onSubmit={onSubmit} noValidate>
                <label className="pf__label" htmlFor="nombre">Nombre</label>
                <input
                    id="nombre"
                    name="nombre"
                    type="text"
                    className="pf__input"
                    placeholder="Nombre"
                    value={nombre}
                    onChange={onChange}
                    autoComplete="given-name"
                    required
                />

                <label className="pf__label" htmlFor="apellidos">Apellidos</label>
                <input
                    id="apellidos"
                    name="apellidos"
                    type="text"
                    className="pf__input"
                    placeholder="Apellidos"
                    value={apellidos}
                    onChange={onChange}
                    autoComplete="family-name"
                    required
                />

                <label className="pf__label" htmlFor="celular">Celular</label>
                <input
                    id="celular"
                    name="celular"
                    type="tel"
                    className="pf__input"
                    placeholder="Celular"
                    value={celular}
                    onChange={onChange}
                    autoComplete="tel"
                    required
                />

                <label className="pf__label" htmlFor="cargo">Cargo</label>
                <input
                    id="cargo"
                    name="cargo"
                    type="text"
                    className="pf__input"
                    placeholder="Cargo"
                    value={cargo}
                    onChange={onChange}
                    required
                />

                <button className="pf__button" type="submit" disabled={!isValid}>
                    Actualizar Perfil
                </button>
            </form>
        </div>
    );
};

export default ProfileView;
