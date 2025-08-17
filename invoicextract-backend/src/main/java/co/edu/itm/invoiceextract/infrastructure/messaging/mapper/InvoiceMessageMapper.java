package co.edu.itm.invoiceextract.infrastructure.messaging.mapper;

import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.domain.entity.invoice.InvoiceMetadata;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMetadataMessage;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InvoiceMessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    Invoice toInvoiceEntity(InvoiceMessage message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    InvoiceMetadata toInvoiceMetadataEntity(InvoiceMetadataMessage metadataMessage);

    @AfterMapping
    default void linkMetadata(@MappingTarget Invoice invoice) {
        if (invoice.getMetadata() != null) {
            invoice.getMetadata().setInvoice(invoice);
        }
    }
}
