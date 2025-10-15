import React from "react";
import LoginView from "../components/LoginView";
import { useLogin } from "../hooks/useLogin";

const LoginPage: React.FC = () => {
  const { email, password, handleEmail, handlePassword, handleSubmit, isValid } = useLogin();

  return (
    <LoginView
      email={email}
      password={password}
      onEmailChange={handleEmail}
      onPasswordChange={handlePassword}
      onSubmit={handleSubmit}
      isValid={isValid}
    />
  );
};

export default LoginPage;
