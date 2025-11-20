Feature: Invoice Detail Modal
  As a user of the InvoiceExtract system
  I want to view detailed information about invoices
  So that I can review and take actions on them

  Background:
    Given I am on the invoices dashboard page
    And the invoice table is loaded with data

  # ============================================================================
  # SCENARIOS WITHOUT TAGS - POSITIVE TEST SCENARIOS
  # ============================================================================

  Scenario: Successfully open invoice detail modal
    When I click on the "Ver Detalle" button for invoice ID 1
    Then the invoice detail modal should be displayed
    And the modal title should show "Detalle de Factura: 1"
    And the "Volver" button should be visible

  Scenario: Verify PDF viewer is displayed in detail modal
    When I click on the "Ver Detalle" button for invoice ID 1
    Then the PDF viewer should be visible in the modal
    And the PDF should be loaded successfully

  Scenario: Verify invoice items table is displayed
    When I click on the "Ver Detalle" button for invoice ID 1
    And I scroll to the items table section in the modal
    Then the "Items de la Factura" section should be visible
    And the items table should display data matching the API items for invoice ID 1

  Scenario: Verify action buttons are displayed
    When I click on the "Ver Detalle" button for invoice ID 1
    Then the "Acciones de Revisión" section should be visible
    And the following action buttons should be present:
      | Aprobar Factura  |
      | Rechazar Factura |
      | Descargar PDF    |
      | Descargar XML    |

  Scenario: Open PDF in new tab from detail modal
    When I click on the "Ver Detalle" button for invoice ID 1
    And I click on the "Abrir PDF en nueva pestaña" link
    Then a new browser tab should open with the PDF

  # ============================================================================
  # SCENARIOS WITHOUT TAGS - NEGATIVE TEST SCENARIOS
  # ============================================================================

  Scenario: Verify modal prevents interaction with background
    When I click on the "Ver Detalle" button for invoice ID 1
    And I attempt to click on the dashboard table behind the modal
    Then the click should not be registered
    And the modal should remain open

  Scenario: Attempt to approve already approved invoice
    Given the invoice detail modal is open for an invoice with status "Aprobada"
    When I click on the "Aprobar Factura" button
    Then an appropriate message should be displayed
    And the status should remain "Aprobada"

  Scenario: Attempt to reject already rejected invoice
    When I click on the "Ver Detalle" button for invoice ID 1
    And the invoice status is "Rechazada"
    When I click on the "Rechazar Factura" button
    Then an appropriate message should be displayed
    And the status should remain "Rechazada"

  Scenario: Verify modal responsiveness on small screen
    Given the browser window is resized to mobile dimensions
    When I click on the "Ver Detalle" button for invoice ID 1
    Then the modal should display properly
    And all elements should be accessible

  Scenario: Handle concurrent actions on same invoice
    When I click on the "Ver Detalle" button for invoice ID 3
    And I quickly click "Aprobar Factura" and "Rechazar Factura" consecutively
    Then only the first action should be processed
    And an appropriate message should inform about the conflict

  