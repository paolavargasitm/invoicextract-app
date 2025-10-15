import { NavLink } from "react-router-dom";
import { keycloak } from "../../auth/keycloak";

function useRoles(): string[] {
  return ((keycloak.tokenParsed?.realm_access as any)?.roles || []) as string[];
}

export default function Navbar() {
  const roles = useRoles();
  const isAdmin = roles.includes("ADMIN");
  const isFinanzas = roles.includes("FINANZAS") || isAdmin;
  const isTecnico = roles.includes("TECNICO") || isAdmin;

  const logout = () => keycloak.logout({ redirectUri: window.location.origin });

  return (
    <header style={{ background: "#fff", borderBottom: "1px solid #e5e7eb" }}>
      <div style={{ maxWidth: 1120, margin: "0 auto", padding: "12px 16px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div style={{ width: 28, height: 28, borderRadius: 6, background: "var(--brand)" }} />
          <strong>InvoicExtract</strong>
        </div>
        <nav style={{ display: "flex", gap: 8, alignItems: "center" }}>
          <NavLink
            to="/"
            style={({ isActive }) => ({
              background: isActive ? "var(--brand)" : "#fff",
              color: isActive ? "#fff" : "inherit",
              border: "1px solid var(--border)",
              borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
              padding: "8px 12px",
              borderRadius: 8,
              textDecoration: "none"
            })}
          >Inicio</NavLink>
          {isAdmin && (
            <NavLink
              to="/email-config"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "#fff",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 8,
                textDecoration: "none"
              })}
            >Email Config</NavLink>
          )}
          {isFinanzas && (
            <NavLink
              to="/invoices"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "#fff",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 8,
                textDecoration: "none"
              })}
            >Dashboard Facturas</NavLink>
          )}
          {isTecnico && (
            <NavLink
              to="/mapping"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "#fff",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 8,
                textDecoration: "none"
              })}
            >Mapeos</NavLink>
          )}
          {isTecnico && (
            <NavLink
              to="/erp-config"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "#fff",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 8,
                textDecoration: "none"
              })}
            >ERPs</NavLink>
          )}
        </nav>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <span style={{ color: "#334155" }}>{keycloak.tokenParsed?.preferred_username}</span>
          <button onClick={logout} style={{ background: "var(--brand)", color: "#fff", border: 0, borderRadius: 8, padding: "8px 12px", cursor: "pointer" }}>Salir</button>
        </div>
      </div>
    </header>
  );
}
