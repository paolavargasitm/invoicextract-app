package co.edu.itm.adapters.out.jdbc.invoices;

import co.edu.itm.domain.model.Invoice;
import co.edu.itm.domain.model.InvoiceItem;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InvoiceRepositoryAdapterTest {

    @Test
    void findApproved_queriesInvoicesAndItems() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        InvoiceRepositoryAdapter adapter = new InvoiceRepositoryAdapter(jdbc);

        Invoice inv = Invoice.builder()
                .id(1L)
                .documentType("FV")
                .documentNumber("INV-1")
                .status("APPROVED")
                .build();
        when(jdbc.query(contains("FROM invoices"), any(RowMapper.class))).thenReturn(List.of(inv));

        InvoiceItem item = InvoiceItem.builder()
                .id(100L)
                .itemCode("A1")
                .quantity(2)
                .unitPrice(new BigDecimal("10.00"))
                .total(new BigDecimal("20.00"))
                .build();
        when(jdbc.query(contains("FROM invoice_items"), any(RowMapper.class), eq(1L))).thenReturn(List.of(item));

        List<Invoice> out = adapter.findApproved();
        assertEquals(1, out.size());
        assertEquals("INV-1", out.get(0).getDocumentNumber());
        assertEquals(1, out.get(0).getItems().size());
        assertEquals("A1", out.get(0).getItems().get(0).getItemCode());

        verify(jdbc).query(contains("FROM invoices"), any(RowMapper.class));
        verify(jdbc).query(contains("FROM invoice_items"), any(RowMapper.class), eq(1L));
    }

    @Test
    void findApproved_whenNoInvoices_returnsEmptyList() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        InvoiceRepositoryAdapter adapter = new InvoiceRepositoryAdapter(jdbc);
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());
        List<Invoice> out = adapter.findApproved();
        assertTrue(out.isEmpty());
        // items query should not be invoked when no invoices present
        verify(jdbc, times(1)).query(anyString(), any(RowMapper.class));
    }
}
