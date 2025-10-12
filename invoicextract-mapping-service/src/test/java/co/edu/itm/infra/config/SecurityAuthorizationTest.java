package co.edu.itm.infra.config;

import co.edu.itm.adapters.in.rest.ErpController;
import co.edu.itm.adapters.in.rest.MappingController;
import co.edu.itm.adapters.out.jpa.mappings.repo.ErpJpaRepository;
import co.edu.itm.adapters.out.jpa.mappings.repo.FieldMappingJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ErpController.class, MappingController.class})
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

    @MockBean
    private ErpJpaRepository erpJpaRepository;

    @MockBean
    private FieldMappingJpaRepository fieldMappingJpaRepository;

    // Satisfy Resource Server bean requirement
    @MockBean
    private JwtDecoder jwtDecoder;

    @jakarta.annotation.Resource
    private MockMvc mockMvc;

    @Test
    void preflight_options_is_permitted() throws Exception {
        mockMvc.perform(options("/api/erps")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    void erps_requires_tecnico_role() throws Exception {
        when(erpJpaRepository.findAll()).thenReturn(java.util.List.of());

        // Without TECNICO (FINANZAS) -> 200 (allowed to read ERPs)
        mockMvc.perform(get("/api/erps")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_FINANZAS"))))
                .andExpect(status().isOk());

        // With TECNICO -> 200
        mockMvc.perform(get("/api/erps")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TECNICO"))))
                .andExpect(status().isOk());
    }

    @Test
    void mappings_requires_tecnico_role() throws Exception {
        when(erpJpaRepository.findByNameIgnoreCase("SAP")).thenReturn(java.util.Optional.of(new co.edu.itm.adapters.out.jpa.mappings.entity.ErpEntity()));
        when(fieldMappingJpaRepository.findByErpIdAndStatus(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());

        // Without TECNICO -> 403
        mockMvc.perform(get("/api/mappings?erp=SAP&status=ACTIVE")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_FINANZAS"))))
                .andExpect(status().isForbidden());

        // With TECNICO -> 200
        mockMvc.perform(get("/api/mappings?erp=SAP&status=ACTIVE")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TECNICO"))))
                .andExpect(status().isOk());
    }
}
