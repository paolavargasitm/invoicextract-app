import { Page } from '@playwright/test';
import { BasePage } from './base-page';

export class InvoiceDashboardPage extends BasePage {
  private readonly locators = {
    pageHeader: 'h1',
    mainContent: 'main',
  };

  constructor(page: Page) {
    super(page);
  }

  /**
   * Navigate to Invoice Dashboard page
   */
  async goto(baseUrl: string = 'http://localhost:3001/invoices'): Promise<void> {
    await this.navigateTo(baseUrl);
  }

  /**
   * Verify page is loaded
   */
  async isLoaded(): Promise<boolean> {
    const url = await this.page.url();
    return url.includes('invoices') && !url.includes('auth');
  }
}
