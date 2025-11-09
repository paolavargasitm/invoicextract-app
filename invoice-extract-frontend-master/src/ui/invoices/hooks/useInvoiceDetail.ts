import { useMemo, useState, useEffect } from "react";
import { invoicesApi } from "../api/invoiceDetailApi"
import { useNavigate } from "react-router-dom";

export type InvoiceStatus = "Aprobada" | "Rechazada" | "Pendiente";

export type Invoice = {
    id: string;
    provider: string;
    date: string;
    amount: number;
    status?: InvoiceStatus;
    pdfUrl?: string;
};

export type InvoiceItem = {
    itemCode?: string;
    description?: string;
    quantity?: number;
    unit?: string;
    unitPrice?: number | string;
    subtotal?: number | string;
    taxAmount?: number | string;
    total?: number | string;
};

const frontendToBackendStatus: Record<InvoiceStatus, "PENDING" | "APPROVED" | "REJECTED"> = {
    Pendiente: "PENDING",
    Aprobada: "APPROVED",
    Rechazada: "REJECTED",
};

export function useInvoiceDetail(initial: Invoice) {
    const navigate = useNavigate();
    const [invoiceStatus, setInvoiceStatus] = useState<InvoiceStatus>(
        initial.status ?? "Pendiente"
    );
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<null | { message: string; details?: any }>(null);
    const [success, setSuccess] = useState<null | { message: string }>(null);
    const [items, setItems] = useState<InvoiceItem[]>([]);
    const [detailPdfUrl, setDetailPdfUrl] = useState<string | undefined>(undefined);

    const changeStatus = async (newStatus: InvoiceStatus): Promise<boolean> => {
        setLoading(true);
        setError(null);
        setSuccess(null);
        try {
            const backendStatus = frontendToBackendStatus[newStatus];
            await invoicesApi.changeStatus(initial.id, backendStatus);
            setInvoiceStatus(newStatus);
            setSuccess({ message: newStatus === "Aprobada" ? "La factura ha sido aprobada." : newStatus === "Rechazada" ? "La factura ha sido rechazada." : "Estado actualizado." });
            return true;
        } catch (err: any) {
            console.error("Error cambiando estado de factura", err);
            setError({ message: err?.message || "No se pudo cambiar el estado", details: err?.details });
            return false;
        } finally {
            setLoading(false);
        }
    };

    const approveInvoice = async (): Promise<boolean> => {
        return changeStatus("Aprobada");
    };

    const rejectInvoice = async (): Promise<boolean> => {
        return changeStatus("Rechazada");
    };

    const downloadPDF = async (): Promise<boolean> => {
        const effectivePdf = detailPdfUrl || initial.pdfUrl;
        if (effectivePdf) {
            try {
                const link = document.createElement("a");
                link.href = effectivePdf;
                link.download = `factura-${initial.id}.pdf`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                return true;
            } catch (err) {
                window.open(effectivePdf, "_blank", "noopener,noreferrer");
                return true;
            }
        } else {
            setError({ message: "No hay PDF disponible para esta factura" });
            return false;
        }
    };

    const goBack = () => {
        try {
            navigate(-1);
        } catch {
            window.history.back();
        }
    };

    const formattedAmount = useMemo(() => {
        try {
            return new Intl.NumberFormat("es-CO", {
                style: "currency",
                currency: "COP",
                maximumFractionDigits: 0,
            }).format(initial.amount);
        } catch {
            return `$${initial.amount.toLocaleString()}`;
        }
    }, [initial.amount]);

    // Load detailed data (items, fileUrl) once per invoice id
    useEffect(() => {
        let ignore = false;
        (async () => {
            try {
                const detail = await invoicesApi.getById(initial.id);
                if (!ignore) {
                    if (detail?.items && Array.isArray(detail.items)) {
                        const normalized = detail.items.map((it: any) => ({
                            itemCode: it?.itemCode ?? it?.ItemCode ?? it?.code ?? it?.Code ?? undefined,
                            description: it?.description ?? it?.Description ?? undefined,
                            quantity: it?.quantity ?? it?.Quantity ?? undefined,
                            unit: it?.unit ?? it?.Unit ?? undefined,
                            unitPrice: it?.unitPrice ?? it?.UnitPrice ?? it?.price ?? it?.Price ?? undefined,
                            subtotal: it?.subtotal ?? it?.Subtotal ?? undefined,
                            taxAmount: it?.taxAmount ?? it?.TaxAmount ?? undefined,
                            total: it?.total ?? it?.Total ?? it?.amount ?? it?.Amount ?? undefined,
                        }));
                        setItems(normalized);
                    }
                    if (detail?.fileUrl) setDetailPdfUrl(detail.fileUrl);
                }
            } catch (err: any) {
                // silent detail load failure to avoid blocking actions
                console.debug('invoice detail load failed', err?.message || err);
            }
        })();
        return () => { ignore = true; };
    }, [initial.id]);

    return {
        invoice: initial,
        invoiceStatus,
        approveInvoice,
        rejectInvoice,
        downloadPDF,
        goBack,
        formattedAmount,
        loading,
        error,
        clearError: () => setError(null),
        success,
        clearSuccess: () => setSuccess(null),
        items,
        pdfUrl: detailPdfUrl || initial.pdfUrl,
    };
}
