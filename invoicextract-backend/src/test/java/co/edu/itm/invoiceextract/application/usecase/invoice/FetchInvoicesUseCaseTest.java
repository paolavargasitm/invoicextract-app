package co.edu.itm.invoiceextract.application.usecase.invoice;

import co.edu.itm.invoiceextract.application.dto.invoice.DashboardStatsDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.RecentInvoiceDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FetchInvoicesUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private FetchInvoicesUseCase useCase;

    private Invoice invoice(Long id) {
        Invoice inv = new Invoice();
        inv.setId(id);
        inv.setDocumentNumber("INV-" + id);
        inv.setAmount(new BigDecimal("10.00"));
        inv.setIssueDate(LocalDate.now());
        inv.setDueDate(LocalDate.now().plusDays(10));
        inv.setStatus(InvoiceStatus.PENDING);
        inv.setCreatedDate(LocalDateTime.now());
        return inv;
    }

    @Test
    @DisplayName("should_find_all_and_paginated")
    void should_find_all_and_paginated() {
        given(invoiceRepository.findAll()).willReturn(List.of(invoice(1L), invoice(2L)));
        List<Invoice> all = useCase.findAll();
        assertThat(all).hasSize(2);

        PageRequest page = PageRequest.of(0, 10);
        given(invoiceRepository.findAll(page)).willReturn(new PageImpl<>(List.of(invoice(3L))));
        Page<Invoice> paged = useCase.findAll(page);
        assertThat(paged.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("should_find_by_id_and_document_number")
    void should_find_by_id_and_document_number() {
        given(invoiceRepository.findById(5L)).willReturn(Optional.of(invoice(5L)));
        given(invoiceRepository.findByDocumentNumber("INV-5")).willReturn(Optional.of(invoice(5L)));

        assertThat(useCase.findById(5L)).isPresent();
        assertThat(useCase.findByDocumentNumber("INV-5")).isPresent();
    }

    @Test
    @DisplayName("should_find_by_status_and_dates")
    void should_find_by_status_and_dates() {
        given(invoiceRepository.findByStatus(InvoiceStatus.PENDING)).willReturn(List.of(invoice(7L)));
        assertThat(useCase.findByStatus(InvoiceStatus.PENDING)).hasSize(1);

        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now();
        given(invoiceRepository.findByIssueDateBetween(start, end)).willReturn(List.of(invoice(8L)));
        assertThat(useCase.findByIssueDateBetween(start, end)).hasSize(1);

        LocalDateTime s2 = LocalDateTime.now().minusDays(2);
        LocalDateTime e2 = LocalDateTime.now();
        given(invoiceRepository.findByCreatedDateBetween(s2, e2)).willReturn(List.of(invoice(9L)));
        assertThat(useCase.findByCreatedDateBetween(s2, e2)).hasSize(1);

        // controller uses this adapter name
        given(invoiceRepository.findByCreatedDateBetween(s2, e2)).willReturn(List.of(invoice(10L)));
        assertThat(useCase.findByDateBetween(s2, e2)).hasSize(1);
    }

    @Test
    @DisplayName("should_find_by_receiver_and_amount_between")
    void should_find_by_receiver_and_amount_between() {
        given(invoiceRepository.findByReceiverTaxId("456")).willReturn(List.of(invoice(11L)));
        assertThat(useCase.findByReceiverTaxId("456")).hasSize(1);

        var min = new BigDecimal("5.00");
        var max = new BigDecimal("15.00");
        given(invoiceRepository.findByAmountBetween(min, max)).willReturn(List.of(invoice(12L)));
        assertThat(useCase.findByAmountBetween(min, max)).hasSize(1);
    }

    @Test
    @DisplayName("should_build_dashboard_stats")
    void should_build_dashboard_stats() {
        given(invoiceRepository.count()).willReturn(10L);
        given(invoiceRepository.countByDocumentType("FACTURA")).willReturn(6L);
        given(invoiceRepository.countByDocumentType("NOTA_CREDITO")).willReturn(3L);
        given(invoiceRepository.countByDocumentType("NOTA_DEBITO")).willReturn(1L);
        given(invoiceRepository.sumAllAmounts()).willReturn(new BigDecimal("123.45"));

        DashboardStatsDTO stats = useCase.getDashboardStats();
        assertThat(stats.getTotalCount()).isEqualTo(10);
        assertThat(stats.getTotalAmount()).isEqualByComparingTo("123.45");
        assertThat(stats.getFacturaCount()).isEqualTo(6);
        assertThat(stats.getNotaCreditoCount()).isEqualTo(3);
        assertThat(stats.getNotaDebitoCount()).isEqualTo(1);
        assertThat(stats.getAverageAmount()).isEqualByComparingTo(new BigDecimal("12.35"));
    }

    @Test
    @DisplayName("should_find_recent_invoices")
    void should_find_recent_invoices() {
        given(invoiceRepository.findTop10ByOrderByCreatedDateDesc()).willReturn(List.of(invoice(1L), invoice(2L), invoice(3L)));
        List<RecentInvoiceDTO> recent = useCase.getRecentInvoices(2);
        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should_convert_to_detail_dto")
    void should_convert_to_detail_dto() {
        Invoice inv = invoice(15L);
        var dto = useCase.convertToDetailDTO(inv);
        assertThat(dto.getId()).isEqualTo(15L);
        assertThat(dto.getDocumentNumber()).isEqualTo("INV-15");
        assertThat(dto.getStatus()).isEqualTo(InvoiceStatus.PENDING);
    }
}
