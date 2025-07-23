package co.edu.itm.invoiceextract.domain.entity;

import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
@Schema(description = "Invoice entity representing an invoice document in the system")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the invoice", example = "1")
    private Long id;

    @Column(name = "email", nullable = false)
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Email associated with the invoice", example = "customer@example.com")
    private String email;

    @Column(name = "date", nullable = false)
    @NotNull(message = "Date is required")
    @Schema(description = "Date and time of the invoice", example = "2024-01-15T10:30:00")
    private LocalDateTime date;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    @Schema(description = "Current status of the invoice", example = "PENDING")
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Type is required")
    @Schema(description = "Type of the invoice document", example = "INVOICE")
    private InvoiceType type = InvoiceType.INVOICE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Timestamp when the invoice was created")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Timestamp when the invoice was last updated")
    private LocalDateTime updatedAt;

    @Column(name = "file_url")
    @Schema(description = "URL to the invoice PDF file stored in Azure Blob Storage", example = "https://your-azure-storage-url/container/invoice.pdf")
    private String fileUrl;

    @Column(name = "provider")
    @Schema(description = "Name of the invoice supplier or provider", example = "Supplier Inc.")
    private String provider;

    @Column(name = "amount", precision = 19, scale = 4)
    @Schema(description = "Total amount of the invoice", example = "1234.56")
    private java.math.BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Schema(description = "Currency of the invoice amount (ISO 4217 code)", example = "USD")
    private String currency;

    // Relationship with metadata
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(description = "Detailed metadata information for this invoice")
    private InvoiceMetadata metadata;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public InvoiceType getType() {
        return type;
    }

    public void setType(InvoiceType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public InvoiceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(InvoiceMetadata metadata) {
        this.metadata = metadata;
        if (metadata != null) {
            metadata.setInvoice(this);
        }
    }


    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", date=" + date +
                ", status=" + status +
                ", type=" + type +
                '}';
    }
}
