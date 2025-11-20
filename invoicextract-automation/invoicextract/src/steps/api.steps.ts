import { Given, When, Then, DataTable } from '@cucumber/cucumber';
import { expect, request, APIResponse } from '@playwright/test';
import { CustomWorld } from '../support/world';
import { UserApiClient } from '../api/user-api-client';
import { TestDataHelper } from '../support/test-data-helper';
import { TestLogger } from '../support/logger';
import { AuthHelper } from '../api/auth-helper';
import * as dotenv from 'dotenv';

dotenv.config();

interface ApiWorld extends CustomWorld {
  apiResponse?: APIResponse;
  userApiClient?: UserApiClient;
  logger?: TestLogger;
  storedValues?: Map<string, any>;
}

interface ResponseExpectation {
  testCaseId: string;
  statusCode: number;
  responseSchema: Record<string, string> | null;
  requiredFields: string[];
  optionalFields: string[];
}

Given('an API context is configured', async function (this: ApiWorld) {
  if (!process.env.API_BASE_URL) {
    throw new Error('API_BASE_URL not configured in .env');
  }
  
  // Create authenticated context using AuthHelper
  this.apiContext = await AuthHelper.createAuthenticatedContext();
  
  // Initialize logger and stored values
  this.logger = new TestLogger(this);
  this.storedValues = new Map<string, any>();
  
  if (this.logger) {
    this.logger.logInfo('AUTHENTICATION', 'API context authenticated with fresh token');
  }
});

When('I send a GET request to {string}', async function (this: ApiWorld, endpoint: string) {
  if (!this.apiContext) throw new Error('API context not initialized');
  this.userApiClient = new UserApiClient(this.apiContext);
  
  // Replace placeholders with stored values
  const resolvedEndpoint = replacePlaceholders(endpoint, this.storedValues);
  
  if (this.logger) {
    this.logger.logInfo('REQUEST', `GET ${process.env.API_BASE_URL}${resolvedEndpoint}`);
  }
  
  this.apiResponse = await this.userApiClient.get(resolvedEndpoint);
});

When('I send a POST request to {string} with data:', async function (this: ApiWorld, endpoint: string, dataTable: DataTable) {
  if (!this.apiContext) throw new Error('API context not initialized');
  this.userApiClient = new UserApiClient(this.apiContext);
  const data = dataTable.rowsHash();
  this.apiResponse = await this.userApiClient.post(endpoint, data);
});

When('I send a POST request to {string} with data {string}', async function (this: ApiWorld, endpoint: string, testCaseId: string) {
  if (!this.apiContext) throw new Error('API context not initialized');
  this.userApiClient = new UserApiClient(this.apiContext);
  const data = TestDataHelper.getTestData('invoices.json', testCaseId);
  this.apiResponse = await this.userApiClient.post(endpoint, data);
});

When('I send a PUT request to {string} with data:', async function (this: ApiWorld, endpoint: string, dataTable: DataTable) {
  if (!this.apiContext) throw new Error('API context not initialized');
  this.userApiClient = new UserApiClient(this.apiContext);
  const data = dataTable.rowsHash();
  this.apiResponse = await this.userApiClient.put(endpoint, data);
});

When('I send a DELETE request to {string}', async function (this: ApiWorld, endpoint: string) {
  if (!this.apiContext) throw new Error('API context not initialized');
  this.userApiClient = new UserApiClient(this.apiContext);
  
  // Replace placeholders with stored values
  const resolvedEndpoint = replacePlaceholders(endpoint, this.storedValues);
  
  this.apiResponse = await this.userApiClient.delete(resolvedEndpoint);
});

When('I send a PUT request to {string} with data {string}', async function (this: ApiWorld, endpoint: string, testCaseId: string) {
  if (!this.apiContext) throw new Error('API context not initialized');
  this.userApiClient = new UserApiClient(this.apiContext);
  
  // Replace placeholders with stored values
  const resolvedEndpoint = replacePlaceholders(endpoint, this.storedValues);
  
  const data = TestDataHelper.getTestData('invoices.json', testCaseId);
  this.apiResponse = await this.userApiClient.put(resolvedEndpoint, data);
});

// Helper function to replace placeholders in endpoint URLs
function replacePlaceholders(endpoint: string, storedValues?: Map<string, any>): string {
  if (!storedValues) return endpoint;
  
  let resolvedEndpoint = endpoint;
  storedValues.forEach((value, key) => {
    resolvedEndpoint = resolvedEndpoint.replace(`{${key}}`, String(value));
  });
  return resolvedEndpoint;
}

Then('the response status should be {int}', async function (this: ApiWorld, expectedStatus: number) {
  if (!this.apiResponse) throw new Error('No API response available');
  const actualStatus = this.apiResponse.status();
  const passed = actualStatus === expectedStatus;
  
  if (this.logger) {
    this.logger.logComparison('Status Code', expectedStatus, actualStatus, passed);
  }
  
  expect(actualStatus).toBe(expectedStatus);
});

Then('the response should contain property {string}', async function (this: ApiWorld, property: string) {
  if (!this.apiResponse || !this.userApiClient) throw new Error('No API response available');
  const responseData = await this.userApiClient.getJsonResponse(this.apiResponse);
  const hasProperty = responseData.hasOwnProperty(property);
  
  if (this.logger) {
    this.logger.logComparison(
      `Property "${property}" exists`,
      true,
      hasProperty,
      hasProperty
    );
  }
  
  expect(responseData).toHaveProperty(property);
});

