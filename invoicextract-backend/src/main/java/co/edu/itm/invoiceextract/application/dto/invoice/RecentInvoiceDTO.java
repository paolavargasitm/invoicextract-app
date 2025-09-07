package co.edu.itm.invoiceextract.application.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Recent invoice summary V2")
public class RecentInvoiceDTO {

    @Schema(description = "Invoice ID", example = "1")
    private Long id;

    @Schema(description = "Document number", example = "INV-2024-001")
    private String documentNumber;

    @Schema(description = "Sender business name", example = "Supplier Inc")
    private String senderBusinessName;

    @Schema(description = "Total amount", example = "1500.75")
    private BigDecimal amount;

    @Schema(description = "Issue date", example = "2024-08-03")
    private LocalDate issueDate;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdDate;
}
