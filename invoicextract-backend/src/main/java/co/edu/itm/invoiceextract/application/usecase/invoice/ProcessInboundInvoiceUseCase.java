package co.edu.itm.invoiceextract.application.usecase.invoice;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.service.InvoiceService;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcessInboundInvoiceUseCase {

    private final InvoiceService invoiceService;

    public ProcessInboundInvoiceUseCase(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public Invoice processInboundInvoice(InvoiceRequestDTO invoiceDto) {
        // Process and validate the inbound invoice
        return invoiceService.create(invoiceDto);
    }

    public Invoice processInboundInvoice(InvoiceRequestDTO invoiceDto, String source) {
        // Process with source tracking
        Invoice invoice = invoiceService.create(invoiceDto);
        // Note: If we need to track source, we would need to add that field to Invoice
        return invoice;
    }

    public Invoice processInboundInvoiceWithValidation(InvoiceRequestDTO invoiceDto) {
        // Additional validation logic can be added here
        validateInvoiceData(invoiceDto);
        return processInboundInvoice(invoiceDto);
    }

    private void validateInvoiceData(InvoiceRequestDTO invoiceDto) {
        if (invoiceDto.getDocumentNumber() == null || invoiceDto.getDocumentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Document number is required");
        }
        if (invoiceDto.getAmount() == null || invoiceDto.getAmount().trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (invoiceDto.getSenderTaxId() == null || invoiceDto.getSenderTaxId().trim().isEmpty()) {
            throw new IllegalArgumentException("Sender tax ID is required");
        }
    }
}
