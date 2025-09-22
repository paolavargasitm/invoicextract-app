package co.edu.itm.adapters.out.jpa.mappings.mapper;

import co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity;
import co.edu.itm.domain.model.FieldMapping;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MappingEntityMapperTest {

    private final MappingEntityMapper mapper = Mappers.getMapper(MappingEntityMapper.class);

    @Test
    void toDomain_shouldMapAllSimpleFields() {
        Instant now = Instant.now();
        FieldMappingEntity e = FieldMappingEntity.builder()
                .id(1L)
                .erpId(2L)
                .sourceField("amount")
                .targetField("total")
                .transformFn("TRIM")
                .status("ACTIVE")
                .version(3)
                .createdAt(now)
                .updatedAt(now)
                .build();

        FieldMapping d = mapper.toDomain(e);

        assertNotNull(d);
        assertEquals(1L, d.getId());
        assertEquals(2L, d.getErpId());
        assertEquals("amount", d.getSourceField());
        assertEquals("total", d.getTargetField());
        assertEquals("TRIM", d.getTransformFn());
        assertEquals("ACTIVE", d.getStatus());
        assertEquals(3, d.getVersion());
        assertEquals(now, d.getCreatedAt());
        assertEquals(now, d.getUpdatedAt());
    }
}
