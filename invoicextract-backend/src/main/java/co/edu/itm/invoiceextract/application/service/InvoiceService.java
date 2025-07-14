package co.edu.itm.invoiceextract.application.service;


import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.domain.repository.InvoiceRepository;

@Service
public class InvoiceService {
    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    public List<Invoice> findAll() {
        return repository.findAll();
    }

    public Optional<Invoice> findById(Long id) {
        return repository.findById(id);
    }

    public Invoice save(Invoice invoice) {
        return repository.save(invoice);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
