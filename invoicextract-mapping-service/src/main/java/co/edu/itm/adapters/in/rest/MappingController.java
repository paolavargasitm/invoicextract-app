package co.edu.itm.adapters.in.rest;
import co.edu.itm.adapters.in.rest.dto.*;
import co.edu.itm.adapters.out.jpa.mappings.repo.*;
import co.edu.itm.adapters.out.jpa.mappings.entity.*;
import co.edu.itm.adapters.in.rest.dto.CreateMappingRequest;
import co.edu.itm.adapters.in.rest.dto.MappingResponse;
import co.edu.itm.adapters.in.rest.dto.UpdateMappingRequest;
import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import java.time.Instant;
import java.util.List;
@RestController
@RequestMapping("/api/mappings")
public class MappingController {
  private final ErpJpaRepository erpRepo;
  private final FieldMappingJpaRepository fmRepo;
  public MappingController(ErpJpaRepository erpRepo, FieldMappingJpaRepository fmRepo){
    this.erpRepo=erpRepo; this.fmRepo=fmRepo;
  }
  @GetMapping
  public List<MappingResponse> list(@RequestParam String erp, @RequestParam(defaultValue="ACTIVE") String status){
    Long erpId = erpRepo.findByNameIgnoreCase(erp).orElseThrow().getId();
    return fmRepo.findByErpIdAndStatus(erpId, status).stream()
      .map(e -> new MappingResponse(e.getId(), e.getErpId(), e.getSourceField(), e.getTargetField(),
                                    e.getTransformFn(), e.getStatus(), e.getVersion(),
                                    e.getCreatedAt()==null?null:e.getCreatedAt().toString(),
                                    e.getUpdatedAt()==null?null:e.getUpdatedAt().toString()))
      .toList();
  }
  @PostMapping
  public ResponseEntity<MappingResponse> create(@Validated @RequestBody CreateMappingRequest req){
    var erp = erpRepo.findByNameIgnoreCase(req.erpName()).orElseThrow();
    var now = Instant.now();
    var e = fmRepo.save(FieldMappingEntity.builder()
      .erpId(erp.getId())
      .sourceField(req.sourceField())
      .targetField(req.targetField())
      .transformFn(req.transformFn())
      .status(req.status()==null? "ACTIVE": req.status())
      .version(1)
      .createdAt(now).updatedAt(now)
      .build());
    return ResponseEntity.ok(new MappingResponse(e.getId(), e.getErpId(), e.getSourceField(), e.getTargetField(),
      e.getTransformFn(), e.getStatus(), e.getVersion(), e.getCreatedAt().toString(), e.getUpdatedAt().toString()));
  }
  @PutMapping("/{id}")
  public ResponseEntity<MappingResponse> update(@PathVariable Long id, @Validated @RequestBody UpdateMappingRequest req){
    var e = fmRepo.findById(id).orElseThrow();
    if(req.sourceField()!=null) e.setSourceField(req.sourceField());
    if(req.targetField()!=null) e.setTargetField(req.targetField());
    if(req.transformFn()!=null) e.setTransformFn(req.transformFn());
    if(req.status()!=null) e.setStatus(req.status());
    e.setUpdatedAt(Instant.now());
    fmRepo.save(e);
    return ResponseEntity.ok(new MappingResponse(e.getId(), e.getErpId(), e.getSourceField(), e.getTargetField(),
      e.getTransformFn(), e.getStatus(), e.getVersion(), e.getCreatedAt().toString(), e.getUpdatedAt().toString()));
  }
  @PatchMapping("/{id}/status")
  public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam String status){
    var e = fmRepo.findById(id).orElseThrow();
    e.setStatus(status);
    e.setUpdatedAt(Instant.now());
    fmRepo.save(e);
    return ResponseEntity.noContent().build();
  }
}
