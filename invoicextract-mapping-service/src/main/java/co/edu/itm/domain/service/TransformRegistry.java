package co.edu.itm.domain.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TransformRegistry {
    private final Map<String, TransformFunction> functions = new HashMap<>();

    public TransformRegistry() {
        register(new TransformFunction() {
            public Object apply(Object v, String arg) {
                return v == null ? null : v.toString().trim();
            }

            public String name() {
                return "TRIM";
            }
        });
        register(new TransformFunction() {
            public Object apply(Object v, String arg) {
                return v == null ? null : v.toString().toUpperCase();
            }

            public String name() {
                return "UPPER";
            }
        });
        register(new TransformFunction() {
            public Object apply(Object v, String arg) {
                if (v == null || arg == null) return v;
                LocalDate d = (v instanceof LocalDate) ? (LocalDate) v : LocalDate.parse(v.toString());
                return d.format(DateTimeFormatter.ofPattern(arg));
            }

            public String name() {
                return "DATE_FMT";
            }
        });

        // Aggregate transforms for lists (e.g., produced by items[].quantity)
        register(new TransformFunction() {
            @SuppressWarnings("rawtypes")
            public Object apply(Object v, String arg) {
                if (!(v instanceof List)) return v;
                List list = (List) v;
                return list.isEmpty() ? null : list.get(0);
            }

            public String name() { return "FIRST"; }
        });

        register(new TransformFunction() {
            @SuppressWarnings({"rawtypes","unchecked"})
            public Object apply(Object v, String arg) {
                if (v instanceof List) {
                    List list = (List) v;
                    double sum = 0d;
                    boolean any = false;
                    for (Object o : list) {
                        Double n = toNumber(o);
                        if (n != null) { sum += n; any = true; }
                    }
                    return any ? sum : null;
                }
                return toNumber(v);
            }

            private Double toNumber(Object o) {
                if (o == null) return null;
                if (o instanceof Number) return ((Number) o).doubleValue();
                try { return Double.parseDouble(o.toString()); } catch (Exception e) { return null; }
            }

            public String name() { return "SUM"; }
        });

        register(new TransformFunction() {
            @SuppressWarnings({"rawtypes","unchecked"})
            public Object apply(Object v, String arg) {
                String sep = arg == null ? "," : arg;
                if (v instanceof List) {
                    List list = (List) v;
                    if (list.isEmpty()) return "";
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < list.size(); i++) {
                        Object x = list.get(i);
                        if (i > 0) sb.append(sep);
                        sb.append(x == null ? "" : x.toString());
                    }
                    return sb.toString();
                }
                return v == null ? null : v.toString();
            }

            public String name() { return "JOIN"; }
        });
    }

    public void register(TransformFunction fn) {
        functions.put(fn.name(), fn);
    }

    public Object apply(String spec, Object value) {
        if (spec == null || spec.isBlank()) return value;
        String name = spec;
        String arg = null;
        if (spec.contains(":")) {
            name = spec.substring(0, spec.indexOf(":"));
            arg = spec.substring(spec.indexOf(":") + 1);
        }
        TransformFunction fn = functions.get(name);
        return fn == null ? value : fn.apply(value, arg);
    }
}
