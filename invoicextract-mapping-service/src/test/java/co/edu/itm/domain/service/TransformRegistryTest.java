package co.edu.itm.domain.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransformRegistryTest {

    @Test
    void trim_shouldHandleNullsAndTrimStrings() {
        TransformRegistry registry = new TransformRegistry();
        assertNull(registry.apply("TRIM", null));
        assertEquals("ABC", registry.apply("TRIM", "  ABC  "));
    }

    @Test
    void upper_shouldUppercaseString() {
        TransformRegistry registry = new TransformRegistry();
        assertEquals("HELLO", registry.apply("UPPER", "hello"));
        assertNull(registry.apply("UPPER", null));
    }

    @Test
    void dateFmt_shouldFormatFromStringAndLocalDate() {
        TransformRegistry registry = new TransformRegistry();
        Object fromString = registry.apply("DATE_FMT:yyyy-MM-dd", "2024-03-15");
        assertEquals("2024-03-15", fromString);

        Object fromDate = registry.apply("DATE_FMT:dd/MM/yyyy", LocalDate.of(2024, 3, 15));
        assertEquals("15/03/2024", fromDate);
    }

    @Test
    void unknownFunction_returnsOriginalValue() {
        TransformRegistry registry = new TransformRegistry();
        assertEquals("abc", registry.apply("UNKNOWN", "abc"));
    }

    @Test
    void emptySpec_returnsOriginalValue() {
        TransformRegistry registry = new TransformRegistry();
        assertEquals("abc", registry.apply("", "abc"));
        assertEquals("abc", registry.apply("   ", "abc"));
        assertEquals("abc", registry.apply(null, "abc"));
    }
}
