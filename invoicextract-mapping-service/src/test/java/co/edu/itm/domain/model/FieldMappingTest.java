package co.edu.itm.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FieldMappingTest {

    @Test
    void builderAndAccessors() {
        Instant now = Instant.now();
        FieldMapping fm = FieldMapping.builder()
                .id(2L)
                .erpId(1L)
                .sourceField("name")
                .targetField("customerName")
                .transformFn("TRIM")
                .status("ACTIVE")
                .version(1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(2L, fm.getId());
        assertEquals("name", fm.getSourceField());
        fm.setTargetField("cust");
        assertEquals("cust", fm.getTargetField());
    }
}
