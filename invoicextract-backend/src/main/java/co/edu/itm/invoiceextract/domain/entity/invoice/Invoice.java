package co.edu.itm.invoiceextract.domain.entity.invoice;

import co.edu.itm.invoiceextract.domain.entity.common.AuditableEntity;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoices")
public class Invoice extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 100, unique = true)
    private String documentNumber;

    @Column(name = "receiver_tax_id", length = 50)
    private String receiverTaxId;

    @Column(name = "receiver_tax_id_without_check_digit", length = 50)
    private String receiverTaxIdWithoutCheckDigit;

    @Column(name = "receiver_business_name", length = 255)
    private String receiverBusinessName;

    @Column(name = "sender_tax_id", length = 50)
    private String senderTaxId;

    @Column(name = "sender_tax_id_without_check_digit", length = 50)
    private String senderTaxIdWithoutCheckDigit;

    @Column(name = "sender_business_name", length = 255)
    private String senderBusinessName;

    @Column(name = "related_document_number", length = 100)
    private String relatedDocumentNumber;

    @Column(name = "amount", precision = 15, scale = 2)
    private java.math.BigDecimal amount;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<InvoiceItem> items = new ArrayList<>();

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }
}
