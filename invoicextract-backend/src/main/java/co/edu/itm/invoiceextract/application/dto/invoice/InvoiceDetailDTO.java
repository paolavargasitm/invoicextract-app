package co.edu.itm.invoiceextract.application.dto.invoice;

import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Detailed invoice information V2")
public class InvoiceDetailDTO {

    @Schema(description = "Invoice ID", example = "1")
    private Long id;

    @Schema(description = "Document type", example = "FACTURA")
    private String documentType;

    @Schema(description = "Document number", example = "INV-2024-001")
    private String documentNumber;

    @Schema(description = "Receiver tax ID", example = "123456789")
    private String receiverTaxId;

    @Schema(description = "Receiver business name", example = "Customer Corp")
    private String receiverBusinessName;

    @Schema(description = "Sender tax ID", example = "987654321")
    private String senderTaxId;

    @Schema(description = "Sender business name", example = "Supplier Inc")
    private String senderBusinessName;

    @Schema(description = "Invoice PDF path", example = "/invoices/INV-2024-001.pdf")
    private String invoicePathPDF;

    @Schema(description = "Invoice XML path", example = "/invoices/INV-2024-001.xml")
    private String invoicePathXML;

    @Schema(description = "Total amount", example = "1500.75")
    private BigDecimal amount;

    @Schema(description = "Issue date", example = "2024-08-03")
    private LocalDate issueDate;

    @Schema(description = "Due date", example = "2024-09-03")
    private LocalDate dueDate;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdDate;

    @Schema(description = "Last modification timestamp")
    private LocalDateTime modifiedDate;

    @Schema(description = "Created by user")
    private String createdBy;

    @Schema(description = "Modified by user")
    private String modifiedBy;

    @Schema(description = "Invoice status", example = "PENDING")
    private InvoiceStatus status;

    @Schema(description = "Invoice items")
    private List<InvoiceItemDTO> items;
}
