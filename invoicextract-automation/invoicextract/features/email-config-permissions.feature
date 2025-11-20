Feature: Email Config Access Control
  As a system administrator
  I want to ensure only admin users can access Email Config
  So that sensitive email credentials are protected

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
