package co.edu.itm.invoiceextract.domain.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.itm.invoiceextract.domain.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}
