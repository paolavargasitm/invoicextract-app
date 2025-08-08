package co.edu.itm.invoiceextract.infrastructure.messaging.dto;

import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvoiceMessage {
    private String email;
    private LocalDateTime date;
    private InvoiceStatus status;
    private InvoiceType type;
    private String fileUrl;
    private String provider;
    private BigDecimal amount;
    private String currency;
    private InvoiceMetadataMessage metadata;
}
