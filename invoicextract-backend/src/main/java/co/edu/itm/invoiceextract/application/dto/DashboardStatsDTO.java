package co.edu.itm.invoiceextract.application.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {

    private long totalInvoices;
    private long successfulInvoices;
    private long errorInvoices;
    private BigDecimal totalAmount;

    public DashboardStatsDTO(long totalInvoices, long successfulInvoices, long errorInvoices, BigDecimal totalAmount) {
        this.totalInvoices = totalInvoices;
        this.successfulInvoices = successfulInvoices;
        this.errorInvoices = errorInvoices;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters

    public long getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(long totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public long getSuccessfulInvoices() {
        return successfulInvoices;
    }

    public void setSuccessfulInvoices(long successfulInvoices) {
        this.successfulInvoices = successfulInvoices;
    }

    public long getErrorInvoices() {
        return errorInvoices;
    }

    public void setErrorInvoices(long errorInvoices) {
        this.errorInvoices = errorInvoices;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
