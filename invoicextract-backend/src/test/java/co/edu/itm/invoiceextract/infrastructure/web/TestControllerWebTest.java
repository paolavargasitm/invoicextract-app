package co.edu.itm.invoiceextract.infrastructure.web;

import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
@AutoConfigureMockMvc(addFilters = false)
class TestControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, InvoiceMessage> kafkaTemplate;

    @Test
    void should_send_message_to_kafka_when_posting_invoice() throws Exception {
        InvoiceMessage message = new InvoiceMessage();
        message.setEmail("john@example.com");

        mockMvc.perform(post("/api/test/send-invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk());

        verify(kafkaTemplate).send(eq("invoices"), any(InvoiceMessage.class));
    }
}
