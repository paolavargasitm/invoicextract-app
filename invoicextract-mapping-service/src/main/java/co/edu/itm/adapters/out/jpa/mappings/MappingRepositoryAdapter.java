package co.edu.itm.adapters.out.jpa.mappings;

import co.edu.itm.adapters.out.jpa.mappings.mapper.MappingEntityMapper;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import co.edu.itm.domain.model.FieldMapping;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MappingRepositoryAdapter implements MappingRepositoryPort {
    private static final Logger log = LoggerFactory.getLogger(MappingRepositoryAdapter.class);
    private final ErpJpaRepository erpRepo;
    private final FieldMappingJpaRepository fmRepo;
    private final MappingEntityMapper mapper;

    public MappingRepositoryAdapter(ErpJpaRepository e, FieldMappingJpaRepository f, MappingEntityMapper m) {
        this.erpRepo = e;
        this.fmRepo = f;
        this.mapper = m;
    }

    @Override
    @Cacheable(value = "mappingsByErp", key = "#erpName.toLowerCase()")
    public List<FieldMapping> findActiveByErpName(String erpName) {
        Long erpId = erpRepo.findByNameIgnoreCase(erpName).orElseThrow().getId();
        var list = fmRepo.findByErpIdAndStatus(erpId, "ACTIVE").stream().map(mapper::toDomain).toList();
        log.info("[repo] findActiveByErpName erpName={} (id={}) -> {} active rules", erpName, erpId, list.size());
        return list;
    }

    @Override
    public boolean existsActiveByErpAndSource(Long erpId, String sourceField) {
        return fmRepo.existsByErpIdAndSourceFieldIgnoreCase(erpId, sourceField);
    }

    @Override
    @CacheEvict(value = "mappingsByErp", key = "#erpName.toLowerCase()")
    public void invalidateCacheForErp(String erpName) {
    }
}
