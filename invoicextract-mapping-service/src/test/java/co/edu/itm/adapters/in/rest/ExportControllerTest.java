package co.edu.itm.adapters.in.rest;

import co.edu.itm.application.usecase.ExportInvoicesUseCase;
import co.edu.itm.domain.ports.ExportServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportControllerTest {

    @Test
    void export_json_returnsJsonStringBody() {
        ExportInvoicesUseCase useCase = mock(ExportInvoicesUseCase.class);
        ExportServicePort exporter = mock(ExportServicePort.class);
        when(useCase.exportMapped("SAP", false)).thenReturn(List.of(Map.of("k", "v")));
        when(exporter.toJson(anyList())).thenReturn("[{\"k\":\"v\"}]");

        ExportController controller = new ExportController(useCase, exporter);
        ResponseEntity<?> resp = controller.export("SAP", "json", false);

        assertEquals(200, resp.getStatusCode().value());
        assertEquals("[{\"k\":\"v\"}]", resp.getBody());
        verify(useCase).exportMapped("SAP", false);
        verify(exporter).toJson(anyList());
    }

    @Test
    void export_csv_returnsBytesWithHeader() {
        ExportInvoicesUseCase useCase = mock(ExportInvoicesUseCase.class);
        ExportServicePort exporter = mock(ExportServicePort.class);
        when(useCase.exportMapped("SAP", true)).thenReturn(List.of(Map.of("a", 1)));
        when(exporter.toCsv(anyList())).thenReturn("a\n1\n".getBytes(StandardCharsets.UTF_8));

        ExportController controller = new ExportController(useCase, exporter);
        ResponseEntity<?> resp = controller.export("SAP", "csv", true);

        assertEquals(200, resp.getStatusCode().value());
        assertTrue(resp.getHeaders().getFirst("Content-Disposition").contains("export.csv"));
        assertArrayEquals("a\n1\n".getBytes(StandardCharsets.UTF_8), (byte[]) resp.getBody());
        verify(useCase).exportMapped("SAP", true);
        verify(exporter).toCsv(anyList());
    }
}
