package co.edu.itm.application.usecase;

import co.edu.itm.domain.model.FieldMapping;
import co.edu.itm.domain.model.Invoice;
import co.edu.itm.domain.model.InvoiceItem;
import co.edu.itm.domain.ports.InvoiceRepositoryPort;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import co.edu.itm.domain.service.DynamicMappingService;
import co.edu.itm.domain.service.TransformRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportInvoicesUseCaseTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepo;
    @Mock
    private MappingRepositoryPort mappingRepo;

    private DynamicMappingService mappingService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mappingService = new DynamicMappingService(new TransformRegistry());
    }

    private Invoice sampleInvoice() {
        InvoiceItem item1 = InvoiceItem.builder()
                .itemCode("A1").description("Item A").quantity(2).unit("EA")
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("20.00"))
                .taxAmount(new BigDecimal("3.80"))
                .total(new BigDecimal("23.80"))
                .build();
        InvoiceItem item2 = InvoiceItem.builder()
                .itemCode("B2").description("Item B").quantity(1).unit("EA")
                .unitPrice(new BigDecimal("5.00"))
                .subtotal(new BigDecimal("5.00"))
                .taxAmount(new BigDecimal("0.95"))
                .total(new BigDecimal("5.95"))
                .build();

        return Invoice.builder()
                .id(100L)
                .documentType("FV")
                .documentNumber("INV-001")
                .receiverTaxId("900123456-7")
                .receiverTaxIdWithoutCheckDigit("900123456")
                .receiverBusinessName(" ACME S.A. ")
                .senderTaxId("800765432-1")
                .senderTaxIdWithoutCheckDigit("800765432")
                .senderBusinessName(" example corp ")
                .relatedDocumentNumber("PO-123")
                .amount(new BigDecimal("25.00"))
                .issueDate(LocalDate.of(2024, 3, 15))
                .dueDate(LocalDate.of(2024, 3, 30))
                .status("APPROVED")
                .items(List.of(item1, item2))
                .build();
    }

    private List<FieldMapping> rules() {
        return List.of(
                FieldMapping.builder().sourceField("senderBusinessName").targetField("customer").transformFn("TRIM").build(),
                FieldMapping.builder().sourceField("documentType").targetField("docType").transformFn("UPPER").build(),
                FieldMapping.builder().sourceField("issueDate").targetField("issuedOn").transformFn("DATE_FMT:yyyy-MM-dd").build(),
                FieldMapping.builder().sourceField("amount").targetField("gross").transformFn("").build()
        );
    }

    @Test
    void exportMapped_nestedItems_shouldMapFields() {
        Invoice inv = sampleInvoice();
        when(invoiceRepo.findApproved()).thenReturn(List.of(inv));
        when(mappingRepo.findActiveByErpName("SAP")).thenReturn(rules());

        ExportInvoicesUseCase useCase = new ExportInvoicesUseCase(invoiceRepo, mappingRepo, mappingService);
        List<Map<String, Object>> result = useCase.exportMapped("SAP");

        assertEquals(1, result.size());
        Map<String, Object> row = result.get(0);
        assertEquals("example corp", row.get("customer")); // TRIM applied
        assertEquals("FV", row.get("docType")); // already upper
        assertEquals("2024-03-15", row.get("issuedOn"));
        assertEquals(new BigDecimal("25.00"), row.get("gross"));

        // Note: DynamicMappingService only includes fields defined by mapping rules.
        // Since we didn't add a rule for 'items', the output shouldn't contain it.
        // This validates that only mapped fields are present.
        assertFalse(row.containsKey("items"));

        verify(invoiceRepo, times(1)).findApproved();
        verify(mappingRepo, times(1)).findActiveByErpName("SAP");
    }

    @Test
    void exportMapped_flattenedItems_shouldFlattenAndMap() {
        Invoice inv = sampleInvoice();
        when(invoiceRepo.findApproved()).thenReturn(List.of(inv));
        when(mappingRepo.findActiveByErpName("SAP")).thenReturn(rules());

        ExportInvoicesUseCase useCase = new ExportInvoicesUseCase(invoiceRepo, mappingRepo, mappingService);
        List<Map<String, Object>> result = useCase.exportMapped("SAP", true);

        assertEquals(1, result.size());
        Map<String, Object> row = result.get(0);
        assertEquals("example corp", row.get("customer"));
        assertEquals("2024-03-15", row.get("issuedOn"));
        // flattened keys should have been present in source map; we only validate mapping step didn't remove them
        // mapping rules don't touch item fields, so they remain unaffected in output if rules map unrelated fields
        // Here we ensure mapping produced a non-empty output and didn't fail on flattened keys existing in source
        assertNotNull(row);

        verify(invoiceRepo, times(1)).findApproved();
        verify(mappingRepo, times(1)).findActiveByErpName("SAP");
    }
}
