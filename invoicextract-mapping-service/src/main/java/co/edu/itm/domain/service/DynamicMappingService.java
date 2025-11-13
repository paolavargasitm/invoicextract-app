package co.edu.itm.domain.service;

import co.edu.itm.domain.model.FieldMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicMappingService {
    private final TransformRegistry registry;

    public DynamicMappingService(TransformRegistry registry) {
        this.registry = registry;
    }

    public Map<String, Object> apply(List<FieldMapping> rules, Map<String, Object> source) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (FieldMapping r : rules) {
            Object val = resolvePath(source, r.getSourceField());
            val = registry.apply(r.getTransformFn(), val);
            putTarget(out, r.getTargetField(), val);
        }
        return out;
    }

    private static final Pattern SEGMENT = Pattern.compile("([a-zA-Z0-9_\\-]+)(?:\\[(\\d+)\\])?");

    private Object resolvePath(Map<String, Object> root, String path) {
        if (path == null || path.isBlank()) return null;
        // Wildcard support: e.g., items[].quantity -> List of values
        if (path.contains("[]")) {
            return resolveWithWildcard(root, path);
        }
        // Fast path: exact key match
        if (root.containsKey(path)) return root.get(path);

        String[] parts = path.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (current == null) return null;
            Matcher m = SEGMENT.matcher(part);
            if (!m.matches()) {
                return null;
            }
            String key = m.group(1);
            String idxStr = m.group(2);

            if (!(current instanceof Map)) return null;
            Object next = ((Map<?, ?>) current).get(key);
            if (idxStr != null) {
                if (!(next instanceof List)) return null;
                int idx;
                try { idx = Integer.parseInt(idxStr); } catch (NumberFormatException e) { return null; }
                List<?> list = (List<?>) next;
                if (idx < 0 || idx >= list.size()) return null;
                next = list.get(idx);
            }
            current = next;
        }
        return current;
    }

    // Expand wildcard paths like items[].quantity or items[].sub[].code
    private Object resolveWithWildcard(Map<String, Object> root, String path) {
        String[] parts = path.split("\\.");
        return resolveWildcardRecursive(root, parts, 0);
    }

    @SuppressWarnings("unchecked")
    private Object resolveWildcardRecursive(Object current, String[] parts, int idx) {
        if (current == null) return null;
        if (idx >= parts.length) return current;

        String part = parts[idx];
        boolean wildcard = part.endsWith("[]");
        String key = wildcard ? part.substring(0, part.length() - 2) : part;

        if (!(current instanceof Map)) return null;
        Object next = ((Map<String, Object>) current).get(key);

        if (!wildcard) {
            // Delegates to standard segment resolution (no index here)
            return resolveWildcardRecursive(next, parts, idx + 1);
        }

        if (!(next instanceof List)) return null;
        List<?> list = (List<?>) next;
        java.util.ArrayList<Object> results = new java.util.ArrayList<>();
        for (Object elem : list) {
            Object val = resolveWildcardRecursive(elem, parts, idx + 1);
            if (val == null) continue;
            if (val instanceof List<?> vl) {
                results.addAll(vl);
            } else {
                results.add(val);
            }
        }
        return results;
    }

    // Write support: allows targets like items[].cantidad
    @SuppressWarnings({"unchecked","rawtypes"})
    private void putTarget(Map<String, Object> out, String targetPath, Object value) {
        if (targetPath == null || targetPath.isBlank()) return;
        if (!targetPath.contains("[]") && !targetPath.contains("[")) {
            out.put(targetPath, value);
            return;
        }

        // Split by '.' and build structures
        String[] parts = targetPath.split("\\.");
        putTargetRecursive(out, parts, 0, value);
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private void putTargetRecursive(Object current, String[] parts, int idx, Object value) {
        if (idx >= parts.length) {
            // nothing to set
            return;
        }
        String part = parts[idx];
        boolean wildcard = part.endsWith("[]");
        String key = wildcard ? part.substring(0, part.length() - 2) : part;

        if (!(current instanceof Map)) return; // Only building maps at root/objects
        Map map = (Map) current;
        Object next = map.get(key);

        if (wildcard) {
            // Ensure list exists
            List list;
            if (next instanceof List) {
                list = (List) next;
            } else {
                list = new java.util.ArrayList<>();
                map.put(key, list);
            }

            // If the value is a list, distribute by index
            if (value instanceof List<?> values) {
                for (int i = 0; i < values.size(); i++) {
                    Object elem = (i < list.size()) ? list.get(i) : null;
                    if (!(elem instanceof Map)) {
                        elem = new LinkedHashMap<>();
                        // grow list if necessary
                        if (i < list.size()) {
                            list.set(i, elem);
                        } else {
                            // fill gaps with empty maps
                            while (list.size() < i) list.add(new LinkedHashMap<>());
                            list.add(elem);
                        }
                    }
                    putTargetRecursive(elem, parts, idx + 1, values.get(i));
                }
            } else {
                // Scalar value: set same value to first element
                Object elem = (list.isEmpty() || !(list.get(0) instanceof Map)) ? new LinkedHashMap<>() : list.get(0);
                if (list.isEmpty()) list.add(elem);
                putTargetRecursive(elem, parts, idx + 1, value);
            }
            return;
        }

        // non-wildcard segment, might be terminal
        if (idx == parts.length - 1) {
            map.put(key, value);
            return;
        }

        // Ensure next is a map
        if (!(next instanceof Map)) {
            next = new LinkedHashMap<>();
            map.put(key, next);
        }
        putTargetRecursive(next, parts, idx + 1, value);
    }
}
