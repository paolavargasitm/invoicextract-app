package co.edu.itm.adapters.out.export;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceAdapterTest {

    @Test
    void toCsv_emptyRows_returnsEmptyBytes() {
        ExportServiceAdapter adapter = new ExportServiceAdapter();
        byte[] out = adapter.toCsv(List.of());
        assertEquals(0, out.length);
    }

    @Test
    void toCsv_writesHeadersAndValues() {
        ExportServiceAdapter adapter = new ExportServiceAdapter();
        List<Map<String, Object>> rows = List.of(
                Map.of("a", 1, "b", "x"),
                Map.of("a", 2, "b", "y")
        );
        byte[] out = adapter.toCsv(rows);
        String csv = new String(out, StandardCharsets.UTF_8);
        // Header order follows first row keySet iteration order (insertion order for LinkedHashMap; Map.of may not preserve).
        // We only assert that both headers exist and two data lines are produced.
        assertTrue(csv.contains("a"));
        assertTrue(csv.contains("b"));
        assertTrue(csv.contains("1"));
        assertTrue(csv.contains("2"));
        assertTrue(csv.lines().count() >= 3);
    }

    @Test
    void toJson_serializesList() {
        ExportServiceAdapter adapter = new ExportServiceAdapter();
        String json = adapter.toJson(List.of(Map.of("k", "v")));
        assertTrue(json.contains("\"k\""));
        assertTrue(json.contains("\"v\""));
    }
}
