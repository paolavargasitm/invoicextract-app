Feature: Invoices Filters

  Background:
    Given I navigate to the InvoiceExtract frontend
    When I login with admin credentials
    Then I should be successfully logged in
    And I can see the "dashboard facturas" view

  Scenario: Buscar facturas con filtros válidos usando datos de la API
    Given I'm on the invoices page
    When I fill the filters with data from API
    And I click Buscar
    Then the totals should update

  Scenario: Limpiar filtros restablece los totales
    Given I'm on the invoices page
    When I fill the filters with data from API
    And I click Limpiar Filtros
    Then the totals should reset

  Scenario: Buscar facturas con rango de fechas inválido
    Given I'm on the invoices page
    When I fill the filters with invalid date range
    And I click Buscar
    Then I should see a validation error or no results
