package co.edu.itm.invoiceextract.application.controller;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.testutil.InvoiceTestData;
import co.edu.itm.invoiceextract.application.usecase.invoice.FetchInvoicesUseCase;
import co.edu.itm.invoiceextract.application.usecase.invoice.ManageInvoiceUseCase;
import co.edu.itm.invoiceextract.application.mapper.invoice.InvoiceMapper;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FetchInvoicesUseCase fetchInvoicesUseCase;

    @MockBean
    private ManageInvoiceUseCase manageInvoiceUseCase;

    @MockBean
    private InvoiceMapper mapper;

    @MockBean
    private KafkaTemplate<String, InvoiceMessage> kafkaTemplate;

    @MockBean
    private co.edu.itm.invoiceextract.infrastructure.messaging.mapper.InvoiceMessageMapper invoiceMessageMapper;

    private static final String BASE = "/api/invoices";

    @Nested
    @DisplayName("POST /api/invoices")
    class CreateInvoice {
        @Test
        void should_create_invoice_when_request_is_valid() throws Exception {
            // Given
            InvoiceRequestDTO request = InvoiceTestData.sampleRequest();
            Invoice entity = InvoiceTestData.sampleEntity(1L);
            InvoiceDetailDTO response = InvoiceTestData.sampleDetailDTO(1L);
            given(manageInvoiceUseCase.createInvoice(org.mockito.ArgumentMatchers.any(InvoiceRequestDTO.class))).willReturn(entity);
            given(mapper.toDetailDTO(entity)).willReturn(response);

            // When / Then
            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.documentNumber", is("INV-001")))
                    .andExpect(jsonPath("$.status", is("PENDING")));
        }

    @Nested
    @DisplayName("GET /api/invoices/filter")
    class FilterEndpoint {
        @Test
        void should_return_single_result_when_id_provided_and_exists() throws Exception {
            Invoice entity = InvoiceTestData.sampleEntity(51L);
            InvoiceDetailDTO dto = InvoiceTestData.sampleDetailDTO(51L);
            given(manageInvoiceUseCase.findInvoiceByIdWithItems(51L)).willReturn(Optional.of(entity));
            given(mapper.toDetailDTO(entity)).willReturn(dto);

            mockMvc.perform(get(BASE + "/filter").param("id", "51").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(51)));
        }

        @Test
        void should_return_empty_list_when_id_not_found() throws Exception {
            given(manageInvoiceUseCase.findInvoiceByIdWithItems(404L)).willReturn(Optional.empty());

            mockMvc.perform(get(BASE + "/filter").param("id", "404").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void should_filter_by_sender_status_and_date_range() throws Exception {
            Invoice e1 = InvoiceTestData.sampleEntity(61L);
            e1.setSenderTaxId("800987654");
            e1.setStatus(InvoiceStatus.APPROVED);
            e1.setCreatedDate(java.time.LocalDateTime.now().minusDays(1));
            Invoice e2 = InvoiceTestData.sampleEntity(62L);
            e2.setSenderTaxId("111111111");
            e2.setStatus(InvoiceStatus.REJECTED);
            e2.setCreatedDate(java.time.LocalDateTime.now().minusDays(10));

            given(fetchInvoicesUseCase.findAll()).willReturn(List.of(e1, e2));

            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(61L);
            given(mapper.toDetailDTO(e1)).willReturn(d1);

            java.time.LocalDateTime start = java.time.LocalDateTime.now().minusDays(2);
            java.time.LocalDateTime end = java.time.LocalDateTime.now();

            mockMvc.perform(get(BASE + "/filter")
                            .param("senderTaxId", "800987654")
                            .param("status", "APPROVED")
                            .param("startDate", start.toString())
                            .param("endDate", end.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(61)));
        }
    }

        @Test
        void should_return_400_when_validation_fails() throws Exception {
            // Given: invalid request (missing required documentNumber)
            InvoiceRequestDTO request = InvoiceTestData.sampleRequest();
            request.setDocumentNumber("");
            given(manageInvoiceUseCase.createInvoice(org.mockito.ArgumentMatchers.any())).willThrow(new IllegalArgumentException("Document number is required"));

            // When / Then
            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/invoices/paginated")
    class Pagination {
        @Test
        void should_return_paginated_invoices() throws Exception {
            Invoice e1 = InvoiceTestData.sampleEntity(21L);
            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(21L);
            org.springframework.data.domain.PageRequest pr = org.springframework.data.domain.PageRequest.of(0, 1);
            org.springframework.data.domain.PageImpl<Invoice> page = new org.springframework.data.domain.PageImpl<>(List.of(e1), pr, 1);
            given(fetchInvoicesUseCase.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                    .willReturn(page);
            given(mapper.toDetailDTO(e1)).willReturn(d1);

            mockMvc.perform(get(BASE + "/paginated")
                            .param("page", "0")
                            .param("size", "1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id", is(21)));
        }
    }

    @Nested
    @DisplayName("GET filters: sender and type")
    class AdditionalFilters {
        @Test
        void should_filter_by_sender_tax_id() throws Exception {
            Invoice e1 = InvoiceTestData.sampleEntity(31L);
            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(31L);
            given(fetchInvoicesUseCase.findBySenderTaxId("123456789")).willReturn(List.of(e1));
            given(mapper.toDetailDTO(e1)).willReturn(d1);

            mockMvc.perform(get(BASE + "/sender/{senderTaxId}", "123456789").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(31)));
        }

        @Test
        void should_filter_by_document_type() throws Exception {
            Invoice e1 = InvoiceTestData.sampleEntity(32L);
            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(32L);
            given(fetchInvoicesUseCase.findByDocumentType("FACTURA")).willReturn(List.of(e1));
            given(mapper.toDetailDTO(e1)).willReturn(d1);

            mockMvc.perform(get(BASE + "/type/{documentType}", "FACTURA").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].documentType", is("FACTURA")));
        }
    }

    @Nested
    @DisplayName("Dashboard endpoints")
    class Dashboard {
        @Test
        void should_return_recent_invoices() throws Exception {
            // Already covered by async/pagination? Add explicit recent test
            var recent = List.of(
                    new co.edu.itm.invoiceextract.application.dto.invoice.RecentInvoiceDTO(1L, "INV-1", "SUP1", new java.math.BigDecimal("10.00"), java.time.LocalDate.now(), java.time.LocalDateTime.now())
            );
            given(fetchInvoicesUseCase.getRecentInvoices(1)).willReturn(recent);

            mockMvc.perform(get(BASE + "/recent").param("limit", "1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].documentNumber", is("INV-1")));
        }

        @Test
        void should_return_dashboard_stats() throws Exception {
            var stats = new co.edu.itm.invoiceextract.application.dto.invoice.DashboardStatsDTO();
            stats.setTotalCount(5);
            stats.setFacturaCount(3);
            stats.setNotaCreditoCount(1);
            stats.setNotaDebitoCount(1);
            stats.setTotalAmount(new java.math.BigDecimal("100.00"));
            stats.setAverageAmount(new java.math.BigDecimal("20.00"));
            given(fetchInvoicesUseCase.getDashboardStats()).willReturn(stats);

            mockMvc.perform(get(BASE + "/dashboard/stats").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount", is(5)))
                    .andExpect(jsonPath("$.facturaCount", is(3)));
        }
    }

    @Nested
    @DisplayName("GET /api/invoices/{id}/details")
    class DetailsById {
        @Test
        void should_return_details_when_found() throws Exception {
            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(41L);
            given(fetchInvoicesUseCase.getInvoiceDetails(41L)).willReturn(d1);

            mockMvc.perform(get(BASE + "/{id}/details", 41L).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(41)));
        }

        @Test
        void should_return_404_when_details_not_found() throws Exception {
            given(fetchInvoicesUseCase.getInvoiceDetails(404L)).willReturn(null);
            mockMvc.perform(get(BASE + "/{id}/details", 404L).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/invoices/{id}")
    class GetById {
        @Test
        void should_return_invoice_when_exists() throws Exception {
            // Given
            Invoice entity = InvoiceTestData.sampleEntity(5L);
            InvoiceDetailDTO dto = InvoiceTestData.sampleDetailDTO(5L);
            given(manageInvoiceUseCase.findInvoiceByIdWithItems(5L)).willReturn(Optional.of(entity));
            given(mapper.toDetailDTO(entity)).willReturn(dto);

            // When / Then
            mockMvc.perform(get(BASE + "/{id}", 5L).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(5)))
                    .andExpect(jsonPath("$.documentNumber", is("INV-001")));
        }

        @Test
        void should_return_404_when_not_found() throws Exception {
            // Given
            given(manageInvoiceUseCase.findInvoiceByIdWithItems(99L)).willReturn(Optional.empty());

            // When / Then
            mockMvc.perform(get(BASE + "/{id}", 99L).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/invoices/search/document-number/{documentNumber}")
    class GetByDocumentNumber {
        @Test
        void should_return_invoice_when_document_number_exists() throws Exception {
            Invoice entity = InvoiceTestData.sampleEntity(2L);
            InvoiceDetailDTO dto = InvoiceTestData.sampleDetailDTO(2L);
            given(manageInvoiceUseCase.findInvoiceByDocumentNumber("INV-001")).willReturn(Optional.of(entity));
            given(mapper.toDetailDTO(entity)).willReturn(dto);

            mockMvc.perform(get(BASE + "/search/document-number/{documentNumber}", "INV-001")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.documentNumber", is("INV-001")));
        }

        @Test
        void should_return_404_when_document_number_not_found() throws Exception {
            given(manageInvoiceUseCase.findInvoiceByDocumentNumber("NOPE")).willReturn(Optional.empty());

            mockMvc.perform(get(BASE + "/search/document-number/{documentNumber}", "NOPE")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/invoices/{id}")
    class UpdateInvoice {
        @Test
        void should_update_invoice_when_valid() throws Exception {
            // Given
            InvoiceRequestDTO request = InvoiceTestData.sampleRequest();
            Invoice entity = InvoiceTestData.sampleEntity(7L);
            InvoiceDetailDTO dto = InvoiceTestData.sampleDetailDTO(7L);
            given(manageInvoiceUseCase.updateInvoice(eq(7L), org.mockito.ArgumentMatchers.any())).willReturn(entity);
            given(mapper.toDetailDTO(entity)).willReturn(dto);

            // When / Then
            mockMvc.perform(put(BASE + "/{id}", 7L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(7)))
                    .andExpect(jsonPath("$.documentNumber", is("INV-001")));
        }

        @Test
        void should_return_404_when_updating_nonexistent_invoice() throws Exception {
            InvoiceRequestDTO request = InvoiceTestData.sampleRequest();
            given(manageInvoiceUseCase.updateInvoice(eq(404L), org.mockito.ArgumentMatchers.any())).willThrow(new IllegalArgumentException("Not found"));

            mockMvc.perform(put(BASE + "/{id}", 404L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_400_when_update_payload_invalid() throws Exception {
            InvoiceRequestDTO request = InvoiceTestData.sampleRequest();
            request.setAmount("");
            given(manageInvoiceUseCase.updateInvoice(eq(5L), org.mockito.ArgumentMatchers.any())).willThrow(new RuntimeException("Invalid input"));

            mockMvc.perform(put(BASE + "/{id}", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/invoices/{id}")
    class DeleteInvoice {
        @Test
        void should_return_204_when_deleted() throws Exception {
            // No content expected, just verify status
            mockMvc.perform(delete(BASE + "/{id}", 9L))
                    .andExpect(status().isNoContent());
            Mockito.verify(manageInvoiceUseCase).deleteInvoice(9L);
        }

        @Test
        void should_return_404_when_deleting_nonexistent_invoice() throws Exception {
            Mockito.doThrow(new IllegalArgumentException("Not found")).when(manageInvoiceUseCase).deleteInvoice(404L);

            mockMvc.perform(delete(BASE + "/{id}", 404L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/invoices/{id}/status")
    class ChangeStatus {
        @Test
        void should_change_status_when_valid() throws Exception {
            Invoice entity = InvoiceTestData.sampleEntity(3L);
            entity.setStatus(InvoiceStatus.APPROVED);
            InvoiceDetailDTO dto = InvoiceTestData.sampleDetailDTO(3L);
            dto.setStatus(InvoiceStatus.APPROVED);
            given(manageInvoiceUseCase.changeInvoiceStatus(3L, InvoiceStatus.APPROVED)).willReturn(entity);
            given(mapper.toDetailDTO(entity)).willReturn(dto);

            mockMvc.perform(put(BASE + "/{id}/status", 3L)
                            .param("status", "APPROVED")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("APPROVED")));
        }

        @Test
        void should_return_400_when_invalid_status_transition() throws Exception {
            given(manageInvoiceUseCase.changeInvoiceStatus(5L, InvoiceStatus.REJECTED))
                    .willThrow(new IllegalStateException("Invalid transition"));

            mockMvc.perform(put(BASE + "/{id}/status", 5L)
                            .param("status", "REJECTED")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET collections and filters")
    class Collections {
        @Test
        void should_list_all_invoices() throws Exception {
            Invoice e1 = InvoiceTestData.sampleEntity(1L);
            Invoice e2 = InvoiceTestData.sampleEntity(2L);
            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(1L);
            InvoiceDetailDTO d2 = InvoiceTestData.sampleDetailDTO(2L);
            given(fetchInvoicesUseCase.findAll()).willReturn(List.of(e1, e2));
            given(mapper.toDetailDTO(e1)).willReturn(d1);
            given(mapper.toDetailDTO(e2)).willReturn(d2);

            mockMvc.perform(get(BASE).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[1].id", is(2)));
        }

        @Test
        void should_filter_by_status() throws Exception {
            Invoice e1 = InvoiceTestData.sampleEntity(10L);
            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(10L);
            given(fetchInvoicesUseCase.findByStatus(InvoiceStatus.PENDING)).willReturn(List.of(e1));
            given(mapper.toDetailDTO(e1)).willReturn(d1);

            mockMvc.perform(get(BASE + "/status/{status}", "PENDING").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].status", is("PENDING")));
        }

        @Test
        void should_filter_by_date_range() throws Exception {
            Invoice e1 = InvoiceTestData.sampleEntity(11L);
            InvoiceDetailDTO d1 = InvoiceTestData.sampleDetailDTO(11L);
            LocalDateTime start = LocalDateTime.now().minusDays(5);
            LocalDateTime end = LocalDateTime.now();
            given(fetchInvoicesUseCase.findByDateBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).willReturn(List.of(e1));
            given(mapper.toDetailDTO(e1)).willReturn(d1);

            mockMvc.perform(get(BASE + "/date-range")
                            .param("startDate", start.toString())
                            .param("endDate", end.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("PUT /approve and /reject")
    class ApproveReject {
        @Test
        void should_approve_invoice() throws Exception {
            mockMvc.perform(put(BASE + "/{id}/approve", 12L))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("approved")));
            Mockito.verify(manageInvoiceUseCase).approveInvoice(12L);
        }

        @Test
        void should_return_400_on_error_approving() throws Exception {
            Mockito.doThrow(new RuntimeException("error")).when(manageInvoiceUseCase).approveInvoice(13L);
            mockMvc.perform(put(BASE + "/{id}/approve", 13L))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_reject_invoice() throws Exception {
            mockMvc.perform(put(BASE + "/{id}/reject", 14L))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("rejected")));
            Mockito.verify(manageInvoiceUseCase).rejectInvoice(14L);
        }
    }

    @Nested
    @DisplayName("POST /api/invoices/async")
    class AsyncCreation {
        @Test
        void should_enqueue_message_and_return_200() throws Exception {
            InvoiceRequestDTO request = InvoiceTestData.sampleRequest();
            given(invoiceMessageMapper.toMessage(org.mockito.ArgumentMatchers.any())).willReturn(new InvoiceMessage());

            mockMvc.perform(post(BASE + "/async")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("processed")));

            Mockito.verify(kafkaTemplate).send(eq("invoices"), org.mockito.ArgumentMatchers.any(InvoiceMessage.class));
        }
    }
}
