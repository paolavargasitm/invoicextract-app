package co.edu.itm.invoiceextract.application.service;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceItemRepository invoiceItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    @Transactional
    public Invoice create(InvoiceRequestDTO request) {
        Invoice invoice = toEntity(request);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        if (request.getInvoiceItem() != null) {
            InvoiceItem item = toItemEntity(request.getInvoiceItem());
            item.setInvoice(savedInvoice);
            invoiceItemRepository.save(item);
        }
        return savedInvoice;
    }

    public Optional<Invoice> findByDocumentNumber(String documentNumber) {
        return invoiceRepository.findByDocumentNumber(documentNumber);
    }

    private Invoice toEntity(InvoiceRequestDTO dto) {
        Invoice e = new Invoice();
        e.setDocumentType(dto.getDocumentType());
        e.setDocumentNumber(dto.getDocumentNumber());
        e.setReceiverTaxId(dto.getReceiverTaxId());
        e.setReceiverTaxIdWithoutCheckDigit(dto.getReceiverTaxIdWithoutCheckDigit());
        e.setReceiverBusinessName(dto.getReceiverBusinessName());
        e.setSenderTaxId(dto.getSenderTaxId());
        e.setSenderTaxIdWithoutCheckDigit(dto.getSenderTaxIdWithoutCheckDigit());
        e.setSenderBusinessName(dto.getSenderBusinessName());
        e.setRelatedDocumentNumber(dto.getRelatedDocumentNumber());
        if (dto.getAmount() != null) {
            try { e.setAmount(new BigDecimal(dto.getAmount())); } catch (NumberFormatException ignored) {}
        }
        if (dto.getIssueDate() != null) {
            e.setIssueDate(dto.getIssueDate());
        }
        if (dto.getDueDate() != null) {
            e.setDueDate(dto.getDueDate());
        }
        return e;
    }

    private InvoiceItem toItemEntity(InvoiceItemDTO dto) {
        InvoiceItem e = new InvoiceItem();
        e.setItemCode(dto.getItemCode());
        e.setDescription(dto.getDescription());
        e.setQuantity(dto.getQuantity());
        e.setUnit(dto.getUnit());
        if (dto.getUnitPrice() != null) e.setUnitPrice(dto.getUnitPrice());
        if (dto.getSubtotal() != null) e.setSubtotal(dto.getSubtotal());
        if (dto.getTaxAmount() != null) e.setTaxAmount(dto.getTaxAmount());
        if (dto.getTotal() != null) e.setTotal(dto.getTotal());
        return e;
    }
}
