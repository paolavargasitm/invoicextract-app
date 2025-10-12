import { useEffect, useState } from "react";
import { keycloak } from "./keycloak";
import { api } from "./api";
import MappingsPanel from "./mappings/MappingsPanel";
import ErpsPanel from "./mappings/ErpsPanel";
import ExportPanel from "./mappings/ExportPanel";

export default function App() {
  const [status, setStatus] = useState("Ready");
  const [apiResponse, setApiResponse] = useState("");
  const [tokenPreview, setTokenPreview] = useState("");
  const [activeView, setActiveView] = useState("dashboard");

  const callApi = async () => {
    setStatus("Fetching...");
    try {
      await keycloak.updateToken(30);
      setTokenPreview(`${keycloak.token?.slice(0, 24)}...`);
      console.info("Calling backend: GET /config");
      const res = await api.get("/api/config");
      if (!res.ok) throw new Error(`HTTP ${res.status}: ${await res.text()}`);
      const text = await res.text();
      setApiResponse(text);
      setStatus("OK");
    } catch (e) {
      setStatus(`Error: ${e.message}`);
      setApiResponse("");
    }
  };

  const callInvoices = async () => {
    setStatus("Fetching invoices...");
    try {
      await keycloak.updateToken(30);
      setTokenPreview(`${keycloak.token?.slice(0, 24)}...`);
      console.info("Calling backend: GET /api/invoices");
      const res = await api.get("/api/invoices");
      if (!res.ok) throw new Error(`HTTP ${res.status}: ${await res.text()}`);
      const text = await res.text();
      setApiResponse(text);
      setStatus("OK");
    } catch (e) {
      setStatus(`Error: ${e.message}`);
      setApiResponse("");
    }
  };

  // Auto-call once on mount so you see logs immediately
  useEffect(() => {
    callApi();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const logout = () => keycloak.logout({ redirectUri: window.location.origin });

  const copyToken = async () => {
    try {
      await keycloak.updateToken(30);
      const tok = keycloak.token || "";
      setTokenPreview(`${tok.slice(0, 24)}...`);
      if (tok) {
        await navigator.clipboard.writeText(tok);
        setStatus("Token copiado al portapapeles");
        setApiResponse(tok);
      } else {
        setStatus("Sin token disponible");
      }
    } catch (e) {
      setStatus(`Error copiando token: ${e.message}`);
    }
  };

  const theme = {
    brand: "#3366ff",
    bg: "#f5f7fb",
    text: "#0f172a",
    muted: "#475569",
    card: "#ffffff",
    border: "#e5e7eb"
  };

  const Header = () => (
    <header style={{ background: theme.card, borderBottom: `1px solid ${theme.border}` }}>
      <div style={{ maxWidth: 1120, margin: "0 auto", padding: "16px 20px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div style={{ width: 36, height: 36, borderRadius: 8, background: theme.brand }} />
          <h1 style={{ margin: 0, fontSize: 20, color: theme.text }}>InvoicExtract</h1>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 12, color: theme.muted, fontSize: 14 }}>
          <span>{keycloak.tokenParsed?.preferred_username}</span>
          <button onClick={logout} style={{ background: theme.brand, color: "#fff", border: 0, borderRadius: 8, padding: "8px 12px", cursor: "pointer" }}>Salir</button>
        </div>
      </div>
    </header>
  );

  const OptionCard = ({ title, description, cta, onClick, tone }) => (
    <div style={{ flex: 1, minWidth: 260, background: theme.card, border: `1px solid ${theme.border}`, borderRadius: 12, boxShadow: "0 8px 24px rgba(31,45,61,0.08)", padding: 20 }}>
      <h3 style={{ margin: 0, color: theme.text }}>{title}</h3>
      <p style={{ color: theme.muted, fontSize: 14 }}>{description}</p>
      <button onClick={onClick} style={{ background: tone || theme.brand, color: "#fff", border: 0, borderRadius: 10, padding: "10px 14px", cursor: "pointer" }}>{cta}</button>
    </div>
  );

  const Footer = () => (
    <footer style={{ borderTop: `1px solid ${theme.border}`, marginTop: 40 }}>
      <div style={{ maxWidth: 1120, margin: "0 auto", padding: "16px 20px", color: theme.muted, fontSize: 13, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <span>© {new Date().getFullYear()} InvoicExtract</span>
        <span>v1.0</span>
      </div>
    </footer>
  );

  // Roles y permisos
  const roles = keycloak.tokenParsed?.realm_access?.roles || [];
  const isAdmin = roles.includes("ADMIN");
  const isFinanzas = roles.includes("FINANZAS") || isAdmin;
  const isTecnico = roles.includes("TECNICO");
  const displayName = keycloak.tokenParsed?.name || keycloak.tokenParsed?.preferred_username || "";

  const tabs = [
    { id: "dashboard", label: "Dashboard", visible: true },
    { id: "export", label: "Exportación", visible: isFinanzas },
    { id: "mappings", label: "Mapeos", visible: isTecnico },
    { id: "erps", label: "ERPs", visible: isTecnico },
    { id: "email", label: "Config. Correo", visible: isAdmin },
  ].filter(t => t.visible);

  const NavBar = () => (
    <nav style={{ position: "sticky", top: 0, zIndex: 10, background: theme.card, borderBottom: `1px solid ${theme.border}` }}>
      <div style={{ maxWidth: 1120, margin: "0 auto", padding: "0 20px" }}>
        <div role="tablist" aria-label="Navegación principal" style={{ display: "flex", gap: 6 }}>
          {tabs.map(t => (
            <button
              key={t.id}
              role="tab"
              aria-selected={activeView === t.id}
              aria-current={activeView === t.id ? "page" : undefined}
              onClick={() => setActiveView(t.id)}
              style={{
                background: activeView === t.id ? theme.brand : "#fff",
                color: activeView === t.id ? "#fff" : theme.text,
                border: `1px solid ${theme.border}`,
                borderBottomColor: activeView === t.id ? theme.brand : theme.border,
                padding: "10px 12px",
                borderTopLeftRadius: 10,
                borderTopRightRadius: 10,
                cursor: "pointer"
              }}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>
    </nav>
  );

  return (
    <div style={{ background: theme.bg, minHeight: "100vh" }}>
      <Header />
      <NavBar />
      <main style={{ maxWidth: 1120, margin: "0 auto", padding: "24px 20px" }}>
        <section style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16, flexWrap: "wrap", marginBottom: 24 }}>
          <div style={{ flex: 1, minWidth: 280 }}>
            <h2 style={{ margin: 0, fontSize: 28, color: theme.text }}>Bienvenido{displayName ? `, ${displayName}` : ""}</h2>
            <p style={{ color: theme.muted, marginTop: 6 }}>Gestiona el flujo de facturas, configura credenciales y consulta el estado general desde un solo lugar.</p>
            <div style={{ display: "flex", gap: 8, marginTop: 10, flexWrap: "wrap" }}>
              <button onClick={copyToken} style={{ background: theme.border, color: theme.text, border: 0, borderRadius: 8, padding: "8px 12px", cursor: "pointer" }}>Copiar token</button>
              <button onClick={callApi} style={{ background: theme.border, color: theme.text, border: 0, borderRadius: 8, padding: "8px 12px", cursor: "pointer" }}>Probar /config</button>
              <button onClick={callInvoices} style={{ background: theme.border, color: theme.text, border: 0, borderRadius: 8, padding: "8px 12px", cursor: "pointer" }}>Probar /api/invoices</button>
            </div>
          </div>
          <div style={{ width: 320, height: 120, background: "#fff", border: `1px dashed ${theme.border}`, borderRadius: 12 }} />
        </section>
        {activeView === "dashboard" && (
          <>
            <section style={{ display: "flex", gap: 16, flexWrap: "wrap" }}>
              {isAdmin && (
                <OptionCard
                  title="Configuración de Correo"
                  description="Registra las credenciales del correo corporativo para automatizar la ingesta de facturas."
                  cta="Probar /config"
                  onClick={callApi}
                  tone="#16a34a"
                />
              )}
              {isFinanzas && (
                <OptionCard
                  title="Dashboard General"
                  description="Consulta métricas, estados y exporta reportes consolidados de facturación."
                  cta="Probar /api/invoices"
                  onClick={callInvoices}
                  tone={theme.brand}
                />
              )}
              {isFinanzas && (
                <OptionCard
                  title="Exportación de Mapeos"
                  description="Selecciona ERP y formato para descargar el resultado mapeado."
                  cta="Ir a Exportación"
                  onClick={() => setActiveView("export")}
                  tone="#f59e0b"
                />
              )}
              {isTecnico && (
                <OptionCard
                  title="Mapeos de Campos"
                  description="Administra reglas de mapeo por ERP en el microservicio de mapeos."
                  cta="Ir a Mapeos"
                  onClick={() => setActiveView("mappings")}
                  tone="#0ea5e9"
                />
              )}
              {isTecnico && (
                <OptionCard
                  title="Administración de ERPs"
                  description="Crea y habilita/deshabilita los ERPs disponibles para mapeo."
                  cta="Ir a ERPs"
                  onClick={() => setActiveView("erps")}
                  tone="#8b5cf6"
                />
              )}
            </section>

            <section style={{ marginTop: 24, display: "grid", gridTemplateColumns: "1fr", gap: 12 }}>
              <div style={{ background: theme.card, border: `1px solid ${theme.border}`, borderRadius: 12, padding: 16 }}>
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  <h3 style={{ margin: 0, color: theme.text }}>Estado</h3>
                  <span style={{ color: theme.muted }}>{status}</span>
                </div>
                <div style={{ marginTop: 12, overflow: "auto" }}>
                  <div style={{ color: theme.muted, fontSize: 13, marginBottom: 8 }}>Token</div>
                  <code style={{ display: "block", background: "#0b1220", color: "#a3e635", padding: 12, borderRadius: 8 }}>{tokenPreview || "—"}</code>
                </div>
                <div style={{ marginTop: 12, overflow: "auto" }}>
                  <div style={{ color: theme.muted, fontSize: 13, marginBottom: 8 }}>Respuesta API</div>
                  <pre style={{ background: "#0b1220", color: "#a3e635", padding: 12, borderRadius: 8 }}>{apiResponse || "—"}</pre>
                </div>
              </div>
            </section>
          </>
        )}

        {activeView === "mappings" && isTecnico && (
          <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: 12 }}>
            <MappingsPanel theme={theme} />
          </div>
        )}

        {activeView === "erps" && isTecnico && (
          <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: 12 }}>
            <ErpsPanel theme={theme} />
          </div>
        )}

        {activeView === "export" && isFinanzas && (
          <div style={{ display: "grid", gridTemplateColumns: "1fr", gap: 12 }}>
            <ExportPanel theme={theme} />
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
}
