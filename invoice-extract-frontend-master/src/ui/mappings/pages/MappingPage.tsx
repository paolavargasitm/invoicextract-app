import MappingsPanel from "../components/MappingsPanel";

export default function MappingPage() {
  const theme = { card: '#fff', border: '#e5e7eb', text: '#0f172a', muted: '#64748b', brand: 'var(--brand)' };
  return (
    <div style={{ display: 'grid', gap: 16 }}>
      <section style={{ background: "#fff", border: "1px solid #e5e7eb", borderRadius: 12, padding: 16 }}>
        <h2 style={{ marginTop: 0 }}>Mapeos</h2>
        <MappingsPanel theme={theme} />
      </section>
    </div>
  );
}
