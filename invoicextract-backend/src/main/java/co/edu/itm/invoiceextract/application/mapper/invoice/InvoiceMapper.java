package co.edu.itm.invoiceextract.application.mapper.invoice;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceDetailDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceItemDTO;
import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceItem;
import co.edu.itm.invoiceextract.domain.enums.InvoiceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "documentType", source = "documentType")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "receiverTaxId", source = "receiverTaxId")
    @Mapping(target = "receiverTaxIdWithoutCheckDigit", source = "receiverTaxIdWithoutCheckDigit")
    @Mapping(target = "receiverBusinessName", source = "receiverBusinessName")
    @Mapping(target = "senderTaxId", source = "senderTaxId")
    @Mapping(target = "senderTaxIdWithoutCheckDigit", source = "senderTaxIdWithoutCheckDigit")
    @Mapping(target = "senderBusinessName", source = "senderBusinessName")
    @Mapping(target = "relatedDocumentNumber", source = "relatedDocumentNumber")
    @Mapping(target = "invoicePathPDF", source = "invoicePathPDF")
    @Mapping(target = "invoicePathXML", source = "invoicePathXML")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "parseAmount")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    Invoice toEntity(InvoiceRequestDTO dto);

    @Mapping(target = "itemCode", source = "itemCode")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "subtotal", source = "subtotal")
    @Mapping(target = "taxAmount", source = "taxAmount")
    @Mapping(target = "total", source = "total")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    InvoiceItem toItemEntity(InvoiceItemDTO dto);

    default InvoiceType mapDocumentType(String documentType) {
        if (documentType == null) return InvoiceType.FACTURA;
        try {
            return InvoiceType.valueOf(documentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InvoiceType.FACTURA;
        }
    }

    @org.mapstruct.Named("parseAmount")
    default BigDecimal parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) return null;
        try {
            return new BigDecimal(amount.replaceAll("[^\\d.,-]", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Entity to DTO mappings for responses
    @Mapping(target = "id", source = "id")
    @Mapping(target = "documentType", source = "documentType")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "receiverTaxId", source = "receiverTaxId")
    @Mapping(target = "receiverBusinessName", source = "receiverBusinessName")
    @Mapping(target = "senderTaxId", source = "senderTaxId")
    @Mapping(target = "senderBusinessName", source = "senderBusinessName")
    @Mapping(target = "invoicePathPDF", source = "invoicePathPDF")
    @Mapping(target = "invoicePathXML", source = "invoicePathXML")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "issueDate", source = "issueDate")
    @Mapping(target = "dueDate", source = "dueDate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "modifiedDate", source = "modifiedDate")
    @Mapping(target = "items", source = "items")
    InvoiceDetailDTO toDetailDTO(Invoice entity);

    @Mapping(target = "itemCode", source = "itemCode")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "subtotal", source = "subtotal")
    @Mapping(target = "taxAmount", source = "taxAmount")
    @Mapping(target = "total", source = "total")
    InvoiceItemDTO toItemDTO(InvoiceItem entity);
}
