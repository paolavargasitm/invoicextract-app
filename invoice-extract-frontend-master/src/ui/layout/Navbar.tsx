import { NavLink } from "react-router-dom";
import { useEffect, useState } from "react";
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

  // Theme toggle (light/dark)
  const [theme, setTheme] = useState<string>(() => localStorage.getItem("theme") || "light");

  useEffect(() => {
    const root = document.documentElement;
    if (theme === "dark") {
      root.setAttribute("data-theme", "dark");
      root.classList.add("dark");
    } else {
      root.removeAttribute("data-theme");
      root.classList.remove("dark");
    }
    localStorage.setItem("theme", theme);
  }, [theme]);

  return (
    <header style={{ background: "var(--card)", borderBottom: "1px solid var(--border)", position: "sticky", top: 0, zIndex: 20, boxShadow: "0 4px 16px rgba(15, 23, 42, 0.06)" }}>
      <div style={{ maxWidth: 1120, margin: "0 auto", padding: "10px 16px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <span className="brand-badge">
            <img
              className="brand-logo"
              src="/logo.png"
              alt="InvoiceExtract"
              width={64}
              height={64}
              style={{
                display: "block"
              }}
            />
          </span>
          <strong style={{ fontSize: 22, letterSpacing: 0.2 }}>InvoiceExtract</strong>
        </div>
        <nav style={{ display: "flex", gap: 8, alignItems: "center" }}>
          <NavLink
            to="/"
            style={({ isActive }) => ({
              background: isActive ? "var(--brand)" : "var(--card)",
              color: isActive ? "#fff" : "inherit",
              border: "1px solid var(--border)",
              borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
              padding: "8px 12px",
              borderRadius: 10,
              textDecoration: "none"
            })}
          >Inicio</NavLink>
          {isAdmin && (
            <NavLink
              to="/email-config"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "var(--card)",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 10,
                textDecoration: "none"
              })}
            >Email Config</NavLink>
          )}
          {isFinanzas && (
            <NavLink
              to="/invoices"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "var(--card)",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 10,
                textDecoration: "none"
              })}
            >Dashboard Facturas</NavLink>
          )}
          {isTecnico && (
            <NavLink
              to="/mapping"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "var(--card)",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 10,
                textDecoration: "none"
              })}
            >Mapeos</NavLink>
          )}
          {isTecnico && (
            <NavLink
              to="/erp-config"
              style={({ isActive }) => ({
                background: isActive ? "var(--brand)" : "var(--card)",
                color: isActive ? "#fff" : "inherit",
                border: "1px solid var(--border)",
                borderBottomColor: isActive ? "var(--brand)" : "var(--border)",
                padding: "8px 12px",
                borderRadius: 10,
                textDecoration: "none"
              })}
            >ERPs</NavLink>
          )}
        </nav>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <button
            onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            aria-label="Toggle dark mode"
            title={theme === "dark" ? "Cambiar a claro" : "Cambiar a oscuro"}
            style={{
              background: "var(--card)",
              color: "inherit",
              border: "1px solid var(--border)",
              borderRadius: 10,
              padding: "6px 10px",
              cursor: "pointer"
            }}
          >{theme === "dark" ? "☀️" : "🌙"}</button>
          <span style={{ color: "var(--text)" }}>{keycloak.tokenParsed?.preferred_username}</span>
          <button onClick={logout} style={{ background: "var(--brand)", color: "#fff", border: 0, borderRadius: 10, padding: "8px 12px", cursor: "pointer" }}>Salir</button>
        </div>
      </div>
    </header>
  );
}
