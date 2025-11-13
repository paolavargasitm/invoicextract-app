package co.edu.itm.invoiceextract.application.dto.invoice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "Invoice request payload mapping the external structure")
public class InvoiceRequestDTO {

    @JsonProperty("DocumentType")
    private String documentType; // e.g., "Factura"

    @JsonProperty("DocumentNumber")
    private String documentNumber;

    @JsonProperty("ReceiverTaxId")
    private String receiverTaxId;

    @JsonProperty("ReceiverTaxIdWithoutCheckDigit")
    private String receiverTaxIdWithoutCheckDigit;

    @JsonProperty("ReceiverBusinessName")
    private String receiverBusinessName;

    @JsonProperty("SenderTaxId")
    private String senderTaxId;

    @JsonProperty("SenderTaxIdWithoutCheckDigit")
    private String senderTaxIdWithoutCheckDigit;

    @JsonProperty("SenderBusinessName")
    private String senderBusinessName;

    @JsonProperty("RelatedDocumentNumber")
    private String relatedDocumentNumber;

    @JsonProperty("InvoicePathPDF")
    @JsonAlias({"invoicePathPDF", "invoice_path_pdf", "FileUrl", "fileUrl", "file_url", "fileurl"})
    private String invoicePathPDF;

    @JsonProperty("InvoicePathXML")
    @JsonAlias({"invoicePathXML", "invoice_path_xml"})
    private String invoicePathXML;

    @JsonProperty("Amount")
    private String amount; // keep as string input, we'll parse to BigDecimal

    @JsonProperty("IssueDate")
    private LocalDate issueDate;

    @JsonProperty("DueDate")
    private LocalDate dueDate;

    @JsonProperty("InvoiceItem")
    private InvoiceItemDTO invoiceItem;

    @JsonProperty("InvoiceItems")
    @JsonAlias({"invoiceItems", "Items", "items"})
    private List<InvoiceItemDTO> invoiceItems;
}
