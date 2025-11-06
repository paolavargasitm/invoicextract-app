import { useEffect, useMemo, useState } from "react";
import { mappingsApi } from "../api/mappingsApi";
import { erpsApi } from "../api/erpsApi";
import { authHeader } from "../../../auth/keycloak";
import ErrorBanner from "../../../components/ErrorBanner";

type Theme = { card: string; border: string; text: string; muted: string; brand: string };

const badge = (text: string, color: string) => (
  <span style={{ padding: '2px 8px', borderRadius: 999, background: color, color: '#fff', fontSize: 12 }}>{text}</span>
);

export default function MappingsPanel({ theme }: { theme: Theme }) {
  const [erp, setErp] = useState("SAP");
  const [status, setStatus] = useState<'ACTIVE' | 'INACTIVE'>("ACTIVE");
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<{ message: string; details?: any } | null>(null);
  const [known, setKnown] = useState<{ invoiceFields: string[]; itemFields: string[]; tables: any }>({ invoiceFields: [], itemFields: [], tables: {} });
  const [erpOptions, setErpOptions] = useState<string[]>(["SAP"]);

  const [form, setForm] = useState({ erpName: 'SAP', sourceField: '', targetField: '', transformFn: '', status: 'ACTIVE' });

  const resetForm = () => setForm({ erpName: erp || 'SAP', sourceField: '', targetField: '', transformFn: '', status: 'ACTIVE' });

  const load = async () => {
    setLoading(true); setError(null);
    try {
      const data = await mappingsApi.list(erp, status);
      setItems(data || []);
    } catch (e: any) {
      setError({ message: e?.message || 'No se pudo obtener los mapeos', details: e?.details });
    } finally { setLoading(false); }
  };

  useEffect(() => { if (erp) load(); }, [erp, status]);

  // Load ERP list for dropdowns
  useEffect(() => {
    const fetchErps = async () => {
      try {
        const data = await erpsApi.list('ACTIVE');
        const raw = Array.isArray(data) ? data : (Array.isArray((data as any)?.items) ? (data as any).items : []);
        const onlyActive = raw.filter((x: any) => (x?.status ?? '').toUpperCase().trim() === 'ACTIVE');
        const names: string[] = onlyActive.map((x: any) => typeof x === 'string' ? x : (x?.name ?? String(x?.id ?? 'SAP')));
        if (names.length) {
          setErpOptions(names);
          if (!names.includes(erp)) { setErp(names[0]); setForm(f => ({ ...f, erpName: names[0] })); }
        }
      } catch { /* keep defaults */ }
    };
    fetchErps();
    const onErpsChanged = () => { fetchErps(); };
    window.addEventListener('erps:changed', onErpsChanged);
    return () => window.removeEventListener('erps:changed', onErpsChanged);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const base = (import.meta.env.VITE_MAPPINGS_BASE_URL || 'http://localhost:8082/invoice-mapping');
    fetch(`${base}/api/reference/fields`, { headers: { ...authHeader() } })
      .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject(new Error(t))))
      .then((data: any) => setKnown({
        invoiceFields: data?.shortcuts?.invoiceFields || [],
        itemFields: data?.shortcuts?.itemFields || [],
        tables: data?.tables || {}
      }))
      .catch(() => setKnown({ invoiceFields: [], itemFields: [], tables: {} }));
  }, []);

  const allKnownFields = useMemo(() => ([...(known.invoiceFields || []), ...(known.itemFields || [])]), [known]);

  // Transform selector helpers
  const transformOptions = [
    { value: '', label: 'Sin transformación' },
    { value: 'TRIM', label: 'TRIM' },
    { value: 'UPPER', label: 'UPPER' },
    { value: 'DATE_FMT', label: 'DATE_FMT (requiere formato)' },
    { value: 'FIRST', label: 'FIRST (listas)' },
    { value: 'SUM', label: 'SUM (listas numéricas)' },
    { value: 'JOIN', label: 'JOIN (requiere separador)' },
  ];
  const parseSpec = (spec?: string): { name: string; arg: string } => {
    if (!spec) return { name: '', arg: '' };
    const idx = spec.indexOf(':');
    if (idx < 0) return { name: spec, arg: '' };
    return { name: spec.substring(0, idx), arg: spec.substring(idx + 1) };
  };
  const buildSpec = (name: string, arg: string) => {
    if (!name) return '';
    if (!arg) return name;
    return `${name}:${arg}`;
  };

  const fieldMeta = (name: string) => {
    const tables = known.tables || {};
    const search = (tbl: any) => (tbl?.fields || []).find((f: any) => f.name === name);
    return search(tables?.invoices) || search(tables?.invoiceItems) || null;
  };

  const submit = async (ev: React.FormEvent) => {
    ev.preventDefault(); setError(null);
    try {
      if (!form.sourceField || !form.targetField) throw new Error('Campos obligatorios: sourceField y targetField');
      await mappingsApi.create(form);
      resetForm();
      await load();
    } catch (e: any) { setError({ message: e?.message, details: e?.details }); }
  };

  const updateRow = async (row: any) => {
    setError(null);
    try {
      await mappingsApi.update(row.id, { sourceField: row.sourceField, targetField: row.targetField, transformFn: row.transformFn, status: row.status });
      await load();
    } catch (e: any) { setError({ message: e?.message, details: e?.details }); }
  };

  const toggleStatus = async (row: any) => {
    const next = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try { await mappingsApi.changeStatus(row.id, next); await load(); } catch (e: any) { setError({ message: e?.message, details: e?.details }); }
  };

  const StatusBadge = ({ s }: { s: string }) => s === 'ACTIVE' ? badge('Activa', '#16a34a') : badge('Inactiva', '#ef4444');

  return (
    <div style={{ background: theme.card, border: `1px solid ${theme.border}`, borderRadius: 12, padding: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12, marginBottom: 12 }}>
        <h3 style={{ margin: 0, color: theme.text }}>Mapeos por ERP</h3>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <select value={erp} onChange={e => setErp(e.target.value)} style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }}>
            {erpOptions.map(opt => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
          <select value={status} onChange={e => setStatus(e.target.value as any)} style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }}>
            <option value="ACTIVE">Activas</option>
            <option value="INACTIVE">Inactivas</option>
          </select>
          <button onClick={load} disabled={loading} style={{ opacity: loading ? 0.7 : 1, background: theme.brand, color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px', cursor: loading ? 'not-allowed' : 'pointer' }}>{loading ? 'Cargando...' : 'Refrescar'}</button>
        </div>
      </div>

      {error && (
        <ErrorBanner message={error.message} details={error.details} onClose={() => setError(null)} />
      )}

      <div style={{ overflow: 'auto', maxHeight: 460, border: `1px solid ${theme.border}`, borderRadius: 8 }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ position: 'sticky', top: 0, background: theme.card, zIndex: 1, boxShadow: `0 1px 0 ${theme.border}` }}>
            <tr style={{ textAlign: 'left', color: theme.muted }}>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>ID</th>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>Fuente</th>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>Destino</th>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>Transformación</th>
              <th style={{ padding: '10px 8px', fontSize: 12, textTransform: 'uppercase' }}>Estado</th>
              <th style={{ padding: '10px 8px' }}></th>
            </tr>
          </thead>
          <tbody>
            {items?.map((row, idx) => (
              <tr key={row.id} style={{ background: idx % 2 === 0 ? theme.card : 'var(--bg)' }}>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}>{row.id}</td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}>
                  <input list="knownFieldsList" value={row.sourceField || ''} onChange={(e) => setItems(prev => prev.map(x => x.id === row.id ? { ...x, sourceField: e.target.value } : x))} style={{ padding: 8, borderRadius: 6, border: `1px solid ${theme.border}`, width: '100%' }} />
                  {row.sourceField && fieldMeta(row.sourceField) && (
                    <div style={{ color: theme.muted, fontSize: 12, marginTop: 4 }}>
                      {fieldMeta(row.sourceField).type}{fieldMeta(row.sourceField).nullable === false ? ' • required' : ' • optional'}
                    </div>
                  )}
                </td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}>
                  <input list="knownFieldsList" value={row.targetField || ''} onChange={(e) => setItems(prev => prev.map(x => x.id === row.id ? { ...x, targetField: e.target.value } : x))} style={{ padding: 8, borderRadius: 6, border: `1px solid ${theme.border}`, width: '100%' }} />
                  {row.targetField && fieldMeta(row.targetField) && (
                    <div style={{ color: theme.muted, fontSize: 12, marginTop: 4 }}>
                      {fieldMeta(row.targetField).type}{fieldMeta(row.targetField).nullable === false ? ' • required' : ' • optional'}
                    </div>
                  )}
                </td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}>
                  {(() => {
                    const { name, arg } = parseSpec(row.transformFn);
                    const needsArg = name === 'DATE_FMT' || name === 'JOIN';
                    return (
                      <div style={{ display: 'flex', gap: 6 }}>
                        <select
                          value={name}
                          onChange={(e) => {
                            const newName = e.target.value;
                            const newSpec = buildSpec(newName, (newName === 'DATE_FMT' || newName === 'JOIN') ? arg : '');
                            setItems(prev => prev.map(x => x.id === row.id ? { ...x, transformFn: newSpec } : x));
                          }}
                          style={{ padding: 8, borderRadius: 6, border: `1px solid ${theme.border}` }}
                        >
                          {transformOptions.map(opt => (<option key={opt.value} value={opt.value}>{opt.label}</option>))}
                        </select>
                        {needsArg && (
                          <input
                            placeholder={name === 'DATE_FMT' ? 'yyyy-MM-dd' : 'sep (p.ej. | )'}
                            value={arg}
                            onChange={(e) => {
                              const newArg = e.target.value;
                              const newSpec = buildSpec(name, newArg);
                              setItems(prev => prev.map(x => x.id === row.id ? { ...x, transformFn: newSpec } : x));
                            }}
                            style={{ padding: 8, borderRadius: 6, border: `1px solid ${theme.border}`, minWidth: 120 }}
                          />
                        )}
                      </div>
                    );
                  })()}
                </td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}` }}>
                  <StatusBadge s={row.status} />
                </td>
                <td style={{ padding: 8, borderTop: `1px solid ${theme.border}`, whiteSpace: 'nowrap' }}>
                  <button onClick={() => toggleStatus(row)} style={{ background: theme.brand, color: '#fff', border: 0, borderRadius: 6, padding: '6px 10px', marginRight: 6, cursor: 'pointer' }}>{row.status === 'ACTIVE' ? 'Desactivar' : 'Activar'}</button>
                  <button onClick={() => updateRow(row)} style={{ background: theme.brand, color: '#fff', border: 0, borderRadius: 6, padding: '6px 10px', cursor: 'pointer' }}>Guardar</button>
                </td>
              </tr>
            ))}
            {(!items || items.length === 0) && (
              <tr>
                <td colSpan={6} style={{ color: theme.muted, padding: 12, textAlign: 'center', borderTop: `1px solid ${theme.border}` }}>Sin resultados</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div style={{ marginTop: 16, borderTop: `1px solid ${theme.border}`, paddingTop: 12 }}>
        <h4 style={{ margin: '4px 0', color: theme.text }}>Crear nuevo mapeo</h4>
        <form onSubmit={submit} style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
          <select value={form.erpName} onChange={(e) => setForm({ ...form, erpName: e.target.value })} style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }}>
            {erpOptions.map(opt => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
          <input list="knownFieldsList" value={form.sourceField} onChange={(e) => setForm({ ...form, sourceField: e.target.value })} placeholder="sourceField" style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }} />
          <input list="knownFieldsList" value={form.targetField} onChange={(e) => setForm({ ...form, targetField: e.target.value })} placeholder="targetField" style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }} />
          {(() => {
            const spec = parseSpec(form.transformFn);
            const needsArg = spec.name === 'DATE_FMT' || spec.name === 'JOIN';
            return (
              <div style={{ display: 'flex', gap: 6 }}>
                <select
                  value={spec.name}
                  onChange={(e) => {
                    const newName = e.target.value;
                    const newSpec = buildSpec(newName, (newName === 'DATE_FMT' || newName === 'JOIN') ? spec.arg : '');
                    setForm({ ...form, transformFn: newSpec });
                  }}
                  style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }}
                >
                  {transformOptions.map(opt => (<option key={opt.value} value={opt.value}>{opt.label}</option>))}
                </select>
                {needsArg && (
                  <input
                    placeholder={spec.name === 'DATE_FMT' ? 'yyyy-MM-dd' : 'sep (p.ej. | )'}
                    value={spec.arg}
                    onChange={(e) => setForm({ ...form, transformFn: buildSpec(spec.name, e.target.value) })}
                    style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }}
                  />
                )}
              </div>
            );
          })()}
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} style={{ padding: 8, borderRadius: 8, border: `1px solid ${theme.border}` }}>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
          <button type="submit" style={{ background: '#16a34a', color: '#fff', border: 0, borderRadius: 8, padding: '8px 12px', cursor: 'pointer' }}>Crear</button>
        </form>
        <datalist id="knownFieldsList">
          {allKnownFields.map(n => (<option key={n} value={n} />))}
        </datalist>
      </div>
    </div>
  );
}
