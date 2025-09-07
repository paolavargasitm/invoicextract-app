package co.edu.itm.invoiceextract.application.service;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.mapper.invoice.InvoiceMapper;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceItemRepository;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceItemRepository invoiceItemRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceService service;

    private InvoiceRequestDTO requestWithItem() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setDocumentNumber("INV-001");
        dto.setSenderTaxId("123");
        dto.setReceiverTaxId("456");
        dto.setDocumentType("FACTURA");
        dto.setAmount("100.00");
        dto.setIssueDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(10));
        InvoiceItemDTO item = new InvoiceItemDTO();
        item.setDescription("Service");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("100.00"));
        dto.setInvoiceItem(item);
        return dto;
    }

    private InvoiceRequestDTO requestWithoutItem() {
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setDocumentNumber("INV-002");
        dto.setAmount("50.00");
        dto.setIssueDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(5));
        return dto;
    }

    @Test
    @DisplayName("should_create_invoice_and_item_when_item_present")
    void should_create_invoice_and_item_when_item_present() {
        // Given mapper to entity
        Invoice mapped = new Invoice();
        mapped.setDocumentNumber("INV-001");
        mapped.setAmount(new BigDecimal("100.00"));
        mapped.setIssueDate(LocalDate.now());
        mapped.setDueDate(LocalDate.now().plusDays(10));
        given(invoiceMapper.toEntity(any(InvoiceRequestDTO.class))).willReturn(mapped);

        // Repository saves and returns saved with id
        Invoice saved = new Invoice();
        saved.setId(1L);
        saved.setDocumentNumber("INV-001");
        saved.setAmount(new BigDecimal("100.00"));
        given(invoiceRepository.save(mapped)).willReturn(saved);

        // Map item
        InvoiceItem mappedItem = new InvoiceItem();
        mappedItem.setDescription("Service");
        mappedItem.setQuantity(1);
        mappedItem.setUnitPrice(new BigDecimal("100.00"));
        given(invoiceMapper.toItemEntity(any(InvoiceItemDTO.class))).willReturn(mappedItem);
        given(invoiceItemRepository.save(any(InvoiceItem.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        InvoiceRequestDTO request = requestWithItem();
        Invoice result = service.create(request);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        ArgumentCaptor<InvoiceItem> itemCaptor = ArgumentCaptor.forClass(InvoiceItem.class);
        verify(invoiceItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getInvoice()).isSameAs(saved);
    }

    @Test
    @DisplayName("should_create_invoice_without_item_when_item_absent")
    void should_create_invoice_without_item_when_item_absent() {
        // Given
        Invoice mapped = new Invoice();
        mapped.setDocumentNumber("INV-002");
        mapped.setAmount(new BigDecimal("50.00"));
        given(invoiceMapper.toEntity(any(InvoiceRequestDTO.class))).willReturn(mapped);

        Invoice saved = new Invoice();
        saved.setId(2L);
        saved.setDocumentNumber("INV-002");
        saved.setAmount(new BigDecimal("50.00"));
        given(invoiceRepository.save(mapped)).willReturn(saved);

        // When
        Invoice result = service.create(requestWithoutItem());

        // Then
        assertThat(result.getId()).isEqualTo(2L);
        verify(invoiceItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_delegate_find_by_document_number")
    void should_delegate_find_by_document_number() {
        given(invoiceRepository.findByDocumentNumber("INV-XYZ")).willReturn(Optional.of(new Invoice()));
        Optional<Invoice> found = service.findByDocumentNumber("INV-XYZ");
        assertThat(found).isPresent();
        verify(invoiceRepository).findByDocumentNumber("INV-XYZ");
    }
}
