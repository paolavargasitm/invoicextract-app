package co.edu.itm.invoiceextract.infrastructure.messaging;

import co.edu.itm.invoiceextract.application.service.InvoiceService;
import co.edu.itm.invoiceextract.domain.entity.Invoice;
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
    private final InvoiceService invoiceService;
    private final InvoiceMessageMapper invoiceMessageMapper;

    public void processInvoice(InvoiceMessage message) {
        logger.info("Mapping and saving invoice from message for provider: {}", message.getProvider());
        Invoice invoice = invoiceMessageMapper.toInvoiceEntity(message);
        Invoice savedInvoice = invoiceService.save(invoice);
        logger.info("Invoice with ID {} saved successfully.", savedInvoice.getId());
    }
}
