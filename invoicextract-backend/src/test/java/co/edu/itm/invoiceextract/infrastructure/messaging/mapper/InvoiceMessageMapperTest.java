package co.edu.itm.invoiceextract.infrastructure.messaging.mapper;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceItemMessage;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceMessageMapperTest {

    private final InvoiceMessageMapper mapper = Mappers.getMapper(InvoiceMessageMapper.class);

    @Test
    void toMessage_should_map_fields_and_set_defaults() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setDocumentType("FACTURA");
        dto.setDocumentNumber("INV-200");
        dto.setReceiverTaxId("900111222");
        dto.setReceiverTaxIdWithoutCheckDigit("90011122");
        dto.setReceiverBusinessName("Customer");
        dto.setSenderTaxId("800333444");
        dto.setSenderTaxIdWithoutCheckDigit("80033344");
        dto.setSenderBusinessName("Supplier");
        dto.setRelatedDocumentNumber("NC-9");
        dto.setAmount("150.00");
        dto.setIssueDate(LocalDate.of(2024, 5, 1));
        dto.setDueDate(LocalDate.of(2024, 6, 1));
        InvoiceItemDTO item = new InvoiceItemDTO();
        item.setItemCode("X1");
        item.setDescription("Item");
        item.setQuantity(1);
        item.setUnit("EA");
        item.setUnitPrice(new BigDecimal("150.00"));
        item.setSubtotal(new BigDecimal("150.00"));
        item.setTaxAmount(BigDecimal.ZERO);
        item.setTotal(new BigDecimal("150.00"));
        dto.setInvoiceItem(item);

        InvoiceMessage msg = mapper.toMessage(dto);
        assertThat(msg.getDocumentNumber()).isEqualTo("INV-200");
        assertThat(msg.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        assertThat(msg.getDate()).isNotNull();
        assertThat(msg.getInvoicePathPDF()).isNull();
        assertThat(msg.getInvoicePathXML()).isNull();
        assertThat(msg.getEmail()).isNull();
        assertThat(msg.getInvoiceItem()).isNotNull();
        assertThat(msg.getInvoiceItem().getDescription()).isEqualTo("Item");
    }

    @Test
    void item_roundtrip_should_preserve_values() {
        InvoiceItemDTO itemDto = new InvoiceItemDTO();
        itemDto.setItemCode("A");
        itemDto.setDescription("Desc");
        itemDto.setQuantity(3);
        itemDto.setUnit("EA");
        itemDto.setUnitPrice(new BigDecimal("10.00"));
        itemDto.setSubtotal(new BigDecimal("30.00"));
        itemDto.setTaxAmount(new BigDecimal("5.70"));
        itemDto.setTotal(new BigDecimal("35.70"));

        InvoiceItemMessage msg = mapper.toInvoiceItemMessage(itemDto);
        InvoiceItemDTO back = mapper.toInvoiceItemDto(msg);

        assertThat(back.getItemCode()).isEqualTo("A");
        assertThat(back.getQuantity()).isEqualTo(3);
        assertThat(back.getTotal()).isEqualByComparingTo("35.70");
    }
}
