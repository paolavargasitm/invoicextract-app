package co.edu.itm.adapters.in.rest;

import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import co.edu.itm.domain.ports.MappingRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MappingController.class)
@AutoConfigureMockMvc(addFilters = false)
class MappingControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ErpJpaRepository erpRepo;

    @MockBean
    private FieldMappingJpaRepository fmRepo;

    @MockBean
    private MappingRepositoryPort mappingPort;

    @Test
    @DisplayName("GET /api/mappings con ERP inexistente devuelve 404")
    void list_unknownErp_returns404() throws Exception {
        when(erpRepo.findByNameIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mappings").param("erp", "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value(404));
    }

    @Test
    @DisplayName("POST /api/mappings con ERP inexistente devuelve 404")
    void create_unknownErp_returns404() throws Exception {
        when(erpRepo.findByNameIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        String body = "{" +
                "\"erpName\":\"UNKNOWN\"," +
                "\"sourceField\":\"a\"," +
                "\"targetField\":\"b\"," +
                "\"transformFn\":\"TRIM\"," +
                "\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/api/mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value(404));
    }

    @Test
    @DisplayName("POST /api/mappings con body inválido devuelve 400 (validación)")
    void create_validationError_returns400() throws Exception {
        // Falta sourceField y targetField está vacío: violará @NotBlank
        String body = "{" +
                "\"erpName\":\"SAP\"," +
                "\"targetField\":\"\"," +
                "\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/api/mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(400))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @DisplayName("POST /api/mappings con conflicto de datos devuelve 409")
    void create_conflict_returns409() throws Exception {
        when(erpRepo.findByNameIgnoreCase("SAP")).thenReturn(java.util.Optional.of(
                co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity.builder().id(1L).name("SAP").status("ACTIVE").build()
        ));
        when(fmRepo.save(any(co.edu.itm.adapters.out.jpa.mappings.entity.FieldMappingEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        String body = "{" +
                "\"erpName\":\"SAP\"," +
                "\"sourceField\":\"a\"," +
                "\"targetField\":\"b\"," +
                "\"transformFn\":\"TRIM\"," +
                "\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/api/mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value(409));
    }

    @Test
    @DisplayName("PATCH /api/mappings/{id}/status con id inexistente devuelve 404")
    void changeStatus_notFound_returns404() throws Exception {
        when(fmRepo.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/mappings/999/status").param("status", "INACTIVE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value(404));
    }

    @Test
    @DisplayName("POST /api/mappings con JSON mal formado devuelve 400 (parse error)")
    void create_malformedJson_returns400() throws Exception {
        String malformed = "{ \"erpName\": \"SAP\", \"sourceField\": \"a\""; // falta cierre y campos

        mockMvc.perform(post("/api/mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(400));
    }
}
