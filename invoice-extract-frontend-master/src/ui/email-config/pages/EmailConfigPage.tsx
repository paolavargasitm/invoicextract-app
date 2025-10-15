import React from "react";
import EmailConfigView from "../components/EmailConfigView";
import { useEmailConfig } from "../hooks/useEmailConfig";

const EmailConfigPage: React.FC = () => {
    const {
        email,
        password,
        validationResult,
        loading,
        isComplete,
        handleEmail,
        handlePassword,
        validarConexion,
        guardarCredenciales,
    } = useEmailConfig();

    return (
        <EmailConfigView
            email={email}
            password={password}
            validationResult={validationResult}
            loading={loading}
            isComplete={isComplete}
            onEmailChange={handleEmail}
            onPasswordChange={handlePassword}
            onValidateClick={validarConexion}
            onSaveClick={guardarCredenciales}
        />
    );
};

export default EmailConfigPage;
