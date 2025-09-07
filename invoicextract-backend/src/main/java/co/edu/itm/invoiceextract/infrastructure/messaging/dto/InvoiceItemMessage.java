package co.edu.itm.invoiceextract.infrastructure.messaging.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemMessage {
    private String itemCode;
    private String description;
    private Integer quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
}
