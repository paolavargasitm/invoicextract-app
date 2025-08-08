package co.edu.itm.invoiceextract.infrastructure.messaging;

import co.edu.itm.invoiceextract.infrastructure.errors.ProcessingErrorLog;
import co.edu.itm.invoiceextract.infrastructure.errors.ProcessingErrorLogRepository;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceListener {

    private final InvoiceMessagingAdapter invoiceMessagingAdapter;
    private final ObjectMapper objectMapper;
    private final ProcessingErrorLogRepository errorLogRepository;

    @KafkaListener(topics = "invoices", groupId = "invoice-group")
    public void listen(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            log.info("Received message from topic {}-{} at offset {}: {}", topic, partition, offset, message);
            InvoiceMessage invoiceMessage = objectMapper.readValue(message, InvoiceMessage.class);
            invoiceMessagingAdapter.processInvoice(invoiceMessage);
            log.info("Successfully processed invoice message for email: {}", invoiceMessage.getEmail());
        } catch (Exception e) {
            log.error("Failed to process message from topic {}-{} at offset {}. Reason: {}. Storing to error log.",
                    topic, partition, offset, e.getMessage());
            saveErrorLog(message, topic, partition, offset, e);
        }
    }

    private void saveErrorLog(String message, String topic, int partition, long offset, Exception e) {
        ProcessingErrorLog errorLog = new ProcessingErrorLog();
        errorLog.setTopic(topic);
        errorLog.setKafkaPartition(partition);
        errorLog.setKafkaOffset(offset);
        errorLog.setRawMessage(message);
        errorLog.setErrorType(e.getClass().getSimpleName());
        errorLog.setErrorMessage(e.getMessage());
        errorLogRepository.save(errorLog);
    }
}
