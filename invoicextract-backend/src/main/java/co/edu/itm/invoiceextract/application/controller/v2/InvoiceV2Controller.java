package co.edu.itm.invoiceextract.application.controller.v2;

import co.edu.itm.invoiceextract.application.dto.v2.InvoiceV2RequestDTO;
import co.edu.itm.invoiceextract.application.mapper.v2.InvoiceV2Mapper;
import co.edu.itm.invoiceextract.application.usecase.FetchInvoicesUseCase;
import co.edu.itm.invoiceextract.application.usecase.ApproveInvoiceUseCase;
import co.edu.itm.invoiceextract.application.usecase.RejectInvoiceUseCase;
import co.edu.itm.invoiceextract.application.service.InvoiceService;
import co.edu.itm.invoiceextract.application.service.v2.InvoiceV2Service;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceMetadata;
import co.edu.itm.invoiceextract.application.dto.DashboardStatsDTO;
import co.edu.itm.invoiceextract.application.dto.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.RecentInvoiceDTO;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2/invoices")
@Tag(name = "Invoice Management V2", description = "API v2 for managing invoices using the new external payload structure")
@CrossOrigin(origins = "*")
public class InvoiceV2Controller {

    private final InvoiceService invoiceService;
    private final FetchInvoicesUseCase fetchInvoicesUseCase;
    private final InvoiceV2Mapper mapper;
    private final InvoiceV2Service invoiceV2Service;
    private final ApproveInvoiceUseCase approveInvoiceUseCase;
    private final RejectInvoiceUseCase rejectInvoiceUseCase;

    public InvoiceV2Controller(InvoiceService invoiceService,
                               FetchInvoicesUseCase fetchInvoicesUseCase,
                               InvoiceV2Mapper mapper,
                               InvoiceV2Service invoiceV2Service,
                               ApproveInvoiceUseCase approveInvoiceUseCase,
                               RejectInvoiceUseCase rejectInvoiceUseCase) {
        this.invoiceService = invoiceService;
        this.fetchInvoicesUseCase = fetchInvoicesUseCase;
        this.mapper = mapper;
        this.invoiceV2Service = invoiceV2Service;
        this.approveInvoiceUseCase = approveInvoiceUseCase;
        this.rejectInvoiceUseCase = rejectInvoiceUseCase;
    }

