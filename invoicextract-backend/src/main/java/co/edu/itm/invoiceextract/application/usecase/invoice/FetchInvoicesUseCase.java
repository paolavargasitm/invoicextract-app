package co.edu.itm.invoiceextract.application.usecase.invoice;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.DashboardStatsDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.RecentInvoiceDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FetchInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;

    public FetchInvoicesUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public Page<Invoice> findAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Optional<Invoice> findByIdWithItems(Long id) {
        return invoiceRepository.findByIdWithItems(id);
    }

    public Optional<Invoice> findByDocumentNumber(String documentNumber) {
        return invoiceRepository.findByDocumentNumber(documentNumber);
    }

    public List<Invoice> findBySenderTaxId(String senderTaxId) {
        return invoiceRepository.findBySenderTaxId(senderTaxId);
    }

    public List<Invoice> findByReceiverTaxId(String receiverTaxId) {
        return invoiceRepository.findByReceiverTaxId(receiverTaxId);
    }

    public List<Invoice> findByDocumentType(String documentType) {
        return invoiceRepository.findByDocumentType(documentType);
    }

    public List<Invoice> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return invoiceRepository.findByAmountBetween(minAmount, maxAmount);
    }

    public List<Invoice> findByIssueDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return invoiceRepository.findByIssueDateBetween(startDate, endDate);
    }

    public List<Invoice> findByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.findByCreatedDateBetween(startDate, endDate);
    }

    public List<RecentInvoiceDTO> findRecentInvoices(int limit) {
        return invoiceRepository.findTop10ByOrderByCreatedDateDesc()
                .stream()
                .limit(limit)
                .map(invoice -> new RecentInvoiceDTO(
                        invoice.getId(),
                        invoice.getDocumentNumber(),
                        invoice.getSenderBusinessName(),
                        invoice.getAmount(),
                        invoice.getIssueDate(),
                        invoice.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        stats.setTotalCount(invoiceRepository.count());
        stats.setFacturaCount(invoiceRepository.countByDocumentType("FACTURA"));
        stats.setNotaCreditoCount(invoiceRepository.countByDocumentType("NOTA_CREDITO"));
        stats.setNotaDebitoCount(invoiceRepository.countByDocumentType("NOTA_DEBITO"));
        
        BigDecimal totalAmount = invoiceRepository.sumAllAmounts();
        stats.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        if (stats.getTotalCount() > 0) {
            stats.setAverageAmount(stats.getTotalAmount().divide(
                BigDecimal.valueOf(stats.getTotalCount()), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            stats.setAverageAmount(BigDecimal.ZERO);
        }
        
        return stats;
    }

    public long count() {
        return invoiceRepository.count();
    }

    public long countByDocumentType(String documentType) {
        return invoiceRepository.countByDocumentType(documentType);
    }

    public List<Invoice> findByStatus(co.edu.itm.invoiceextract.domain.enums.InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<RecentInvoiceDTO> getRecentInvoices(int limit) {
        return findRecentInvoices(limit);
    }

    public InvoiceDetailDTO getInvoiceDetails(Long id) {
        Optional<Invoice> invoice = findByIdWithItems(id);
        return invoice.map(this::convertToDetailDTO).orElse(null);
    }

    public List<Invoice> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return findByCreatedDateBetween(startDate, endDate);
    }

    public InvoiceDetailDTO convertToDetailDTO(Invoice invoice) {
        InvoiceDetailDTO dto = new InvoiceDetailDTO();
        dto.setId(invoice.getId());
        dto.setDocumentType(invoice.getDocumentType());
        dto.setDocumentNumber(invoice.getDocumentNumber());
        dto.setReceiverTaxId(invoice.getReceiverTaxId());
        dto.setReceiverBusinessName(invoice.getReceiverBusinessName());
        dto.setSenderTaxId(invoice.getSenderTaxId());
        dto.setSenderBusinessName(invoice.getSenderBusinessName());
        dto.setAmount(invoice.getAmount());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        dto.setCreatedDate(invoice.getCreatedDate());
        dto.setModifiedDate(invoice.getModifiedDate());
        
        // Convert items if present
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            dto.setItems(invoice.getItems().stream()
                    .map(item -> {
                        InvoiceItemDTO itemDto = new InvoiceItemDTO();
                        itemDto.setItemCode(item.getItemCode());
                        itemDto.setDescription(item.getDescription());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setUnit(item.getUnit());
                        itemDto.setUnitPrice(item.getUnitPrice());
                        itemDto.setSubtotal(item.getSubtotal());
                        itemDto.setTaxAmount(item.getTaxAmount());
                        itemDto.setTotal(item.getTotal());
                        return itemDto;
                    })
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
