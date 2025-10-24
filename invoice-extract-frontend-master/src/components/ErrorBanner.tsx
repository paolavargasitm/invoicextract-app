export default function ErrorBanner({ message, details, onClose }: { message: string; details?: any; onClose?: () => void }) {
  return (
    <div style={{
      background: '#fff1f2',
      color: '#b91c1c',
      padding: 12,
      borderRadius: 10,
      marginBottom: 12,
      border: '2px solid #fda4af',
      boxShadow: '0 1px 2px rgba(0,0,0,0.04)',
      display: 'flex',
      alignItems: 'flex-start',
      gap: 10
    }}>
      <div style={{ flex: 1 }}>
        <div>{message}</div>
        {Array.isArray(details) && details.length > 0 && (
          <ul style={{ margin: '6px 0 0 18px', padding: 0 }}>
            {details.map((d: any, i: number) => (
              <li key={i} style={{ fontSize: 13 }}>
                {typeof d === 'string' ? d : (d?.field ? `${d.field}: ${d.message || ''}` : (d?.message || JSON.stringify(d)))}
              </li>
            ))}
          </ul>
        )}
      </div>
      {onClose && (
        <button onClick={onClose} style={{ background: 'transparent', border: 0, color: '#b91c1c', cursor: 'pointer' }} aria-label="Cerrar aviso">
          ✕
        </button>
      )}
    </div>
  );
}
