import React from "react";
import InvoiceDetailView from "../components/InvoiceDetailView";
import { useInvoiceDetail } from "../hooks/useInvoiceDetail";
import { useParams } from "react-router-dom";

const InvoiceDetailPage: React.FC = () => {
    const { id: routeId } = useParams<{ id: string }>();
    const {
        invoice,
        invoiceStatus,
        approveInvoice,
        rejectInvoice,
        downloadPDF,
        goBack,
        formattedAmount,
    } = useInvoiceDetail({
        id: routeId || "FCT-001",
        provider: "Grupo Éxito",
        date: "2025-04-21",
        amount: 1_200_000,
        status: "Aprobada",
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
