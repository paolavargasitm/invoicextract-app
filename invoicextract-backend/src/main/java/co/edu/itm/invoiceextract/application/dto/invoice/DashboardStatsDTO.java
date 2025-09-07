package co.edu.itm.invoiceextract.application.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "Dashboard statistics V2")
public class DashboardStatsDTO {

    @Schema(description = "Total number of invoices", example = "150")
    private long totalCount;

    @Schema(description = "Number of pending invoices", example = "25")
    private long pendingCount;

    @Schema(description = "Number of approved invoices", example = "100")
    private long approvedCount;

    @Schema(description = "Number of rejected invoices", example = "15")
    private long rejectedCount;

    @Schema(description = "Number of paid invoices", example = "10")
    private long paidCount;

    @Schema(description = "Number of facturas", example = "120")
    private long facturaCount;

    @Schema(description = "Number of credit notes", example = "20")
    private long notaCreditoCount;

    @Schema(description = "Number of debit notes", example = "10")
    private long notaDebitoCount;

    @Schema(description = "Total amount of all invoices", example = "125000.50")
    private BigDecimal totalAmount;

    @Schema(description = "Average invoice amount", example = "833.34")
    private BigDecimal averageAmount;
}
