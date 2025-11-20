Feature: Invoice Dashboard Table
  As a user of the InvoiceExtract system
  I want to view invoice information in the dashboard table
  So that I can see a list of all invoices with their key details

  Background:
    Given I am on the invoices dashboard page
    And the invoice table is loaded with data

  Scenario: Verify invoice table displays data matching API
    When I fetch invoice data for invoice ID 1 from the API
    Then the dashboard table should display the invoice information matching the API data for invoice ID 1

  Scenario: Verify all invoices in table match API data
    When I fetch all invoices from the API
    Then the dashboard table should display all invoices with correct information

  Scenario: Verify invoice table columns are present
    Then the invoice table should have the following columns:
      | ID | FECHA | NIT - EMISOR | MONTO | NIT - RECEPTOR | ESTADO | ACCIONES |

  Scenario: Verify invoice table displays correct data format
    Then the first invoice row should contain the following data:
      | Column         | Value Pattern                    |
      | ID             | numeric                          |
      | FECHA          | date format                      |
      | NIT - EMISOR   | NIT and supplier name           |
      | MONTO          | currency amount                  |
      | NIT - RECEPTOR | NIT and customer name           |
      | ESTADO         | status badge                     |
      | ACCIONES       | action button                    |

  Scenario: Verify invoice data values are properly formatted
    Then each invoice in the table should have:
      | Field          | Format                                    |
      | ID             | positive integer                          |
      | FECHA          | YYYY-MM-DD                                |
      | NIT - EMISOR   | digits - company name                     |
      | MONTO          | $ amount                                  |
      | NIT - RECEPTOR | digits - company name                     |
      | ESTADO         | Pendiente or Aprobada or Rechazada        |
      | ACCIONES       | Ver Detalle button                        |

  Scenario: Verify dashboard totals match invoice counts
    When I fetch all invoices from the API
    Then the "Facturas Ingresadas" total should match the total count from API
    And the "Pendientes" total should match the pending invoices count from API
    And the "Aprobadas" total should match the approved invoices count from API
    And the "Rechazadas" total should match the rejected invoices count from API

  Scenario: Verify dashboard amounts match API calculations
    When I fetch all invoices from the API
    Then the "Monto Aprobadas" should match the sum of approved invoices from API
    And the "Monto Total" should match the sum of all invoices from API
    And the "Monto Pendientes" should match the sum of pending invoices from API

  Scenario: Verify invoice table is sortable
    When I click on the "FECHA" column header
    Then the table should be sorted by date

  Scenario: Verify invoice status badges display correctly
    Then each invoice status in the table should have the appropriate badge styling:
      | Status      | Badge Style    |
      | Pendiente   | badge-warning  |
      | Aprobada    | badge-success  |
      | Rechazada   | badge-danger   |

  
