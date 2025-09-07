package co.edu.itm.invoiceextract.application.controller;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import co.edu.itm.invoiceextract.application.usecase.invoice.ApproveInvoiceUseCase;
import co.edu.itm.invoiceextract.application.usecase.invoice.FetchInvoicesUseCase;
import co.edu.itm.invoiceextract.application.mapper.invoice.InvoiceMapper;
import co.edu.itm.invoiceextract.application.usecase.invoice.ProcessInboundInvoiceUseCase;
import co.edu.itm.invoiceextract.application.usecase.invoice.RejectInvoiceUseCase;
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

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice Management", description = "API for managing invoices using the external payload structure")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final FetchInvoicesUseCase fetchInvoicesUseCase;
    private final InvoiceMapper mapper;
    private final ProcessInboundInvoiceUseCase processInboundInvoiceUseCase;
    private final ApproveInvoiceUseCase approveInvoiceUseCase;
    private final RejectInvoiceUseCase rejectInvoiceUseCase;
    private final KafkaTemplate<String, InvoiceMessage> kafkaTemplate;
    private final co.edu.itm.invoiceextract.infrastructure.messaging.mapper.InvoiceMessageMapper invoiceMessageMapper;

    public InvoiceController(FetchInvoicesUseCase fetchInvoicesUseCase,
                             InvoiceMapper mapper,
                             ProcessInboundInvoiceUseCase processInboundInvoiceUseCase,
                             ApproveInvoiceUseCase approveInvoiceUseCase,
                             RejectInvoiceUseCase rejectInvoiceUseCase,
                             KafkaTemplate<String, InvoiceMessage> kafkaTemplate,
                             co.edu.itm.invoiceextract.infrastructure.messaging.mapper.InvoiceMessageMapper invoiceMessageMapper) {
        this.fetchInvoicesUseCase = fetchInvoicesUseCase;
        this.mapper = mapper;
        this.processInboundInvoiceUseCase = processInboundInvoiceUseCase;
        this.approveInvoiceUseCase = approveInvoiceUseCase;
        this.rejectInvoiceUseCase = rejectInvoiceUseCase;
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
            Invoice created = processInboundInvoiceUseCase.processInboundInvoiceWithValidation(request);
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
        return fetchInvoicesUseCase.findByIdWithItems(id)
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
        return fetchInvoicesUseCase.findByDocumentNumber(documentNumber)
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
            approveInvoiceUseCase.approve(id);
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
            rejectInvoiceUseCase.reject(id);
            return ResponseEntity.ok("Invoice rejected successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error rejecting invoice: " + e.getMessage());
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
