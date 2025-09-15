package co.edu.itm.domain.service;

import co.edu.itm.domain.model.FieldMapping;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DynamicMappingServiceTest {

    @Test
    void apply_shouldMapAndTransformAccordingToRules() {
        // Registry with default functions (TRIM, UPPER, DATE_FMT)
        TransformRegistry registry = new TransformRegistry();
        DynamicMappingService service = new DynamicMappingService(registry);

        // Rules: take 'name' to 'customerName' with TRIM, and 'type' to 'docType' with UPPER
        List<FieldMapping> rules = List.of(
                FieldMapping.builder()
                        .sourceField("name").targetField("customerName").transformFn("TRIM").build(),
                FieldMapping.builder()
                        .sourceField("type").targetField("docType").transformFn("UPPER").build()
        );

        Map<String, Object> source = new LinkedHashMap<>();
        source.put("name", "  paola  ");
        source.put("type", "inv");

        Map<String, Object> out = service.apply(rules, source);

        assertEquals(2, out.size());
        assertEquals("paola", out.get("customerName"));
        assertEquals("INV", out.get("docType"));
    }

    @Test
    void apply_shouldHandleNullSourceValues() {
        TransformRegistry registry = new TransformRegistry();
        DynamicMappingService service = new DynamicMappingService(registry);

        List<FieldMapping> rules = List.of(
                FieldMapping.builder().sourceField("unknown").targetField("x").transformFn("TRIM").build()
        );

        Map<String, Object> source = new LinkedHashMap<>(); // no key 'unknown'
        Map<String, Object> out = service.apply(rules, source);

        assertTrue(out.containsKey("x"));
        assertNull(out.get("x"));
    }
}
