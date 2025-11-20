import { Page, Locator } from '@playwright/test';

export class BasePage {
  protected page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * Navigate to a specific URL
   */
  async navigateTo(url: string): Promise<void> {
    await this.page.goto(url);
  }

  /**
   * Click on an element
   */
  async click(locator: string): Promise<void> {
    await this.page.locator(locator).click();
  }

  /**
   * Fill text into an input field
   */
  async fill(locator: string, text: string): Promise<void> {
    await this.page.locator(locator).fill(text);
  }

  /**
   * Get text content from an element
   */
  async getText(locator: string): Promise<string> {
    return await this.page.locator(locator).textContent() || '';
  }

  /**
   * Check if element is visible
   */
  async isVisible(locator: string): Promise<boolean> {
    return await this.page.locator(locator).isVisible();
  }

  /**
   * Wait for element to be visible
   */
  async waitForElement(locator: string, timeout: number = 30000): Promise<void> {
    await this.page.locator(locator).waitFor({ state: 'visible', timeout });
  }

  /**
   * Get the current page title
   */
  async getTitle(): Promise<string> {
    return await this.page.title();
  }

  /**
   * Get the current URL
   */
  async getCurrentUrl(): Promise<string> {
    return this.page.url();
  }

  /**
   * Press a key
   */
  async pressKey(key: string): Promise<void> {
    await this.page.keyboard.press(key);
  }

  /**
   * Select option from dropdown
   */
  async selectOption(locator: string, value: string): Promise<void> {
    await this.page.locator(locator).selectOption(value);
  }

  /**
   * Check a checkbox
   */
  async check(locator: string): Promise<void> {
    await this.page.locator(locator).check();
  }

  /**
   * Uncheck a checkbox
   */
  async uncheck(locator: string): Promise<void> {
    await this.page.locator(locator).uncheck();
  }

  /**
   * Wait for page load
   */
  async waitForPageLoad(): Promise<void> {
    await this.page.waitForLoadState('domcontentloaded');
  }

  /**
   * Get element locator
   */
  getLocator(locator: string): Locator {
    return this.page.locator(locator);
  }
}
