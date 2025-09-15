package co.edu.itm.domain.ports;
import co.edu.itm.domain.model.Invoice;
import java.util.List;
public interface InvoiceRepositoryPort {
  List<Invoice> findApproved();
}
