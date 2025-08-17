package co.edu.itm.invoiceextract.domain.entity.email;

import co.edu.itm.invoiceextract.domain.entity.ConfigurationStatus;
import co.edu.itm.invoiceextract.domain.entity.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_configurations")
public class EmailConfiguration extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password; // This will store the encrypted password

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfigurationStatus status = ConfigurationStatus.ACTIVE;

    @Column(name = "encryption_key", nullable = false, length = 64)
    private String encryptionKey;

}
