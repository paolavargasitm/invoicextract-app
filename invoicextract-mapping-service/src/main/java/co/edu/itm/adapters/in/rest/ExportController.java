package co.edu.itm.adapters.in.rest;
import co.edu.itm.application.usecase.ExportInvoicesUseCase;
import co.edu.itm.domain.ports.ExportServicePort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/export")
public class ExportController {
  private final ExportInvoicesUseCase usecase;
  private final ExportServicePort exporter;
  public ExportController(ExportInvoicesUseCase u, ExportServicePort e){ this.usecase=u; this.exporter=e; }
  @GetMapping
  public ResponseEntity<?> export(@RequestParam String erp, @RequestParam(defaultValue="json") String format){
    List<Map<String,Object>> rows = usecase.exportMapped(erp);
    if("csv".equalsIgnoreCase(format)){
      byte[] csv = exporter.toCsv(rows);
      return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export.csv")
        .contentType(MediaType.TEXT_PLAIN)
        .body(csv);
    }
    return ResponseEntity.ok(exporter.toJson(rows));
  }
}
