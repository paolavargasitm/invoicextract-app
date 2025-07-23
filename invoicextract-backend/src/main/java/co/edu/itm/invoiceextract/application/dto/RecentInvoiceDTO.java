package co.edu.itm.invoiceextract.application.dto;

import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentInvoiceDTO {

    private Long id;
    private LocalDateTime date;
    private String supplierName;
    private BigDecimal totalAmount;
    private InvoiceStatus status;

    public RecentInvoiceDTO(Long id, LocalDateTime date, String supplierName, BigDecimal totalAmount, InvoiceStatus status) {
        this.id = id;
        this.date = date;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }
}
