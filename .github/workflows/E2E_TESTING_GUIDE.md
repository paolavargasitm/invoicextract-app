# E2E Testing with Playwright - Guide

## 🎭 Overview

The E2E test workflow (`e2e-tests.yml`) runs Playwright tests against your complete InvoiceExtract application running in Docker. All services use the localhost URLs you specified:

- **Frontend**: `http://localhost:3001`
- **Database Admin (Adminer)**: `http://localhost:8081`
- **Backend API**: `http://localhost:8080/invoicextract`
- **Keycloak**: `http://localhost:8085`

## 🚀 Quick Start

### 1. Workflow automatically installs Playwright
The workflow will:
- Install `@playwright/test` package
- Install Chromium browser
- Create a default `playwright.config.ts` if one doesn't exist
- Create sample tests if none exist

### 2. Your tests should be in:
```
invoice-extract-frontend-master/
  └── tests/
      ├── auth.spec.ts
      ├── invoices.spec.ts
      └── dashboard.spec.ts
```

### 3. Test structure example:
```typescript
import { test, expect } from '@playwright/test';

test.describe('Invoice Management', () => {
  test('should create a new invoice', async ({ page }) => {
    await page.goto('http://localhost:3001');
    
    // Your test logic here
    await page.click('button:has-text("New Invoice")');
    await page.fill('input[name="documentNumber"]', 'INV-2024-001');
    await page.click('button:has-text("Save")');
    
    await expect(page.locator('.success-message')).toBeVisible();
  });
});
```

## 📁 Project Structure

```
invoice-extract-frontend-master/
├── package.json                    # Playwright added as devDependency
├── playwright.config.ts            # Auto-generated if not exists
├── tests/                          # Your E2E tests go here
│   ├── auth.spec.ts
│   ├── invoices.spec.ts
│   ├── dashboard.spec.ts
│   └── api.spec.ts
├── playwright-report/              # HTML reports (after test run)
└── test-results/                   # Screenshots, videos, traces
```

## ✍️ Writing Tests

### Basic Page Test
```typescript
import { test, expect } from '@playwright/test';

test('should load invoice list', async ({ page }) => {
  await page.goto('http://localhost:3001/invoices');
  
  await expect(page.locator('h1')).toContainText('Invoices');
  await expect(page.locator('table')).toBeVisible();
});
```

### API Testing
```typescript
import { test, expect } from '@playwright/test';

test('should get invoices from API', async ({ request }) => {
  const response = await request.get('http://localhost:8080/invoicextract/api/invoices');
  
  expect(response.ok()).toBeTruthy();
  const data = await response.json();
  expect(Array.isArray(data)).toBeTruthy();
});
```

### Database Admin Test
```typescript
import { test, expect } from '@playwright/test';

test('should access Adminer', async ({ page }) => {
  await page.goto('http://localhost:8081');
  
  // Check Adminer login page
  await expect(page.locator('input[name="auth[server]"]')).toBeVisible();
  
  // Login to MySQL
  await page.fill('input[name="auth[server]"]', 'mysql');
  await page.fill('input[name="auth[username]"]', 'root');
  await page.fill('input[name="auth[password]"]', 'root');
  await page.click('input[type="submit"]');
  
  // Verify logged in
  await expect(page.locator('a:has-text("invoices")')).toBeVisible();
});
```

### Keycloak Authentication Test
```typescript
import { test, expect } from '@playwright/test';

test('should handle Keycloak login', async ({ page }) => {
  await page.goto('http://localhost:3001');
  
  // Wait for Keycloak redirect
  await page.waitForURL(/localhost:8085/);
  
  // Fill login form
  await page.fill('input[name="username"]', 'testuser');
  await page.fill('input[name="password"]', 'password');
  await page.click('input[type="submit"]');
  
  // Verify redirected back to app
  await expect(page).toHaveURL(/localhost:3001/);
});
```

## 🔧 Playwright Configuration

The workflow creates this default config:

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }]
  ],
  use: {
    baseURL: 'http://localhost:3001',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: undefined, // Services already running via docker-compose
});
```

## 🌐 Environment Variables Available

Your tests can access these environment variables:

```typescript
const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:3001';
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/invoicextract';
const DB_ADMIN_URL = process.env.DB_ADMIN_URL || 'http://localhost:8081';
const KEYCLOAK_URL = process.env.KEYCLOAK_URL || 'http://localhost:8085';
```

Example usage:
```typescript
test('should access all services', async ({ page, request }) => {
  const frontendUrl = process.env.FRONTEND_URL;
  const backendUrl = process.env.BACKEND_URL;
  
  await page.goto(frontendUrl);
  const response = await request.get(`${backendUrl}/actuator/health`);
  expect(response.ok()).toBeTruthy();
});
```

## 📊 Test Reports

After tests run, you'll get:

### 1. HTML Report
- Interactive report with test results
- Screenshots of failures
- Video recordings of failed tests
- Detailed test traces

### 2. JSON Results
- Programmatic access to test results
- Can be used for custom reporting

### 3. JUnit XML
- Compatible with CI/CD systems
- Automatically posted as PR comments

## 🎯 Best Practices

### 1. Use Page Objects
```typescript
// pages/InvoicePage.ts
export class InvoicePage {
  constructor(private page: Page) {}
  
