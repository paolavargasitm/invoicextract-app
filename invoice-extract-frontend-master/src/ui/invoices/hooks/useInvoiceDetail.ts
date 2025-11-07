import { useMemo, useState } from "react";
import { invoicesApi } from "../api/invoiceDetailApi"

export type InvoiceStatus = "Aprobada" | "Rechazada" | "Pendiente";

export type Invoice = {
    id: string;
    provider: string;
    date: string;
    amount: number;
    status?: InvoiceStatus;
    pdfUrl?: string;
};

const frontendToBackendStatus: Record<InvoiceStatus, "Pendiente" | "Aprobada" | "Rechazada"> = {
    Pendiente: "Pendiente",
    Aprobada: "Aprobada",
    Rechazada: "Rechazada",
};

export function useInvoiceDetail(initial: Invoice) {
    const [invoiceStatus, setInvoiceStatus] = useState<InvoiceStatus>(
        initial.status ?? "Pendiente"
    );
    const [loading, setLoading] = useState(false);

    const changeStatus = async (newStatus: InvoiceStatus) => {
        setLoading(true);
        try {
            const backendStatus = frontendToBackendStatus[newStatus];
            await invoicesApi.changeStatus(initial.id, backendStatus);
            setInvoiceStatus(newStatus);
        } catch (err: any) {
            console.error("Error cambiando estado de factura", err);
            alert(err?.message || "No se pudo cambiar el estado");
        } finally {
            setLoading(false);
        }
    };

    const approveInvoice = async () => {
        await changeStatus("Aprobada");
    };

    const rejectInvoice = async () => {
        await changeStatus("Rechazada");
    };

    const downloadPDF = async () => {
        if (initial.pdfUrl) {
            try {
                const link = document.createElement("a");
                link.href = initial.pdfUrl;
                link.download = `factura-${initial.id}.pdf`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            } catch (err) {
                window.open(initial.pdfUrl, "_blank", "noopener,noreferrer");
            }
        } else {
            alert("No hay PDF disponible para esta factura");
        }
    };

    const goBack = () => {
        alert("Funcionalidad para volver");
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
    };
}
