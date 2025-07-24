package co.edu.itm.invoiceextract.application.usecase;

import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;

    public RejectInvoiceUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice reject(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(invoice -> {
                    invoice.setStatus(InvoiceStatus.REJECTED);
                    return invoiceRepository.save(invoice);
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
    }
}