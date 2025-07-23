package co.edu.itm.invoiceextract.application.dto;

import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceDetailDTO {

    private String supplierName;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private String fileUrl;

    public InvoiceDetailDTO(String supplierName, LocalDate issueDate, BigDecimal totalAmount, InvoiceStatus status, String fileUrl) {
        this.supplierName = supplierName;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
