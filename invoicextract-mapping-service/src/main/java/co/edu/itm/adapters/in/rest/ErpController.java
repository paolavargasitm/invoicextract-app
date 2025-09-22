package co.edu.itm.adapters.in.rest;

import co.edu.itm.adapters.in.rest.dto.CreateErpRequest;
import co.edu.itm.adapters.in.rest.dto.ErpResponse;
import co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/erps")
@Tag(name = "ERPs", description = "Gestión de ERPs")
public class ErpController {
    private final ErpJpaRepository erpRepo;

    public ErpController(ErpJpaRepository erpRepo) {
        this.erpRepo = erpRepo;
    }

    @PostMapping
    @Operation(summary = "Crear ERP")
    @ApiResponse(responseCode = "200", description = "ERP creado")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "409", description = "Conflicto de datos")
    public ResponseEntity<ErpResponse> create(@Validated @RequestBody CreateErpRequest req) {
        var now = Instant.now();
        var e = erpRepo.save(ErpEntity.builder().name(req.name()).status("ACTIVE").createdAt(now).updatedAt(now).build());
        return ResponseEntity.ok(new ErpResponse(e.getId(), e.getName(), e.getStatus(), e.getCreatedAt().toString(), e.getUpdatedAt().toString()));
    }

    @GetMapping
    @Operation(summary = "Listar ERPs")
    @ApiResponse(responseCode = "200", description = "Listado de ERPs")
    public List<ErpResponse> list() {
        return erpRepo.findAll().stream().map(e -> new ErpResponse(e.getId(), e.getName(), e.getStatus(),
                e.getCreatedAt() == null ? null : e.getCreatedAt().toString(),
                e.getUpdatedAt() == null ? null : e.getUpdatedAt().toString()
        )).toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado del ERP")
    @ApiResponse(responseCode = "204", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "ERP no encontrado")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        var e = erpRepo.findById(id).orElseThrow();
        e.setStatus(status);
        e.setUpdatedAt(Instant.now());
        erpRepo.save(e);
        return ResponseEntity.noContent().build();
    }
}