Then('the response should match expectations for {string}', async function (this: ApiWorld, testCaseId: string) {
  if (!this.apiResponse || !this.userApiClient) {
    throw new Error('No API response available');
  }
  
  if (!this.logger) {
    this.logger = new TestLogger(this);
  }

  // Load expectations from JSON
  const expectation = TestDataHelper.getTestData<ResponseExpectation>('response-expectations.json', testCaseId);
  
  this.logger.logInfo('TEST CASE', testCaseId);
  this.logger.logExpected(expectation);
  
  // Validate status code
  const actualStatus = this.apiResponse.status();
  const statusPassed = actualStatus === expectation.statusCode;
  
  // Log response body for non-2xx responses for debugging
  if (actualStatus >= 400) {
    const responseText = await this.apiResponse.text();
    this.logger.logInfo('ERROR RESPONSE', responseText || '(empty response body)');
  }
  
  this.logger.logComparison('Status Code', expectation.statusCode, actualStatus, statusPassed);
  expect(actualStatus).toBe(expectation.statusCode);
  
  // If status code is 204 (No Content), no need to validate response body
  if (expectation.statusCode === 204) {
    this.logger.logInfo('VALIDATION', 'No response body expected for 204 status');
    this.logger.logValidationSummary(1, 1, 0);
    return;
  }
  
  // Get response body
  const responseData = await this.userApiClient.getJsonResponse(this.apiResponse);
  this.logger.logResponse(responseData);
  
  let totalChecks = 1; // Status code already checked
  let passedChecks = statusPassed ? 1 : 0;
  let failedChecks = statusPassed ? 0 : 1;
  
  // Validate required fields exist
  for (const field of expectation.requiredFields) {
    totalChecks++;
    const hasField = responseData.hasOwnProperty(field);
    if (hasField) {
      passedChecks++;
      this.logger.logComparison(`Required Field: ${field}`, 'exists', 'exists', true);
    } else {
      failedChecks++;
      this.logger.logComparison(`Required Field: ${field}`, 'exists', 'missing', false);
    }
    expect(responseData).toHaveProperty(field);
  }
  
  // Validate field types if schema is provided
  if (expectation.responseSchema) {
    for (const [field, expectedType] of Object.entries(expectation.responseSchema)) {
      if (responseData.hasOwnProperty(field)) {
        totalChecks++;
        const actualType = typeof responseData[field];
        const typePassed = actualType === expectedType;
        if (typePassed) {
          passedChecks++;
        } else {
          failedChecks++;
        }
        this.logger.logComparison(
          `Field Type: ${field}`,
          expectedType,
          actualType,
          typePassed
        );
        expect(typeof responseData[field]).toBe(expectedType);
      }
    }
  }
  
  // Log validation summary
  this.logger.logValidationSummary(totalChecks, passedChecks, failedChecks);
});

Then('I store the response property {string} as {string}', async function (this: ApiWorld, propertyPath: string, variableName: string) {
  if (!this.apiResponse || !this.userApiClient) {
    throw new Error('No API response available');
  }
  
  const responseData = await this.userApiClient.getJsonResponse(this.apiResponse);
  
  // Support nested property paths like "data.id"
  const value = getNestedProperty(responseData, propertyPath);
  
  if (value === undefined) {
    throw new Error(`Property "${propertyPath}" not found in response`);
  }
  
  if (!this.storedValues) {
    this.storedValues = new Map<string, any>();
  }
  
  this.storedValues.set(variableName, value);
  
  if (this.logger) {
    this.logger.logInfo('STORED VALUE', `${variableName} = ${value}`);
  }
});

// Helper function to get nested property from object
function getNestedProperty(obj: any, path: string): any {
  return path.split('.').reduce((current, prop) => current?.[prop], obj);
}


// In api.steps.users.ts
Then('the response should contain a list of users', async function (this: ApiWorld) {
  if (!this.apiResponse || !this.userApiClient) throw new Error('No API response available');
  const data = await this.userApiClient.getJsonResponse(this.apiResponse);
  expect(Array.isArray(data)).toBe(true);
});
Then('the response should contain the created user', async function (this: ApiWorld) {
  if (!this.apiResponse || !this.userApiClient) throw new Error('No API response available');
  const data = await this.userApiClient.getJsonResponse(this.apiResponse);
  expect(data).toHaveProperty("id");
  expect(data).toHaveProperty("name");
  expect(data).toHaveProperty("email");
});
Then('the response should contain user details', async function (this: ApiWorld) {
  if (!this.apiResponse || !this.userApiClient) throw new Error('No API response available');
  const data = await this.userApiClient.getJsonResponse(this.apiResponse);
  expect(data).toHaveProperty("id");
  expect(data).toHaveProperty("name");
  expect(data).toHaveProperty("email");
});
Then('the response should contain the updated user', async function (this: ApiWorld) {
  if (!this.apiResponse || !this.userApiClient) throw new Error('No API response available');
  const data = await this.userApiClient.getJsonResponse(this.apiResponse);
  expect(data).toHaveProperty("id");
  expect(data).toHaveProperty("name");
  expect(data).toHaveProperty("email");
});
