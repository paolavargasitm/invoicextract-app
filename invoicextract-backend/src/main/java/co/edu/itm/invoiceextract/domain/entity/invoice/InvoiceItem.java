package co.edu.itm.invoiceextract.domain.entity.invoice;

import co.edu.itm.invoiceextract.domain.entity.common.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "invoice_items")
public class InvoiceItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonBackReference
    private Invoice invoice;

    @Column(name = "item_code", length = 100)
    private String itemCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total;
}
