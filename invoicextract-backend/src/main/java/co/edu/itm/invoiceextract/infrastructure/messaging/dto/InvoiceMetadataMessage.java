package co.edu.itm.invoiceextract.infrastructure.messaging.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceMetadataMessage {
    private String invoiceNumber;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String supplierName;
    private String supplierEmail;
    private String supplierAddress;
    private BigDecimal amount;
    private String currency;
    private BigDecimal taxAmount;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String paymentTerms;
    private String description;
}
