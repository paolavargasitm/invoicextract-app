package co.edu.itm.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    private Long id;
    private String documentType;
    private String documentNumber;
    private String receiverTaxId;
    private String receiverTaxIdWithoutCheckDigit;
    private String receiverBusinessName;
    private String senderTaxId;
    private String senderTaxIdWithoutCheckDigit;
    private String senderBusinessName;
    private String relatedDocumentNumber;
    private BigDecimal amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String status; // PENDING, APPROVED, REJECTED, PAID
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();
}
