package co.edu.itm.invoiceextract.infrastructure.messaging;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.usecase.invoice.ManageInvoiceUseCase;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import co.edu.itm.invoiceextract.infrastructure.messaging.mapper.InvoiceMessageMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InvoiceMessagingAdapter {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceMessagingAdapter.class);
    private final ManageInvoiceUseCase manageInvoiceUseCase;
    private final InvoiceMessageMapper invoiceMessageMapper;

    public void processInvoice(InvoiceMessage message) {
        logger.info("Mapping and saving invoice from message for sender: {}", message.getSenderTaxId());
        InvoiceRequestDTO invoice = invoiceMessageMapper.toDto(message);
        Invoice savedInvoice = manageInvoiceUseCase.createInvoice(invoice);

        logger.info("Invoice with ID {} saved successfully.", savedInvoice.getId());
    }
}
