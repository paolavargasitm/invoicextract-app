export default function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer style={{ borderTop: '1px solid #e5e7eb', background: 'transparent' }}>
      <div style={{ maxWidth: 1120, margin: '0 auto', padding: '14px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: '#64748b', fontSize: 14 }}>
        <span>© {year} InvoicExtract</span>
        <span>v1.0</span>
      </div>
    </footer>
  );
}
