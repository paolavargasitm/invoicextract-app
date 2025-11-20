import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright Configuration for InvoiceExtract Automation
 * 
 * This configuration sets up Playwright for use with Cucumber BDD tests.
 * It defines base URLs, browser configurations, and other test settings.
 */
export default defineConfig({
  // Test directory - although we use Cucumber, this helps with type definitions
  testDir: './src',
  
  // Maximum time one test can run for
  timeout: 30 * 1000,
  
  // Run tests in files in parallel
  fullyParallel: true,
  
  // Fail the build on CI if you accidentally left test.only in the source code
  forbidOnly: !!process.env.CI,
  
  // Retry on CI only
  retries: process.env.CI ? 2 : 0,
  
  // Opt out of parallel tests on CI
  workers: process.env.CI ? 1 : undefined,
  
  // Reporter to use
  reporter: [
    ['html', { outputFolder: 'reports/playwright-report' }],
    ['list'],
  ],
  
  // Shared settings for all the projects below
  use: {
    // Base URL to use in actions like `await page.goto('/')`.
    baseURL: process.env.BASE_URL || 'http://localhost:3001',
    
    // Collect trace when retrying the failed test
    trace: 'on-first-retry',
    
    // Screenshot on failure
    screenshot: 'only-on-failure',
    
    // Video on failure
    video: 'retain-on-failure',
    
    // Browser viewport size
    viewport: { width: 1280, height: 720 },
    
    // Maximum time each action such as `click()` can take
    actionTimeout: 10 * 1000,
    
    // Navigation timeout
    navigationTimeout: 30 * 1000,
  },

  // Configure projects for major browsers
  projects: [
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        // You can override baseURL per project if needed
        // baseURL: 'http://localhost:3001',
      },
    },

    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
      },
    },

    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
      },
    },

    // Mobile viewports
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },
  ],

  // Run your local dev server before starting the tests (optional)
  // Uncomment and configure if you want Playwright to start your servers
  // webServer: [
  //   {
  //     command: 'npm run start:frontend',
  //     url: 'http://localhost:3001',
  //     reuseExistingServer: !process.env.CI,
  //     timeout: 120 * 1000,
  //   },
  //   {
  //     command: 'npm run start:keycloak',
  //     url: 'http://localhost:8085',
  //     reuseExistingServer: !process.env.CI,
  //     timeout: 120 * 1000,
  //   },
  // ],
});

/**
 * Custom URLs for the InvoiceExtract application
 * These can be imported and used in your test files
 */
export const APP_URLS = {
  // Frontend application URL
  frontend: process.env.FRONTEND_URL || 'http://localhost:3001',
  
  // Keycloak admin console URL (generates tokens)
  keycloakAdmin: process.env.KEYCLOAK_ADMIN_URL || 'http://localhost:8085/admin/master/console/#/invoicextract/users',
  
  // Keycloak base URL
  keycloakBase: process.env.KEYCLOAK_BASE_URL || 'http://localhost:8085',
} as const;
