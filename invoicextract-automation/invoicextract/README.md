# invoicextract
Automated test for InvoiceExtract Project

## Project Structure

This project follows the Page Object Model (POM) pattern with Cucumber BDD framework, TypeScript, and Playwright.

```
invoicextract/
├── .github/
│   └── workflows/
│       └── test-and-report.yml  # GitHub Actions workflow
├── features/                     # Gherkin feature files
│   ├── login.feature            # Login scenarios
│   └── api-users.feature        # API test scenarios
├── src/
│   ├── pages/                   # Page Object Model classes (dashed-style names)
│   │   ├── base-page.ts        # Base class with atomic methods
│   │   ├── home-page.ts        # Home page object
│   │   ├── login-page.ts       # Login page object
│   │   ├── email-config-page.ts
│   │   ├── invoice-dashboard-page.ts
│   │   ├── mapping-page.ts
│   │   └── erp-config-page.ts
│   ├── steps/                   # Cucumber step definitions
│   │   ├── login.steps.ts      # Login step definitions
│   │   └── api.steps.ts        # API step definitions
│   ├── support/                # Support files
│   │   ├── world.ts            # Custom World for Cucumber
│   │   ├── hooks.ts            # Before/After hooks
│   │   ├── page-manager.ts     # Centralized page management
│   │   └── report.js           # Report generator
│   └── api/                    # API request helpers
│       ├── api-helper.ts       # Base API helper class
│       └── user-api-client.ts  # User-specific API client
├── reports/                    # Generated test reports (gitignored)
├── cucumber.js                 # Cucumber configuration
├── tsconfig.json              # TypeScript configuration
└── package.json               # NPM dependencies and scripts
```

## Naming Convention

All TypeScript files follow **dashed-style naming** (e.g., `email-config-page.ts`):
- Files: `my-component.ts`
- Classes: `export class MyComponent`
- Imports: `import { MyComponent } from './my-component'`

## Design Pattern

### Page Object Model (POM)
- **BasePage**: Contains atomic methods like `click()`, `fill()`, `getText()`, etc.
- **Specific Pages**: Each view/page extends BasePage and defines page-specific locators and methods
- **PageManager**: Centralized management of page instances, accessible via Custom World

### Custom World
- Initializes browser, page, and API contexts
- Provides access to PageManager for step definitions
- Manages lifecycle of browser resources

### Step Definitions
- Implement Gherkin scenarios
- Access pages through `this.pageManager`
- Keep business logic in page objects, not in steps

### API Testing
- **ApiHelper**: Base class with common HTTP methods
- **Specific API Clients**: Extend ApiHelper for specific API domains
- API context available through Custom World

## Installation

```bash
npm install
```

Install Playwright browsers:
```bash
npx playwright install chromium
```

## Running Tests

Run all tests:
```bash
npm test
```

Run tests with any tag:
```bash
npm run test:tag "@login"
npm run test:tag "@smoke"
npm run test:tag "@roles"              # Run role-based login tests
npm run test:tag "@smoke and @login"
npm run test:tag "not @skip"
```

Run predefined test suites:
```bash
npm run test:login          # Run @login tests
npm run test:smoke          # Run @smoke tests
npm run test:invoicextract  # Run invoicextract-login.feature
```

Run tests in parallel:
```bash
npm run test:parallel
```

Run specific feature file:
```bash
npm test -- features/login.feature
```

Run tests and generate report:
```bash
npm run test:report
```

Generate report from existing test results:
```bash
npm run report
```

Clean reports directory:
```bash
npm run clean
```

## Test Credentials

This project supports multiple user roles with different credentials configured via environment variables:

### Available Roles:
- **Admin**: Administrative access
- **Finance**: Finance department user
- **Technician**: Technical support user

