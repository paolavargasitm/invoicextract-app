package co.edu.itm.invoiceextract.domain.repository;

import co.edu.itm.invoiceextract.domain.entity.EmailConfiguration;
import co.edu.itm.invoiceextract.domain.entity.ConfigurationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Long> {

    /**
     * Finds an email configuration by its username.
     *
     * @param username The username to search for.
     * @return An Optional containing the EmailConfiguration if found, or an empty Optional otherwise.
     */
    List<EmailConfiguration> findByUsernameAndStatus(String username, ConfigurationStatus status);

    Optional<EmailConfiguration> findFirstByUsernameAndStatusOrderByCreatedAtDesc(String username, ConfigurationStatus status);
}
