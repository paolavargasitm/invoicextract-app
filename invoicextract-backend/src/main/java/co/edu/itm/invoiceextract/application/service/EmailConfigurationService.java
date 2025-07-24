package co.edu.itm.invoiceextract.application.service;

import co.edu.itm.invoiceextract.domain.entity.EmailConfiguration;
import co.edu.itm.invoiceextract.domain.repository.EmailConfigurationRepository;
import co.edu.itm.invoiceextract.domain.entity.ConfigurationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmailConfigurationService {

    private final EmailConfigurationRepository repository;
    private final EncryptionService encryptionService;

    public EmailConfigurationService(EmailConfigurationRepository repository, EncryptionService encryptionService) {
        this.repository = repository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public EmailConfiguration saveConfiguration(String username, String plainPassword) throws Exception {
        // Deactivate all existing active configurations for this username
        List<EmailConfiguration> activeConfigs = repository.findByUsernameAndStatus(username, ConfigurationStatus.ACTIVE);
        for (EmailConfiguration activeConfig : activeConfigs) {
            activeConfig.setStatus(ConfigurationStatus.INACTIVE);
            repository.save(activeConfig);
        }

        // Create and save the new active configuration
        String encryptedPassword = encryptionService.encrypt(plainPassword);
        EmailConfiguration newConfig = new EmailConfiguration();
        newConfig.setUsername(username);
        newConfig.setPassword(encryptedPassword);
        newConfig.setStatus(ConfigurationStatus.ACTIVE);

        return repository.save(newConfig);
    }

    public Optional<EmailConfiguration> getConfiguration(String username) {
        return repository.findFirstByUsernameAndStatusOrderByCreatedAtDesc(username, ConfigurationStatus.ACTIVE);
    }

    public Optional<String> getDecryptedPassword(String username) {
        Optional<EmailConfiguration> configOpt = repository.findFirstByUsernameAndStatusOrderByCreatedAtDesc(username, ConfigurationStatus.ACTIVE);
        if (configOpt.isPresent()) {
            try {
                String encryptedPassword = configOpt.get().getPassword();
                return Optional.of(encryptionService.decrypt(encryptedPassword));
            } catch (Exception e) {
                // Consider logging the exception
                throw new RuntimeException("Could not decrypt password for username: " + username, e);
            }
        }
        return Optional.empty();
    }

    public Optional<EmailConfiguration> getConfigurationByUsername(String username) {
        return repository.findFirstByUsernameAndStatusOrderByCreatedAtDesc(username, ConfigurationStatus.ACTIVE);
    }

    public List<EmailConfiguration> getConfigurationsByUsernameAndStatus(String username, ConfigurationStatus status) {
        return repository.findByUsernameAndStatus(username, status);
    }
}
