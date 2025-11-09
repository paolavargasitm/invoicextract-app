package co.edu.itm.invoiceextract.infrastructure.messaging.mapper;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceItemMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMessageMapper {

    @Mapping(target = "fileUrl", source = "fileUrl")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "date", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "documentType", source = "documentType")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "receiverTaxId", source = "receiverTaxId")
    @Mapping(target = "receiverTaxIdWithoutCheckDigit", source = "receiverTaxIdWithoutCheckDigit")
    @Mapping(target = "receiverBusinessName", source = "receiverBusinessName")
    @Mapping(target = "senderTaxId", source = "senderTaxId")
    @Mapping(target = "senderTaxIdWithoutCheckDigit", source = "senderTaxIdWithoutCheckDigit")
    @Mapping(target = "senderBusinessName", source = "senderBusinessName")
    @Mapping(target = "relatedDocumentNumber", source = "relatedDocumentNumber")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "invoiceItem", source = "invoiceItem")
    InvoiceMessage toMessage(InvoiceRequestDTO dto);

    // Direct mapping back to DTO - all fields map 1:1
    @Mapping(target = "documentType", source = "documentType")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "receiverTaxId", source = "receiverTaxId")
    @Mapping(target = "receiverTaxIdWithoutCheckDigit", source = "receiverTaxIdWithoutCheckDigit")
    @Mapping(target = "receiverBusinessName", source = "receiverBusinessName")
    @Mapping(target = "senderTaxId", source = "senderTaxId")
    @Mapping(target = "senderTaxIdWithoutCheckDigit", source = "senderTaxIdWithoutCheckDigit")
    @Mapping(target = "senderBusinessName", source = "senderBusinessName")
    @Mapping(target = "relatedDocumentNumber", source = "relatedDocumentNumber")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "invoiceItem", source = "invoiceItem")
    InvoiceRequestDTO toDto(InvoiceMessage message);

    // Direct mapping for invoice items
    @Mapping(target = "itemCode", source = "itemCode")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "subtotal", source = "subtotal")
    @Mapping(target = "taxAmount", source = "taxAmount")
    @Mapping(target = "total", source = "total")
    InvoiceItemDTO toInvoiceItemDto(InvoiceItemMessage itemMessage);

    @Mapping(target = "itemCode", source = "itemCode")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "subtotal", source = "subtotal")
    @Mapping(target = "taxAmount", source = "taxAmount")
    @Mapping(target = "total", source = "total")
    InvoiceItemMessage toInvoiceItemMessage(InvoiceItemDTO itemDto);
}
