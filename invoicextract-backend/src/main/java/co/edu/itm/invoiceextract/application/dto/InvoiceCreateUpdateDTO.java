package co.edu.itm.invoiceextract.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO for creating or updating an invoice")
public class InvoiceCreateUpdateDTO {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    @Schema(description = "Unique invoice number", example = "INV-2024-001", required = true)
    private String invoiceNumber;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    @Schema(description = "Name of the customer", example = "John Doe", required = true)
    private String customerName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Invoice amount", example = "1500.50", required = true)
    private BigDecimal amount;

    @NotNull(message = "Issue date is required")
    @Schema(description = "Date when the invoice was issued", example = "2024-01-15", required = true)
    private LocalDate issueDate;

    @Schema(description = "Due date for payment", example = "2024-02-15")
    private LocalDate dueDate;

    @Size(max = 500, message = "PDF URL must not exceed 500 characters")
    @Schema(description = "URL to the PDF file of the invoice", example = "https://example.com/invoices/inv-001.pdf")
    private String pdfUrl;

    @Schema(description = "Current status of the invoice", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED", "PAID"})
    private String status;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Description or notes about the invoice", example = "Monthly service invoice")
    private String description;

    // Constructors
    public InvoiceCreateUpdateDTO() {}

    public InvoiceCreateUpdateDTO(String invoiceNumber, String customerName, BigDecimal amount, LocalDate issueDate) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.amount = amount;
        this.issueDate = issueDate;
    }

    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
