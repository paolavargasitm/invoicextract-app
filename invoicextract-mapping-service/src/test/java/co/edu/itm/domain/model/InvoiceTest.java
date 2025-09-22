package co.edu.itm.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceTest {

    @Test
    void builderDefaultItemsAndAccessors() {
        Invoice inv = Invoice.builder()
                .id(10L)
                .documentType("FV")
                .documentNumber("INV-10")
                .receiverTaxId("900111222-3")
                .receiverTaxIdWithoutCheckDigit("900111222")
                .receiverBusinessName("ACME")
                .senderTaxId("800222333-4")
                .senderTaxIdWithoutCheckDigit("800222333")
                .senderBusinessName("SUPPLIER")
                .relatedDocumentNumber("PO-10")
                .amount(new BigDecimal("100.00"))
                .issueDate(LocalDate.of(2024, 3, 5))
                .dueDate(LocalDate.of(2024, 3, 20))
                .status("APPROVED")
                .build();

        assertNotNull(inv.getItems());
        assertTrue(inv.getItems().isEmpty());

        InvoiceItem it = InvoiceItem.builder().itemCode("X").quantity(1).build();
        inv.setItems(List.of(it));
        assertEquals(1, inv.getItems().size());
        assertEquals("X", inv.getItems().get(0).getItemCode());
    }
}
