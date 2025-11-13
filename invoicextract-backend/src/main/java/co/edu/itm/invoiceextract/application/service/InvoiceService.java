package co.edu.itm.invoiceextract.application.service;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.mapper.invoice.InvoiceMapper;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceItemRepository invoiceItemRepository,
                          InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Transactional
    public Invoice create(InvoiceRequestDTO request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        if (request.getInvoiceItems() != null && !request.getInvoiceItems().isEmpty()) {
            for (var itemDto : request.getInvoiceItems()) {
                if (itemDto == null) continue;
                InvoiceItem item = invoiceMapper.toItemEntity(itemDto);
                item.setInvoice(savedInvoice);
                invoiceItemRepository.save(item);
            }
        }
        if (request.getInvoiceItem() != null) {
            InvoiceItem item = invoiceMapper.toItemEntity(request.getInvoiceItem());
            item.setInvoice(savedInvoice);
            invoiceItemRepository.save(item);
        }
        return savedInvoice;
    }

    public Optional<Invoice> findByDocumentNumber(String documentNumber) {
        return invoiceRepository.findByDocumentNumber(documentNumber);
    }
}
