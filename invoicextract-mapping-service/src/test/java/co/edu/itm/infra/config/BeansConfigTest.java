package co.edu.itm.infra.config;

import co.edu.itm.domain.model.FieldMapping;
import co.edu.itm.domain.service.DynamicMappingService;
import co.edu.itm.domain.service.TransformRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BeansConfigTest {

    @Test
    void beansAreWiredAndFunctional() {
        BeansConfig config = new BeansConfig();
        TransformRegistry registry = config.transformRegistry();
        DynamicMappingService service = config.dynamicMappingService(registry);

        Map<String, Object> src = Map.of("name", "  paola  ");
        List<FieldMapping> rules = List.of(FieldMapping.builder()
                .sourceField("name").targetField("customerName").transformFn("TRIM").build());

        Map<String, Object> out = service.apply(rules, src);
        assertEquals("paola", out.get("customerName"));
    }
}
