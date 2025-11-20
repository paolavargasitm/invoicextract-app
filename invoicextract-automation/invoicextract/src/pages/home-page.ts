import { Page } from '@playwright/test';
import { BasePage } from './base-page';

export class HomePage extends BasePage {
  // Locators
  private readonly locators = {
    logo: '[data-testid="logo"]',
    searchBox: 'input[name="search"]',
    searchButton: 'button[type="submit"]',
    loginButton: 'a[href="/login"]',
    signupButton: 'a[href="/signup"]',
    navMenu: 'nav.main-menu',
    mainHeading: 'h1'
  };

  constructor(page: Page) {
    super(page);
  }

  /**
   * Navigate to the home page
   */
  async goto(baseUrl: string = 'http://localhost:3001'): Promise<void> {
    await this.navigateTo(`${baseUrl}/home`);
  }

  /**
   * Check if the home page is loaded
   */
  async isLoaded(): Promise<boolean> {
    const currentUrl = this.page.url();
    return currentUrl.includes('/home') && !currentUrl.includes('auth');
  }

  /**
   * Perform a search
   */
  async search(searchTerm: string): Promise<void> {
    await this.fill(this.locators.searchBox, searchTerm);
    await this.click(this.locators.searchButton);
  }

  /**
   * Click on login button
   */
  async clickLogin(): Promise<void> {
    await this.click(this.locators.loginButton);
  }

  /**
   * Click on signup button
   */
  async clickSignup(): Promise<void> {
    await this.click(this.locators.signupButton);
  }

  /**
   * Check if logo is visible
   */
  async isLogoVisible(): Promise<boolean> {
    return await this.isVisible(this.locators.logo);
  }

  /**
   * Get main heading text
   */
  async getMainHeading(): Promise<string> {
    return await this.getText(this.locators.mainHeading);
  }

  /**
   * Check if navigation menu is visible
   */
  async isNavMenuVisible(): Promise<boolean> {
    return await this.isVisible(this.locators.navMenu);
  }
}
