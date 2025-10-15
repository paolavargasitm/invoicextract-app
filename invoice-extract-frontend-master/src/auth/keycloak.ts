import Keycloak from "keycloak-js";

const KC_URL = import.meta.env.VITE_KEYCLOAK_URL || "http://localhost:8085";
const KC_REALM = import.meta.env.VITE_KEYCLOAK_REALM || "invoicextract";
const KC_CLIENT = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || "invoices-frontend";

export const keycloak = new Keycloak({
  url: KC_URL,
  realm: KC_REALM,
  clientId: KC_CLIENT,
});

export async function initKeycloak() {
  await keycloak.init({
    onLoad: "login-required",
    pkceMethod: "S256",
    checkLoginIframe: false,
  });
  setInterval(async () => {
    try {
      await keycloak.updateToken(30);
    } catch {
      keycloak.login();
    }
  }, 10000);
  return true;
}

export function authHeader(): Record<string, string> {
  return keycloak.token ? { Authorization: `Bearer ${keycloak.token}` } : {};
}
