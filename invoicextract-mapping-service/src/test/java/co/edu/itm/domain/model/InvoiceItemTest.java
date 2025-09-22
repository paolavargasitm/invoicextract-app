package co.edu.itm.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceItemTest {

    @Test
    void builderAndAccessors() {
        InvoiceItem it = InvoiceItem.builder()
                .id(5L)
                .itemCode("SKU-1")
                .description("Desc")
                .quantity(3)
                .unit("EA")
                .unitPrice(new BigDecimal("2.50"))
                .subtotal(new BigDecimal("7.50"))
                .taxAmount(new BigDecimal("1.43"))
                .total(new BigDecimal("8.93"))
                .build();

        assertEquals(5L, it.getId());
        assertEquals("SKU-1", it.getItemCode());
        assertEquals(3, it.getQuantity());
        assertEquals(new BigDecimal("8.93"), it.getTotal());

        it.setQuantity(4);
        assertEquals(4, it.getQuantity());
    }
}
