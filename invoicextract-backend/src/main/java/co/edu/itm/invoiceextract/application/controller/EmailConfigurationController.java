package co.edu.itm.invoiceextract.application.controller;

import co.edu.itm.invoiceextract.application.dto.EmailConfigurationDTO;
import co.edu.itm.invoiceextract.application.service.EmailConfigurationService;
import co.edu.itm.invoiceextract.domain.entity.email.EmailConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.itm.invoiceextract.domain.entity.ConfigurationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/config/email")
@Tag(name = "Email Configuration", description = "API for managing email service credentials securely")
@CrossOrigin(origins = "*")
public class EmailConfigurationController {

    private final EmailConfigurationService service;

    public EmailConfigurationController(EmailConfigurationService service) {
        this.service = service;
    }

    @Operation(summary = "Set or update email credentials", description = "Creates or updates the credentials for the email service. The password is encrypted before being stored.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email configuration saved successfully",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    @PostMapping
    public ResponseEntity<String> createOrUpdateConfiguration(@Valid @RequestBody EmailConfigurationDTO configDTO) {
        try {
            service.saveConfiguration(configDTO.getUsername(), configDTO.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body("Email configuration saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving email configuration: " + e.getMessage());
        }
    }

    @Operation(summary = "Get Latest Active Email Credentials", description = "Retrieves the latest ACTIVE encrypted credentials and encryption key for a given username. This endpoint should be used by the Windows service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Configuration not found for the given username",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/{username}")
    public ResponseEntity<?> getConfiguration(@Parameter(description = "Username for the email configuration to retrieve") @PathVariable String username) {
        try {
            Optional<EmailConfiguration> configOpt = service.getConfigurationByUsername(username);
            if (configOpt.isPresent()) {
                EmailConfiguration config = configOpt.get();
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", username);
                credentials.put("password", config.getPassword()); // Return encrypted password
                credentials.put("key", config.getEncryptionKey()); // Return encryption key
                return ResponseEntity.ok(credentials);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Configuration not found for username: " + username);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving email configuration: " + e.getMessage());
        }
    }

    @Operation(summary = "Get latest active email configuration", description = "Retrieves the latest ACTIVE email configuration with encryption key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved email configuration",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "No active configuration found",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/active")
    public ResponseEntity<?> getActiveConfiguration() {
        try {
            // Get all active configurations
            List<EmailConfiguration> allConfigs = service.getAllConfigurations();
            List<EmailConfiguration> activeConfigs = allConfigs.stream()
                .filter(config -> config.getStatus() == ConfigurationStatus.ACTIVE)
                .collect(Collectors.toList());

            if (activeConfigs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active email configuration found");
            }

            // Get the most recent active configuration
            EmailConfiguration latestConfig = activeConfigs.stream()
                .sorted((c1, c2) -> c2.getCreatedDate().compareTo(c1.getCreatedDate()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unexpected error processing active configurations"));

            // Return the response in the requested format
            Map<String, String> response = new HashMap<>();
            response.put("username", latestConfig.getUsername());
            response.put("password", latestConfig.getPassword());
            response.put("key", latestConfig.getEncryptionKey());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving active email configuration: " + e.getMessage());
        }
    }

    @Operation(summary = "Get Email Configurations by Status", description = "Retrieves a list of email configurations for a given username, filtered by status (ACTIVE or INACTIVE).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved configurations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/filter")
    public ResponseEntity<?> getConfigurationsByStatus(
            @Parameter(description = "Username to filter by") @RequestParam String username,
            @Parameter(description = "Status to filter by (ACTIVE or INACTIVE)") @RequestParam ConfigurationStatus status) {
        try {
            List<EmailConfiguration> configs = service.getConfigurationsByUsernameAndStatus(username, status);
            List<Map<String, String>> result = configs.stream()
                    .map(config -> {
                        Map<String, String> credentials = new HashMap<>();
                        credentials.put("username", config.getUsername());
                        credentials.put("password", config.getPassword()); // Return encrypted password
                        credentials.put("status", config.getStatus().toString());
                        credentials.put("createdAt", config.getCreatedDate().toString());
                        return credentials;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving email configurations: " + e.getMessage());
        }
    }
}
