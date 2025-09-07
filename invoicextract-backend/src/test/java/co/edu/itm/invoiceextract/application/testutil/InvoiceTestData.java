package co.edu.itm.invoiceextract.application.testutil;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class InvoiceTestData {

    private InvoiceTestData() {}

    public static InvoiceRequestDTO sampleRequest() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setDocumentNumber("INV-001");
        dto.setSenderTaxId("123456789");
        dto.setReceiverTaxId("987654321");
        dto.setDocumentType("FACTURA");
        dto.setAmount("1000.50");
        dto.setIssueDate(LocalDate.now().minusDays(1));
        dto.setDueDate(LocalDate.now().plusDays(29));
        InvoiceItemDTO item = new InvoiceItemDTO();
        item.setDescription("Service");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("1000.50"));
        dto.setInvoiceItem(item);
        return dto;
    }

    public static Invoice sampleEntity(Long id) {
        Invoice inv = new Invoice();
        inv.setId(id);
        inv.setDocumentNumber("INV-001");
        inv.setSenderTaxId("123456789");
        inv.setReceiverTaxId("987654321");
        inv.setDocumentType("FACTURA");
        inv.setAmount(new BigDecimal("1000.50"));
        inv.setIssueDate(LocalDate.now().minusDays(1));
        inv.setDueDate(LocalDate.now().plusDays(29));
        inv.setStatus(InvoiceStatus.PENDING);
        InvoiceItem item = new InvoiceItem();
        item.setDescription("Service");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("1000.50"));
        item.setInvoice(inv);
        inv.addItem(item);
        return inv;
    }

    public static InvoiceDetailDTO sampleDetailDTO(Long id) {
        InvoiceDetailDTO dto = new InvoiceDetailDTO();
        dto.setId(id);
        dto.setDocumentNumber("INV-001");
        dto.setSenderTaxId("123456789");
        dto.setReceiverTaxId("987654321");
        dto.setDocumentType("FACTURA");
        dto.setAmount(new BigDecimal("1000.50"));
        dto.setStatus(InvoiceStatus.PENDING);
        return dto;
    }
}
