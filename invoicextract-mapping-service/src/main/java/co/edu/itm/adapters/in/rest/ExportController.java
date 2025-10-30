package co.edu.itm.adapters.in.rest;

import co.edu.itm.application.usecase.ExportInvoicesUseCase;
import co.edu.itm.domain.ports.ExportServicePort;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ExportController.class);
    private final ExportInvoicesUseCase usecase;
    private final ExportServicePort exporter;
    private final MappingRepositoryPort mappingPort;

    public ExportController(ExportInvoicesUseCase u, ExportServicePort e, MappingRepositoryPort m) {
        this.usecase = u;
        this.exporter = e;
        this.mappingPort = m;
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
                                  @RequestParam(defaultValue = "false") boolean flatten,
                                  @RequestParam(required = false, defaultValue = "false") boolean refresh) {
      long start = System.currentTimeMillis();
      if (refresh) {
          log.info("[export] refresh=true -> evict cache for erp={}", erp);
          mappingPort.invalidateCacheForErp(erp);
      }
      log.info("[export] start erp={}, format={}, flatten={}", erp, format, flatten);
      List<Map<String, Object>> rows = usecase.exportMapped(erp, flatten);
      log.info("[export] mapped rows size={} (elapsed {} ms)", rows.size(), (System.currentTimeMillis() - start));
      if (!rows.isEmpty()) {
          Map<String, Object> first = rows.get(0);
          log.debug("[export] first row keys sample={}", first.keySet());
      }
      if ("csv".equalsIgnoreCase(format)) {
          byte[] csv = exporter.toCsv(rows);
          log.info("[export] responding CSV bytes={} (rows={})", csv.length, rows.size());
          return ResponseEntity.ok()
                  .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.csv")
                  .contentType(MediaType.TEXT_PLAIN)
                  .body(csv);
      }
      String json = exporter.toJson(rows);
      log.info("[export] responding JSON length={} (rows={})", json.length(), rows.size());
      return ResponseEntity.ok(json);
  }
}
