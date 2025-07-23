package co.edu.itm.invoiceextract.application.dto;

import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Schema(description = "DTO for creating or updating an invoice")
public class InvoiceRequestDTO {

    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "Email associated with the invoice", example = "customer@example.com", required = true)
    private String email;

    @NotNull(message = "Date is required")
    @Schema(description = "Date and time of the invoice", example = "2024-01-15T10:30:00", required = true)
    private LocalDateTime date;

    @Schema(description = "Current status of the invoice", example = "PENDING")
    private InvoiceStatus status;

    @NotNull(message = "Type is required")
    @Schema(description = "Type of the invoice document", example = "INVOICE", required = true)
    private InvoiceType type;

    @Schema(description = "Detailed metadata information for this invoice")
    private InvoiceMetadataRequestDTO metadata;

    // Constructors
    public InvoiceRequestDTO() {}

    public InvoiceRequestDTO(String email, LocalDateTime date, InvoiceType type) {
        this.email = email;
        this.date = date;
        this.type = type;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public InvoiceMetadataRequestDTO getMetadata() {
        return metadata;
    }

    public void setMetadata(InvoiceMetadataRequestDTO metadata) {
        this.metadata = metadata;
    }
}
