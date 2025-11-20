Feature: Mapping Validations

  Background:
    Given I navigate to the InvoiceExtract frontend
    When I login with admin credentials
    Then I should be successfully logged in
    And I can see the "mapeos" view


  @positive
  Scenario: Should allow adding a valid mapping
    When I click the "Crear" button
    And I fill in valid mapping information
    And I submit the new mapping
    Then the new mapping exists in the list

  @negative
  Scenario: Should display the list of mappings correctly
    When I click the "Crear" button
    Then I should see the alert modal

  @negative @bug
  Scenario: Should show error for duplicate mapping
    When I click the "Crear" button
    And I fill in mapping information that already exists
    And I submit the new mapping
    Then the new mapping exists in the list


 @positive
  Scenario: Should display all transformation options in the select dropdown
    When I click on the transformation select dropdown
    Then I should see the following transformation options:
      | Sin transformación              |
      | TRIM                            |
      | UPPER                           |
      | DATE_FMT (requiere formato)     |
      | FIRST (listas)                  |
      | SUM (listas numéricas)          |
      | JOIN (requiere separador)       |

  @positive
  Scenario: Should allow creating a mapping with TRIM transformation
    When I fill in "sourceField" with "unique_trim_{{timestamp}}"
    And I fill in "targetField" with "unique_trim_target_{{timestamp}}"
    And I select "TRIM" from the transformation dropdown
    And I fill in any additional required fields for the transformation
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping with UPPER transformation
    When I fill in "sourceField" with "unique_upper_{{timestamp}}"
    And I fill in "targetField" with "unique_upper_target_{{timestamp}}"
    And I select "UPPER" from the transformation dropdown
    And I fill in any additional required fields for the transformation
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping with DATE_FMT transformation
    When I fill in "sourceField" with "unique_date_{{timestamp}}"
    And I fill in "targetField" with "unique_date_target_{{timestamp}}"
    And I select "DATE_FMT (requiere formato)" from the transformation dropdown
    And I fill in any additional required fields for the transformation
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping with Sin transformación
    When I fill in "sourceField" with "unique_sin_trans_{{timestamp}}"
    And I fill in "targetField" with "unique_sin_trans_target_{{timestamp}}"
    And I select "Sin transformación" from the transformation dropdown
    And I fill in any additional required fields for the transformation
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping with FIRST transformation
    When I fill in "sourceField" with "unique_first_{{timestamp}}"
    And I fill in "targetField" with "unique_first_target_{{timestamp}}"
    And I select "FIRST (listas)" from the transformation dropdown
    And I fill in any additional required fields for the transformation
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping with SUM transformation
    When I fill in "sourceField" with "unique_sum_{{timestamp}}"
    And I fill in "targetField" with "unique_sum_target_{{timestamp}}"
    And I select "SUM (listas numéricas)" from the transformation dropdown
    And I fill in any additional required fields for the transformation
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping with JOIN transformation
    When I fill in "sourceField" with "unique_join_{{timestamp}}"
    And I fill in "targetField" with "unique_join_target_{{timestamp}}"
    And I select "JOIN (requiere separador)" from the transformation dropdown
    And I fill in any additional required fields for the transformation
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping with UPPER transformation
    When I fill in "sourceField" with "unique_nombre_{{timestamp}}"
    And I fill in "targetField" with "unique_name_{{timestamp}}"
    And I select "UPPER" from the transformation dropdown
    And I click the "Crear" button
    Then I should not see validation errors
    And the new mapping with unique values exists in the list

  @positive
  Scenario: Should allow creating a mapping without transformation
    When I fill in "sourceField" with "email"
    And I fill in "targetField" with "mail"
    And I select "Sin transformación" from the transformation dropdown
    And I click the "Crear" button
    Then I should not see validation errors
    And the mapping should be created successfully

  @negative
  Scenario: Should show validation error when submitting without required fields
    When I select "UPPER" from the transformation dropdown
    And I click the "Crear" button
    Then I should see an alert with message "Campos obligatorios: sourceField y targetField"

  @negative
  Scenario: Should show validation error when sourceField is empty
    When I fill in "targetField" with "destination"
    And I select "TRIM" from the transformation dropdown
    And I click the "Crear" button
    Then I should see an alert with message "Campos obligatorios: sourceField y targetField"

  @negative
  Scenario: Should show validation error when targetField is empty
    When I fill in "sourceField" with "source"
    And I select "TRIM" from the transformation dropdown
    And I click the "Crear" button
    Then I should see an alert with message "Campos obligatorios: sourceField y targetField"

  @positive
  Scenario: Should maintain selected transformation after validation error
    When I select "UPPER" from the transformation dropdown
    And I click the "Crear" button
    Then I should see an alert with message "Campos obligatorios: sourceField y targetField"
    And the transformation dropdown should display "UPPER"

  @positive
  Scenario: Should filter mappings by 'Activas' status
    When I select "Activas" from the status filter
    Then I should see only active mappings in the list

  @positive
  Scenario: Should filter mappings by 'Inactivas' status
    When I select "Inactivas" from the status filter
    Then I should see only inactive mappings in the list

  @positive
  Scenario: Should refresh the mappings list
    When I click the "Refrescar" button
    Then the mappings list should be refreshed

  @positive
  Scenario: Should deactivate a mapping and move it to inactive list
    Given I select "Activas" from the status filter
    When I click the "Desactivar" button for the first mapping
    And I select "Inactivas" from the status filter
    Then the deactivated mapping should appear in the inactive list

  @positive
  Scenario: Should update a mapping and save changes
    When I update the sourceField of the first mapping to "updated_source_{{timestamp}}"
    And I update the targetField of the first mapping to "updated_target_{{timestamp}}"
    And I click the "Guardar" button for the first mapping
    Then the mapping should be updated with the new values
    And the updated mapping should exist in the list
