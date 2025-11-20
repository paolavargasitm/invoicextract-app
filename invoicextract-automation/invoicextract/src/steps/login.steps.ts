import { Given, When, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { CustomWorld } from '../support/world';
import { config } from '../support/config';

// Given Steps
Given('I am on the login page', async function (this: CustomWorld) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  await this.pageManager.loginPage.goto();
});

Given('I navigate to the InvoiceExtract frontend', async function (this: CustomWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  // Check if already on a different page - if so, close and reinitialize
  const currentUrl = this.page.url();
  if (currentUrl && currentUrl !== 'about:blank' && !currentUrl.includes('login')) {
    // Already logged in - need fresh session
    await this.cleanup();
    await this.initBrowser();
  }
  
  await this.page!.goto(config.frontendUrl);
  await this.page!.waitForLoadState('domcontentloaded');
});

Given('I navigate to the login page', async function (this: CustomWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  await this.page.goto(config.frontendUrl, { waitUntil: 'networkidle' });
  await this.page.waitForTimeout(1000);
});

// When Steps
When('I enter username {string} and password {string}', async function (this: CustomWorld, username: string, password: string) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  await this.pageManager.loginPage.enterEmail(username);
  await this.pageManager.loginPage.enterPassword(password);
});

When('I click the login button', async function (this: CustomWorld) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  await this.pageManager.loginPage.clickLoginButton();
  
  // Wait for navigation after login
  if (this.page) {
    await this.page.waitForURL(url => !url.toString().includes('auth'), { timeout: 10000 }).catch(() => {
      // Continue if timeout - will be validated in Then step
    });
  }
});

When('I login with admin credentials', async function (this: CustomWorld) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  const { username, password } = config.credentials.admin;
  await this.pageManager.loginPage.login(username, password);
});

When('I login with {string} credentials', async function (this: CustomWorld, role: string) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  const { username, password } = config.getCredentialsByRole(role);
  await this.pageManager.loginPage.login(username, password);
});

When('I login as {string} with username {string} and password {string}', async function (
  this: CustomWorld,
  role: string,
  username: string,
  password: string
) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  await this.pageManager.loginPage.login(username, password);
  this.attach(`Logged in as: ${role}`, 'text/plain');
});

When('I check the remember me option', async function (this: CustomWorld) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  await this.pageManager.loginPage.clickRememberMe();
});

// Then Steps
Then('I should be redirected to the home page', async function (this: CustomWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  await this.page.waitForLoadState('domcontentloaded');
  const currentUrl = await this.page.url();
  expect(currentUrl).toContain('home');
});

Then('I should be successfully logged in', async function (this: CustomWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  await this.page.waitForLoadState('networkidle', { timeout: 10000 });
  
  const currentUrl = await this.page.url();
  expect(currentUrl).not.toContain('login');
  expect(currentUrl).not.toContain('auth');
});

Then('login is successful', async function (this: CustomWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  const currentUrl = this.page.url();
  expect(currentUrl).not.toContain('auth');
  expect(currentUrl).toContain(config.frontendUrl);
});

Then('I should see the dashboard', async function (this: CustomWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  await this.page.waitForLoadState('domcontentloaded');
  const currentUrl = await this.page.url();
  const isDashboardVisible = currentUrl.includes(config.frontendUrl)
  expect(isDashboardVisible).toBeTruthy();
});

Then('I should see an error message', async function (this: CustomWorld) {
  if (!this.pageManager) {
    throw new Error('PageManager not initialized');
  }
  const isVisible = await this.pageManager.loginPage.isErrorMessageVisible();
  expect(isVisible).toBeTruthy();
});

Then('I can see the {string} view', async function (this: CustomWorld, viewName: string) {
  if (!this.page || !this.pageManager) {
    throw new Error('Page or PageManager not initialized');
  }
  
  const viewMap: { [key: string]: () => any } = {
    'home': () => this.pageManager!.homePage,
    'inicio': () => this.pageManager!.homePage,
    'email config': () => this.pageManager!.emailConfigPage,
    'invoice dashboard': () => this.pageManager!.invoiceDashboardPage,
    'dashboard facturas': () => this.pageManager!.invoiceDashboardPage,
    'mapping': () => this.pageManager!.mappingPage,
    'mapeos': () => this.pageManager!.mappingPage,
    'erp config': () => this.pageManager!.erpConfigPage,
    'erps': () => this.pageManager!.erpConfigPage
  };
  
  const normalizedView = viewName.toLowerCase();
  const page = viewMap[normalizedView];
  
  if (!page) {
    throw new Error(`Unknown view: ${viewName}`);
  }
  
  await page().goto();
  const isLoaded = await page().isLoaded();
  expect(isLoaded).toBeTruthy();
});

Then('all views are available', async function (this: CustomWorld) {
  if (!this.page || !this.pageManager) {
    throw new Error('Page or PageManager not initialized');
  }
  
  const views = [
    { name: 'Home', page: this.pageManager.homePage },
    { name: 'Email Config', page: this.pageManager.emailConfigPage },
    { name: 'Invoice Dashboard', page: this.pageManager.invoiceDashboardPage },
    { name: 'Mapping', page: this.pageManager.mappingPage },
    { name: 'ERP Config', page: this.pageManager.erpConfigPage }
  ];
  
  for (const view of views) {
    await view.page.goto();
    const isLoaded = await view.page.isLoaded();
    expect(isLoaded).toBeTruthy();
    console.log(`✓ View "${view.name}" is available`);
  }
});
