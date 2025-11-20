Feature: ERP Configuration

  Background:
    Given I navigate to the InvoiceExtract frontend
    When I login with admin credentials
    Then I should be successfully logged in
    And I can see the "erps" view

  @positive
  Scenario: Should display ERP configuration page correctly
    Then I should see the "Administración de ERPs" section
    And I should see the "Refrescar" button
    And I should see the "Crear nuevo ERP" section
    And I should see the ERP list table with columns "ID", "Nombre", "Estado"

  @positive
  Scenario: Should refresh ERP list when clicking Refrescar button
    When I click the "Refrescar" button
    Then the ERP list should be reloaded
    And I should see existing ERPs in the table

  @positive
  Scenario: Should create a new ERP with valid name
    When I enter "Oracle ERP" in the ERP name field
    And I click the "Crear" button
    Then I should see a success message for ERP creation
    And the new ERP "Oracle ERP" should appear in the list
    And the new ERP should have "Activo" status

  @negative
  Scenario: Should show validation error when creating ERP without name
    When I leave the ERP name field empty
    And I click the "Crear" button
    Then I should see a validation error message
    And no new ERP should be created

  @negative
  Scenario: Should show error when creating duplicate ERP name
    Given an ERP with name "SAP" already exists
    When I enter "SAP" in the ERP name field
    And I click the "Crear" button
    Then I should see an error message indicating duplicate name
    And no new ERP should be created

  @positive
  Scenario: Should activate an inactive ERP
    Given an ERP with "Inactivo" status exists
    When I click the "Activar" button for that ERP
    Then the ERP status should change to "Activo"
    And the "Activar" button should change to "Desactivar"
    And I should see a success message for activation

  @positive
  Scenario: Should deactivate an active ERP
    Given an ERP with "Activo" status exists
    When I click the "Desactivar" button for that ERP
    Then the ERP status should change to "Inactivo"
    And the "Desactivar" button should change to "Activar"
    And I should see a success message for deactivation

  @positive
  Scenario: Should toggle ERP status multiple times
    Given an ERP with "Inactivo" status exists
    When I click the "Activar" button for that ERP
    Then the ERP status should change to "Activo"
    When I click the "Desactivar" button for that ERP
    Then the ERP status should change to "Inactivo"
    When I click the "Activar" button for that ERP
    Then the ERP status should change to "Activo"

  @negative
  Scenario: Should handle network error gracefully when creating ERP
    Given the backend service is unavailable
    When I enter "New ERP" in the ERP name field
    And I click the "Crear" button
    Then I should see a network error message
    And the ERP should not be added to the list

  @negative
  Scenario: Should handle network error when toggling ERP status
    Given the backend service is unavailable
    And an ERP with "Activo" status exists
    When I click the "Desactivar" button for that ERP
    Then I should see a network error message
    And the ERP status should remain "Activo"

  @positive
  Scenario: Should clear input field after successful ERP creation
    When I enter "Microsoft Dynamics" in the ERP name field
    And I click the "Crear" button
    Then I should see a success message for ERP creation
    And the ERP name field should be cleared
