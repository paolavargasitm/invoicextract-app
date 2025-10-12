package co.edu.itm.adapters.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/fields")
    public ResponseEntity<JsonNode> getKnownFields() throws IOException {
        ClassPathResource res = new ClassPathResource("reference/invoice-fields.json");
        try (InputStream in = res.getInputStream()) {
            JsonNode node = objectMapper.readTree(in);
            return ResponseEntity.ok(node);
        }
    }
}
