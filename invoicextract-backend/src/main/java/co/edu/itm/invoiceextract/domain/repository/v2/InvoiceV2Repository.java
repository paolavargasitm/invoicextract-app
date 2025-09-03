package co.edu.itm.invoiceextract.domain.repository.v2;

import co.edu.itm.invoiceextract.domain.entity.invoice.v2.InvoiceV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceV2Repository extends JpaRepository<InvoiceV2, Long> {
    Optional<InvoiceV2> findByDocumentNumber(String documentNumber);
}
