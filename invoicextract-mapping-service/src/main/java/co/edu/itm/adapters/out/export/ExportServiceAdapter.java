package co.edu.itm.adapters.out.export;

import co.edu.itm.domain.ports.ExportServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ExportServiceAdapter implements ExportServicePort {
    private final ObjectMapper mapper;

    public ExportServiceAdapter() {
        this.mapper = new ObjectMapper();
        // Soporta java.time.* en JSON y usa ISO-8601 en lugar de timestamps
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public byte[] toCsv(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) return new byte[0];
        var headers = new ArrayList<>(rows.get(0).keySet());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(String.join(",", headers).concat("\n").getBytes(StandardCharsets.UTF_8));
        for (Map<String, Object> r : rows) {
            List<String> vals = headers.stream().map(h -> Optional.ofNullable(r.get(h)).orElse("").toString()).toList();
            out.writeBytes(String.join(",", vals).concat("\n").getBytes(StandardCharsets.UTF_8));
        }
        return out.toByteArray();
    }

    @Override
    public String toJson(List<Map<String, Object>> rows) {
        try {
            return mapper.writeValueAsString(rows);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pushToErp(String erpName, List<Map<String, Object>> rows) {
        System.out.println("Pushing " + rows.size() + " rows to ERP " + erpName);
    }
}
