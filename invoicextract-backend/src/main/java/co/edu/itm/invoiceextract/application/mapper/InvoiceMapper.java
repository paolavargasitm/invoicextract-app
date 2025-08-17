package co.edu.itm.invoiceextract.application.mapper;

import co.edu.itm.invoiceextract.application.dto.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.dto.InvoiceMetadataRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceMetadata;
import co.edu.itm.invoiceextract.domain.enums.InvoiceStatus;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public Invoice toEntity(InvoiceRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Invoice invoice = new Invoice();
        invoice.setStatus(dto.getStatus() != null ? dto.getStatus() : InvoiceStatus.PENDING);
        invoice.setType(dto.getType());
        
        // Convert metadata if present
        if (dto.getMetadata() != null) {
            InvoiceMetadata metadata = toMetadataEntity(dto.getMetadata());
            
            if (dto.getEmail() != null && metadata.getCustomerEmail() == null) {
                metadata.setCustomerEmail(dto.getEmail());
            }
            if (dto.getDate() != null && metadata.getIssueDate() == null) {
                metadata.setIssueDate(dto.getDate().toLocalDate());
            }
            
            invoice.setMetadata(metadata);
        } else if (dto.getEmail() != null || dto.getDate() != null) {
            
            InvoiceMetadata metadata = new InvoiceMetadata();
            if (dto.getEmail() != null) {
                metadata.setCustomerEmail(dto.getEmail());
            }
            if (dto.getDate() != null) {
                metadata.setIssueDate(dto.getDate().toLocalDate());
            }
            invoice.setMetadata(metadata);
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
        dto.setStatus(entity.getStatus());
        dto.setType(entity.getType());
        
        // Convert metadata if present
        if (entity.getMetadata() != null) {
            dto.setEmail(entity.getMetadata().getCustomerEmail());
            if (entity.getMetadata().getIssueDate() != null) {
                dto.setDate(entity.getMetadata().getIssueDate().atStartOfDay());
            }
            
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
