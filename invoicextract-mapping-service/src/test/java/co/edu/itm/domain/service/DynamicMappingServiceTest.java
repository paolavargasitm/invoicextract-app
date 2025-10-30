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

    @Test
    void apply_shouldSupportWildcardArraysAndAggregates() {
        TransformRegistry registry = new TransformRegistry();
        DynamicMappingService service = new DynamicMappingService(registry);

        List<FieldMapping> rules = List.of(
                FieldMapping.builder().sourceField("items[].quantity").targetField("cantidades").transformFn("").build(),
                FieldMapping.builder().sourceField("items[].quantity").targetField("cantidadTotal").transformFn("SUM").build(),
                FieldMapping.builder().sourceField("items[].description").targetField("descs").transformFn("JOIN: | ").build(),
                FieldMapping.builder().sourceField("items[0].description").targetField("primera").transformFn("").build()
        );

        Map<String, Object> item1 = new LinkedHashMap<>();
        item1.put("quantity", 2);
        item1.put("description", "A");
        Map<String, Object> item2 = new LinkedHashMap<>();
        item2.put("quantity", 3);
        item2.put("description", "B");

        Map<String, Object> source = new LinkedHashMap<>();
        source.put("items", List.of(item1, item2));

        Map<String, Object> out = service.apply(rules, source);

        assertTrue(out.containsKey("cantidades"));
        assertEquals(List.of(2,3), out.get("cantidades"));
        assertEquals(5.0, out.get("cantidadTotal"));
        assertEquals("A | B", out.get("descs"));
        assertEquals("A", out.get("primera"));
    }
}
