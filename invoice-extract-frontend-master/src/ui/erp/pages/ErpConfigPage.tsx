import ErpsPanel from "../../mappings/components/ErpsPanel";

export default function ErpConfigPage() {
  const theme = { card: '#fff', border: '#e5e7eb', text: '#0f172a', muted: '#64748b', brand: 'var(--brand)' };
  return (
    <section style={{ background: "#fff", border: "1px solid #e5e7eb", borderRadius: 12, padding: 16 }}>
      <h2 style={{ marginTop: 0 }}>Configuración de ERP</h2>
      <ErpsPanel theme={theme} />
    </section>
  );
}
