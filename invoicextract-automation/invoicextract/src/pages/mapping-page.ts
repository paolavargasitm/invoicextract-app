import { Page } from '@playwright/test';
import { BasePage } from './base-page';

export class MappingPage extends BasePage {
  private readonly locators = {
    pageHeader: 'h1',
    mainContent: 'main',
  };

  constructor(page: Page) {
    super(page);
  }

  /**
   * Navigate to Mapping page
   */
  async goto(baseUrl: string = 'http://localhost:3001/mapping'): Promise<void> {
    await this.navigateTo(baseUrl);
  }

  /**
   * Verify page is loaded
   */
  async isLoaded(): Promise<boolean> {
    const url = await this.page.url();
    return url.includes('mapping') && !url.includes('auth');
  }
}
