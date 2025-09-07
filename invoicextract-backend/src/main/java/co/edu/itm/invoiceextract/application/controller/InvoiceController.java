package co.edu.itm.invoiceextract.application.controller;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import co.edu.itm.invoiceextract.application.usecase.invoice.FetchInvoicesUseCase;
import co.edu.itm.invoiceextract.application.usecase.invoice.ManageInvoiceUseCase;
import co.edu.itm.invoiceextract.application.mapper.invoice.InvoiceMapper;
import co.edu.itm.invoiceextract.application.dto.invoice.DashboardStatsDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.RecentInvoiceDTO;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice Management", description = "API for managing invoices using the external payload structure")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final FetchInvoicesUseCase fetchInvoicesUseCase;
    private final ManageInvoiceUseCase manageInvoiceUseCase;
    private final InvoiceMapper mapper;
    private final KafkaTemplate<String, InvoiceMessage> kafkaTemplate;
    private final co.edu.itm.invoiceextract.infrastructure.messaging.mapper.InvoiceMessageMapper invoiceMessageMapper;

    public InvoiceController(FetchInvoicesUseCase fetchInvoicesUseCase,
                             ManageInvoiceUseCase manageInvoiceUseCase,
                             InvoiceMapper mapper,
                             KafkaTemplate<String, InvoiceMessage> kafkaTemplate,
                             co.edu.itm.invoiceextract.infrastructure.messaging.mapper.InvoiceMessageMapper invoiceMessageMapper) {
        this.fetchInvoicesUseCase = fetchInvoicesUseCase;
        this.manageInvoiceUseCase = manageInvoiceUseCase;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.invoiceMessageMapper = invoiceMessageMapper;
    }

    @Operation(summary = "Create invoice (v2)", description = "Creates an invoice from the new v2 payload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/async")
    public ResponseEntity<String> createInvoiceAsync(@RequestBody InvoiceRequestDTO invoiceDto) {
        InvoiceMessage message = invoiceMessageMapper.toMessage(invoiceDto);
        kafkaTemplate.send("invoices", message);
        return ResponseEntity.ok("Request received and is being processed.");
    }

    @Operation(summary = "Create invoice (v2)", description = "Creates an invoice from the new v2 payload.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<InvoiceDetailDTO> create(@Valid @RequestBody InvoiceRequestDTO request) {
        try {
            Invoice created = manageInvoiceUseCase.createInvoice(request);
            InvoiceDetailDTO dto = mapper.toDetailDTO(created);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get invoice by ID (v2)", description = "Retrieves an invoice by its ID including metadata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailDTO> getById(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id) {
        return manageInvoiceUseCase.findInvoiceByIdWithItems(id)
                .map(mapper::toDetailDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search by document number (v2)", description = "Find invoice by DocumentNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Invoice.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/search/document-number/{documentNumber}")
    public ResponseEntity<InvoiceDetailDTO> getByDocumentNumber(
            @Parameter(description = "Document number", example = "INV-001")
            @PathVariable String documentNumber) {
        return manageInvoiceUseCase.findInvoiceByDocumentNumber(documentNumber)
                .map(mapper::toDetailDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Parity with v1 reads and actions

    @Operation(summary = "Get all invoices (v2)", description = "Retrieve a list of all invoices with items.")
    @GetMapping
    public ResponseEntity<List<InvoiceDetailDTO>> getAllInvoices() {
        List<Invoice> invoices = fetchInvoicesUseCase.findAll();
        List<InvoiceDetailDTO> dtos = invoices.stream()
                .map(mapper::toDetailDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Filter invoices (v2)", description = "Filter by optional criteria: id, senderTaxId, receiverTaxId, status, createdDate range")
    @GetMapping("/filter")
    public ResponseEntity<List<InvoiceDetailDTO>> filterInvoices(
            @Parameter(description = "Invoice ID", example = "1")
            @RequestParam(required = false) Long id,
            @Parameter(description = "Sender tax ID", example = "987654321")
            @RequestParam(required = false) String senderTaxId,
            @Parameter(description = "Receiver tax ID", example = "123456789")
            @RequestParam(required = false) String receiverTaxId,
            @Parameter(description = "Invoice status", example = "PENDING")
            @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Start created date", example = "2024-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End created date", example = "2024-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        // If ID provided, short-circuit
        if (id != null) {
            return manageInvoiceUseCase.findInvoiceByIdWithItems(id)
                    .map(mapper::toDetailDTO)
                    .map(dto -> ResponseEntity.ok(List.of(dto)))
                    .orElse(ResponseEntity.ok(List.of()));
        }

        // Fetch all and filter in-memory according to optional params
        List<Invoice> all = fetchInvoicesUseCase.findAll();
        List<InvoiceDetailDTO> filtered = all.stream()
                .filter(inv -> senderTaxId == null || senderTaxId.equals(inv.getSenderTaxId()))
                .filter(inv -> receiverTaxId == null || receiverTaxId.equals(inv.getReceiverTaxId()))
                .filter(inv -> status == null || status == inv.getStatus())
                .filter(inv -> {
                    if (startDate == null && endDate == null) return true;
                    LocalDateTime created = inv.getCreatedDate();
                    if (created == null) return false;
                    boolean after = startDate == null || !created.isBefore(startDate);
                    boolean before = endDate == null || !created.isAfter(endDate);
                    return after && before;
                })
                .map(mapper::toDetailDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    @Operation(summary = "Get invoices with pagination (v2)", description = "Retrieve a paginated list of invoices")
    @GetMapping("/paginated")
    public ResponseEntity<Page<InvoiceDetailDTO>> getAllInvoicesPaginated(@Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<Invoice> invoices = fetchInvoicesUseCase.findAll(pageable);
        Page<InvoiceDetailDTO> dtos = invoices.map(mapper::toDetailDTO);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get invoices by sender tax ID (v2)", description = "Retrieve all invoices from a specific sender")
    @GetMapping("/sender/{senderTaxId}")
    public ResponseEntity<List<InvoiceDetailDTO>> getInvoicesBySender(
            @Parameter(description = "Sender tax ID to search for", example = "987654321")
            @PathVariable String senderTaxId) {
        List<Invoice> invoices = fetchInvoicesUseCase.findBySenderTaxId(senderTaxId);
        List<InvoiceDetailDTO> dtos = invoices.stream()
                .map(mapper::toDetailDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get invoices by document type (v2)", description = "Retrieve all invoices with a specific document type")
    @GetMapping("/type/{documentType}")
    public ResponseEntity<List<InvoiceDetailDTO>> getInvoicesByType(
            @Parameter(description = "Document type to filter by", example = "FACTURA")
            @PathVariable String documentType) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByDocumentType(documentType);
        List<InvoiceDetailDTO> dtos = invoices.stream()
                .map(mapper::toDetailDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get invoices by status (v2)", description = "Retrieve all invoices with a specific status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceDetailDTO>> getInvoicesByStatus(
            @Parameter(description = "Status to filter by", example = "PENDING")
            @PathVariable InvoiceStatus status) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByStatus(status);
        List<InvoiceDetailDTO> dtos = invoices.stream()
                .map(mapper::toDetailDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // Dashboard and summary endpoints
    
    @Operation(summary = "Get recent invoices (v2)", description = "Get recent invoices for dashboard")
    @GetMapping("/recent")
    public ResponseEntity<List<RecentInvoiceDTO>> getRecentInvoices(
            @Parameter(description = "Number of recent invoices to retrieve", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<RecentInvoiceDTO> recentInvoices = fetchInvoicesUseCase.getRecentInvoices(limit);
        return ResponseEntity.ok(recentInvoices);
    }

    @Operation(summary = "Get dashboard statistics (v2)", description = "Get dashboard statistics")
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = fetchInvoicesUseCase.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get invoice details (v2)", description = "Get detailed invoice information")
    @GetMapping("/{id}/details")
    public ResponseEntity<InvoiceDetailDTO> getInvoiceDetails(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id) {
        InvoiceDetailDTO details = fetchInvoicesUseCase.getInvoiceDetails(id);
        if (details != null) {
            return ResponseEntity.ok(details);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Action endpoints
    
    @Operation(summary = "Approve invoice (v2)", description = "Approve an invoice by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice approved successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "400", description = "Invalid operation")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveInvoice(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id) {
        try {
            manageInvoiceUseCase.approveInvoice(id);
            return ResponseEntity.ok("Invoice approved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error approving invoice: " + e.getMessage());
        }
    }

    @Operation(summary = "Reject invoice (v2)", description = "Reject an invoice by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "400", description = "Invalid operation")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectInvoice(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id) {
        try {
            manageInvoiceUseCase.rejectInvoice(id);
            return ResponseEntity.ok("Invoice rejected successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error rejecting invoice: " + e.getMessage());
        }
    }

    @Operation(summary = "Update invoice (v2)", description = "Update an existing invoice by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice updated successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDetailDTO> updateInvoice(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequestDTO request) {
        try {
            Invoice updated = manageInvoiceUseCase.updateInvoice(id, request);
            InvoiceDetailDTO dto = mapper.toDetailDTO(updated);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete invoice (v2)", description = "Delete an invoice by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Invoice deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "400", description = "Invalid operation")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id) {
        try {
            manageInvoiceUseCase.deleteInvoice(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Change invoice status (v2)", description = "Change the status of an invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status or operation")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceDetailDTO> changeInvoiceStatus(
            @Parameter(description = "Invoice ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status", example = "APPROVED")
            @RequestParam InvoiceStatus status) {
        try {
            Invoice updated = manageInvoiceUseCase.changeInvoiceStatus(id, status);
            InvoiceDetailDTO dto = mapper.toDetailDTO(updated);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get invoices by date range (v2)", description = "Retrieve invoices within a specific date range")
    @GetMapping("/date-range")
    public ResponseEntity<List<InvoiceDetailDTO>> getInvoicesByDateRange(
            @Parameter(description = "Start date", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Invoice> invoices = fetchInvoicesUseCase.findByDateBetween(startDate, endDate);
        List<InvoiceDetailDTO> dtos = invoices.stream()
                .map(mapper::toDetailDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
