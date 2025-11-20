Feature: Email Config Management
  As an admin user of the InvoiceExtract application
  I want to configure email credentials for RPA automation
  So that the system can send automated emails properly

  Background:
    Given I navigate to the InvoiceExtract frontend
    When I login with admin credentials
    Then I should be successfully logged in
    And I can see the "email config" view

  # ===== POSITIVE TEST CASES =====

  @smoke @email_config @positive
  Scenario: Successfully access Email Config page and verify all UI elements
    Then I should see "Configuración de Correo para Automatización" heading
    And I should see email input field with id "email"
    And I should see password input field with id "password"
    And I should see "Guardar Credenciales" button
    And I should see "Correo activo" section
    And I should see "Consultar correo activo" button
    And the save button should be disabled initially

  @email_config @positive @credentials @critical
  Scenario: Successfully save valid email credentials
    And I enter email "automation@empresa.com"
    And I enter password "SecurePassword123"
    And I click the "Guardar Credenciales" button
    Then I should see success message "Credenciales guardadas correctamente."
    And the email field should be cleared
    And the "Correo activo" section should display "automation@empresa.com"
    And the "Correo activo" section should display a timestamp

  @email_config @positive @credentials
  Scenario: Successfully update existing email credentials
    Given email credentials "oldtest@empresa.com" are already saved
    And I enter email "newtest@empresa.com"
    And I enter password "NewPassword456"
    And I click the "Guardar Credenciales" button
    Then I should see success message "Credenciales guardadas correctamente."
    And the "Correo activo" section should display "newtest@empresa.com"

  @email_config @positive @active_email
  Scenario: Successfully query active email configuration
    Given valid email credentials are already configured
    And I click the "Consultar correo activo" button
    Then the "Usuario" field should display the saved email
    And the "Configurado" field should display a valid timestamp

  @email_config @positive @persistence
  Scenario: Email credentials persist after page refresh
    Given valid email credentials "persistent1@empresa.com" are saved
    When I refresh the page
    Then the "Correo activo" section should still display "persistent1@empresa.com"

  @email_config @positive @email_formats
  Scenario: Accept email with special characters in local part
    And I enter email "user+automation@empresa.com"
    And I enter password "SecurePass123"
    And I click the "Guardar Credenciales" button
    Then the credentials should be saved successfully

  @email_config @positive @security
  Scenario: Password field masks input for security
    And I enter password "MySecurePass123!"
    Then the password field should display masked characters

  @email_config @positive @button_state
  Scenario: Save button enables only when both fields are filled
    Then the save button should be disabled
    When I enter email "test@empresa.com"
    Then the save button should still be disabled
    When I enter password "Password123"
    Then the save button should be enabled

  # ===== NEGATIVE TEST CASES =====

  @email_config @negative @validation @critical
  Scenario: Prevent save with empty email field
    And I leave the email field empty
    And I enter password "password123"
    Then the save button should be disabled

  @email_config @negative @validation @critical
  Scenario: Prevent save with empty password field
    And I enter email "test@empresa.com"
    And I leave the password field empty
    Then the save button should be disabled

  @email_config @negative @validation
  Scenario: Reject any text in email field (client validation enforced)
    And I enter email "invalidemail"
    And I enter password "password123"
    Then the save button should be disabled

  @email_config @negative @email_formats
  Scenario: Reject email missing @ symbol
    And I enter email "testemp.com"
    And I enter password "password123"
    Then the save button should be disabled

  @email_config @negative @password_strength
  Scenario: Accept very short password
    And I enter email "test@empresa.com"
    And I enter password "a"
    And I click the "Guardar Credenciales" button
    Then the credentials should be saved successfully

  @email_config @negative @whitespace
  Scenario: Preserve spaces in password field
    And I enter email "test@empresa.com"
    And I enter password "  password123  "
    And I click the "Guardar Credenciales" button
    Then the credentials should be saved successfully

  @email_config @negative @case_sensitivity
  Scenario: Accept uppercase emails
    And I enter email "TEST@EMPRESA.COM"
    And I enter password "password123"
    And I click the "Guardar Credenciales" button
    Then the credentials should be saved successfully

  @email_config @negative @security
  Scenario: Prevent SQL injection attempt in email
    And I enter email "test@empresa.com' OR '1'='1"
    And I enter password "password123"
    Then the save button should be disabled

  @email_config @negative @security
  Scenario: Prevent XSS attack in email field
    And I enter email "<script>alert('xss')</script>@empresa.com"
    And I enter password "password123"
    And I click the "Guardar Credenciales" button
    Then no JavaScript should be executed

# Permission scenarios do not use the Background since they test non-admin access
  @email_config @negative @permissions
  Scenario: Finance role cannot access Email Config
    Given I navigate to the InvoiceExtract frontend
    When I login with "finance" credentials
    Then I should be successfully logged in
    And I should not see the "Email Config" link

  @email_config @negative @permissions
  Scenario: Technician role cannot access Email Config
    Given I navigate to the InvoiceExtract frontend
    When I login with "technician" credentials
    Then I should be successfully logged in
    And I should not see the "Email Config" link

