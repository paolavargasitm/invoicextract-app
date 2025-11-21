Feature: Email Configuration API

  Background:
    Given an API context is configured

  @api
  Scenario: Get active email configurations
    When I send a GET request to "/api/config/email/active"
    Then the response status should be 200

  @api
  Scenario: Get email configuration by username (non-existent)
    When I send a GET request to "/api/config/email/nonexistent@company.com"
    Then the response status should be 404
