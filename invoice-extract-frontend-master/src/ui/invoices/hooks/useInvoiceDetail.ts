import { useMemo, useState } from "react";
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
        if (initial.pdfUrl) {
            try {
                const link = document.createElement("a");
                link.href = initial.pdfUrl;
                link.download = `factura-${initial.id}.pdf`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                return true;
            } catch (err) {
                window.open(initial.pdfUrl, "_blank", "noopener,noreferrer");
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
    };
}
