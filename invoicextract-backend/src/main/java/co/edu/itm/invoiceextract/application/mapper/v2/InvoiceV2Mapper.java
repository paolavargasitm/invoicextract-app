package co.edu.itm.invoiceextract.application.mapper.v2;

import co.edu.itm.invoiceextract.application.dto.v2.InvoiceItemV2DTO;
import co.edu.itm.invoiceextract.application.dto.v2.InvoiceV2RequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceMetadata;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InvoiceV2Mapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Invoice toEntity(InvoiceV2RequestDTO dto) {
        if (dto == null) return null;

        Invoice invoice = new Invoice();
        invoice.setType(mapDocumentType(dto.getDocumentType()));

        InvoiceMetadata md = new InvoiceMetadata();
        md.setInvoiceNumber(dto.getDocumentNumber());
        // Receiver -> Customer
        md.setCustomerName(dto.getReceiverBusinessName());
        // Sender -> Supplier
        md.setSupplierName(dto.getSenderBusinessName());

        if (dto.getIssueDate() != null) md.setIssueDate(dto.getIssueDate());
        if (dto.getDueDate() != null) md.setDueDate(dto.getDueDate());

        // Amounts
        BigDecimal parsedAmount = parseAmount(dto.getAmount());
        if (parsedAmount != null) {
            md.setTotalAmount(parsedAmount);
            md.setAmount(parsedAmount);
        }

        InvoiceItemV2DTO item = dto.getInvoiceItem();
        if (item != null) {
            if (item.getSubtotal() != null) md.setSubtotal(item.getSubtotal());
            if (item.getTaxAmount() != null) md.setTaxAmount(item.getTaxAmount());
            if (item.getTotal() != null) md.setTotalAmount(item.getTotal());
            if (md.getAmount() == null && item.getTotal() != null) md.setAmount(item.getTotal());
            if (item.getDescription() != null) md.setDescription(item.getDescription());
        }

        // Persist full payload as JSON for traceability
        try {
            md.setExtractedData(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException ignored) { }

        invoice.setMetadata(md);
        return invoice;
    }

    private InvoiceType mapDocumentType(String documentType) {
        if (documentType == null) return InvoiceType.INVOICE;
        String dt = documentType.trim().toLowerCase();
        // Spanish mappings; extend as needed
        if (dt.contains("credito") || dt.contains("crédito")) return InvoiceType.CREDIT_NOTE;
        if (dt.contains("debito") || dt.contains("débito")) return InvoiceType.DEBIT_NOTE;
        return InvoiceType.INVOICE; // Factura or default
    }

    private BigDecimal parseAmount(String amount) {
        if (amount == null || amount.isBlank()) return null;
        try {
            return new BigDecimal(amount.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
