import MappingsPanel from "../components/MappingsPanel";

export default function MappingPage() {
  const theme = { card: 'var(--card)', border: 'var(--border)', text: 'var(--text)', muted: 'var(--muted)', brand: 'var(--brand)' } as const;
  return (
    <div style={{ display: 'grid', gap: 16 }}>
      <section style={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 12, padding: 16 }}>
        <h2 style={{ marginTop: 0 }}>Mapeos</h2>
        <MappingsPanel theme={theme} />
      </section>
    </div>
  );
}
