package co.edu.itm.adapters.in.rest;

import co.edu.itm.adapters.in.rest.dto.CreateMappingRequest;
import co.edu.itm.adapters.in.rest.dto.MappingResponse;
import co.edu.itm.adapters.in.rest.dto.UpdateMappingRequest;
import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.net.URI;
import co.edu.itm.infra.web.NotFoundException;

@RestController
@RequestMapping("/api/mappings")
@Tag(name = "Mappings", description = "Gestión de reglas de mapeo por ERP")
public class MappingController {
    private final ErpJpaRepository erpRepo;
    private final FieldMappingJpaRepository fmRepo;

    public MappingController(ErpJpaRepository erpRepo, FieldMappingJpaRepository fmRepo) {
        this.erpRepo = erpRepo;
        this.fmRepo = fmRepo;
    }

    @GetMapping
    @Operation(summary = "Listar reglas de mapeo")
    @ApiResponse(responseCode = "200", description = "Listado de reglas")
    @ApiResponse(responseCode = "404", description = "ERP no encontrado")
    public List<MappingResponse> list(@RequestParam String erp, @RequestParam(defaultValue = "ACTIVE") String status) {
        Long erpId = erpRepo.findByNameIgnoreCase(erp).orElseThrow(() -> new NotFoundException("ERP not found: " + erp)).getId();
        return fmRepo.findByErpIdAndStatus(erpId, status).stream()
                .map(e -> new MappingResponse(e.getId(), e.getErpId(), e.getSourceField(), e.getTargetField(),
                        e.getTransformFn(), e.getStatus(), e.getVersion(),
                        e.getCreatedAt() == null ? null : e.getCreatedAt().toString(),
                        e.getUpdatedAt() == null ? null : e.getUpdatedAt().toString()))
                .toList();
    }

    @PostMapping
    @Operation(summary = "Crear regla de mapeo")
    @ApiResponse(responseCode = "200", description = "Regla creada")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "404", description = "ERP no encontrado")
    @ApiResponse(responseCode = "409", description = "Conflicto de datos")
    public ResponseEntity<MappingResponse> create(@Validated @RequestBody CreateMappingRequest req) {
        var erp = erpRepo.findByNameIgnoreCase(req.erpName()).orElseThrow(() -> new NotFoundException("ERP not found: " + req.erpName()));
        var now = Instant.now();
        var e = fmRepo.save(FieldMappingEntity.builder()
                .erpId(erp.getId())
                .sourceField(req.sourceField())
                .targetField(req.targetField())
                .transformFn(req.transformFn())
                .status(req.status() == null ? "ACTIVE" : req.status())
                .version(1)
                .createdAt(now).updatedAt(now)
                .build());
        var location = URI.create("/api/mappings/" + e.getId());
        return ResponseEntity.created(location).body(new MappingResponse(e.getId(), e.getErpId(), e.getSourceField(), e.getTargetField(),
                e.getTransformFn(), e.getStatus(), e.getVersion(), e.getCreatedAt().toString(), e.getUpdatedAt().toString()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar regla de mapeo")
    @ApiResponse(responseCode = "200", description = "Regla actualizada")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "404", description = "Regla no encontrada")
    public ResponseEntity<MappingResponse> update(@PathVariable Long id, @Validated @RequestBody UpdateMappingRequest req) {
        var e = fmRepo.findById(id).orElseThrow(() -> new NotFoundException("Mapping not found: " + id));
        if (req.sourceField() != null) e.setSourceField(req.sourceField());
        if (req.targetField() != null) e.setTargetField(req.targetField());
        if (req.transformFn() != null) e.setTransformFn(req.transformFn());
        if (req.status() != null) e.setStatus(req.status());
        e.setUpdatedAt(Instant.now());
        fmRepo.save(e);
        return ResponseEntity.ok(new MappingResponse(e.getId(), e.getErpId(), e.getSourceField(), e.getTargetField(),
                e.getTransformFn(), e.getStatus(), e.getVersion(), e.getCreatedAt().toString(), e.getUpdatedAt().toString()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de la regla")
    @ApiResponse(responseCode = "204", description = "Estado actualizado")
    @ApiResponse(responseCode = "404", description = "Regla no encontrada")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        var e = fmRepo.findById(id).orElseThrow(() -> new NotFoundException("Mapping not found: " + id));
        e.setStatus(status);
        e.setUpdatedAt(Instant.now());
        fmRepo.save(e);
        return ResponseEntity.noContent().build();
    }
}
