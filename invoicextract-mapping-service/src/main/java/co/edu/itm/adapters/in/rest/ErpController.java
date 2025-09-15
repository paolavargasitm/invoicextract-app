package co.edu.itm.adapters.in.rest;
import co.edu.itm.adapters.in.rest.dto.*;
import co.edu.itm.adapters.in.rest.dto.CreateErpRequest;
import co.edu.itm.adapters.in.rest.dto.ErpResponse;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import java.time.Instant;
import java.util.List;
@RestController
@RequestMapping("/api/erps")
public class ErpController {
  private final ErpJpaRepository erpRepo;
  public ErpController(ErpJpaRepository erpRepo){ this.erpRepo = erpRepo; }
  @PostMapping
  public ResponseEntity<ErpResponse> create(@Validated @RequestBody CreateErpRequest req){
    var now = Instant.now();
    var e = erpRepo.save(ErpEntity.builder().name(req.name()).status("ACTIVE").createdAt(now).updatedAt(now).build());
    return ResponseEntity.ok(new ErpResponse(e.getId(), e.getName(), e.getStatus(), e.getCreatedAt().toString(), e.getUpdatedAt().toString()));
  }
  @GetMapping
  public List<ErpResponse> list(){
    return erpRepo.findAll().stream().map(e-> new ErpResponse(e.getId(), e.getName(), e.getStatus(),
      e.getCreatedAt()==null?null:e.getCreatedAt().toString(),
      e.getUpdatedAt()==null?null:e.getUpdatedAt().toString()
    )).toList();
  }
  @PatchMapping("/{id}/status")
  public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam String status){
    var e = erpRepo.findById(id).orElseThrow();
    e.setStatus(status);
    e.setUpdatedAt(Instant.now());
    erpRepo.save(e);
    return ResponseEntity.noContent().build();
  }
}
