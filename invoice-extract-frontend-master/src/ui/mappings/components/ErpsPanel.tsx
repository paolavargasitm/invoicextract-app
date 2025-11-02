import { useEffect, useState } from "react";
import { erpsApi } from "../api/erpsApi";
import ErrorBanner from "../../../components/ErrorBanner";

type Theme = { card: string; border: string; text: string; muted: string; brand: string };

export default function ErpsPanel({ theme }: { theme: Theme }) {
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ message: string; details?: any } | null>(null);
  const [name, setName] = useState("");

  const load = async () => {
    setLoading(true); setError(null);
    try {
      const data = await erpsApi.list();
      setItems(data || []);
    } catch (e: any) { setError({ message: e?.message || 'No se pudo obtener los ERPs', details: e?.details }); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const create = async (ev: React.FormEvent) => {
    ev.preventDefault(); setError(null);
    try {
      if (!name.trim()) throw new Error('Nombre requerido');
      await erpsApi.create({ name: name.trim() });
      setName('');
      await load();
    } catch (e: any) { setError({ message: e?.message, details: e?.details }); }
  };

  const toggleStatus = async (row: any) => {
    const next = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try { await erpsApi.changeStatus(row.id, next); await load(); } catch (e: any) { setError({ message: e?.message, details: e?.details }); }
  };

  const badge = (text: string, color: string) => (
    <span style={{ padding: '2px 8px', borderRadius: 999, background: color, color: '#fff', fontSize: 12 }}>{text}</span>
  );
  const StatusBadge = ({ s }: { s: string }) => s === 'ACTIVE' ? badge('Activo', '#16a34a') : badge('Inactivo', '#ef4444');

  return (
    <div style={{ background: theme.card, border: `1px solid ${theme.border}`, borderRadius: 12, padding: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, marginBottom: 12 }}>
        <h3 style={{ margin: 0, color: theme.text }}>Administración de ERPs</h3>
        <button onClick={load} disabled={loading} style={{ opacity: loading ? 0.7 : 1, background: theme.brand, color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px', cursor: loading ? 'not-allowed' : 'pointer' }}>{loading ? 'Cargando...' : 'Refrescar'}</button>
      </div>

      {error && (
        <ErrorBanner message={error.message} details={error.details} onClose={() => setError(null)} />
      )}

      <div style={{ overflow: 'auto', maxHeight: 460, border: `1px solid ${theme.border}`, borderRadius: 8 }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ position: 'sticky', top: 0, background: theme.card, zIndex: 1, boxShadow: `0 1px 0 ${theme.border}` }}>
            <tr style={{ textAlign: 'left', color: theme.muted }}>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>ID</th>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>Nombre</th>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>Estado</th>
              <th style={{ padding: '10px 8px' }}></th>
            </tr>
          </thead>
          <tbody>
            {items?.map((row, idx) => (
              <tr key={row.id} style={{ background: idx % 2 === 0 ? '#ffffff' : '#f9fafb' }}>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}>{row.id}</td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}>{row.name}</td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}><StatusBadge s={row.status} /></td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}`, whiteSpace: 'nowrap' }}>
                  <button onClick={() => toggleStatus(row)} style={{ background: '#0ea5e9', color: '#fff', border: 0, borderRadius: 6, padding: '6px 10px', cursor: 'pointer' }}>{row.status === 'ACTIVE' ? 'desactivar' : 'activar'}</button>
                </td>
              </tr>
            ))}
            {(!items || items.length === 0) && (
              <tr>
                <td colSpan={4} style={{ color: theme.muted, padding: 12, textAlign: 'center', borderTop: `1px solid ${theme.border}` }}>Sin resultados</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div style={{ marginTop: 16, borderTop: `1px solid ${theme.border}`, paddingTop: 12 }}>
        <h4 style={{ margin: '4px 0', color: theme.text }}>Crear nuevo ERP</h4>
        <form onSubmit={create} style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Nombre del ERP" style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }} />
          <button type="submit" style={{ background: '#16a34a', color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px', cursor: 'pointer' }}>Crear</button>
        </form>
      </div>
    </div>
  );
}
