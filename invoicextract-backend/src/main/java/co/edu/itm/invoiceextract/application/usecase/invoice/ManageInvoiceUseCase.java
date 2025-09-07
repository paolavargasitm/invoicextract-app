package co.edu.itm.invoiceextract.application.usecase.invoice;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.mapper.invoice.InvoiceMapper;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceRepository;
import co.edu.itm.invoiceextract.domain.repository.invoices.InvoiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManageInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceMapper invoiceMapper;

    /**
     * Creates a new invoice with its items
     */
    @Transactional
    public Invoice createInvoice(InvoiceRequestDTO request) {
        log.debug("Creating invoice with document number: {}", request.getDocumentNumber());
        
        // Validate input data
        validateInvoiceData(request);
        
        // Validate if invoice already exists
        if (invoiceRepository.findByDocumentNumber(request.getDocumentNumber()).isPresent()) {
            throw new IllegalArgumentException("Invoice with document number " + request.getDocumentNumber() + " already exists");
        }

        // Map DTO to entity using MapStruct
        Invoice invoice = invoiceMapper.toEntity(request);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Create and associate invoice item if present
        if (request.getInvoiceItem() != null) {
            InvoiceItem item = invoiceMapper.toItemEntity(request.getInvoiceItem());
            item.setInvoice(savedInvoice);
            invoiceItemRepository.save(item);
            savedInvoice.addItem(item);
        }

        log.info("Invoice created successfully with ID: {}", savedInvoice.getId());
        return savedInvoice;
    }

    /**
     * Updates an existing invoice
     */
    @Transactional
    public Invoice updateInvoice(Long invoiceId, InvoiceRequestDTO request) {
        log.debug("Updating invoice with ID: {}", invoiceId);
        
        Invoice existingInvoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        // Update invoice fields
        updateInvoiceFields(existingInvoice, request);
        
        // Update items if present
        if (request.getInvoiceItem() != null) {
            updateInvoiceItems(existingInvoice, request);
        }

        Invoice updatedInvoice = invoiceRepository.save(existingInvoice);
        log.info("Invoice updated successfully with ID: {}", updatedInvoice.getId());
        return updatedInvoice;
    }

    /**
     * Deletes an invoice and its items
     */
    @Transactional
    public void deleteInvoice(Long invoiceId) {
        log.debug("Deleting invoice with ID: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        // Delete associated items first (cascade should handle this, but being explicit)
        invoiceItemRepository.deleteAll(invoice.getItems());
        
        // Delete the invoice
        invoiceRepository.delete(invoice);
        
        log.info("Invoice deleted successfully with ID: {}", invoiceId);
    }

    /**
     * Finds an invoice by ID
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> findInvoiceById(Long invoiceId) {
        log.debug("Finding invoice with ID: {}", invoiceId);
        return invoiceRepository.findById(invoiceId);
    }

    /**
     * Finds an invoice by ID with items loaded
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> findInvoiceByIdWithItems(Long invoiceId) {
        log.debug("Finding invoice with items for ID: {}", invoiceId);
        return invoiceRepository.findByIdWithItems(invoiceId);
    }

    /**
     * Finds an invoice by document number
     */
    @Transactional(readOnly = true)
    public Optional<Invoice> findInvoiceByDocumentNumber(String documentNumber) {
        log.debug("Finding invoice with document number: {}", documentNumber);
        return invoiceRepository.findByDocumentNumber(documentNumber);
    }

    /**
     * Approves an invoice
     */
    @Transactional
    public Invoice approveInvoice(Long invoiceId) {
        log.debug("Approving invoice with ID: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.APPROVED) {
            throw new IllegalStateException("Invoice is already approved");
        }

        invoice.setStatus(InvoiceStatus.APPROVED);
        Invoice approvedInvoice = invoiceRepository.save(invoice);
        
        log.info("Invoice approved successfully with ID: {}", approvedInvoice.getId());
        return approvedInvoice;
    }

    /**
     * Rejects an invoice
     */
    @Transactional
    public Invoice rejectInvoice(Long invoiceId) {
        log.debug("Rejecting invoice with ID: {}", invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.REJECTED) {
            throw new IllegalStateException("Invoice is already rejected");
        }

        invoice.setStatus(InvoiceStatus.REJECTED);
        Invoice rejectedInvoice = invoiceRepository.save(invoice);
        
        log.info("Invoice rejected successfully with ID: {}", rejectedInvoice.getId());
        return rejectedInvoice;
    }

    /**
     * Changes invoice status
     */
    @Transactional
    public Invoice changeInvoiceStatus(Long invoiceId, InvoiceStatus newStatus) {
        log.debug("Changing invoice status to {} for ID: {}", newStatus, invoiceId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        invoice.setStatus(newStatus);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        log.info("Invoice status changed to {} for ID: {}", newStatus, updatedInvoice.getId());
        return updatedInvoice;
    }

    // Private helper methods

    private void updateInvoiceFields(Invoice existingInvoice, InvoiceRequestDTO request) {
        // Map updated fields from DTO to existing entity
        Invoice updatedData = invoiceMapper.toEntity(request);
        
        existingInvoice.setDocumentType(updatedData.getDocumentType());
        existingInvoice.setDocumentNumber(updatedData.getDocumentNumber());
        existingInvoice.setReceiverTaxId(updatedData.getReceiverTaxId());
        existingInvoice.setReceiverTaxIdWithoutCheckDigit(updatedData.getReceiverTaxIdWithoutCheckDigit());
        existingInvoice.setReceiverBusinessName(updatedData.getReceiverBusinessName());
        existingInvoice.setSenderTaxId(updatedData.getSenderTaxId());
        existingInvoice.setSenderTaxIdWithoutCheckDigit(updatedData.getSenderTaxIdWithoutCheckDigit());
        existingInvoice.setSenderBusinessName(updatedData.getSenderBusinessName());
        existingInvoice.setRelatedDocumentNumber(updatedData.getRelatedDocumentNumber());
        existingInvoice.setAmount(updatedData.getAmount());
        existingInvoice.setIssueDate(updatedData.getIssueDate());
        existingInvoice.setDueDate(updatedData.getDueDate());
    }

    private void updateInvoiceItems(Invoice existingInvoice, InvoiceRequestDTO request) {
        // Clear existing items
        invoiceItemRepository.deleteAll(existingInvoice.getItems());
        existingInvoice.getItems().clear();

        // Add new item
        InvoiceItem newItem = invoiceMapper.toItemEntity(request.getInvoiceItem());
        newItem.setInvoice(existingInvoice);
        invoiceItemRepository.save(newItem);
        existingInvoice.addItem(newItem);
    }

    /**
     * Validates invoice data before processing
     */
    private void validateInvoiceData(InvoiceRequestDTO invoiceDto) {
        if (invoiceDto.getDocumentNumber() == null || invoiceDto.getDocumentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Document number is required");
        }
        if (invoiceDto.getAmount() == null || invoiceDto.getAmount().trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (invoiceDto.getSenderTaxId() == null || invoiceDto.getSenderTaxId().trim().isEmpty()) {
            throw new IllegalArgumentException("Sender tax ID is required");
        }
    }
}
