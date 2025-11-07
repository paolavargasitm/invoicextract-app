package co.edu.itm.invoiceextract.application.mapper.invoice;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceMapperTest {

    private final InvoiceMapper mapper = Mappers.getMapper(InvoiceMapper.class);

    @Test
    void toEntity_should_map_basic_fields_and_parse_amount() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setDocumentType("FACTURA");
        dto.setDocumentNumber("INV-100");
        dto.setReceiverTaxId("900999999");
        dto.setReceiverTaxIdWithoutCheckDigit("90099999");
        dto.setReceiverBusinessName("Customer");
        dto.setSenderTaxId("800888888");
        dto.setSenderTaxIdWithoutCheckDigit("80088888");
        dto.setSenderBusinessName("Supplier");
        dto.setRelatedDocumentNumber("NC-1");
        dto.setAmount("$ 1,234.56");
        dto.setIssueDate(LocalDate.of(2024,1,10));
        dto.setDueDate(LocalDate.of(2024,2,10));

        Invoice entity = mapper.toEntity(dto);

        assertThat(entity.getDocumentNumber()).isEqualTo("INV-100");
        assertThat(entity.getIssueDate()).isEqualTo(LocalDate.of(2024,1,10));
        assertThat(entity.getDueDate()).isEqualTo(LocalDate.of(2024,2,10));
    }

    @Test
    void toItemEntity_should_map_all_item_fields() {
        InvoiceItemDTO item = new InvoiceItemDTO();
        item.setItemCode("A1");
        item.setDescription("Service");
        item.setQuantity(2);
        item.setUnit("EA");
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setSubtotal(new BigDecimal("20.00"));
        item.setTaxAmount(new BigDecimal("3.80"));
        item.setTotal(new BigDecimal("23.80"));

        InvoiceItem mapped = mapper.toItemEntity(item);
        assertThat(mapped.getItemCode()).isEqualTo("A1");
        assertThat(mapped.getQuantity()).isEqualTo(2);
        assertThat(mapped.getTotal()).isEqualByComparingTo("23.80");
    }

    @Test
    void toDetailDTO_should_copy_back_values() {
        Invoice entity = new Invoice();
        entity.setId(7L);
        entity.setDocumentType("FACTURA");
        entity.setDocumentNumber("INV-7");
        entity.setReceiverTaxId("123");
        entity.setReceiverBusinessName("ACME");
        entity.setSenderTaxId("321");
        entity.setSenderBusinessName("SUP");
        entity.setAmount(new BigDecimal("99.99"));
        entity.setIssueDate(LocalDate.of(2024,3,1));
        entity.setDueDate(LocalDate.of(2024,3,30));

        InvoiceDetailDTO dto = mapper.toDetailDTO(entity);
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getAmount()).isEqualByComparingTo("99.99");
        assertThat(dto.getDueDate()).isEqualTo(LocalDate.of(2024,3,30));
    }

    @Test
    void parseAmount_should_handle_null_and_bad_values() {
        // null
        assertThat(mapper.parseAmount(null)).isNull();
        // empty
        assertThat(mapper.parseAmount("   ")).isNull();
        // bad format
        assertThat(mapper.parseAmount("abc")).isNull();
    }
}
