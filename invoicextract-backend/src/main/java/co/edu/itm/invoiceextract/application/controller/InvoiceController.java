package co.edu.itm.invoiceextract.application.controller;

import co.edu.itm.invoiceextract.application.dto.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.mapper.InvoiceMapper;
import co.edu.itm.invoiceextract.application.dto.DashboardStatsDTO;
import co.edu.itm.invoiceextract.application.dto.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.RecentInvoiceDTO;
import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.domain.entity.InvoiceMetadata;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import co.edu.itm.invoiceextract.application.service.InvoiceService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoice Management", description = "API for managing invoices with new structure - email, date, status, type (invoice/credit note/debit note) and metadata")
@CrossOrigin(origins = "*")
public class InvoiceController {
    private final InvoiceService service;
    private final InvoiceMapper mapper;

    public InvoiceController(InvoiceService service, InvoiceMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Get all invoices", description = "Retrieve a list of all invoices in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of invoices",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class)))
    })
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(
            @Parameter(description = "Include metadata in response") @RequestParam(defaultValue = "false") boolean includeMetadata) {
        List<Invoice> invoices = includeMetadata ? service.findAllWithMetadata() : service.findAll();
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices with pagination", description = "Retrieve a paginated list of invoices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated invoices",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/paginated")
    public ResponseEntity<Page<Invoice>> getAllInvoicesPaginated(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<Invoice> invoices = service.findAll(pageable);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoice by ID", description = "Retrieve a specific invoice by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(
            @Parameter(description = "ID of the invoice to retrieve", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Include metadata in response") @RequestParam(defaultValue = "true") boolean includeMetadata) {
        if (includeMetadata) {
            return service.findByIdWithMetadata(id)
                    .map(invoice -> ResponseEntity.ok(invoice))
                    .orElse(ResponseEntity.notFound().build());
        } else {
            return service.findById(id)
                    .map(invoice -> ResponseEntity.ok(invoice))
                    .orElse(ResponseEntity.notFound().build());
        }
    }

    @Operation(summary = "Get invoices by email", description = "Retrieve all invoices associated with a specific email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices by email",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<List<Invoice>> getInvoicesByEmail(
            @Parameter(description = "Email to search for", example = "customer@example.com")
            @PathVariable String email) {
        List<Invoice> invoices = service.findByEmail(email);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Search invoices by email (partial match)", description = "Search for invoices by email (case-insensitive partial match)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices by email search",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/search/email")
    public ResponseEntity<List<Invoice>> searchInvoicesByEmail(
            @Parameter(description = "Email to search for", example = "customer")
            @RequestParam String email) {
        List<Invoice> invoices = service.findByEmailContaining(email);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by status", description = "Retrieve all invoices with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices by status",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Invoice>> getInvoicesByStatus(
            @Parameter(description = "Status to filter by", example = "PENDING")
            @PathVariable InvoiceStatus status) {
        List<Invoice> invoices = service.findByStatus(status);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by type", description = "Retrieve all invoices with a specific type (INVOICE, CREDIT_NOTE, DEBIT_NOTE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices by type",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Invoice>> getInvoicesByType(
            @Parameter(description = "Type to filter by", example = "INVOICE")
            @PathVariable InvoiceType type) {
        List<Invoice> invoices = service.findByType(type);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by email and status", description = "Retrieve invoices filtered by both email and status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices by email and status",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/filter")
    public ResponseEntity<List<Invoice>> getInvoicesByEmailAndStatus(
            @Parameter(description = "Email to filter by") @RequestParam String email,
            @Parameter(description = "Status to filter by") @RequestParam InvoiceStatus status) {
        List<Invoice> invoices = service.findByEmailAndStatus(email, status);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoices by date range", description = "Retrieve invoices within a specific date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices by date range",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<Invoice>> getInvoicesByDateRange(
            @Parameter(description = "Start date", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Invoice> invoices = service.findByDateBetween(startDate, endDate);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Create a new invoice", description = "Create a new invoice with metadata in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invoice created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(
            @Parameter(description = "Invoice data to create")
            @Valid @RequestBody InvoiceRequestDTO invoiceRequest) {
        try {
            Invoice invoice = mapper.toEntity(invoiceRequest);
            Invoice createdInvoice = service.save(invoice);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update an existing invoice", description = "Update an existing invoice with metadata by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(
            @Parameter(description = "ID of the invoice to update", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated invoice data")
            @Valid @RequestBody InvoiceRequestDTO invoiceRequest) {
        try {
            Invoice invoiceDetails = mapper.toEntity(invoiceRequest);
            Invoice updatedInvoice = service.update(id, invoiceDetails);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update invoice status", description = "Update only the status of an existing invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Invoice> updateInvoiceStatus(
            @Parameter(description = "ID of the invoice to update", example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status for the invoice")
            @RequestBody Map<String, InvoiceStatus> statusUpdate) {
        try {
            InvoiceStatus newStatus = statusUpdate.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().build();
            }
            Invoice updatedInvoice = service.updateStatus(id, newStatus);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update invoice type", description = "Update only the type of an existing invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice type updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
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
            Invoice updatedInvoice = service.updateType(id, newType);
            return ResponseEntity.ok(updatedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete an invoice", description = "Delete an existing invoice by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Invoice deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard-stats")
    @Operation(summary = "Get dashboard statistics", description = "Provides summary statistics for the dashboard view, including total invoices, success/error counts, and total amount.")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = service.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent invoices", description = "Provides a paginated list of the most recent invoices for the dashboard table.")
    public ResponseEntity<Page<RecentInvoiceDTO>> getRecentInvoices(@PageableDefault(size = 10, sort = "date,desc") Pageable pageable) {
        Page<RecentInvoiceDTO> recentInvoices = service.getRecentInvoices(pageable);
        return ResponseEntity.ok(recentInvoices);
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get invoice details", description = "Provides detailed information for a single invoice, including the file URL for download.")
    public ResponseEntity<InvoiceDetailDTO> getInvoiceDetails(@PathVariable Long id) {
        return service.getInvoiceDetails(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Check if invoice exists", description = "Check if an invoice exists by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> checkInvoiceExists(
            @Parameter(description = "ID of the invoice to check", example = "1")
            @PathVariable Long id) {
        boolean exists = service.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Get invoice statistics", description = "Get comprehensive statistics about invoices in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
        Map<String, Object> stats = Map.of(
                "totalCount", service.count(),
                "pendingCount", service.countByStatus(InvoiceStatus.PENDING),
                "approvedCount", service.countByStatus(InvoiceStatus.APPROVED),
                "rejectedCount", service.countByStatus(InvoiceStatus.REJECTED),
                "paidCount", service.countByStatus(InvoiceStatus.PAID),
                "invoiceCount", service.countByType(InvoiceType.INVOICE),
                "creditNoteCount", service.countByType(InvoiceType.CREDIT_NOTE),
                "debitNoteCount", service.countByType(InvoiceType.DEBIT_NOTE)
        );
        return ResponseEntity.ok(stats);
    }

    // Metadata-specific endpoints
    @Operation(summary = "Get invoice metadata", description = "Get detailed metadata for a specific invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvoiceMetadata.class))),
            @ApiResponse(responseCode = "404", description = "Metadata not found")
    })
    @GetMapping("/{id}/metadata")
    public ResponseEntity<InvoiceMetadata> getInvoiceMetadata(
            @Parameter(description = "ID of the invoice", example = "1")
            @PathVariable Long id) {
        return service.findMetadataByInvoiceId(id)
                .map(metadata -> ResponseEntity.ok(metadata))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search by invoice number in metadata", description = "Find invoice by invoice number stored in metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice metadata found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = InvoiceMetadata.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @GetMapping("/search/invoice-number/{invoiceNumber}")
    public ResponseEntity<InvoiceMetadata> getByInvoiceNumber(
            @Parameter(description = "Invoice number to search for", example = "INV-2024-001")
            @PathVariable String invoiceNumber) {
        return service.findMetadataByInvoiceNumber(invoiceNumber)
                .map(metadata -> ResponseEntity.ok(metadata))
                .orElse(ResponseEntity.notFound().build());
    }
}
