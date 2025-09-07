package co.edu.itm.invoiceextract.application.controller;

import co.edu.itm.invoiceextract.application.dto.EmailConfigurationDTO;
import co.edu.itm.invoiceextract.application.service.EmailConfigurationService;
import co.edu.itm.invoiceextract.domain.entity.ConfigurationStatus;
import co.edu.itm.invoiceextract.domain.entity.email.EmailConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailConfigurationControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailConfigurationService service;

    private static final String BASE = "/api/config/email";

    private EmailConfigurationDTO dto(String user, String pass) {
        EmailConfigurationDTO dto = new EmailConfigurationDTO();
        dto.setUsername(user);
        dto.setPassword(pass);
        return dto;
    }

    private EmailConfiguration config(String user, String encPass, String key, ConfigurationStatus status) {
        EmailConfiguration c = new EmailConfiguration();
        c.setUsername(user);
        c.setPassword(encPass);
        c.setEncryptionKey(key);
        c.setStatus(status);
        c.setCreatedDate(LocalDateTime.now());
        return c;
    }

    @Nested
    @DisplayName("POST /api/config/email")
    class CreateOrUpdate {
        @Test
        void should_create_or_update_when_valid() throws Exception {
            // Given
            EmailConfigurationDTO request = dto("alice@example.com", "secret");
            EmailConfiguration saved = config("alice@example.com", "encPw", "k123", ConfigurationStatus.ACTIVE);
            given(service.saveConfiguration(anyString(), anyString())).willReturn(saved);

            // When / Then
            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().string(containsString("Email configuration saved successfully")));
        }

        @Test
        void should_return_500_when_service_throws() throws Exception {
            EmailConfigurationDTO request = dto("bob@example.com", "pass");
            given(service.saveConfiguration(anyString(), anyString())).willThrow(new RuntimeException("boom"));

            mockMvc.perform(post(BASE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Error saving email configuration")));
        }
    }

    @Nested
    @DisplayName("GET /api/config/email/{username}")
    class GetByUsername {
        @Test
        void should_return_latest_credentials_when_found() throws Exception {
            EmailConfiguration conf = config("alice@example.com", "encPw", "k123", ConfigurationStatus.ACTIVE);
            given(service.getConfigurationByUsername("alice@example.com")).willReturn(Optional.of(conf));

            mockMvc.perform(get(BASE + "/{username}", "alice@example.com")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username", is("alice@example.com")))
                    .andExpect(jsonPath("$.password", is("encPw")))
                    .andExpect(jsonPath("$.key", is("k123")));
        }

        @Test
        void should_return_404_when_not_found() throws Exception {
            given(service.getConfigurationByUsername("none@example.com")).willReturn(Optional.empty());

            mockMvc.perform(get(BASE + "/{username}", "none@example.com")
                            .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Configuration not found")));
        }

        @Test
        void should_return_500_on_error() throws Exception {
            Mockito.when(service.getConfigurationByUsername(anyString())).thenThrow(new RuntimeException("err"));

            mockMvc.perform(get(BASE + "/{username}", "x")
                            .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Error retrieving email configuration")));
        }
    }

    @Nested
    @DisplayName("GET /api/config/email/active")
    class GetActive {
        @Test
        void should_return_latest_active() throws Exception {
            EmailConfiguration a1 = config("u1", "p1", "k1", ConfigurationStatus.ACTIVE);
            EmailConfiguration a2 = config("u2", "p2", "k2", ConfigurationStatus.ACTIVE);
            EmailConfiguration i1 = config("u3", "p3", "k3", ConfigurationStatus.INACTIVE);
            given(service.getAllConfigurations()).willReturn(List.of(a1, a2, i1));

            mockMvc.perform(get(BASE + "/active").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username", anyOf(is("u1"), is("u2"))))
                    .andExpect(jsonPath("$.password", anyOf(is("p1"), is("p2"))))
                    .andExpect(jsonPath("$.key", anyOf(is("k1"), is("k2"))));
        }

        @Test
        void should_return_404_when_no_active() throws Exception {
            given(service.getAllConfigurations()).willReturn(List.of());

            mockMvc.perform(get(BASE + "/active").accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("No active email configuration found")));
        }

        @Test
        void should_return_500_on_error() throws Exception {
            Mockito.when(service.getAllConfigurations()).thenThrow(new RuntimeException("err"));

            mockMvc.perform(get(BASE + "/active").accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Error retrieving active email configuration")));
        }
    }

    @Nested
    @DisplayName("GET /api/config/email/filter")
    class FilterByStatus {
        @Test
        void should_return_list_filtered_by_status() throws Exception {
            EmailConfiguration a1 = config("u1", "p1", "k1", ConfigurationStatus.ACTIVE);
            EmailConfiguration a2 = config("u2", "p2", "k2", ConfigurationStatus.ACTIVE);
            given(service.getConfigurationsByUsernameAndStatus(eq("u"), eq(ConfigurationStatus.ACTIVE)))
                    .willReturn(List.of(a1, a2));

            mockMvc.perform(get(BASE + "/filter")
                            .param("username", "u")
                            .param("status", "ACTIVE")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].username", anyOf(is("u1"), is("u2"))));
        }

        @Test
        void should_return_500_on_error_filtering() throws Exception {
            Mockito.when(service.getConfigurationsByUsernameAndStatus(anyString(), any()))
                    .thenThrow(new RuntimeException("err"));

            mockMvc.perform(get(BASE + "/filter")
                            .param("username", "u")
                            .param("status", "ACTIVE")
                            .accept(MediaType.TEXT_PLAIN))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Error retrieving email configurations")));
        }
    }
}
