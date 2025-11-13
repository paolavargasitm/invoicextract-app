import React from "react";
import EmailConfigView from "../components/EmailConfigView";
import { useEmailConfig } from "../hooks/useEmailConfig";

const EmailConfigPage: React.FC = () => {
    const {
        email,
        password,
        loading,
        isComplete,
        handleEmail,
        handlePassword,
        guardarCredenciales,
        activeUsername,
        activeConfiguredAt,
        successMessage,
        errorMessage,
        refreshActiveEmail,
    } = useEmailConfig();

    return (
        <EmailConfigView
            email={email}
            password={password}
            loading={loading}
            isComplete={isComplete}
            onEmailChange={handleEmail}
            onPasswordChange={handlePassword}
            onSaveClick={guardarCredenciales}
            activeUsername={activeUsername}
            activeConfiguredAt={activeConfiguredAt}
            successMessage={successMessage}
            errorMessage={errorMessage}
            onRefreshActive={refreshActiveEmail}
        />
    );
};

export default EmailConfigPage;
