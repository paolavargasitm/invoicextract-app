package co.edu.itm.invoiceextract.application.service;

import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceMetadata;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import co.edu.itm.invoiceextract.domain.repository.InvoiceRepository;
import co.edu.itm.invoiceextract.domain.repository.InvoiceMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMetadataRepository metadataRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceMetadataRepository metadataRepository) {
        this.invoiceRepository = invoiceRepository;
        this.metadataRepository = metadataRepository;
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
                    // Updated to remove references to email and date which were moved to metadata
                    invoice.setStatus(invoiceDetails.getStatus());
                    invoice.setType(invoiceDetails.getType());
                    invoice.setFileUrl(invoiceDetails.getFileUrl());
                    
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
    
    public InvoiceMetadata saveMetadata(InvoiceMetadata metadata) {
        return metadataRepository.save(metadata);
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
