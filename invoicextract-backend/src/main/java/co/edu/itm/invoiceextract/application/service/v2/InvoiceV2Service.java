package co.edu.itm.invoiceextract.application.service.v2;

import co.edu.itm.invoiceextract.application.dto.v2.InvoiceItemV2DTO;
import co.edu.itm.invoiceextract.application.dto.v2.InvoiceV2RequestDTO;
import co.edu.itm.invoiceextract.application.mapper.v2.InvoiceV2Mapper;
import co.edu.itm.invoiceextract.application.service.InvoiceService;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.v2.InvoiceItemV2;
import co.edu.itm.invoiceextract.domain.entity.invoice.v2.InvoiceV2;
import co.edu.itm.invoiceextract.domain.repository.v2.InvoiceItemV2Repository;
import co.edu.itm.invoiceextract.domain.repository.v2.InvoiceV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class InvoiceV2Service {

    private final InvoiceService legacyInvoiceService;
    private final InvoiceV2Repository invoiceV2Repository;
    private final InvoiceItemV2Repository invoiceItemV2Repository;
    private final InvoiceV2Mapper legacyMapper;

    public InvoiceV2Service(InvoiceService legacyInvoiceService,
                            InvoiceV2Repository invoiceV2Repository,
                            InvoiceItemV2Repository invoiceItemV2Repository,
                            InvoiceV2Mapper legacyMapper) {
        this.legacyInvoiceService = legacyInvoiceService;
        this.invoiceV2Repository = invoiceV2Repository;
        this.invoiceItemV2Repository = invoiceItemV2Repository;
        this.legacyMapper = legacyMapper;
    }

    @Transactional
    public Invoice createDualWrite(InvoiceV2RequestDTO request) {
        // 1) Persist v2 tables
        InvoiceV2 v2 = toV2Entity(request);
        InvoiceV2 savedV2 = invoiceV2Repository.save(v2);
        if (request.getInvoiceItem() != null) {
            InvoiceItemV2 item = toItemEntity(request.getInvoiceItem());
            item.setInvoice(savedV2);
            invoiceItemV2Repository.save(item);
        }
        // 2) Persist legacy tables using existing flow
        Invoice legacy = legacyMapper.toEntity(request);
        return legacyInvoiceService.save(legacy);
    }

    public Optional<InvoiceV2> findV2ByDocumentNumber(String documentNumber) {
        return invoiceV2Repository.findByDocumentNumber(documentNumber);
    }

    private InvoiceV2 toV2Entity(InvoiceV2RequestDTO dto) {
        InvoiceV2 e = new InvoiceV2();
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

    private InvoiceItemV2 toItemEntity(InvoiceItemV2DTO dto) {
        InvoiceItemV2 e = new InvoiceItemV2();
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