  async goto() {
    await this.page.goto('http://localhost:3001/invoices');
  }
  
  async createInvoice(data: InvoiceData) {
    await this.page.click('button:has-text("New Invoice")');
    await this.page.fill('input[name="documentNumber"]', data.documentNumber);
    // ... more fields
    await this.page.click('button:has-text("Save")');
  }
}

// In test
import { InvoicePage } from './pages/InvoicePage';

test('create invoice', async ({ page }) => {
  const invoicePage = new InvoicePage(page);
  await invoicePage.goto();
  await invoicePage.createInvoice({ documentNumber: 'INV-001' });
});
```

### 2. Wait for API Calls
```typescript
test('should create invoice', async ({ page }) => {
  // Wait for API response
  const responsePromise = page.waitForResponse(
    response => response.url().includes('/api/invoices') && response.status() === 201
  );
  
  await page.click('button:has-text("Save")');
  await responsePromise;
  
  await expect(page.locator('.success-message')).toBeVisible();
});
```

### 3. Use Fixtures
```typescript
// fixtures.ts
import { test as base } from '@playwright/test';

type MyFixtures = {
  authenticatedPage: Page;
};

export const test = base.extend<MyFixtures>({
  authenticatedPage: async ({ page }, use) => {
    // Login logic
    await page.goto('http://localhost:3001');
    // ... perform login
    await use(page);
  },
});

// In test
import { test } from './fixtures';

test('use authenticated page', async ({ authenticatedPage }) => {
  await authenticatedPage.goto('http://localhost:3001/dashboard');
  // Already logged in!
});
```

## 🐛 Debugging

### Run tests locally with UI mode:
```bash
cd invoice-extract-frontend-master
npx playwright test --ui
```

### Run with headed browser:
```bash
npx playwright test --headed
```

### Debug specific test:
```bash
npx playwright test --debug auth.spec.ts
```

### View test report:
```bash
npx playwright show-report
```

## 📈 CI/CD Integration

The workflow runs automatically on:
- Push to `main` or `develop`
- Pull requests
- Manual trigger (Actions tab → Run workflow)

Test results appear as:
- PR comments
- GitHub Checks
- Downloadable artifacts

## 🚨 Troubleshooting

### Tests timing out?
- Increase timeout in config:
```typescript
use: {
  timeout: 60000, // 60 seconds
}
```

### Services not ready?
- The workflow waits 60 seconds for each service
- Check service logs in workflow output
- Increase sleep time in workflow if needed

### Tests pass locally but fail in CI?
- Check if you're using hardcoded URLs
- Use environment variables instead
- Verify timing/wait strategies

## 💡 Tips

1. **Keep tests independent** - Each test should work on its own
2. **Use meaningful selectors** - Prefer `data-testid` over CSS classes
3. **Add explicit waits** - Wait for elements/API calls to complete
4. **Clean up data** - Reset state between tests if needed
5. **Mock external APIs** - Don't depend on external services

## 📚 Resources

- [Playwright Documentation](https://playwright.dev)
- [Best Practices](https://playwright.dev/docs/best-practices)
- [API Reference](https://playwright.dev/docs/api/class-test)
- [Debugging Guide](https://playwright.dev/docs/debug)

## ✅ Sample Test Suite

Here's a complete example test suite you can use:

```typescript
// tests/complete-flow.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Invoice Extract - Complete Flow', () => {
  test.beforeEach(async ({ page }) => {
    // Setup before each test
  });

  test('should verify all services are running', async ({ page, request }) => {
    // Test frontend
    await page.goto('http://localhost:3001');
    await expect(page).toHaveTitle(/Invoice/i);
    
    // Test backend health
    const health = await request.get('http://localhost:8080/invoicextract/actuator/health');
    expect(health.ok()).toBeTruthy();
    
    // Test database admin
    await page.goto('http://localhost:8081');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should handle invoice creation flow', async ({ page }) => {
    await page.goto('http://localhost:3001');
    
    // Navigate to create invoice
    await page.click('text=New Invoice');
    
    // Fill form
    await page.fill('[name="documentNumber"]', 'INV-TEST-001');
    await page.fill('[name="amount"]', '1500.75');
    
    // Submit
    await page.click('button:has-text("Save")');
    
    // Verify success
    await expect(page.locator('.success-notification')).toBeVisible();
  });
});
```

---

**Ready to test?** Add your Playwright tests to `invoice-extract-frontend-master/tests/` and push to GitHub! 🎭
