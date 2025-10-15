import { useMemo, useState } from "react";

export type InvoiceStatus = "Aprobada" | "Rechazada" | "Pendiente";

export type Invoice = {
    id: string;
    provider: string;
    date: string;
    amount: number;
    status?: InvoiceStatus;
    pdfUrl?: string;
};

export function useInvoiceDetail(initial: Invoice) {
    const [invoiceStatus, setInvoiceStatus] = useState<InvoiceStatus>(
        initial.status ?? "Pendiente"
    );

    const approveInvoice = () => setInvoiceStatus("Aprobada");
    const rejectInvoice = () => setInvoiceStatus("Rechazada");

    const downloadPDF = () => {
        if (initial.pdfUrl) {
            window.open(initial.pdfUrl, "_blank", "noopener,noreferrer");
        } else {
            alert("Funcionalidad para descargar PDF");
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
    };
}
