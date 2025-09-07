package co.edu.itm.invoiceextract.infrastructure.messaging;

import co.edu.itm.invoiceextract.infrastructure.errors.ProcessingErrorLog;
import co.edu.itm.invoiceextract.infrastructure.errors.ProcessingErrorLogRepository;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceListenerTest {

    @Mock
    private InvoiceMessagingAdapter invoiceMessagingAdapter;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProcessingErrorLogRepository errorLogRepository;

    @InjectMocks
    private InvoiceListener listener;

    @Test
    @DisplayName("should_process_message_successfully")
    void should_process_message_successfully() throws Exception {
        // Given a valid JSON payload
        String json = "{\"email\":\"john@example.com\"}";
        InvoiceMessage msg = new InvoiceMessage();
        msg.setEmail("john@example.com");
        given(objectMapper.readValue(json, InvoiceMessage.class)).willReturn(msg);

        // When
        listener.listen(json, "invoices", 0, 123L);

        // Then
        verify(invoiceMessagingAdapter).processInvoice(any(InvoiceMessage.class));
        verify(errorLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_save_error_log_when_processing_fails")
    void should_save_error_log_when_processing_fails() throws Exception {
        // Given JSON that causes processing exception
        String json = "{\"email\":\"bad@example.com\"}";
        InvoiceMessage msg = new InvoiceMessage();
        msg.setEmail("bad@example.com");
        given(objectMapper.readValue(json, InvoiceMessage.class)).willReturn(msg);
        doThrow(new RuntimeException("boom")).when(invoiceMessagingAdapter).processInvoice(any(InvoiceMessage.class));

        // When
        listener.listen(json, "invoices", 1, 456L);

        // Then
        ArgumentCaptor<ProcessingErrorLog> captor = ArgumentCaptor.forClass(ProcessingErrorLog.class);
        verify(errorLogRepository).save(captor.capture());
        ProcessingErrorLog saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo("invoices");
        assertThat(saved.getKafkaPartition()).isEqualTo(1);
        assertThat(saved.getKafkaOffset()).isEqualTo(456L);
        assertThat(saved.getRawMessage()).contains("bad@example.com");
        assertThat(saved.getErrorType()).isEqualTo("RuntimeException");
        assertThat(saved.getErrorMessage()).contains("boom");
    }

    @Test
    @DisplayName("should_save_error_log_when_deserialization_fails")
    void should_save_error_log_when_deserialization_fails() throws Exception {
        // Given ObjectMapper throws
        String json = "{not-json}";
        given(objectMapper.readValue(json, InvoiceMessage.class)).willThrow(new RuntimeException("json error"));

        // When
        listener.listen(json, "invoices", 2, 789L);

        // Then
        ArgumentCaptor<ProcessingErrorLog> captor = ArgumentCaptor.forClass(ProcessingErrorLog.class);
        verify(errorLogRepository).save(captor.capture());
        ProcessingErrorLog saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo("invoices");
        assertThat(saved.getKafkaPartition()).isEqualTo(2);
        assertThat(saved.getKafkaOffset()).isEqualTo(789L);
        assertThat(saved.getRawMessage()).contains("{not-json}");
        assertThat(saved.getErrorType()).isEqualTo("RuntimeException");
        assertThat(saved.getErrorMessage()).contains("json error");
    }
}
