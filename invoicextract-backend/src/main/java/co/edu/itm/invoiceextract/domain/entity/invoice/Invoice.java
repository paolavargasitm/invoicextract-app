package co.edu.itm.invoiceextract.domain.entity.invoice;

import co.edu.itm.invoiceextract.domain.entity.common.AuditableEntity;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}
