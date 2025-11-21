Feature: Invoice Management API

  Background:
    Given an API context is configured

  @api
  Scenario: Create an invoice (Sync)
    When I send a POST request to "/api/invoices" with data "sell-invoice"
    Then the response should match expectations for "create-invoice"

  @api
  Scenario: Get all invoices
    When I send a GET request to "/api/invoices"
    Then the response should match expectations for "get-all-invoices"

  @api
  Scenario: Get invoices by status - Pending
    When I send a GET request to "/api/invoices/status/PENDING"
    Then the response status should be 200

  @api
  Scenario: Get recent invoices
    When I send a GET request to "/api/invoices/recent?limit=10"
    Then the response status should be 200

  @api
  Scenario: Get dashboard statistics
    When I send a GET request to "/api/invoices/dashboard/stats"
    Then the response status should be 200


  @api
  Scenario: Create new invoice
    When I send a POST request to "/api/invoices" with data "service-invoice"
    Then the response should match expectations for "create-invoice"

  @api
  Scenario: Full invoice lifecycle - Create, Get, Update, Delete
    # Create invoice
    When I send a POST request to "/api/invoices" with data "update-invoice"
    Then the response should match expectations for "create-invoice"
    And I store the response property "id" as "invoiceId"
    
    # Get the created invoice
    When I send a GET request to "/api/invoices/{invoiceId}"
    Then the response should match expectations for "get-invoice-by-id"
    
    # Update the invoice
    When I send a PUT request to "/api/invoices/{invoiceId}" with data "update-invoice"
    Then the response should match expectations for "update-invoice"
    
    # Delete the invoice
    When I send a DELETE request to "/api/invoices/{invoiceId}"
    Then the response should match expectations for "delete-invoice"