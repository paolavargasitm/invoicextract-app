package co.edu.itm.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ErpTest {

    @Test
    void builderAndAccessorsWork() {
        Instant now = Instant.now();
        Erp e = Erp.builder()
                .id(1L)
                .name("SAP")
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
        assertEquals(1L, e.getId());
        assertEquals("SAP", e.getName());
        assertEquals("ACTIVE", e.getStatus());
        assertEquals(now, e.getCreatedAt());
        assertEquals(now, e.getUpdatedAt());

        e.setName("Dynamics");
        assertEquals("Dynamics", e.getName());
    }
}
