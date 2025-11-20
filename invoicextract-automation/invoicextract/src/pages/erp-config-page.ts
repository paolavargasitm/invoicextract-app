import { Page } from '@playwright/test';
import { BasePage } from './base-page';

export class ErpConfigPage extends BasePage {
  private readonly locators = {
    pageHeader: 'h1',
    mainContent: 'main',
  };

  constructor(page: Page) {
    super(page);
  }

  /**
   * Navigate to ERP Config page
   */
  async goto(baseUrl: string = 'http://localhost:3001/erp-config'): Promise<void> {
    await this.navigateTo(baseUrl);
  }

  /**
   * Verify page is loaded
   */
  async isLoaded(): Promise<boolean> {
    const url = await this.page.url();
    return url.includes('erp-config') && !url.includes('auth');
  }
}
