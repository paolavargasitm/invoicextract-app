package co.edu.itm.adapters.in.rest;

import co.edu.itm.application.usecase.ExportInvoicesUseCase;
import co.edu.itm.domain.ports.ExportServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
@Tag(name = "Export", description = "Exporta facturas mapeadas en JSON o CSV")
public class ExportController {
    private final ExportInvoicesUseCase usecase;
    private final ExportServicePort exporter;

    public ExportController(ExportInvoicesUseCase u, ExportServicePort e) {
        this.usecase = u;
        this.exporter = e;
    }

    @GetMapping
    @Operation(summary = "Exportar facturas mapeadas",
            description = "Devuelve las facturas aprobadas mapeadas para el ERP indicado en formato JSON o CSV.")
    @ApiResponse(responseCode = "200", description = "Exportación generada",
            content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Object.class)),
                    @Content(mediaType = "text/plain")
            })
    @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content)
    @ApiResponse(responseCode = "409", description = "Conflicto de datos", content = @Content)
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    public ResponseEntity<?> export(@RequestParam String erp,
                                  @RequestParam(defaultValue = "json") String format,
                                  @RequestParam(defaultValue = "false") boolean flatten) {
      List<Map<String, Object>> rows = usecase.exportMapped(erp, flatten);
      if ("csv".equalsIgnoreCase(format)) {
          byte[] csv = exporter.toCsv(rows);
          return ResponseEntity.ok()
                  .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.csv")
                  .contentType(MediaType.TEXT_PLAIN)
                  .body(csv);
      }
      return ResponseEntity.ok(exporter.toJson(rows));
  }
}
