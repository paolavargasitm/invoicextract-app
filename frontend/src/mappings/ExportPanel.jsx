import { useEffect, useState } from 'react';
import { authHeader } from '../keycloak';
import { erpsApi } from './erpsApi';

export default function ExportPanel({ theme }) {
  const [erps, setErps] = useState([]);
  const [erp, setErp] = useState('');
  const [format, setFormat] = useState('json');
  const [flatten, setFlatten] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [preview, setPreview] = useState('');

  useEffect(() => {
    const loadErps = async () => {
      try {
        const data = await erpsApi.list();
        setErps(data || []);
        if (data && data.length > 0) setErp(data[0].name);
      } catch (e) { setError(e.message || 'No se pudo cargar ERPs'); }
    };
    loadErps();
  }, []);

  const exportBaseUrl = () => (import.meta.env.VITE_MAPPINGS_BASE_URL || 'http://localhost:8082/invoice-mapping');

  const handleDownload = async () => {
    setError(''); setLoading(true); setPreview('');
    try {
      if (!erp) throw new Error('Selecciona un ERP');
      const url = `${exportBaseUrl()}/api/export?erp=${encodeURIComponent(erp)}&format=${encodeURIComponent(format)}&flatten=${flatten}`;
      const res = await fetch(url, { headers: { ...authHeader() } });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      if (format === 'json') {
        const text = await res.text();
        setPreview(text);
      } else {
        const blob = await res.blob();
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = `export-${erp}.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
      }
    } catch (e) { setError(e.message || 'No se pudo exportar'); }
    finally { setLoading(false); }
  };

  return (
    <div style={{ background: theme.card, border: `1px solid ${theme.border}`, borderRadius: 12, padding: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, marginBottom: 12 }}>
        <h3 style={{ margin: 0, color: theme.text }}>Exportación de Mapeos</h3>
        <button onClick={handleDownload} disabled={loading || !erp} aria-disabled={loading || !erp} style={{ opacity: (loading || !erp) ? 0.7 : 1, background: theme.brand, color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px', cursor: (loading || !erp) ? 'not-allowed' : 'pointer' }}>{loading ? 'Generando…' : (format === 'csv' ? 'Descargar CSV' : 'Previsualizar JSON')}</button>
      </div>

      <div aria-live="polite" style={{ minHeight: 1 }}>
        {error && (
          <div role="alert" style={{ background: '#fef2f2', color: '#991b1b', padding: 10, borderRadius: 8, marginBottom: 12 }}>{error}</div>
        )}
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'flex-end', marginBottom: 12 }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          <label htmlFor="erpSelect" style={{ color: theme.muted, fontSize: 12 }}>ERP</label>
          <select id="erpSelect" value={erp} onChange={(e) => setErp(e.target.value)} style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}`, minWidth: 220 }}>
            {!erp && <option value="">Selecciona ERP…</option>}
            {erps.map(x => (
              <option key={x.id || x.name} value={x.name}>{x.name}</option>
            ))}
          </select>
          {(!erps || erps.length === 0) && (
            <span style={{ color: theme.muted, fontSize: 12 }}>No hay ERPs disponibles.</span>
          )}
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          <span style={{ color: theme.muted, fontSize: 12 }}>Formato</span>
          <div role="group" aria-label="Formato" style={{ display: 'inline-flex', border: `1px solid ${theme.border}`, borderRadius: 10, overflow: 'hidden' }}>
            <button type="button" onClick={() => setFormat('json')} aria-pressed={format==='json'} style={{ padding: '8px 12px', border: 0, background: format==='json' ? theme.brand : '#fff', color: format==='json' ? '#fff' : theme.text, cursor: 'pointer' }}>JSON</button>
            <button type="button" onClick={() => setFormat('csv')} aria-pressed={format==='csv'} style={{ padding: '8px 12px', border: 0, background: format==='csv' ? theme.brand : '#fff', color: format==='csv' ? '#fff' : theme.text, cursor: 'pointer', borderLeft: `1px solid ${theme.border}` }}>CSV</button>
          </div>
        </div>
        <label htmlFor="flattenChk" style={{ display: 'flex', alignItems: 'center', gap: 6, color: theme.muted }} title="Convierte items en llaves planas items[0].campo, items[1].campo">
          <input id="flattenChk" type="checkbox" checked={flatten} onChange={(e) => setFlatten(e.target.checked)} />
          Aplanar estructura
        </label>
      </div>

      {format === 'json' && (
        <div style={{ border: `1px solid ${theme.border}`, borderRadius: 8, overflow: 'hidden' }}>
          <div style={{ background: '#0b1220', color: '#a3e635', padding: 12, fontSize: 12, overflowX: 'auto', minHeight: 160 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
              <span style={{ color: '#93c5fd', fontSize: 12 }}>Previsualización</span>
              <button type="button" disabled={!preview} onClick={() => { if (preview) navigator.clipboard.writeText(preview); }} style={{ opacity: preview ? 1 : 0.6, background: '#111827', color: '#e5e7eb', border: 0, borderRadius: 6, padding: '4px 8px', cursor: preview ? 'pointer' : 'not-allowed' }}>Copiar</button>
            </div>
            {!preview && loading && (
              <div style={{ opacity: 0.7 }}>// Cargando previsualización…</div>
            )}
            <pre style={{ margin: 0, fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace' }}>{preview || (!loading ? '// Pulsa "Previsualizar JSON" para ver el contenido' : '')}</pre>
          </div>
        </div>
      )}
    </div>
  );
}
