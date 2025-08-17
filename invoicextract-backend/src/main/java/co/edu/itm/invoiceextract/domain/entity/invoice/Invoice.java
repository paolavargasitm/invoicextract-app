package co.edu.itm.invoiceextract.domain.entity.invoice;

import co.edu.itm.invoiceextract.domain.entity.common.AuditableEntity;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
@Schema(description = "Invoice entity representing an invoice document in the system")
public class Invoice extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the invoice", example = "1")
    private Long id;

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

    @Column(name = "file_url")
    @Schema(description = "URL to the invoice PDF file stored in Azure Blob Storage", example = "https://your-azure-storage-url/container/invoice.pdf")
    private String fileUrl;

    // Relationship with metadata
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(description = "Detailed metadata information for this invoice")
    private InvoiceMetadata metadata;



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



    public InvoiceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(InvoiceMetadata metadata) {
        this.metadata = metadata;
        if (metadata != null) {
            metadata.setInvoice(this);
        }
    }


    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", status=" + status +
                ", type=" + type +
                ", fileUrl='" + fileUrl + '\'' +
                '}';
    }
}
