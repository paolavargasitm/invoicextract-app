package co.edu.itm.invoiceextract.domain.entity.invoice;

import co.edu.itm.invoiceextract.domain.entity.common.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice_metadata")
@Schema(description = "Detailed metadata information for invoices")
public class InvoiceMetadata extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the metadata", example = "1")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "invoice_id", nullable = false)
    @Schema(description = "Associated invoice")
    private Invoice invoice;

    @Column(name = "invoice_number")
    @Size(max = 100, message = "Invoice number must not exceed 100 characters")
    @Schema(description = "Invoice number from the document", example = "INV-2024-001")
    private String invoiceNumber;

    @Column(name = "customer_name")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    @Schema(description = "Customer name", example = "John Doe")
    private String customerName;

    @Column(name = "customer_email")
    @Size(max = 255, message = "Customer email must not exceed 255 characters")
    @Schema(description = "Customer email address", example = "john.doe@example.com")
    private String customerEmail;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    @Schema(description = "Customer address", example = "123 Main St, City, State 12345")
    private String customerAddress;

    @Column(name = "supplier_name")
    @Size(max = 255, message = "Supplier name must not exceed 255 characters")
    @Schema(description = "Supplier/vendor name", example = "ABC Company Inc.")
    private String supplierName;

    @Column(name = "supplier_email")
    @Size(max = 255, message = "Supplier email must not exceed 255 characters")
    @Schema(description = "Supplier email address", example = "billing@abccompany.com")
    private String supplierEmail;

    @Column(name = "supplier_address", columnDefinition = "TEXT")
    @Schema(description = "Supplier address", example = "456 Business Ave, City, State 67890")
    private String supplierAddress;

    @Column(name = "amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Amount must be non-negative")
    @Schema(description = "Invoice amount", example = "1500.50")
    private BigDecimal amount;

    @Column(name = "currency")
    @Size(max = 10, message = "Currency must not exceed 10 characters")
    @Schema(description = "Currency code", example = "USD")
    private String currency;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Tax amount must be non-negative")
    @Schema(description = "Tax amount", example = "150.05")
    private BigDecimal taxAmount;

    @Column(name = "subtotal", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Subtotal must be non-negative")
    @Schema(description = "Subtotal before taxes", example = "1350.45")
    private BigDecimal subtotal;

    @Column(name = "total_amount", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Total amount must be non-negative")
    @Schema(description = "Total amount including taxes", example = "1500.50")
    private BigDecimal totalAmount;

    @Column(name = "issue_date", nullable = false)
    @NotNull(message = "Issue date is required")
    @Schema(description = "Issue date of the invoice", example = "2024-01-15")
    private LocalDate issueDate;

    @Column(name = "due_date")
    @Schema(description = "Due date for payment", example = "2024-02-15")
    private LocalDate dueDate;

    @Column(name = "payment_terms")
    @Size(max = 100, message = "Payment terms must not exceed 100 characters")
    @Schema(description = "Payment terms", example = "Net 30")
    private String paymentTerms;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Invoice description", example = "Monthly consulting services")
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Schema(description = "Additional notes", example = "Payment due within 30 days")
    private String notes;

    @Column(name = "pdf_url")
    @Size(max = 500, message = "PDF URL must not exceed 500 characters")
    @Schema(description = "URL to the PDF file", example = "https://example.com/invoices/inv-001.pdf")
    private String pdfUrl;

    @Column(name = "original_filename")
    @Size(max = 255, message = "Original filename must not exceed 255 characters")
    @Schema(description = "Original filename of the uploaded document", example = "invoice_001.pdf")
    private String originalFilename;

    @Column(name = "file_size")
    @Min(value = 0, message = "File size must be non-negative")
    @Schema(description = "File size in bytes", example = "1024000")
    private Long fileSize;

    @Column(name = "extracted_data", columnDefinition = "TEXT")
    @Schema(description = "Raw extracted data in JSON format")
    private String extractedData;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Confidence score must be between 0 and 1")
    @DecimalMax(value = "1.0000", message = "Confidence score must be between 0 and 1")
    @Schema(description = "AI extraction confidence score", example = "0.9500")
    private BigDecimal confidenceScore;

    @Column(name = "processing_status")
    @Size(max = 50, message = "Processing status must not exceed 50 characters")
    @Schema(description = "Processing status", example = "COMPLETED")
    private String processingStatus;



    // Constructors
    public InvoiceMetadata() {
        this.currency = "USD";
        this.processingStatus = "PENDING";
    }

    public InvoiceMetadata(Invoice invoice) {
        this();
        this.invoice = invoice;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

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

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getExtractedData() {
        return extractedData;
    }

    public void setExtractedData(String extractedData) {
        this.extractedData = extractedData;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }



    @Override
    public String toString() {
        return "InvoiceMetadata{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", currency='" + currency + '\'' +
                ", processingStatus='" + processingStatus + '\'' +
                '}';
    }
}
