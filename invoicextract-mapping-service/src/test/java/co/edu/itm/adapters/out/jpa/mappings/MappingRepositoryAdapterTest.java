package co.edu.itm.adapters.out.jpa.mappings;

import co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity;
import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import co.edu.itm.adapters.out.jpa.mappings.mapper.MappingEntityMapper;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import co.edu.itm.domain.model.FieldMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MappingRepositoryAdapterTest {

    private ErpJpaRepository erpRepo;
    private FieldMappingJpaRepository fmRepo;
    private MappingEntityMapper mapper;

    @BeforeEach
    void setup() {
        erpRepo = mock(ErpJpaRepository.class);
        fmRepo = mock(FieldMappingJpaRepository.class);
        mapper = mock(MappingEntityMapper.class);
    }

    @Test
    void findActiveByErpName_mapsEntitiesToDomain() {
        MappingRepositoryAdapter adapter = new MappingRepositoryAdapter(erpRepo, fmRepo, mapper);
        ErpEntity erp = ErpEntity.builder().id(1L).name("SAP").status("ACTIVE").createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(erpRepo.findByNameIgnoreCase("SAP")).thenReturn(Optional.of(erp));
        FieldMappingEntity e = FieldMappingEntity.builder().id(10L).erpId(1L).sourceField("a").targetField("b").transformFn("TRIM").status("ACTIVE").version(1).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(fmRepo.findByErpIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(e));

        FieldMapping dm = FieldMapping.builder().id(10L).erpId(1L).sourceField("a").targetField("b").transformFn("TRIM").status("ACTIVE").version(1).build();
        when(mapper.toDomain(e)).thenReturn(dm);

        List<FieldMapping> out = adapter.findActiveByErpName("SAP");
        assertEquals(1, out.size());
        assertEquals("b", out.get(0).getTargetField());

        verify(erpRepo).findByNameIgnoreCase("SAP");
        verify(fmRepo).findByErpIdAndStatus(1L, "ACTIVE");
        verify(mapper).toDomain(e);
    }

    @Test
    void existsActiveByErpAndSource_delegatesToRepo() {
        MappingRepositoryAdapter adapter = new MappingRepositoryAdapter(erpRepo, fmRepo, mapper);
        when(fmRepo.existsByErpIdAndSourceFieldIgnoreCase(1L, "a")).thenReturn(true);
        assertTrue(adapter.existsActiveByErpAndSource(1L, "a"));
        verify(fmRepo).existsByErpIdAndSourceFieldIgnoreCase(1L, "a");
    }

    @Test
    void invalidateCacheForErp_noop() {
        MappingRepositoryAdapter adapter = new MappingRepositoryAdapter(erpRepo, fmRepo, mapper);
        assertDoesNotThrow(() -> adapter.invalidateCacheForErp("SAP"));
    }
}
