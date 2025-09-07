package co.edu.itm.invoiceextract.application.usecase.invoice;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.mapper.invoice.InvoiceMapper;
import co.edu.itm.invoiceextract.application.testutil.InvoiceTestData;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceItemRepository;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceItemRepository invoiceItemRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private ManageInvoiceUseCase useCase;

    private InvoiceRequestDTO request;

    @BeforeEach
    void setUp() {
        request = InvoiceTestData.sampleRequest();
    }

    @Test
    @DisplayName("should_create_invoice_with_item_when_request_valid")
    void should_create_invoice_with_item_when_request_valid() {
        // Given
        given(invoiceRepository.findByDocumentNumber("INV-001")).willReturn(Optional.empty());
        Invoice toSave = new Invoice();
        toSave.setDocumentNumber("INV-001");
        toSave.setAmount(new BigDecimal("1000.50"));
        toSave.setIssueDate(LocalDate.now().minusDays(1));
        toSave.setDueDate(LocalDate.now().plusDays(29));
        Invoice saved = InvoiceTestData.sampleEntity(1L);
        given(invoiceMapper.toEntity(any(InvoiceRequestDTO.class))).willReturn(toSave);
        given(invoiceRepository.save(toSave)).willReturn(saved);

        InvoiceItem mappedItem = new InvoiceItem();
        mappedItem.setDescription("Service");
        mappedItem.setQuantity(1);
        mappedItem.setUnitPrice(new BigDecimal("1000.50"));
        given(invoiceMapper.toItemEntity(any(InvoiceItemDTO.class))).willReturn(mappedItem);
        given(invoiceItemRepository.save(any(InvoiceItem.class))).willAnswer(inv -> inv.getArgument(0));

        // When
        Invoice result = useCase.createInvoice(request);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getItems()).hasSize(2);
        verify(invoiceRepository).save(toSave);
        verify(invoiceItemRepository).save(any(InvoiceItem.class));
    }

    @Test
    @DisplayName("should_throw_when_duplicate_document_number")
    void should_throw_when_duplicate_document_number() {
        given(invoiceRepository.findByDocumentNumber("INV-001")).willReturn(Optional.of(new Invoice()));
        assertThrows(IllegalArgumentException.class, () -> useCase.createInvoice(request));
    }

    @Test
    @DisplayName("should_update_invoice_fields_and_items")
    void should_update_invoice_fields_and_items() {
        // Given existing invoice
        Invoice existing = InvoiceTestData.sampleEntity(5L);
        given(invoiceRepository.findById(5L)).willReturn(Optional.of(existing));

        // Mapper returns updated fields from request
        Invoice mapped = new Invoice();
        mapped.setDocumentType("FACTURA");
        mapped.setDocumentNumber("INV-001-U");
        mapped.setReceiverTaxId("987654321");
        mapped.setReceiverTaxIdWithoutCheckDigit(null);
        mapped.setReceiverBusinessName("Customer");
        mapped.setSenderTaxId("123456789");
        mapped.setSenderTaxIdWithoutCheckDigit(null);
        mapped.setSenderBusinessName("Supplier");
        mapped.setRelatedDocumentNumber(null);
        mapped.setAmount(new BigDecimal("2000.00"));
        mapped.setIssueDate(LocalDate.now());
        mapped.setDueDate(LocalDate.now().plusDays(10));
        given(invoiceMapper.toEntity(any(InvoiceRequestDTO.class))).willReturn(mapped);

        // For items
        InvoiceItem newItem = new InvoiceItem();
        newItem.setDescription("Updated");
        newItem.setQuantity(2);
        newItem.setUnitPrice(new BigDecimal("2000.00"));
        given(invoiceMapper.toItemEntity(any(InvoiceItemDTO.class))).willReturn(newItem);

        given(invoiceRepository.save(existing)).willReturn(existing);

        // When
        Invoice updated = useCase.updateInvoice(5L, request);

        // Then
        assertEquals("INV-001-U", updated.getDocumentNumber());
        assertThat(updated.getAmount()).isEqualByComparingTo("2000.00");
        verify(invoiceItemRepository).deleteAll(anyList());
        verify(invoiceItemRepository).save(any(InvoiceItem.class));
    }

    @Test
    @DisplayName("should_delete_invoice_and_its_items")
    void should_delete_invoice_and_its_items() {
        Invoice existing = InvoiceTestData.sampleEntity(9L);
        given(invoiceRepository.findById(9L)).willReturn(Optional.of(existing));
        // When
        useCase.deleteInvoice(9L);
        // Then
        verify(invoiceItemRepository).deleteAll(existing.getItems());
        verify(invoiceRepository).delete(existing);
    }

    @Nested
    class StatusTransitions {
        @Test
        @DisplayName("should_approve_invoice")
        void should_approve_invoice() {
            Invoice existing = InvoiceTestData.sampleEntity(2L);
            existing.setStatus(InvoiceStatus.PENDING);
            given(invoiceRepository.findById(2L)).willReturn(Optional.of(existing));
            given(invoiceRepository.save(existing)).willReturn(existing);

            Invoice result = useCase.approveInvoice(2L);
            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.APPROVED);
        }

        @Test
        @DisplayName("should_reject_invoice")
        void should_reject_invoice() {
            Invoice existing = InvoiceTestData.sampleEntity(3L);
            existing.setStatus(InvoiceStatus.PENDING);
            given(invoiceRepository.findById(3L)).willReturn(Optional.of(existing));
            given(invoiceRepository.save(existing)).willReturn(existing);

            Invoice result = useCase.rejectInvoice(3L);
            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.REJECTED);
        }

        @Test
        @DisplayName("should_change_status_to_given_value")
        void should_change_status_to_given_value() {
            Invoice existing = InvoiceTestData.sampleEntity(4L);
            existing.setStatus(InvoiceStatus.PENDING);
            given(invoiceRepository.findById(4L)).willReturn(Optional.of(existing));
            given(invoiceRepository.save(existing)).willReturn(existing);

            Invoice result = useCase.changeInvoiceStatus(4L, InvoiceStatus.APPROVED);
            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.APPROVED);
        }

        @Test
        @DisplayName("should_throw_when_approving_already_approved")
        void should_throw_when_approving_already_approved() {
            Invoice existing = InvoiceTestData.sampleEntity(20L);
            existing.setStatus(InvoiceStatus.APPROVED);
            given(invoiceRepository.findById(20L)).willReturn(Optional.of(existing));
            assertThrows(IllegalStateException.class, () -> useCase.approveInvoice(20L));
        }

        @Test
        @DisplayName("should_throw_when_rejecting_already_rejected")
        void should_throw_when_rejecting_already_rejected() {
            Invoice existing = InvoiceTestData.sampleEntity(21L);
            existing.setStatus(InvoiceStatus.REJECTED);
            given(invoiceRepository.findById(21L)).willReturn(Optional.of(existing));
            assertThrows(IllegalStateException.class, () -> useCase.rejectInvoice(21L));
        }

        @Test
        @DisplayName("should_throw_when_change_status_not_found")
        void should_throw_when_change_status_not_found() {
            given(invoiceRepository.findById(404L)).willReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> useCase.changeInvoiceStatus(404L, InvoiceStatus.APPROVED));
        }
    }

    @Test
    @DisplayName("should_find_invoice_by_id_with_items")
    void should_find_invoice_by_id_with_items() {
        Invoice existing = InvoiceTestData.sampleEntity(6L);
        given(invoiceRepository.findByIdWithItems(6L)).willReturn(Optional.of(existing));
        Optional<Invoice> found = useCase.findInvoiceByIdWithItems(6L);
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).isNotEmpty();
    }
}
