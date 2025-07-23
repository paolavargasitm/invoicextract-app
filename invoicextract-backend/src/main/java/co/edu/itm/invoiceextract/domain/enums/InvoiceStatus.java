package co.edu.itm.invoiceextract.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of invoice processing")
public enum InvoiceStatus {
    @Schema(description = "Invoice is pending review")
    PENDING("Pending"),
    
    @Schema(description = "Invoice has been approved")
    APPROVED("Approved"),
    
    @Schema(description = "Invoice has been rejected")
    REJECTED("Rejected"),
    
    @Schema(description = "Invoice has been paid")
    PAID("Paid");

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}