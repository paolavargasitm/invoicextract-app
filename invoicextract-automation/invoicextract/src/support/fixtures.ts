import { test as base, Page, Browser, BrowserContext } from '@playwright/test';
import { APP_URLS } from '../../playwright.config';

/**
 * Custom fixture types for the InvoiceExtract application
 */
export type CustomFixtures = {
  /**
   * Frontend application URL
   */
  frontendURL: string;
  
  /**
   * Keycloak admin console URL
   */
  keycloakAdminURL: string;
  
  /**
   * Keycloak base URL
   */
  keycloakBaseURL: string;
  
  /**
   * Authenticated page with token
   */
  authenticatedPage: Page;
};

/**
 * Extend Playwright test with custom fixtures
 * 
 * Usage in Cucumber world:
 * import { test } from './fixtures';
 * 
 * Then you can access fixtures like:
 * const { frontendURL, keycloakAdminURL } = test.info();
 */
export const test = base.extend<CustomFixtures>({
  /**
   * Frontend URL fixture
   * Provides the frontend application URL
   */
  frontendURL: async ({}, use) => {
    await use(APP_URLS.frontend);
  },

  /**
   * Keycloak Admin URL fixture
   * Provides the Keycloak admin console URL
   */
  keycloakAdminURL: async ({}, use) => {
    await use(APP_URLS.keycloakAdmin);
  },

  /**
   * Keycloak Base URL fixture
   * Provides the Keycloak base URL
   */
  keycloakBaseURL: async ({}, use) => {
    await use(APP_URLS.keycloakBase);
  },

  /**
   * Authenticated Page fixture
   * Creates a page with authentication token
   * This is a worker-scoped fixture that sets up authentication once per worker
   */
  authenticatedPage: async ({ browser }, use) => {
    // Create a new page
    const context = await browser.newContext();
    const page = await context.newPage();

    // TODO: Implement your authentication logic here
    // Example:
    // 1. Navigate to keycloak login
    // 2. Get the token
    // 3. Store it in the context/cookies/localStorage
    
    // For now, just navigate to the frontend
    await page.goto(APP_URLS.frontend);

    // Use the authenticated page
    await use(page);

    // Cleanup
    await context.close();
  },
});

export { expect } from '@playwright/test';

/**
 * Helper function to get auth token from Keycloak
 * You can implement this based on your authentication requirements
 */
export async function getAuthToken(
  page: Page,
  username: string,
  password: string
): Promise<string> {
  // TODO: Implement token retrieval logic
  // Example:
  // 1. Navigate to Keycloak token endpoint
  // 2. Submit credentials
  // 3. Extract and return the token
  
  // Placeholder implementation
  console.log('Getting auth token for:', username);
  return 'placeholder-token';
}

/**
 * Helper function to set auth token in page context
 */
export async function setAuthToken(
  page: Page,
  token: string
): Promise<void> {
  // TODO: Implement token setting logic
  // Example: Set token in localStorage, cookies, or headers
  
  await page.evaluate((authToken) => {
    localStorage.setItem('authToken', authToken);
  }, token);
}

/**
 * URL configuration object
 * Export this to use in your tests without importing the entire config
 */
export const urls = APP_URLS;
