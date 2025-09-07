package co.edu.itm.invoiceextract.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Types of invoice documents")
public enum InvoiceType {
    @Schema(description = "Factura estándar")
    FACTURA("Factura"),
    
    @Schema(description = "Nota crédito - reduce el monto adeudado")
    NOTA_CREDITO("Nota Crédito"),
    
    @Schema(description = "Nota débito - aumenta el monto adeudado")
    NOTA_DEBITO("Nota Débito");

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
