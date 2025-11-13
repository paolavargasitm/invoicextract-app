package co.edu.itm.invoiceextract.infrastructure.messaging.dto;

import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceMessage {
    // Kafka processing fields
    private String email;
    private LocalDateTime date;
    private InvoiceStatus status;
    private String invoicePathPDF;
    private String invoicePathXML;
    
    // Core invoice fields matching InvoiceRequestDTO
    private String documentType;
    private String documentNumber;
    private String receiverTaxId;
    private String receiverTaxIdWithoutCheckDigit;
    private String receiverBusinessName;
    private String senderTaxId;
    private String senderTaxIdWithoutCheckDigit;
    private String senderBusinessName;
    private String relatedDocumentNumber;
    private String amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    
    // Invoice item details
    private InvoiceItemMessage invoiceItem;
    private List<InvoiceItemMessage> invoiceItems;
}
