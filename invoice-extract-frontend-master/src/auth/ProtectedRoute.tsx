import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { keycloak } from "./keycloak";

export function hasAnyRole(roles: string[], required: string[]): boolean {
  if (!roles || roles.length === 0) return false;
  if (!required || required.length === 0) return true;
  return required.some(r => roles.includes(r));
}

export default function ProtectedRoute({ children, roles }: { children: ReactNode, roles?: string[] }) {
  const userRoles: string[] = (keycloak.tokenParsed?.realm_access as any)?.roles || [];
  const allowed = hasAnyRole(userRoles, roles || []);
  if (!keycloak.authenticated) {
    keycloak.login({ redirectUri: window.location.href });
    return null;
  }
  if (!allowed) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}
