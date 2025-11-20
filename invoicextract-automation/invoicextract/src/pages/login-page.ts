import { Page } from '@playwright/test';
import { BasePage } from './base-page';

export class LoginPage extends BasePage {
  // Locators - Supporting both email and Keycloak username formats
  private readonly locators = {
    // Primary Keycloak locators
    usernameInput: '#username',
    passwordInput: '#password',
    loginButton: '#kc-login',
    showPasswordButton: 'button[aria-label="Show password"]',
    
    // Fallback locators for other login forms
    emailInput: 'input[name="email"]',
    submitButton: 'button[type="submit"]',
    
    // Other elements
    errorMessage: '.error-message',
    rememberMeCheckbox: 'input[name="remember"]',
  };

  constructor(page: Page) {
    super(page);
  }

  /**
   * Navigate to the login page
   */
  async goto(baseUrl: string = 'http://localhost:3001'): Promise<void> {
    await this.navigateTo(baseUrl);
  }

  /**
   * Enter email or username
   */
  async enterEmail(email: string): Promise<void> {
    // Try username input first (Keycloak), fallback to email input
    const usernameExists = await this.isVisible(this.locators.usernameInput);
    if (usernameExists) {
      await this.fill(this.locators.usernameInput, email);
    } else {
      await this.fill(this.locators.emailInput, email);
    }
  }

  /**
   * Enter password
   */
  async enterPassword(password: string): Promise<void> {
    await this.fill(this.locators.passwordInput, password);
  }

  /**
   * Click login button
   */
  async clickLoginButton(): Promise<void> {
    // Try Keycloak login button first, fallback to generic submit
    const kcLoginExists = await this.isVisible(this.locators.loginButton);
    if (kcLoginExists) {
      await this.click(this.locators.loginButton);
    } else {
      await this.click(this.locators.submitButton);
    }
  }

  /**
   * Perform complete login
   */
  async login(email: string, password: string): Promise<void> {
    await this.enterEmail(email);
    await this.enterPassword(password);
    await this.clickLoginButton();
  }

  /**
   * Click remember me checkbox
   */
  async clickRememberMe(): Promise<void> {
    await this.check(this.locators.rememberMeCheckbox);
  }

  /**
   * Click show password button
   */
  async clickShowPassword(): Promise<void> {
    const showPasswordExists = await this.isVisible(this.locators.showPasswordButton);
    if (showPasswordExists) {
      await this.click(this.locators.showPasswordButton);
    }
  }

  /**
   * Get error message
   */
  async getErrorMessage(): Promise<string> {
    await this.waitForElement(this.locators.errorMessage);
    return await this.getText(this.locators.errorMessage);
  }

  /**
   * Check if error message is visible
   */
  async isErrorMessageVisible(): Promise<boolean> {
    return await this.isVisible(this.locators.errorMessage);
  }
}

