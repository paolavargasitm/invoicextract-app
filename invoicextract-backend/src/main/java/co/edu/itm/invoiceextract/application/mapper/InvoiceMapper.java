package co.edu.itm.invoiceextract.application.mapper;

import co.edu.itm.invoiceextract.application.dto.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.dto.InvoiceMetadataRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.Invoice;
import co.edu.itm.invoiceextract.domain.entity.InvoiceMetadata;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public Invoice toEntity(InvoiceRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Invoice invoice = new Invoice();
        invoice.setEmail(dto.getEmail());
        invoice.setDate(dto.getDate());
        invoice.setStatus(dto.getStatus() != null ? dto.getStatus() : InvoiceStatus.PENDING);
        invoice.setType(dto.getType());

        // Convert metadata if present
        if (dto.getMetadata() != null) {
            InvoiceMetadata metadata = toMetadataEntity(dto.getMetadata());
            invoice.setMetadata(metadata);

            // Lift important fields from metadata to the main invoice entity
            invoice.setProvider(metadata.getSupplierName());
            invoice.setAmount(metadata.getTotalAmount());
            invoice.setCurrency(metadata.getCurrency());
        }

        return invoice;
    }

    public InvoiceMetadata toMetadataEntity(InvoiceMetadataRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        InvoiceMetadata metadata = new InvoiceMetadata();
        metadata.setInvoiceNumber(dto.getInvoiceNumber());
        metadata.setCustomerName(dto.getCustomerName());
        metadata.setCustomerEmail(dto.getCustomerEmail());
        metadata.setCustomerAddress(dto.getCustomerAddress());
        metadata.setSupplierName(dto.getSupplierName());
        metadata.setSupplierEmail(dto.getSupplierEmail());
        metadata.setSupplierAddress(dto.getSupplierAddress());
        metadata.setAmount(dto.getAmount());
        metadata.setCurrency(dto.getCurrency());
        metadata.setTaxAmount(dto.getTaxAmount());
        metadata.setSubtotal(dto.getSubtotal());
        metadata.setTotalAmount(dto.getTotalAmount());
        metadata.setIssueDate(dto.getIssueDate());
        metadata.setDueDate(dto.getDueDate());
        metadata.setPaymentTerms(dto.getPaymentTerms());
        metadata.setDescription(dto.getDescription());
        metadata.setNotes(dto.getNotes());
        metadata.setPdfUrl(dto.getPdfUrl());
        metadata.setOriginalFilename(dto.getOriginalFilename());
        metadata.setFileSize(dto.getFileSize());
        metadata.setExtractedData(dto.getExtractedData());
        metadata.setConfidenceScore(dto.getConfidenceScore());
        metadata.setProcessingStatus(dto.getProcessingStatus());

        return metadata;
    }

    public InvoiceRequestDTO toDto(Invoice entity) {
        if (entity == null) {
            return null;
        }

        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setEmail(entity.getEmail());
        dto.setDate(entity.getDate());
        dto.setStatus(entity.getStatus());
        dto.setType(entity.getType());

        // Convert metadata if present
        if (entity.getMetadata() != null) {
            InvoiceMetadataRequestDTO metadataDto = toMetadataDto(entity.getMetadata());
            dto.setMetadata(metadataDto);
        }

        return dto;
    }

    public InvoiceMetadataRequestDTO toMetadataDto(InvoiceMetadata entity) {
        if (entity == null) {
            return null;
        }

        InvoiceMetadataRequestDTO dto = new InvoiceMetadataRequestDTO();
        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setCustomerName(entity.getCustomerName());
        dto.setCustomerEmail(entity.getCustomerEmail());
        dto.setCustomerAddress(entity.getCustomerAddress());
        dto.setSupplierName(entity.getSupplierName());
        dto.setSupplierEmail(entity.getSupplierEmail());
        dto.setSupplierAddress(entity.getSupplierAddress());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setSubtotal(entity.getSubtotal());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setIssueDate(entity.getIssueDate());
        dto.setDueDate(entity.getDueDate());
        dto.setPaymentTerms(entity.getPaymentTerms());
        dto.setDescription(entity.getDescription());
        dto.setNotes(entity.getNotes());
        dto.setPdfUrl(entity.getPdfUrl());
        dto.setOriginalFilename(entity.getOriginalFilename());
        dto.setFileSize(entity.getFileSize());
        dto.setExtractedData(entity.getExtractedData());
        dto.setConfidenceScore(entity.getConfidenceScore());
        dto.setProcessingStatus(entity.getProcessingStatus());

        return dto;
    }
}
