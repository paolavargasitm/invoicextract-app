import ErpsPanel from "../../mappings/components/ErpsPanel";

export default function ErpConfigPage() {
  const theme = { card: 'var(--card)', border: 'var(--border)', text: 'var(--text)', muted: 'var(--muted)', brand: 'var(--brand)' } as const;
  return (
    <section style={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 12, padding: 16 }}>
      <h2 style={{ marginTop: 0 }}>Configuración de ERP</h2>
      <ErpsPanel theme={theme} />
    </section>
  );
}
