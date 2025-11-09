import React from "react";
import "../styles/InvoiceDetail.css";

export type InvoiceDetailViewProps = {
    id: string;
    provider: string;
    date: string;
    formattedAmount: string;
    status: string;
    pdfUrl?: string;
    onApprove: () => void;
    onReject: () => void;
    onDownload: () => void;
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
    provider,
    date,
    formattedAmount,
    status,
    pdfUrl,
    onApprove,
    onReject,
    onDownload,
    onBack,
}) => {
    console.log("ID", id);
    const variant = statusVariant(status);

    return (
        <div className="invd__container">
            <button className="invd__btn invd__btn--back invd__btn--back-top" onClick={onBack}>
                Volver
            </button>
            <h2 className="invd__title">Detalle de Factura: {id}</h2>

            <p><strong>Proveedor:</strong> {provider}</p>
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

            <h3 className="invd__actions-title">Acciones de Revisión</h3>
            <div className="invd__actions">
                <button className="invd__btn invd__btn--approve" onClick={onApprove}>
                    Aprobar Factura
                </button>
                <button className="invd__btn invd__btn--reject" onClick={onReject}>
                    Rechazar Factura
                </button>
                <button className="invd__btn invd__btn--download" onClick={() => { if (pdfUrl) { window.open(pdfUrl, '_blank', 'noopener,noreferrer'); } else { onDownload(); } }}>
                    Descargar PDF
                </button>
            </div>
        </div>
    );
};

export default InvoiceDetailView;
