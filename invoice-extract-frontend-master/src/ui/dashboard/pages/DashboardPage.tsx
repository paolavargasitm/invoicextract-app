import { useEffect, useMemo, useState } from "react";
import { authHeader } from "../../../auth/keycloak";
import InvoiceDetailView from "../../invoices/components/InvoiceDetailView";
import { useInvoiceDetail } from "../../invoices/hooks/useInvoiceDetail";
import ErrorBanner from "../../../components/ErrorBanner";

type InvoiceRow = {
  id: string;
  date: string; // yyyy-mm-dd
  provider: string;
  amount: number;
  status: "Aprobada" | "Rechazada" | "Pendiente";
  erp?: string;
  customer?: string;
};

export default function DashboardPage() {
  // Filtros (placeholder)
  const [userOrEmail, setUserOrEmail] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [statusFilter, setStatusFilter] = useState<"" | "APPROVED" | "REJECTED" | "PENDING">("");
  const [senderTaxId, setSenderTaxId] = useState<string>("");
  const [receiverTaxId, setReceiverTaxId] = useState<string>("");
  // Invoices state
  const [rows, setRows] = useState<InvoiceRow[]>([]);
  const [loadingInvoices, setLoadingInvoices] = useState(false);
  const [errorInvoices, setErrorInvoices] = useState<string>("");
  // Export modal state
  const [showExport, setShowExport] = useState(false);
  const [erp, setErp] = useState("SAP");
  const [format, setFormat] = useState<"CSV" | "JSON">("CSV");
  const [loadingExport, setLoadingExport] = useState(false);
  const [errorExport, setErrorExport] = useState<string>("");
  const [erpOptions, setErpOptions] = useState<string[]>(["SAP"]);
  // Detail modal state
  const [showDetail, setShowDetail] = useState(false);
  const [detailId, setDetailId] = useState<string | null>(null);

  const backendBase = () => (import.meta.env.VITE_BACKEND_BASE_URL || "http://localhost:8080/invoicextract");
  // Load invoices from API (fallback-friendly mapping)
  const mapStatus = (s: any): InvoiceRow["status"] => {
    if (!s) return "Pendiente";
    const t = String(s).toUpperCase();
    if (t.includes("APPROVED") || t.includes("APROB")) return "Aprobada";
    if (t.includes("REJECT")) return "Rechazada";
    return "Pendiente";
  };
  const toRow = (inv: any): InvoiceRow => {
    const id = inv?.id || inv?.invoiceNumber || inv?.code || inv?.uuid || "—";
    const dateRaw = inv?.date || inv?.issueDate || inv?.createdDate || new Date().toISOString();
    const date = String(dateRaw).slice(0, 10);
    const provider = inv?.provider || inv?.providerBusinessName || inv?.senderBusinessName || inv?.supplierName || "—";
    const amountNum = Number(inv?.amount ?? inv?.total ?? inv?.totalAmount ?? inv?.grandTotal ?? 0);
    const erp = inv?.erp || inv?.erpName || inv?.system || inv?.sourceErp || undefined;
    const customer = inv?.customer || inv?.customerName || inv?.buyerBusinessName || inv?.receiverBusinessName || undefined;
    return { id: String(id), date, provider: String(provider), amount: isNaN(amountNum) ? 0 : amountNum, status: mapStatus(inv?.status), erp, customer };
  };
  const loadInvoices = async () => {
    setLoadingInvoices(true); setErrorInvoices("");
    try {
      // Use backend GET /api/invoices/filter as defined in InvoiceController
      const qs = new URLSearchParams();
      // Nota: userOrEmail no mapea a ningún parámetro del backend; si se requiere, definimos qué campo usar.
      // Soportados: id, senderTaxId, receiverTaxId, status, startDate, endDate
      if (statusFilter) qs.set('status', statusFilter);
      if (senderTaxId.trim()) qs.set('senderTaxId', senderTaxId.trim());
      if (receiverTaxId.trim()) qs.set('receiverTaxId', receiverTaxId.trim());
      if (from) qs.set('startDate', `${from}T00:00:00`);
      if (to) qs.set('endDate', `${to}T23:59:59`);
      // ERP no está soportado en el endpoint actual; filtro retirado del Dashboard.
      const url = `${backendBase()}/api/invoices/filter${qs.toString() ? `?${qs}` : ''}`;
      const res = await fetch(url, { headers: { ...authHeader() } });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      const list = Array.isArray(data) ? data : (Array.isArray(data?.items) ? data.items : []);
      setRows(list.map(toRow));
    } catch (e: any) {
      setErrorInvoices(e?.message || 'No se pudieron cargar las facturas');
      setRows([]);
    } finally { setLoadingInvoices(false); }
  };

  useEffect(() => { loadInvoices(); /* eslint-disable-next-line */ }, []);

  const stats = useMemo(() => {
    const total = rows.length;
    const aprobadas = rows.filter(d => d.status === "Aprobada").length;
    const rechazadas = rows.filter(d => d.status === "Rechazada").length;
    const monto = rows.reduce((acc, d) => acc + d.amount, 0);
    return { total, aprobadas, rechazadas, monto };
  }, [rows]);

  const fmtCurrency = (n: number) => new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 }).format(n);

  const Badge = ({ text, tone }: { text: string, tone: "green" | "red" | "amber" }) => (
    <span style={{
      padding: "4px 10px",
      borderRadius: 999,
      fontSize: 12,
      background: tone === "green" ? "#dcfce7" : tone === "red" ? "#fee2e2" : "#fef3c7",
      color: tone === "green" ? "#166534" : tone === "red" ? "#991b1b" : "#92400e",
      border: `1px solid ${tone === "green" ? "#86efac" : tone === "red" ? "#fca5a5" : "#fde68a"}`
    }}>{text}</span>
  );

  const onSearch = () => { loadInvoices(); };

  const mappingsBase = () => (import.meta.env.VITE_MAPPINGS_BASE_URL || "http://localhost:8082/invoice-mapping");

  useEffect(() => {
    const loadErps = async () => {
      try {
        const res = await fetch(`${mappingsBase()}/api/erps?status=ACTIVE`, { headers: { ...authHeader() } });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        // Normalize to string list (some backends return objects: {id,name,status,...})
        const raw = Array.isArray(data) ? data : (Array.isArray(data?.items) ? data.items : []);
        const list: string[] = raw
          .filter((x: any) => (x?.status ?? 'ACTIVE').toUpperCase() === 'ACTIVE')
          .map((x: any) => typeof x === 'string' ? x : (x?.name ?? String(x?.id ?? 'SAP')));
        if (list.length) {
          setErpOptions(list);
          if (!list.includes(erp)) setErp(list[0]);
        }
      } catch {
        // fallback; keep defaults
      }
    };
    loadErps();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Open export modal if URL contains ?export=1
  useEffect(() => {
    try {
      const p = new URLSearchParams(window.location.search);
      if (p.get('export') === '1') setShowExport(true);
    } catch {}
  }, []);

  async function exportMappings() {
    setLoadingExport(true); setErrorExport("");
    try {
      const qs = new URLSearchParams({ erp, format });
      qs.set('_ts', String(Date.now())); // cache-busting
      const url = `${mappingsBase()}/api/export?${qs.toString()}`;
      const res = await fetch(url, { headers: { ...authHeader() } as any, cache: 'no-store' });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `HTTP ${res.status}`);
      }
      const blob = await res.blob();
      const a = document.createElement('a');
      const href = URL.createObjectURL(blob);
      a.href = href;
      const ext = format.toLowerCase();
      a.download = `mapeos-${erp}.${ext}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(href);
      setShowExport(false);
    } catch (e: any) {
      setErrorExport(e?.message || 'No se pudo exportar');
    } finally {
      setLoadingExport(false);
    }
  }

  return (
    <div style={{ display: "grid", gap: 16 }}>
      <section style={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 12, padding: 16 }}>
        <h2 style={{ marginTop: 0, color: "var(--text)" }}>Dashboard General</h2>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(6, 1fr)", gap: 12, alignItems: "end" }}>
          <input placeholder="ID usuario / Correo" value={userOrEmail} onChange={e => setUserOrEmail(e.target.value)} style={{ padding: 10, borderRadius: 8, border: `1px solid var(--border)` }} />
          <input type="date" value={from} onChange={e => setFrom(e.target.value)} style={{ padding: 10, borderRadius: 8, border: `1px solid var(--border)` }} />
          <input type="date" value={to} onChange={e => setTo(e.target.value)} style={{ padding: 10, borderRadius: 8, border: `1px solid var(--border)` }} />
          <select value={statusFilter} onChange={e => setStatusFilter(e.target.value as any)} style={{ padding: 10, borderRadius: 8, border: `1px solid var(--border)` }}>
            <option value="">Todos los estados</option>
            <option value="PENDING">Pendiente</option>
            <option value="APPROVED">Aprobada</option>
            <option value="REJECTED">Rechazada</option>
          </select>
          <input placeholder="Sender Tax ID" value={senderTaxId} onChange={e => setSenderTaxId(e.target.value)} style={{ padding: 10, borderRadius: 8, border: `1px solid var(--border)` }} />
          <input placeholder="Receiver Tax ID" value={receiverTaxId} onChange={e => setReceiverTaxId(e.target.value)} style={{ padding: 10, borderRadius: 8, border: `1px solid var(--border)` }} />
          <button type="button" onClick={() => { console.debug('dashboard: open export modal'); setShowExport(true); }} style={{ background: "#16a34a", color: "#fff", border: 0, borderRadius: 8, padding: "10px 12px" }}>Exportar data a ERP</button>
        </div>
        <div style={{ marginTop: 12 }}>
          <button onClick={onSearch} style={{ background: "var(--brand)", color: "#fff", border: 0, borderRadius: 8, padding: "10px 16px" }}>Buscar</button>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 12, marginTop: 16 }}>
          <div style={{ background: "#e0e7ff", border: `1px solid var(--border)`, borderRadius: 12, padding: 14 }}>
            <div style={{ color: "#334155", fontSize: 12 }}>Facturas Ingresadas</div>
            <div style={{ fontSize: 28, color: "#1f2937" }}>{stats.total}</div>
          </div>
          <div style={{ background: "#dcfce7", border: `1px solid var(--border)`, borderRadius: 12, padding: 14 }}>
            <div style={{ color: "#166534", fontSize: 12 }}>Aprobadas</div>
            <div style={{ fontSize: 28, color: "#14532d" }}>{stats.aprobadas}</div>
          </div>
          <div style={{ background: "#fee2e2", border: `1px solid var(--border)`, borderRadius: 12, padding: 14 }}>
            <div style={{ color: "#991b1b", fontSize: 12 }}>Rechazadas</div>
            <div style={{ fontSize: 28, color: "#7f1d1d" }}>{stats.rechazadas}</div>
          </div>
          <div style={{ background: "#fef3c7", border: `1px solid var(--border)`, borderRadius: 12, padding: 14 }}>
            <div style={{ color: "#92400e", fontSize: 12 }}>Monto Total</div>
            <div style={{ fontSize: 24, color: "#7c2d12", fontWeight: 700 }}>{fmtCurrency(stats.monto)}</div>
          </div>
        </div>
      </section>

      <section style={{ background: "var(--card)", border: "1px solid var(--border)", borderRadius: 12, padding: 16 }}>
        <h3 style={{ marginTop: 0 }}>Facturas Recientes</h3>
        {errorInvoices && (
          <ErrorBanner message={errorInvoices} />
        )}
        <div style={{ overflow: "auto" }}>
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr style={{ textAlign: "left", color: "#64748b" }}>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>ID</th>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Fecha</th>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Proveedor</th>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Monto</th>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>ERP</th>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Cliente</th>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Estado</th>
                <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {loadingInvoices && (
                <tr><td colSpan={8} style={{ color: '#64748b', padding: 8, borderTop: `1px solid var(--border)` }}>Cargando…</td></tr>
              )}
              {!loadingInvoices && !errorInvoices && rows.map((row, idx) => (
                <tr key={row.id} style={{ background: idx % 2 === 0 ? "#ffffff" : "#f9fafb" }}>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{row.id}</td>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{row.date}</td>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{row.provider}</td>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{fmtCurrency(row.amount)}</td>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{row.erp || '—'}</td>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{row.customer || '—'}</td>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>
                    {row.status === "Aprobada" && <Badge text="Aprobada" tone="green" />}
                    {row.status === "Rechazada" && <Badge text="Rechazada" tone="red" />}
                    {row.status === "Pendiente" && <Badge text="Pendiente" tone="amber" />}
                  </td>
                  <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>
                    <button type="button" onClick={() => { setDetailId(row.id); setShowDetail(true); }} style={{ background: 'transparent', color: 'var(--brand)', border: 0, padding: 0, cursor: 'pointer' }}>Ver Detalle</button>
                  </td>
                </tr>
              ))}
              {!loadingInvoices && !errorInvoices && rows.length === 0 && (
                <tr>
                  <td colSpan={8} style={{ color: '#64748b', padding: 8, borderTop: `1px solid var(--border)` }}>Sin resultados</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      <Modal open={showExport} onClose={() => setShowExport(false)}>
        <div style={{ display: 'grid', gap: 12 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div>
              <label style={{ display: 'block', fontSize: 12, color: '#64748b', marginBottom: 6 }}>ERP</label>
              <select value={erp} onChange={e => setErp(e.target.value)} style={{ width: '100%', padding: 10, borderRadius: 8, border: '1px solid var(--border)' }}>
                {erpOptions.map(opt => (
                  <option key={opt} value={opt}>{opt}</option>
                ))}
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 12, color: '#64748b', marginBottom: 6 }}>Formato</label>
              <div style={{ display: 'flex', gap: 8 }}>
                <button type="button" onClick={() => setFormat('JSON')} style={{ background: format==='JSON'? 'var(--brand)' : '#e2e8f0', color: format==='JSON'? '#fff' : '#0f172a', border: 0, borderRadius: 8, padding: '8px 12px' }}>JSON</button>
                <button type="button" onClick={() => setFormat('CSV')} style={{ background: format==='CSV'? 'var(--brand)' : '#e2e8f0', color: format==='CSV'? '#fff' : '#0f172a', border: 0, borderRadius: 8, padding: '8px 12px' }}>CSV</button>
              </div>
            </div>
          </div>
          {errorExport && <div style={{ color: '#b91c1c', fontSize: 14 }}>{errorExport}</div>}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
            <button onClick={() => setShowExport(false)} style={{ background: '#e2e8f0', color: '#0f172a', border: 0, borderRadius: 8, padding: '10px 12px' }}>Cancelar</button>
            <button disabled={loadingExport} onClick={exportMappings} style={{ opacity: loadingExport? 0.7:1, background: '#16a34a', color: '#fff', border: 0, borderRadius: 8, padding: '10px 12px' }}>
              {loadingExport ? 'Exportando…' : 'Exportar'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Detalle de factura en modal */}
      <InvoiceDetailModal open={showDetail} id={detailId} onClose={() => {
        setShowDetail(false);
        // refrescar lista: aquí conectaremos a API; por ahora, no-op
      }} />
    </div>
  );
}
 
function Modal({ open, onClose, children }: { open: boolean, onClose: () => void, children: React.ReactNode }) {
  if (!open) return null;
  console.debug('dashboard: export modal rendered');
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(15, 23, 42, 0.5)', display: 'grid', placeItems: 'center', zIndex: 9999 }}>
      <div style={{ background: '#fff', borderRadius: 12, border: '1px solid var(--border)', width: 'min(560px, 92vw)', padding: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <h3 style={{ margin: 0 }}>Exportación de Mapeos</h3>
          <button onClick={onClose} style={{ background: '#e2e8f0', color: '#0f172a', border: 0, borderRadius: 8, padding: '6px 10px' }}>Cerrar</button>
        </div>
        {children}
      </div>
    </div>
  );
}

function InvoiceDetailModal({ open, id, onClose }: { open: boolean, id: string | null, onClose: () => void }) {
  if (!open || !id) return null;
  const { invoice, invoiceStatus, approveInvoice, rejectInvoice, downloadPDF, formattedAmount } = useInvoiceDetail({
    id,
    provider: "—",
    date: new Date().toISOString().slice(0,10),
    amount: 0,
    status: "Pendiente",
  });
  const handleBack = () => {
    onClose();
  };
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(15, 23, 42, 0.5)', display: 'grid', placeItems: 'center', zIndex: 50 }}>
      <div style={{ background: '#fff', borderRadius: 12, border: '1px solid var(--border)', width: 'min(980px, 96vw)', padding: 16 }}>
        <InvoiceDetailView
          id={invoice.id}
          provider={invoice.provider}
          date={invoice.date}
          formattedAmount={formattedAmount}
          status={invoiceStatus}
          pdfUrl={invoice.pdfUrl}
          onApprove={async () => { await approveInvoice(); onClose(); }}
          onReject={async () => { await rejectInvoice(); onClose(); }}
          onDownload={downloadPDF}
          onBack={handleBack}
        />
      </div>
    </div>
  );
}
