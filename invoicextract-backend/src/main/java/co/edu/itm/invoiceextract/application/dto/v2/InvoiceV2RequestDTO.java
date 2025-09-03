package co.edu.itm.invoiceextract.application.dto.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Invoice V2 request payload mapping the new external structure")
public class InvoiceV2RequestDTO {

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

    @JsonProperty("Amount")
    private String amount; // keep as string input, we'll parse to BigDecimal

    @JsonProperty("IssueDate")
    private LocalDate issueDate;

    @JsonProperty("DueDate")
    private LocalDate dueDate;

    @JsonProperty("InvoiceItem")
    private InvoiceItemV2DTO invoiceItem;

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getReceiverTaxId() { return receiverTaxId; }
    public void setReceiverTaxId(String receiverTaxId) { this.receiverTaxId = receiverTaxId; }

    public String getReceiverTaxIdWithoutCheckDigit() { return receiverTaxIdWithoutCheckDigit; }
    public void setReceiverTaxIdWithoutCheckDigit(String receiverTaxIdWithoutCheckDigit) { this.receiverTaxIdWithoutCheckDigit = receiverTaxIdWithoutCheckDigit; }

    public String getReceiverBusinessName() { return receiverBusinessName; }
    public void setReceiverBusinessName(String receiverBusinessName) { this.receiverBusinessName = receiverBusinessName; }

    public String getSenderTaxId() { return senderTaxId; }
    public void setSenderTaxId(String senderTaxId) { this.senderTaxId = senderTaxId; }

    public String getSenderTaxIdWithoutCheckDigit() { return senderTaxIdWithoutCheckDigit; }
    public void setSenderTaxIdWithoutCheckDigit(String senderTaxIdWithoutCheckDigit) { this.senderTaxIdWithoutCheckDigit = senderTaxIdWithoutCheckDigit; }

    public String getSenderBusinessName() { return senderBusinessName; }
    public void setSenderBusinessName(String senderBusinessName) { this.senderBusinessName = senderBusinessName; }

    public String getRelatedDocumentNumber() { return relatedDocumentNumber; }
    public void setRelatedDocumentNumber(String relatedDocumentNumber) { this.relatedDocumentNumber = relatedDocumentNumber; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public InvoiceItemV2DTO getInvoiceItem() { return invoiceItem; }
    public void setInvoiceItem(InvoiceItemV2DTO invoiceItem) { this.invoiceItem = invoiceItem; }
}