    @Operation(summary = "Create invoice (v2)", description = "Creates an invoice from the new v2 payload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<Invoice> create(@Valid @RequestBody InvoiceV2RequestDTO request) {
        try {
            // Dual-write: persist in v2 tables and legacy tables
            Invoice created = invoiceV2Service.createDualWrite(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get invoice by ID (v2)", description = "Retrieves an invoice by its ID including metadata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getById(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id) {
        return fetchInvoicesUseCase.findByIdWithMetadata(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search by document number (v2)", description = "Find invoice metadata by DocumentNumber (mapped to invoice_number)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvoiceMetadata.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/search/document-number/{documentNumber}")
    public ResponseEntity<InvoiceMetadata> getByDocumentNumber(
            @Parameter(description = "Document number", example = "INV-001")
            @PathVariable String documentNumber) {
        return fetchInvoicesUseCase.findMetadataByInvoiceNumber(documentNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Parity with v1 reads and actions

    @Operation(summary = "Get all invoices (v2)", description = "Retrieve a list of all invoices. Optionally include metadata.")
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(
            @Parameter(description = "Include metadata in response") @RequestParam(defaultValue = "false") boolean includeMetadata) {
        List<Invoice> invoices = includeMetadata ? fetchInvoicesUseCase.findAllWithMetadata() : fetchInvoicesUseCase.findAll();
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices with pagination (v2)", description = "Retrieve a paginated list of invoices")
    @GetMapping("/paginated")
    public ResponseEntity<Page<Invoice>> getAllInvoicesPaginated(@Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<Invoice> invoices = fetchInvoicesUseCase.findAll(pageable);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by email (v2)", description = "Retrieve all invoices associated with a specific email")
    @GetMapping("/email/{email}")
    public ResponseEntity<List<Invoice>> getInvoicesByEmail(
            @Parameter(description = "Email to search for", example = "customer@example.com")
            @PathVariable String email) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByEmail(email);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Search invoices by email (partial match) (v2)", description = "Search for invoices by email (case-insensitive partial match)")
    @GetMapping("/search/email")
    public ResponseEntity<List<Invoice>> searchInvoicesByEmail(
            @Parameter(description = "Email to search for", example = "customer")
            @RequestParam String email) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByEmailContaining(email);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by status (v2)", description = "Retrieve all invoices with a specific status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Invoice>> getInvoicesByStatus(
            @Parameter(description = "Status to filter by", example = "PENDING")
            @PathVariable InvoiceStatus status) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByStatus(status);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by type (v2)", description = "Retrieve all invoices with a specific type")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Invoice>> getInvoicesByType(
            @Parameter(description = "Type to filter by", example = "INVOICE")
            @PathVariable InvoiceType type) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByType(type);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by email and status (v2)", description = "Retrieve invoices filtered by both email and status")
    @GetMapping("/filter")
    public ResponseEntity<List<Invoice>> getInvoicesByEmailAndStatus(
            @Parameter(description = "Email to filter by") @RequestParam String email,
            @Parameter(description = "Status to filter by") @RequestParam InvoiceStatus status) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByEmailAndStatus(email, status);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by date range (v2)", description = "Retrieve invoices within a specific date range")
    @GetMapping("/date-range")
    public ResponseEntity<List<Invoice>> getInvoicesByDateRange(
            @Parameter(description = "Start date", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByDateBetween(startDate, endDate);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Approve an invoice (v2)", description = "Marks an invoice as APPROVED.")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Invoice> approveInvoice(
            @Parameter(description = "ID of the invoice to approve", example = "1")
            @PathVariable Long id) {
        try {
            Invoice updatedInvoice = approveInvoiceUseCase.approve(id);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Reject an invoice (v2)", description = "Marks an invoice as REJECTED.")
    @PostMapping("/{id}/reject")
    public ResponseEntity<Invoice> rejectInvoice(
            @Parameter(description = "ID of the invoice to reject", example = "1")
            @PathVariable Long id) {
        try {
            Invoice updatedInvoice = rejectInvoiceUseCase.reject(id);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update invoice type (v2)", description = "Update only the type of an existing invoice")
    @PatchMapping("/{id}/type")
    public ResponseEntity<Invoice> updateInvoiceType(
            @Parameter(description = "ID of the invoice to update", example = "1")
            @PathVariable Long id,
            @Parameter(description = "New type for the invoice")
            @RequestBody Map<String, InvoiceType> typeUpdate) {
        try {
            InvoiceType newType = typeUpdate.get("type");
            if (newType == null) {
                return ResponseEntity.badRequest().build();
            }
            Invoice updatedInvoice = invoiceService.updateType(id, newType);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete an invoice (v2)", description = "Delete an existing invoice by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard-stats")
    @Operation(summary = "Get dashboard statistics (v2)", description = "Provides summary statistics for the dashboard view.")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = fetchInvoicesUseCase.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent invoices (v2)", description = "Provides a paginated list of the most recent invoices for the dashboard table.")
    public ResponseEntity<Page<RecentInvoiceDTO>> getRecentInvoices(@PageableDefault(size = 10, sort = "date,desc") Pageable pageable) {
        Page<RecentInvoiceDTO> recentInvoices = fetchInvoicesUseCase.getRecentInvoices(pageable);
        return ResponseEntity.ok(recentInvoices);
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get invoice details (v2)", description = "Provides detailed information for a single invoice, including the file URL for download.")
    public ResponseEntity<InvoiceDetailDTO> getInvoiceDetails(@PathVariable Long id) {
        return fetchInvoicesUseCase.getInvoiceDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Check if invoice exists (v2)", description = "Check if an invoice exists by ID")
    @GetMapping("/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> checkInvoiceExists(
            @Parameter(description = "ID of the invoice to check", example = "1")
            @PathVariable Long id) {
        boolean exists = fetchInvoicesUseCase.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Get invoice statistics (v2)", description = "Get comprehensive statistics about invoices in the system")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
        Map<String, Object> stats = Map.of(
                "totalCount", fetchInvoicesUseCase.count(),
                "pendingCount", fetchInvoicesUseCase.countByStatus(InvoiceStatus.PENDING),
                "approvedCount", fetchInvoicesUseCase.countByStatus(InvoiceStatus.APPROVED),
                "rejectedCount", fetchInvoicesUseCase.countByStatus(InvoiceStatus.REJECTED),
                "paidCount", fetchInvoicesUseCase.countByStatus(InvoiceStatus.PAID),
                "invoiceCount", fetchInvoicesUseCase.countByType(InvoiceType.INVOICE),
                "creditNoteCount", fetchInvoicesUseCase.countByType(InvoiceType.CREDIT_NOTE),
                "debitNoteCount", fetchInvoicesUseCase.countByType(InvoiceType.DEBIT_NOTE)
        );
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get invoice metadata (v2)", description = "Get detailed metadata for a specific invoice")
    @GetMapping("/{id}/metadata")
    public ResponseEntity<InvoiceMetadata> getInvoiceMetadata(
            @Parameter(description = "ID of the invoice", example = "1")
            @PathVariable Long id) {
        return fetchInvoicesUseCase.findMetadataByInvoiceId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search by invoice number in metadata (v2)", description = "Find invoice by invoice number stored in metadata")
    @GetMapping("/search/invoice-number/{invoiceNumber}")
    public ResponseEntity<InvoiceMetadata> getByInvoiceNumber(
            @Parameter(description = "Invoice number to search for", example = "INV-2024-001")
            @PathVariable String invoiceNumber) {
        return fetchInvoicesUseCase.findMetadataByInvoiceNumber(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
