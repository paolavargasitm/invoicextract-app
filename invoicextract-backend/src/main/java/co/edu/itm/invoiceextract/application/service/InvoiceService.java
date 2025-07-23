package co.edu.itm.invoiceextract.application.service;

import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.domain.entity.InvoiceMetadata;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import co.edu.itm.invoiceextract.domain.repository.InvoiceRepository;
import co.edu.itm.invoiceextract.application.dto.DashboardStatsDTO;
import co.edu.itm.invoiceextract.application.dto.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.RecentInvoiceDTO;
import co.edu.itm.invoiceextract.domain.repository.InvoiceMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMetadataRepository metadataRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMetadataRepository metadataRepository) {
        this.invoiceRepository = invoiceRepository;
        this.metadataRepository = metadataRepository;
    }

    @Transactional(readOnly = true)
    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Invoice> findAllWithMetadata() {
        return invoiceRepository.findAllWithMetadata();
    }

    @Transactional(readOnly = true)
    public Page<Invoice> findAll(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findByIdWithMetadata(Long id) {
        return invoiceRepository.findByIdWithMetadata(id);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByEmail(String email) {
        return invoiceRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByEmailContaining(String email) {
        return invoiceRepository.findByEmailContainingIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByType(InvoiceType type) {
        return invoiceRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByEmailAndStatus(String email, InvoiceStatus status) {
        return invoiceRepository.findByEmailAndStatus(email, status);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByEmailAndType(String email, InvoiceType type) {
        return invoiceRepository.findByEmailAndType(email, type);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByStatusAndType(InvoiceStatus status, InvoiceType type) {
        return invoiceRepository.findByStatusAndType(status, type);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.findByDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return invoiceRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public Invoice save(Invoice invoice) {
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // If metadata is provided, save it as well
        if (invoice.getMetadata() != null) {
            invoice.getMetadata().setInvoice(savedInvoice);
            metadataRepository.save(invoice.getMetadata());
        }
        
        return savedInvoice;
    }

    public Invoice update(Long id, Invoice invoiceDetails) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setEmail(invoiceDetails.getEmail());
                    invoice.setDate(invoiceDetails.getDate());
                    invoice.setStatus(invoiceDetails.getStatus());
                    invoice.setType(invoiceDetails.getType());
                    
                    Invoice updatedInvoice = invoiceRepository.save(invoice);
                    
                    // Update metadata if provided
                    if (invoiceDetails.getMetadata() != null) {
                        InvoiceMetadata existingMetadata = metadataRepository.findByInvoiceId(id)
                                .orElse(new InvoiceMetadata(updatedInvoice));
                        
                        // Copy metadata fields
                        copyMetadataFields(invoiceDetails.getMetadata(), existingMetadata);
                        metadataRepository.save(existingMetadata);
                        updatedInvoice.setMetadata(existingMetadata);
                    }
                    
                    return updatedInvoice;
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public Invoice updateStatus(Long id, InvoiceStatus status) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setStatus(status);
                    return invoiceRepository.save(invoice);
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public Invoice updateType(Long id, InvoiceType type) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setType(type);
                    return invoiceRepository.save(invoice);
                })
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public void delete(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new RuntimeException("Invoice not found with id: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return invoiceRepository.existsById(id);
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

    // Metadata-specific methods
    @Transactional(readOnly = true)
    public Optional<InvoiceMetadata> findMetadataByInvoiceId(Long invoiceId) {
        return metadataRepository.findByInvoiceId(invoiceId);
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceMetadata> findMetadataByInvoiceNumber(String invoiceNumber) {
        return metadataRepository.findByInvoiceNumber(invoiceNumber);
    }

    public InvoiceMetadata saveMetadata(InvoiceMetadata metadata) {
        return metadataRepository.save(metadata);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        long totalInvoices = invoiceRepository.count();
        long successfulInvoices = invoiceRepository.countByStatus(InvoiceStatus.PAID);
        long errorInvoices = invoiceRepository.countByStatus(InvoiceStatus.REJECTED);
        BigDecimal totalAmount = metadataRepository.findTotalAmount().orElse(BigDecimal.ZERO);
        return new DashboardStatsDTO(totalInvoices, successfulInvoices, errorInvoices, totalAmount);
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

    public InvoiceMetadata updateMetadata(Long invoiceId, InvoiceMetadata metadataDetails) {
        InvoiceMetadata existingMetadata = metadataRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new RuntimeException("Metadata not found for invoice id: " + invoiceId));
        
        copyMetadataFields(metadataDetails, existingMetadata);
        return metadataRepository.save(existingMetadata);
    }

    private void copyMetadataFields(InvoiceMetadata source, InvoiceMetadata target) {
        if (source.getInvoiceNumber() != null) target.setInvoiceNumber(source.getInvoiceNumber());
        if (source.getCustomerName() != null) target.setCustomerName(source.getCustomerName());
        if (source.getCustomerEmail() != null) target.setCustomerEmail(source.getCustomerEmail());
        if (source.getCustomerAddress() != null) target.setCustomerAddress(source.getCustomerAddress());
        if (source.getSupplierName() != null) target.setSupplierName(source.getSupplierName());
        if (source.getSupplierEmail() != null) target.setSupplierEmail(source.getSupplierEmail());
        if (source.getSupplierAddress() != null) target.setSupplierAddress(source.getSupplierAddress());
        if (source.getAmount() != null) target.setAmount(source.getAmount());
        if (source.getCurrency() != null) target.setCurrency(source.getCurrency());
        if (source.getTaxAmount() != null) target.setTaxAmount(source.getTaxAmount());
        if (source.getSubtotal() != null) target.setSubtotal(source.getSubtotal());
        if (source.getTotalAmount() != null) target.setTotalAmount(source.getTotalAmount());
        if (source.getIssueDate() != null) target.setIssueDate(source.getIssueDate());
        if (source.getDueDate() != null) target.setDueDate(source.getDueDate());
        if (source.getPaymentTerms() != null) target.setPaymentTerms(source.getPaymentTerms());
        if (source.getDescription() != null) target.setDescription(source.getDescription());
        if (source.getNotes() != null) target.setNotes(source.getNotes());
        if (source.getPdfUrl() != null) target.setPdfUrl(source.getPdfUrl());
        if (source.getOriginalFilename() != null) target.setOriginalFilename(source.getOriginalFilename());
        if (source.getFileSize() != null) target.setFileSize(source.getFileSize());
        if (source.getExtractedData() != null) target.setExtractedData(source.getExtractedData());
        if (source.getConfidenceScore() != null) target.setConfidenceScore(source.getConfidenceScore());
        if (source.getProcessingStatus() != null) target.setProcessingStatus(source.getProcessingStatus());
    }
}
