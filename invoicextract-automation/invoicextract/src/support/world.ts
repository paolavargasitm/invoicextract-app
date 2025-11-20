import { World, IWorldOptions, setWorldConstructor } from '@cucumber/cucumber';
import { Browser, BrowserContext, Page, chromium, APIRequestContext, request } from '@playwright/test';
import { PageManager } from './page-manager';
import { urls } from './fixtures';
import { config } from './config';

export interface ICustomWorld extends World {
  browser?: Browser;
  context?: BrowserContext;
  page?: Page;
  apiContext?: APIRequestContext;
  pageManager?: PageManager;
  // URL accessors
  frontendURL: string;
  keycloakAdminURL: string;
  keycloakBaseURL: string;
  // Flag to indicate if this is an API-only test
  isApiTest: boolean;
}

export class CustomWorld extends World implements ICustomWorld {
  browser?: Browser;
  context?: BrowserContext;
  page?: Page;
  apiContext?: APIRequestContext;
  pageManager?: PageManager;
  
  // URL properties
  frontendURL: string;
  keycloakAdminURL: string;
  keycloakBaseURL: string;
  isApiTest: boolean;
  sessionData?: Record<string, any>;
  currentSection?: any;
  attemptedInvoiceId?: any;
  initialStatus?: string;
  attemptedBackgroundClick?: boolean;
  currentInvoiceStatus?: string;
  newTab?: any;
  lastDownload?: any;
  currentInvoiceId?: number;

  constructor(options: IWorldOptions) {
    super(options);
    
    // Initialize URL properties from fixtures
    this.frontendURL = urls.frontend;
    this.keycloakAdminURL = urls.keycloakAdmin;
    this.keycloakBaseURL = urls.keycloakBase;
    this.isApiTest = false;
  }

  /**
   * Initialize browser context for UI tests only
   */
  async initBrowser() {
    const headlessMode = this.parameters.headless ?? config.browser.headless ?? true;
    const slowMo = this.parameters.slowMo ?? config.browser.slowMo ?? 0;
    
    this.browser = await chromium.launch({
      headless: headlessMode,
      slowMo: slowMo
    });
    this.context = await this.browser.newContext();
    this.page = await this.context.newPage();
    
    // Initialize Page Manager with all page objects
    this.pageManager = new PageManager(this.page);
  }

  /**
   * Initialize API context for API tests
   */
  async initApi() {
    // API context is initialized on-demand in the API steps
    // This method is here for future enhancements if needed
  }

  /**
   * Legacy init method - now calls initBrowser for backward compatibility
   * @deprecated Use initBrowser() or initApi() directly
   */
  async init() {
    await this.initBrowser();
  }

  async cleanup() {
    if (this.page) {
      await this.page.close();
    }
    if (this.context) {
      await this.context.close();
    }
    if (this.browser) {
      await this.browser.close();
    }
    if (this.apiContext) {
      await this.apiContext.dispose();
    }
  }
}

setWorldConstructor(CustomWorld);
