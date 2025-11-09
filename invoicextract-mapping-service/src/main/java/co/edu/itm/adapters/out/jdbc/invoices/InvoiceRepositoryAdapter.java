package co.edu.itm.adapters.out.jdbc.invoices;

import co.edu.itm.domain.model.Invoice;
import co.edu.itm.domain.model.InvoiceItem;
import co.edu.itm.domain.ports.InvoiceRepositoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {
    private final JdbcTemplate jdbc;

    public InvoiceRepositoryAdapter(@Qualifier("invoicesJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Invoice> INVOICE_MAPPER = new RowMapper<>() {
        @Override
        public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Invoice.builder()
                    .id(rs.getLong("id"))
                    .documentType(rs.getString("document_type"))
                    .documentNumber(rs.getString("document_number"))
                    .receiverTaxId(rs.getString("receiver_tax_id"))
                    .receiverTaxIdWithoutCheckDigit(rs.getString("receiver_tax_id_without_check_digit"))
                    .receiverBusinessName(rs.getString("receiver_business_name"))
                    .senderTaxId(rs.getString("sender_tax_id"))
                    .senderTaxIdWithoutCheckDigit(rs.getString("sender_tax_id_without_check_digit"))
                    .senderBusinessName(rs.getString("sender_business_name"))
                    .invoicePathPDF(rs.getString("invoice_path_pdf"))
                    .invoicePathXML(rs.getString("invoice_path_xml"))
                    .relatedDocumentNumber(rs.getString("related_document_number"))
                    .amount(rs.getBigDecimal("amount"))
                    .issueDate(rs.getObject("issue_date", LocalDate.class))
                    .dueDate(rs.getObject("due_date", LocalDate.class))
                    .status(rs.getString("status"))
                    .createdDate(rs.getObject("created_date", LocalDateTime.class))
                    .modifiedDate(rs.getObject("modified_date", LocalDateTime.class))
                    .createdBy(rs.getString("created_by"))
                    .modifiedBy(rs.getString("modified_by"))
                    .build();
        }
    };

    private static final RowMapper<InvoiceItem> ITEM_MAPPER = new RowMapper<>() {
        @Override
        public InvoiceItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return InvoiceItem.builder()
                    .id(rs.getLong("id"))
                    .itemCode(rs.getString("item_code"))
                    .description(rs.getString("description"))
                    .quantity((Integer) rs.getObject("quantity"))
                    .unit(rs.getString("unit"))
                    .unitPrice(rs.getBigDecimal("unit_price"))
                    .subtotal(rs.getBigDecimal("subtotal"))
                    .taxAmount(rs.getBigDecimal("tax_amount"))
                    .total(rs.getBigDecimal("total"))
                    .build();
        }
    };

    @Override
    public List<Invoice> findApproved() {
        String sql = "SELECT id, document_type, document_number, receiver_tax_id, receiver_tax_id_without_check_digit, " +
                "receiver_business_name, sender_tax_id, sender_tax_id_without_check_digit, sender_business_name, invoice_path_pdf, invoice_path_xml, " +
                "related_document_number, amount, issue_date, due_date, status, created_date, modified_date, created_by, modified_by FROM invoices WHERE status = 'APPROVED'";
        List<Invoice> invoices = jdbc.query(sql, INVOICE_MAPPER);

        if (invoices.isEmpty()) return invoices;

        // Fetch items per invoice
        String itemsSql = "SELECT id, invoice_id, item_code, description, quantity, unit, unit_price, subtotal, tax_amount, total " +
                "FROM invoice_items WHERE invoice_id = ?";
        for (Invoice inv : invoices) {
            List<InvoiceItem> items = jdbc.query(itemsSql, ITEM_MAPPER, inv.getId());
            inv.setItems(items);
        }
        return invoices;
    }
}

