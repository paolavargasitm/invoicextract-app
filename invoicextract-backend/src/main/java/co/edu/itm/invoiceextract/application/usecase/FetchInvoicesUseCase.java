package co.edu.itm.invoiceextract.application.usecase;

import co.edu.itm.invoiceextract.application.dto.DashboardStatsDTO;
import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.domain.entity.InvoiceMetadata;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import co.edu.itm.invoiceextract.domain.repository.InvoiceMetadataRepository;
import co.edu.itm.invoiceextract.application.dto.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.RecentInvoiceDTO;
import co.edu.itm.invoiceextract.domain.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FetchInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMetadataRepository metadataRepository;

    public FetchInvoicesUseCase(InvoiceRepository invoiceRepository, InvoiceMetadataRepository metadataRepository) {
        this.invoiceRepository = invoiceRepository;
        this.metadataRepository = metadataRepository;
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> findAllWithMetadata() {
        return invoiceRepository.findAllWithMetadata();
    }

    public Page<Invoice> findAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Optional<Invoice> findByIdWithMetadata(Long id) {
        return invoiceRepository.findByIdWithMetadata(id);
    }

    public List<Invoice> findByEmail(String email) {
        return invoiceRepository.findByEmail(email);
    }

    public List<Invoice> findByEmailContaining(String email) {
        return invoiceRepository.findByEmailContainingIgnoreCase(email);
    }

    public List<Invoice> findByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<Invoice> findByType(InvoiceType type) {
        return invoiceRepository.findByType(type);
    }

    public List<Invoice> findByEmailAndStatus(String email, InvoiceStatus status) {
        return invoiceRepository.findByEmailAndStatus(email, status);
    }

    public List<Invoice> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.findByDateBetween(startDate, endDate);
    }

    public boolean existsById(Long id) {
        return invoiceRepository.existsById(id);
    }

    public DashboardStatsDTO getDashboardStats() {
        long totalInvoices = invoiceRepository.count();
        long successfulInvoices = invoiceRepository.countByStatus(InvoiceStatus.PAID);
        long errorInvoices = invoiceRepository.countByStatus(InvoiceStatus.REJECTED);
        BigDecimal totalAmount = metadataRepository.findTotalAmount().orElse(BigDecimal.ZERO);
        return new DashboardStatsDTO(totalInvoices, successfulInvoices, errorInvoices, totalAmount);
    }

    public Optional<InvoiceMetadata> findMetadataByInvoiceId(Long invoiceId) {
        return metadataRepository.findByInvoiceId(invoiceId);
    }

    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return metadataRepository.findByInvoiceNumber(invoiceNumber)
                .map(InvoiceMetadata::getInvoice);
    }

    public Optional<InvoiceMetadata> findMetadataByInvoiceNumber(String invoiceNumber) {
        return metadataRepository.findByInvoiceNumber(invoiceNumber);
    }

    @Transactional(readOnly = true)
    public Page<RecentInvoiceDTO> getRecentInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(invoice -> {
            InvoiceMetadata metadata = invoice.getMetadata();
            return new RecentInvoiceDTO(
                    invoice.getId(),
                    invoice.getDate(),
                    metadata != null ? metadata.getSupplierName() : null,
                    metadata != null ? metadata.getTotalAmount() : null,
                    invoice.getStatus()
            );
        });
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceDetailDTO> getInvoiceDetails(Long id) {
        return invoiceRepository.findByIdWithMetadata(id).map(invoice -> {
            InvoiceMetadata metadata = invoice.getMetadata();
            return new InvoiceDetailDTO(
                    metadata != null ? metadata.getSupplierName() : null,
                    metadata != null ? metadata.getIssueDate() : null,
                    metadata != null ? metadata.getTotalAmount() : null,
                    invoice.getStatus(),
                    invoice.getFileUrl()
            );
        });
    }

    @Transactional(readOnly = true)
    public long count() {
        return invoiceRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByStatus(InvoiceStatus status) {
        return invoiceRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByType(InvoiceType type) {
        return invoiceRepository.countByType(type);
    }
}