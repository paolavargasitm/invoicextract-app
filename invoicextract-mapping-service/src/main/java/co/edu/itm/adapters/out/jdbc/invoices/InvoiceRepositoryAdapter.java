package co.edu.itm.adapters.out.jdbc.invoices;
import co.edu.itm.domain.ports.InvoiceRepositoryPort;
import co.edu.itm.domain.model.Invoice;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Component
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {
  @Override
  public List<Invoice> findApproved(){
    // Demo data; reemplazar con JdbcTemplate/JPA a Invoices DB (read-only)
    return List.of(
      Invoice.builder().invoiceId("INV-001").customerId("C-01").total(new BigDecimal("123.45")).issueDate(LocalDate.now()).currency("USD").status("APPROVED").build(),
      Invoice.builder().invoiceId("INV-002").customerId("C-02").total(new BigDecimal("67.89")).issueDate(LocalDate.now().minusDays(1)).currency("USD").status("APPROVED").build()
    );
  }
}
