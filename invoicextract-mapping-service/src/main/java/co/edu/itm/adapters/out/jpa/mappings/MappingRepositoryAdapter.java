package co.edu.itm.adapters.out.jpa.mappings;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import co.edu.itm.adapters.out.jpa.mappings.repo.*;
import co.edu.itm.adapters.out.jpa.mappings.entity.*;
import co.edu.itm.adapters.out.jpa.mappings.mapper.MappingEntityMapper;
import co.edu.itm.domain.model.FieldMapping;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class MappingRepositoryAdapter implements MappingRepositoryPort {
  private final ErpJpaRepository erpRepo;
  private final FieldMappingJpaRepository fmRepo;
  private final MappingEntityMapper mapper;
  public MappingRepositoryAdapter(ErpJpaRepository e, FieldMappingJpaRepository f, MappingEntityMapper m){
    this.erpRepo=e; this.fmRepo=f; this.mapper=m;
  }
  @Override
  @Cacheable(value="mappingsByErp", key="#erpName")
  public List<FieldMapping> findActiveByErpName(String erpName){
    Long erpId = erpRepo.findByNameIgnoreCase(erpName).orElseThrow().getId();
    return fmRepo.findByErpIdAndStatus(erpId, "ACTIVE").stream().map(mapper::toDomain).toList();
  }
  @Override
  public boolean existsActiveByErpAndSource(Long erpId, String sourceField){
    return fmRepo.existsByErpIdAndSourceFieldIgnoreCase(erpId, sourceField);
  }
  @Override
  @CacheEvict(value="mappingsByErp", key="#erpName")
  public void invalidateCacheForErp(String erpName){}
}
