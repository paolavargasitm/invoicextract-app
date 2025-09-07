package co.edu.itm.invoiceextract.application.usecase.invoice;

import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class RejectInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;

    public RejectInvoiceUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice reject(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
        
        // Set status to REJECTED
        invoice.setStatus(InvoiceStatus.REJECTED);
        
        // Update modification date
        invoice.setModifiedDate(LocalDateTime.now());
        
        return invoiceRepository.save(invoice);
    }
}
