package co.edu.itm.application.usecase;

import co.edu.itm.domain.model.FieldMapping;
import co.edu.itm.domain.model.Invoice;
import co.edu.itm.domain.model.InvoiceItem;
import co.edu.itm.domain.ports.InvoiceRepositoryPort;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import co.edu.itm.domain.service.DynamicMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExportInvoicesUseCase {
    private static final Logger log = LoggerFactory.getLogger(ExportInvoicesUseCase.class);
    private final InvoiceRepositoryPort invoiceRepo;
    private final MappingRepositoryPort mappingRepo;
    private final DynamicMappingService mapper;

    public ExportInvoicesUseCase(InvoiceRepositoryPort i, MappingRepositoryPort m, DynamicMappingService d) {
        this.invoiceRepo = i;
        this.mappingRepo = m;
        this.mapper = d;
    }

    public List<Map<String, Object>> exportMapped(String erpName) {
        return exportMapped(erpName, false);
    }

    public List<Map<String, Object>> exportMapped(String erpName, boolean flatten) {
        long t0 = System.currentTimeMillis();
        List<Invoice> invoices = invoiceRepo.findApproved();
        List<FieldMapping> rules = mappingRepo.findActiveByErpName(erpName);
        log.info("[usecase] exportMapped erp={} invoices={} rules={} flatten={}", erpName, invoices.size(), rules.size(), flatten);
        if (!rules.isEmpty()) {
            FieldMapping r0 = rules.get(0);
            log.debug("[usecase] first rule: sourceField={} targetField={} status={}", r0.getSourceField(), r0.getTargetField(), r0.getStatus());
        }
        return invoices.stream().map(inv -> {
            Map<String, Object> src = new HashMap<>();
            // Canonical fields aligned with backend Invoice entity (top-level)
            src.put("id", inv.getId());
            src.put("documentType", inv.getDocumentType());
            src.put("documentNumber", inv.getDocumentNumber());
            src.put("receiverTaxId", inv.getReceiverTaxId());
            src.put("receiverTaxIdWithoutCheckDigit", inv.getReceiverTaxIdWithoutCheckDigit());
            src.put("receiverBusinessName", inv.getReceiverBusinessName());
            src.put("senderTaxId", inv.getSenderTaxId());
            src.put("senderTaxIdWithoutCheckDigit", inv.getSenderTaxIdWithoutCheckDigit());
            src.put("senderBusinessName", inv.getSenderBusinessName());
            src.put("relatedDocumentNumber", inv.getRelatedDocumentNumber());
            src.put("amount", inv.getAmount());
            src.put("issueDate", inv.getIssueDate());
            src.put("dueDate", inv.getDueDate());
            src.put("status", inv.getStatus());
            // Audit fields
            src.put("createdDate", inv.getCreatedDate());
            src.put("modifiedDate", inv.getModifiedDate());
            src.put("createdBy", inv.getCreatedBy());
            src.put("modifiedBy", inv.getModifiedBy());

            // Items: nested JSON by default; flattened if requested
            if (flatten) {
                List<InvoiceItem> items = inv.getItems();
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        InvoiceItem it = items.get(i);
                        String p = "items[" + i + "].";
                        src.put(p + "itemCode", it.getItemCode());
                        src.put(p + "description", it.getDescription());
                        src.put(p + "quantity", it.getQuantity());
                        src.put(p + "unit", it.getUnit());
                        src.put(p + "unitPrice", it.getUnitPrice());
                        src.put(p + "subtotal", it.getSubtotal());
                        src.put(p + "taxAmount", it.getTaxAmount());
                        src.put(p + "total", it.getTotal());
                    }
                }
            } else {
                // Nested: as a list of maps for better JSON output
                List<Map<String, Object>> itemsOut = inv.getItems() == null ? List.of() : inv.getItems().stream().map(it -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("itemCode", it.getItemCode());
                    m.put("description", it.getDescription());
                    m.put("quantity", it.getQuantity());
                    m.put("unit", it.getUnit());
                    m.put("unitPrice", it.getUnitPrice());
                    m.put("subtotal", it.getSubtotal());
                    m.put("taxAmount", it.getTaxAmount());
                    m.put("total", it.getTotal());
                    return m;
                }).collect(Collectors.toList());
                src.put("items", itemsOut);
            }

            Map<String, Object> out = mapper.apply(rules, src);
            return out;
        }).collect(Collectors.toList());
    }
}

