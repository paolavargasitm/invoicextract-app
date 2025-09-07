package co.edu.itm.invoiceextract.application.service;

import co.edu.itm.invoiceextract.domain.entity.ConfigurationStatus;
import co.edu.itm.invoiceextract.domain.entity.email.EmailConfiguration;
import co.edu.itm.invoiceextract.domain.repository.EmailConfigurationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailConfigurationServiceTest {

    @Mock
    private EmailConfigurationRepository repository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private EmailConfigurationService service;

    private EmailConfiguration active(String username) {
        EmailConfiguration c = new EmailConfiguration();
        c.setUsername(username);
        c.setPassword("oldEnc");
        c.setEncryptionKey("oldKey");
        c.setStatus(ConfigurationStatus.ACTIVE);
        return c;
    }

    @Nested
    class SaveConfiguration {
        @Test
        @DisplayName("should_deactivate_previous_active_and_save_new_encrypted_config")
        void should_deactivate_previous_active_and_save_new_encrypted_config() throws Exception {
            // Given existing active configs
            EmailConfiguration a1 = active("alice");
            given(repository.findByUsernameAndStatus("alice", ConfigurationStatus.ACTIVE)).willReturn(List.of(a1));

            // And encryption
            given(encryptionService.generateEncryptionKey()).willReturn("newKey");
            given(encryptionService.encrypt("plainPass", "newKey")).willReturn("encPass");

            // Capture saved entity
            ArgumentCaptor<EmailConfiguration> captor = ArgumentCaptor.forClass(EmailConfiguration.class);
            given(repository.save(any(EmailConfiguration.class))).willAnswer(inv -> inv.getArgument(0));

            // When
            EmailConfiguration result = service.saveConfiguration("alice", "plainPass");

            // Then: previous active set to INACTIVE and saved
            assertThat(a1.getStatus()).isEqualTo(ConfigurationStatus.INACTIVE);
            verify(repository).save(a1);

            // New config saved with encrypted password and key
            verify(repository, atLeastOnce()).save(captor.capture());
            EmailConfiguration saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("alice");
            assertThat(saved.getPassword()).isEqualTo("encPass");
            assertThat(saved.getEncryptionKey()).isEqualTo("newKey");
            assertThat(saved.getStatus()).isEqualTo(ConfigurationStatus.ACTIVE);

            // And returned entity is the same as saved
            assertEquals("alice", result.getUsername());
        }
    }

    @Nested
    class GetDecryptedPassword {
        @Test
        @DisplayName("should_decrypt_password_with_stored_key")
        void should_decrypt_password_with_stored_key() throws Exception {
            EmailConfiguration cfg = new EmailConfiguration();
            cfg.setUsername("bob");
            cfg.setPassword("enc");
            cfg.setEncryptionKey("key1");
            given(repository.findFirstByUsernameAndStatusOrderByCreatedDateDesc("bob", ConfigurationStatus.ACTIVE))
                    .willReturn(Optional.of(cfg));
            given(encryptionService.decrypt("enc", "key1")).willReturn("plain");

            Optional<String> result = service.getDecryptedPassword("bob");
            assertThat(result).contains("plain");
        }

        @Test
        @DisplayName("should_use_empty_key_when_missing_for_backward_compatibility")
        void should_use_empty_key_when_missing_for_backward_compatibility() throws Exception {
            EmailConfiguration cfg = new EmailConfiguration();
            cfg.setUsername("carol");
            cfg.setPassword("enc");
            cfg.setEncryptionKey("");
            given(repository.findFirstByUsernameAndStatusOrderByCreatedDateDesc("carol", ConfigurationStatus.ACTIVE))
                    .willReturn(Optional.of(cfg));
            given(encryptionService.decrypt("enc", "")).willReturn("plain");

            Optional<String> result = service.getDecryptedPassword("carol");
            assertThat(result).contains("plain");
        }

        @Test
        @DisplayName("should_throw_runtime_when_decryption_fails")
        void should_throw_runtime_when_decryption_fails() throws Exception {
            EmailConfiguration cfg = new EmailConfiguration();
            cfg.setUsername("dave");
            cfg.setPassword("enc");
            cfg.setEncryptionKey("k");
            given(repository.findFirstByUsernameAndStatusOrderByCreatedDateDesc("dave", ConfigurationStatus.ACTIVE))
                    .willReturn(Optional.of(cfg));
            given(encryptionService.decrypt(anyString(), anyString())).willThrow(new RuntimeException("boom"));

            assertThrows(RuntimeException.class, () -> service.getDecryptedPassword("dave"));
        }
    }

    @Test
    @DisplayName("should_delegate_simple_queries")
    void should_delegate_simple_queries() {
        service.getConfiguration("u");
        verify(repository).findFirstByUsernameAndStatusOrderByCreatedDateDesc("u", ConfigurationStatus.ACTIVE);

        service.getConfigurationByUsername("x");
        verify(repository, times(1)).findFirstByUsernameAndStatusOrderByCreatedDateDesc("x", ConfigurationStatus.ACTIVE);

        service.getConfigurationsByUsernameAndStatus("u", ConfigurationStatus.ACTIVE);
        verify(repository).findByUsernameAndStatus("u", ConfigurationStatus.ACTIVE);

        service.getAllConfigurations();
        verify(repository).findAll();
    }
}