### Configuration:
Credentials are defined in `.env` file:
```env
ADMIN_USERNAME=yuly.murillo.admin
ADMIN_PASSWORD=admin123

FINANCE_USERNAME=finance.user
FINANCE_PASSWORD=finance123

TECHNICIAN_USERNAME=technician.user
TECHNICIAN_PASSWORD=tech123
```

### Using Roles in Tests:
The test suite includes Scenario Outlines that validate login for all user roles. See `features/invoicextract-login.feature` for examples.

## Writing Tests

### Feature Files (Gherkin)
Place `.feature` files in the `features/` directory:

```gherkin
Feature: User Login
  Scenario: Successful login
    Given I am on the login page
    When I enter email "user@example.com"
    And I enter password "password123"
    And I click the login button
    Then I should be redirected to the home page
```

### Page Objects
Create page classes in `src/pages/` using dashed-style filenames:

```typescript
// src/pages/login-page.ts
export class LoginPage extends BasePage {
  private readonly locators = {
    emailInput: 'input[name="email"]',
    passwordInput: 'input[name="password"]',
    loginButton: 'button[type="submit"]'
  };

  async login(email: string, password: string): Promise<void> {
    await this.fill(this.locators.emailInput, email);
    await this.fill(this.locators.passwordInput, password);
    await this.click(this.locators.loginButton);
  }
}
```

### Step Definitions
Create step files in `src/steps/`:

```typescript
import { Given, When, Then } from '@cucumber/cucumber';
import { CustomWorld } from '../support/world';

Given('I am on the login page', async function (this: CustomWorld) {
  await this.pageManager.loginPage.goto();
});
```

## GitHub Actions CI/CD

This project includes automated testing via GitHub Actions.

### Workflow Configuration
Tests run automatically on:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop` branches

### Required GitHub Secrets
Configure these in **Settings → Secrets and variables → Actions**:
- `FRONTEND_URL` - InvoiceExtract frontend URL
- `KEYCLOAK_URL` - Keycloak authentication URL
- `ADMIN_USERNAME` / `ADMIN_PASSWORD`
- `FINANCE_USERNAME` / `FINANCE_PASSWORD`
- `TECHNICIAN_USERNAME` / `TECHNICIAN_PASSWORD`

### Test Reports as Artifacts
- Reports are uploaded as GitHub Actions artifacts
- Retained for **5 days** (automatic cleanup)
- Download from the **Actions** tab → workflow run → **Artifacts** section
- Extract ZIP and open `index.html`

See `.github/workflows/test-and-report.yml` for full configuration.

### API Testing
Use the API context in step definitions:

```typescript
When('I send a GET request to {string}', async function (this: CustomWorld, endpoint: string) {
  const apiClient = new UserApiClient(this.apiContext);
  this.apiResponse = await apiClient.get(endpoint);
});
```

## Reports

After running tests, HTML reports are generated in the `reports/` directory:
- `index.html`: Main report with test results
- `cucumber-report.json`: Raw JSON report data

Open `reports/index.html` in a browser to view the detailed test report.

## Configuration

### Cucumber Configuration (cucumber.js)
- Configure test paths, formatters, and world parameters
- Set headless mode, slowMo, and other browser options

### TypeScript Configuration (tsconfig.json)
- Compiler options for TypeScript
- Include/exclude patterns for source files

## Best Practices

1. **Keep steps simple**: Steps should call page methods, not contain logic
2. **Atomic page methods**: Each method should do one thing
3. **Use PageManager**: Access pages through the PageManager in Custom World
4. **Descriptive locators**: Use data-testid or semantic selectors
5. **Handle waits**: Use built-in waiting mechanisms in Playwright
6. **API and UI separation**: Keep API tests and UI tests organized
7. **Reusable components**: Share common logic in BasePage or helper classes

## Troubleshooting

If tests fail to run:
1. Ensure all dependencies are installed: `npm install`
2. Install Playwright browsers: `npx playwright install`
3. Check that the features directory has .feature files
4. Verify that step definitions match the Gherkin steps

## License
ISC

