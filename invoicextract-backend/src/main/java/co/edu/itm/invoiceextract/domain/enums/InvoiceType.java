package co.edu.itm.invoiceextract.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Types of invoice documents")
public enum InvoiceType {
    @Schema(description = "Standard invoice")
    INVOICE("Invoice"),
    
    @Schema(description = "Credit note - reduces amount owed")
    CREDIT_NOTE("Credit Note"),
    
    @Schema(description = "Debit note - increases amount owed")
    DEBIT_NOTE("Debit Note");

    private final String displayName;

    InvoiceType(String displayName) {
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
