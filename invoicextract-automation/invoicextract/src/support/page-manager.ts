import { Page } from '@playwright/test';
import { HomePage } from '../pages/home-page';
import { LoginPage } from '../pages/login-page';
import { EmailConfigPage } from '../pages/email-config-page';
import { InvoiceDashboardPage } from '../pages/invoice-dashboard-page';
import { MappingPage } from '../pages/mapping-page';
import { ErpConfigPage } from '../pages/erp-config-page';

export class PageManager {
  private page: Page;
  private _homePage?: HomePage;
  private _loginPage?: LoginPage;
  private _emailConfigPage?: EmailConfigPage;
  private _invoiceDashboardPage?: InvoiceDashboardPage;
  private _mappingPage?: MappingPage;
  private _erpConfigPage?: ErpConfigPage;

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * Get HomePage instance
   */
  get homePage(): HomePage {
    if (!this._homePage) {
      this._homePage = new HomePage(this.page);
    }
    return this._homePage;
  }

  /**
   * Get LoginPage instance
   */
  get loginPage(): LoginPage {
    if (!this._loginPage) {
      this._loginPage = new LoginPage(this.page);
    }
    return this._loginPage;
  }

  /**
   * Get EmailConfigPage instance
   */
  get emailConfigPage(): EmailConfigPage {
    if (!this._emailConfigPage) {
      this._emailConfigPage = new EmailConfigPage(this.page);
    }
    return this._emailConfigPage;
  }

  /**
   * Get InvoiceDashboardPage instance
   */
  get invoiceDashboardPage(): InvoiceDashboardPage {
    if (!this._invoiceDashboardPage) {
      this._invoiceDashboardPage = new InvoiceDashboardPage(this.page);
    }
    return this._invoiceDashboardPage;
  }

  /**
   * Get MappingPage instance
   */
  get mappingPage(): MappingPage {
    if (!this._mappingPage) {
      this._mappingPage = new MappingPage(this.page);
    }
    return this._mappingPage;
  }

  /**
   * Get ErpConfigPage instance
   */
  get erpConfigPage(): ErpConfigPage {
    if (!this._erpConfigPage) {
      this._erpConfigPage = new ErpConfigPage(this.page);
    }
    return this._erpConfigPage;
  }
}
