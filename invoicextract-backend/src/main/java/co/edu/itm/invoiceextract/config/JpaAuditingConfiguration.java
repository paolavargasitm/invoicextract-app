package co.edu.itm.invoiceextract.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // In a real application, this would be integrated with Spring Security
        // to return the currently logged-in user.
        // return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName());
        return () -> Optional.of("api-user"); // Placeholder
    }
}
