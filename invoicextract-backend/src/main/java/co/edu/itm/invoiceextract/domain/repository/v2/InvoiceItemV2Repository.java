package co.edu.itm.invoiceextract.domain.repository.v2;

import co.edu.itm.invoiceextract.domain.entity.invoice.v2.InvoiceItemV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemV2Repository extends JpaRepository<InvoiceItemV2, Long> {
}
