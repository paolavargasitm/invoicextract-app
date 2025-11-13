package co.edu.itm.invoiceextract.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptionServiceTest {

    private EncryptionService service;

    @BeforeEach
    void setUp() {
        service = new EncryptionService();
        // simulate @Value injection
        ReflectionTestUtils.setField(service, "secretKey", "test-master-secret-key");
    }

    @Test
    @DisplayName("should_encrypt_and_decrypt_with_config_key")
    void should_encrypt_and_decrypt_with_config_key() throws Exception {
        String configKey = service.generateEncryptionKey();
        String plain = "bfnx ygve bpep mjac";

        String enc = service.encrypt(plain, configKey);
        String dec = service.decrypt(enc, configKey);

        assertThat(enc).isNotBlank();
        assertThat(dec).isEqualTo(plain);
    }

    @Test
    @DisplayName("should_generate_32_char_ascii_key")
    void should_generate_64_char_hex_key() {
        String key = service.generateEncryptionKey();
        assertThat(key).hasSize(32);
        assertThat(key).matches("[A-Za-z0-9]{32}");
    }
}
