package co.edu.itm.invoiceextract.domain.repository.invoices;

import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
}
