import React from "react";
import "../styles/InvoiceDetail.css";

export type InvoiceDetailViewProps = {
    id: string;
    provider: string;
    date: string;
    formattedAmount: string;
    status: string;
    pdfUrl?: string;
    xmlUrl?: string;
    // Nuevos campos: NIT y nombres de emisor/receptor y número de factura
    senderTaxId?: string;
    receiverTaxId?: string;
    senderName?: string;
    receiverName?: string;
    items?: Array<{ itemCode?: string; description?: string; quantity?: number; unit?: string; subtotal?: number | string; total?: number | string; }>;
    onApprove: () => void;
    onReject: () => void;
    onDownload: () => void;
    onDownloadXml: () => void;
    onBack: () => void;
};

function statusVariant(status: string) {
    const s = status.toLowerCase();
    if (s.includes("aprob")) return "approved";
    if (s.includes("rechaz")) return "rejected";
    return "pending";
}

const InvoiceDetailView: React.FC<InvoiceDetailViewProps> = ({
    id,
    date,
    formattedAmount,
    status,
    pdfUrl,
    xmlUrl,
    senderTaxId,
    receiverTaxId,
    senderName,
    receiverName,
    items = [],
    onApprove,
    onReject,
    onDownload,
    onDownloadXml,
    onBack,
}) => {
    console.log("ID", id);
    const variant = statusVariant(status);

    return (
        <div className="invd__container">
            <button className="invd__btn invd__btn--back invd__btn--back-top" onClick={onBack}>
                Volver
            </button>
            <div className="invd__content-scroll">
                <h2 className="invd__title">Detalle de Factura: {id}</h2>

                
                
                <p><strong>Emisor (NIT):</strong> {senderTaxId || "—"} {senderName ? `- ${senderName}` : ""}</p>
                <p><strong>Receptor (NIT):</strong> {receiverTaxId || "—"} {receiverName ? `- ${receiverName}` : ""}</p>
                <p><strong>Fecha:</strong> {date}</p>
                <p><strong>Monto:</strong> {formattedAmount}</p>
                <p>
                    <strong>Estado:</strong>
                    <span className={`invd__status invd__status--${variant}`}>{status}</span>
                </p>

                <div className="invd__pdf">
                    {pdfUrl ? (
                        <iframe
                            title={`PDF ${id}`}
                            src={pdfUrl}
                            className="invd__pdf-frame"
                        />
                    ) : (
                        <div className="invd__pdf-placeholder">[Visor PDF - Visualización previa]</div>
                    )}
                </div>

                <div style={{ marginTop: 8 }}>
                    {pdfUrl && (
                        <a href={pdfUrl} target="_blank" rel="noopener noreferrer" className="invd__link">
                            Abrir PDF en nueva pestaña
                        </a>
                    )}
                </div>

                {Array.isArray(items) && items.length > 0 && (
                    <div style={{ marginTop: 16 }}>
                        <h3 className="invd__actions-title">Items de la Factura</h3>
                        <div style={{ overflow: "auto" }}>
                            <table style={{ width: "100%", borderCollapse: "collapse" }}>
                                <thead>
                                    <tr style={{ textAlign: "left", color: "var(--muted)" }}>
                                        <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Código</th>
                                        <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Descripción</th>
                                        <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Cantidad</th>
                                        <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Unidad</th>
                                        <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Subtotal</th>
                                        <th style={{ padding: "10px 8px", fontSize: 12, textTransform: "uppercase" }}>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {items.map((it, idx) => (
                                        <tr key={idx} style={{ background: idx % 2 === 0 ? "var(--card)" : "var(--bg)" }}>
                                            <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{it.itemCode || "—"}</td>
                                            <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{it.description || "—"}</td>
                                            <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{it.quantity ?? "—"}</td>
                                            <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{it.unit || "—"}</td>
                                            <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{String(it.subtotal ?? "—")}</td>
                                            <td style={{ padding: 8, borderTop: `1px solid var(--border)` }}>{String(it.total ?? "—")}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>

            <h3 className="invd__actions-title">Acciones de Revisión</h3>
            <div className="invd__actions invd__actions--sticky">
                <button className="invd__btn invd__btn--approve" onClick={onApprove}>
                    Aprobar Factura
                </button>
                <button className="invd__btn invd__btn--reject" onClick={onReject}>
                    Rechazar Factura
                </button>
                <button className="invd__btn invd__btn--download" onClick={() => { if (pdfUrl) { window.open(pdfUrl, '_blank', 'noopener,noreferrer'); } else { onDownload(); } }}>
                    Descargar PDF
                </button>
                <button className="invd__btn invd__btn--download" onClick={() => { if (xmlUrl) { window.open(xmlUrl, '_blank', 'noopener,noreferrer'); } else { onDownloadXml(); } }} disabled={!xmlUrl && !onDownloadXml}>
                    Descargar XML
                </button>
            </div>
        </div>
    );
};

export default InvoiceDetailView;
