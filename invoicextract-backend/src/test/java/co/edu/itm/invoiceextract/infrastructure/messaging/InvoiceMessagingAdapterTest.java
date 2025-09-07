package co.edu.itm.invoiceextract.infrastructure.messaging;

import co.edu.itm.invoiceextract.application.dto.invoice.InvoiceRequestDTO;
import co.edu.itm.invoiceextract.application.usecase.invoice.ManageInvoiceUseCase;
import co.edu.itm.invoiceextract.domain.entity.invoice.Invoice;
import co.edu.itm.invoiceextract.infrastructure.messaging.dto.InvoiceMessage;
import co.edu.itm.invoiceextract.infrastructure.messaging.mapper.InvoiceMessageMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvoiceMessagingAdapterTest {

    @Mock
    private ManageInvoiceUseCase manageInvoiceUseCase;

    @Mock
    private InvoiceMessageMapper invoiceMessageMapper;

    @InjectMocks
    private InvoiceMessagingAdapter adapter;

    @Test
    @DisplayName("should_map_message_and_create_invoice")
    void should_map_message_and_create_invoice() {
        // Given
        InvoiceMessage message = new InvoiceMessage();
        message.setSenderTaxId("123");
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setDocumentNumber("INV-001");
        given(invoiceMessageMapper.toDto(any(InvoiceMessage.class))).willReturn(dto);
        given(manageInvoiceUseCase.createInvoice(any(InvoiceRequestDTO.class))).willReturn(new Invoice());

        // When
        adapter.processInvoice(message);

        // Then
        verify(invoiceMessageMapper).toDto(any(InvoiceMessage.class));
        verify(manageInvoiceUseCase).createInvoice(any(InvoiceRequestDTO.class));
    }
}
