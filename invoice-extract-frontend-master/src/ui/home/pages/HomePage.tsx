import { keycloak } from "../../../auth/keycloak";
import { useNavigate } from "react-router-dom";

export default function HomePage() {
  const navigate = useNavigate();
  const username = (keycloak.tokenParsed?.name as string) || (keycloak.tokenParsed?.preferred_username as string) || "";
  const roles: string[] = ((keycloak.tokenParsed?.realm_access as any)?.roles || []) as string[];
  const isAdmin = roles.includes("ADMIN");
  const isFinanzas = roles.includes("FINANZAS") || isAdmin;
  const isTecnico = roles.includes("TECNICO") || isAdmin;
  // Home simplificada: solo bienvenida y acceso al Dashboard

  return (
    <div style={{ display: "grid", gap: 16 }}>
      <section style={{ background: "#fff", border: "1px solid #e5e7eb", borderRadius: 12, padding: 16 }}>
        <h2 style={{ marginTop: 0 }}>Bienvenido{username ? `, ${username}` : ""}</h2>
        <div style={{ color: "#475569" }}>Gestiona el flujo de facturas desde un solo lugar.</div>
      </section>

      <section style={{ display: 'grid', gridTemplateColumns: 'repeat( auto-fit, minmax(280px, 1fr) )', gap: 16 }}>
        {(isFinanzas) && (
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 16 }}>
            <h3 style={{ marginTop: 0 }}>Dashboard General</h3>
            <div style={{ color: '#475569' }}>Consulta métricas, estados y exporta reportes consolidados de facturación.</div>
            <div style={{ marginTop: 12 }}>
              <button onClick={() => navigate('/dashboard')} style={{ background: 'var(--brand)', color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px' }}>Ir al Dashboard</button>
            </div>
          </div>
        )}
        {(isTecnico) && (
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 16 }}>
            <h3 style={{ marginTop: 0 }}>Mapeos</h3>
            <div style={{ color: '#475569' }}>Administra los mapeos de extracción.</div>
            <div style={{ marginTop: 12 }}>
              <button onClick={() => navigate('/mapping')} style={{ background: 'var(--brand)', color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px' }}>Ir a Mapeos</button>
            </div>
          </div>
        )}
        {(isTecnico) && (
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 16 }}>
            <h3 style={{ marginTop: 0 }}>ERPs</h3>
            <div style={{ color: '#475569' }}>Configura los ERPs disponibles.</div>
            <div style={{ marginTop: 12 }}>
              <button onClick={() => navigate('/erp-config')} style={{ background: 'var(--brand)', color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px' }}>Ir a ERPs</button>
            </div>
          </div>
        )}
        {isAdmin && (
          <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: 12, padding: 16 }}>
            <h3 style={{ marginTop: 0 }}>Configurar Correo</h3>
            <div style={{ color: '#475569' }}>Administra las credenciales y parámetros de correo para automatización.</div>
            <div style={{ marginTop: 12 }}>
              <button onClick={() => navigate('/email-config')} style={{ background: 'var(--brand)', color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px' }}>Ir a Email Config</button>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
