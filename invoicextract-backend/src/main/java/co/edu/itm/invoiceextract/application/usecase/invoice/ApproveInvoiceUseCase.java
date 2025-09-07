package co.edu.itm.invoiceextract.application.usecase.invoice;

import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ApproveInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;

    public ApproveInvoiceUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice approve(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
        
        // Set status to APPROVED
        invoice.setStatus(InvoiceStatus.APPROVED);
        
        // Update modification date
        invoice.setModifiedDate(LocalDateTime.now());
        
        return invoiceRepository.save(invoice);
    }
}
