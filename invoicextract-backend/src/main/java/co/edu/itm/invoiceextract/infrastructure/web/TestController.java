package co.edu.itm.invoiceextract.infrastructure.web;

import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@AllArgsConstructor
public class TestController {

    private final KafkaTemplate<String, InvoiceMessage> kafkaTemplate;

    @PostMapping("/send-invoice")
    public void sendInvoice(@RequestBody InvoiceMessage message) {
        kafkaTemplate.send("invoices", message);
    }
}
