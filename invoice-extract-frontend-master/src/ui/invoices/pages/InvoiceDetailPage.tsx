import React from "react";
import InvoiceDetailView from "../components/InvoiceDetailView";
import { useInvoiceDetail } from "../hooks/useInvoiceDetail";
import { useLocation, useParams } from "react-router-dom";

const InvoiceDetailPage: React.FC = () => {
    const { id: routeId } = useParams<{ id: string }>();
    const location = useLocation();
    const state = (location.state || {}) as Partial<{
        id: string;
        provider: string;
        date: string;
        amount: number;
        status: "Aprobada" | "Rechazada" | "Pendiente";
        pdfUrl?: string;
    }>;

    const {
        invoice,
        invoiceStatus,
        approveInvoice,
        rejectInvoice,
        downloadPDF,
        goBack,
        formattedAmount,
    } = useInvoiceDetail({
        id: routeId || state.id || "FCT-001",
        provider: state.provider || "—",
        date: state.date || new Date().toISOString().slice(0, 10),
        amount: typeof state.amount === 'number' ? state.amount : 0,
        status: state.status || "Pendiente",
        pdfUrl: state.pdfUrl,
    });

    return (
        <InvoiceDetailView
            id={invoice.id}
            provider={invoice.provider}
            date={invoice.date}
            formattedAmount={formattedAmount}
            status={invoiceStatus}
            pdfUrl={invoice.pdfUrl}
            onApprove={approveInvoice}
            onReject={rejectInvoice}
            onDownload={downloadPDF}
            onBack={goBack}
        />
    );
};

export default InvoiceDetailPage;
