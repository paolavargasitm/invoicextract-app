Feature: InvoiceExtract Login
  As a user of the InvoiceExtract application
  I want to be able to login with my credentials
  So that I can access the application features

  @smoke @login
  Scenario: Successful login with valid admin credentials
    Given I navigate to the InvoiceExtract frontend
    When I login with admin credentials
    Then I should be successfully logged in
    And I should see the dashboard

  @login @roles
  Scenario Outline: Login using role-based credentials from environment
    Given I navigate to the InvoiceExtract frontend
    When I login with "<role>" credentials
    Then I should be successfully logged in
    And I should see the dashboard

    Examples:
      | role       |
      | admin      |
      | finance    |
      | technician |

  @smoke @login @multi-view
  Scenario: Successful login and access to all views
    Given I navigate to the InvoiceExtract frontend
    When I login with admin credentials
    Then I should be successfully logged in
    And I can see the "Home" view
    And I can see the "Email Config" view
    And I can see the "Invoice Dashboard" view
    And I can see the "Mapping" view
    And I can see the "ERP Config" view

  @login @views
  Scenario: Validate all views are available after login
    Given I navigate to the InvoiceExtract frontend
    When I login with admin credentials
    Then I should be successfully logged in
    And all views are available

  @login @views @roles
  Scenario Outline: Access views with different roles
    Given I navigate to the InvoiceExtract frontend
    When I login with "<role>" credentials
    Then I should be successfully logged in
    And I can see the "<view>" view

    Examples:
      | role       | view               |
      | admin      | Home               |
      | admin      | Email Config       |
      | admin      | Invoice Dashboard  |
      | admin      | Mapping            |
      | admin      | ERP Config         |
      | finance    | Home               |
      | finance    | Invoice Dashboard  |
      | technician | Home               |
      | technician | Mapping            |
