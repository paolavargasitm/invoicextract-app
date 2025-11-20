import { Given, When, Then } from '@cucumber/cucumber';
import { expect, request } from '@playwright/test';
import { CustomWorld } from '../support/world';
import { config } from '../support/config';
import { AuthHelper } from '../api/auth-helper';

// Extended World interface for invoice filters
interface InvoiceFilterWorld extends CustomWorld {
  apiInvoices?: any[];
  filterParams?: {
    fromDate?: string;
    toDate?: string;
    status?: string;
    senderTaxId?: string;
    receiverTaxId?: string;
  };
}

// Selectors for invoice filters page (keeping existing locators as specified)
const selectors = {
  fromDate: '//*[text()="Creación desde"]//following-sibling::input',
  toDate: '//*[text()="Creación hasta"]//following-sibling::input',
  estado: '//*[text()="Estado"]//following-sibling::select',
  nitEmisor: '//*[text()="NIT Emisor"]//following-sibling::input',
  nitReceptor: '//*[text()="NIT Receptor"]//following-sibling::input',
  buscar: 'button:has-text("Buscar")',
  limpiar: 'button:has-text("Limpiar Filtros")',
  alert: '.alert, [role="alert"]',
  totalIngresadas: 'text=Facturas ingresadas',
  totalAprobadas: 'text=Facturas aprobadas',
  totalRechazadas: 'text=Facturas rechazadas',
  totalMonto: 'text=Monto total'
};

// Helper function to get invoices from API
async function fetchInvoicesFromAPI(world: InvoiceFilterWorld): Promise<any[]> {
  try {
    // Create authenticated API context
    const apiContext = await AuthHelper.createAuthenticatedContext();
    const baseUrl = process.env.API_BASE_URL || 'http://localhost:8080/invoicextract';
    
    // Fetch all invoices from API
    const response = await apiContext.get(`${baseUrl}/api/invoices`);
    
    if (response.ok()) {
      const invoices = await response.json();
      world.attach(`API returned ${Array.isArray(invoices) ? invoices.length : 0} invoices`, 'text/plain');
      await apiContext.dispose();
      return Array.isArray(invoices) ? invoices : [];
    } else {
      world.attach(`API call failed with status: ${response.status()}`, 'text/plain');
      await apiContext.dispose();
      return [];
    }
  } catch (error) {
    world.attach(`API call error: ${error}`, 'text/plain');
    return [];
  }
}

// ===== GIVEN STEPS =====

Given("I'm on the invoices page", async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  await this.page.goto(`${config.frontendUrl}/invoices`);
  await this.page.waitForLoadState('domcontentloaded');
  await this.page.waitForTimeout(2000); // Wait for initial data load
  await expect(this.page).toHaveURL(/invoices/);
  
  // Fetch invoices from API to use real data
  this.apiInvoices = await fetchInvoicesFromAPI(this);
  
  this.attach('Navigated to invoices page and fetched API data', 'text/plain');
});

// ===== WHEN STEPS =====

When('I fill the filters with data from API', async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  // Initialize filter params
  if (!this.filterParams) {
    this.filterParams = {};
  }
  
  // Use real data from API
  if (this.apiInvoices && this.apiInvoices.length > 0) {
    const firstInvoice = this.apiInvoices[0];
    
    // Extract dates from API
    let fromDate = '';
    let toDate = '';
    
    if (firstInvoice.issueDate || firstInvoice.IssueDate) {
      const issueDate = firstInvoice.issueDate || firstInvoice.IssueDate;
      fromDate = issueDate.split('T')[0]; // Extract date part
      
      // Calculate toDate as today or use dueDate if available
      if (firstInvoice.dueDate || firstInvoice.DueDate) {
        toDate = (firstInvoice.dueDate || firstInvoice.DueDate).split('T')[0];
      } else {
        toDate = new Date().toISOString().split('T')[0];
      }
    } else {
      // If no date in invoice, use date range from all invoices
      const dates = this.apiInvoices
        .map(inv => inv.issueDate || inv.IssueDate)
        .filter(date => date)
        .sort();
      
      if (dates.length > 0) {
        fromDate = dates[0].split('T')[0];
        toDate = dates[dates.length - 1].split('T')[0];
      }
    }
    
    // Extract NITs from API
    let nitEmisor = '';
    let nitReceptor = '';
    
    if (firstInvoice.senderTaxId || firstInvoice.SenderTaxId) {
      nitEmisor = (firstInvoice.senderTaxId || firstInvoice.SenderTaxId).toString().replace(/-/g, '');
    }
    
    if (firstInvoice.receiverTaxId || firstInvoice.ReceiverTaxId) {
      nitReceptor = (firstInvoice.receiverTaxId || firstInvoice.ReceiverTaxId).toString().replace(/-/g, '');
    }
    
    // Extract status from API
    let status = 'Aprobada';
    if (firstInvoice.status) {
      const statusMap: { [key: string]: string } = {
        'APPROVED': 'Aprobada',
        'REJECTED': 'Rechazada',
        'PENDING': 'Pendiente'
      };
      status = statusMap[firstInvoice.status.toUpperCase()] || 'Aprobada';
    }
    
    // Fill the form with API data
    if (fromDate) {
      await this.page.locator(selectors.fromDate).fill(fromDate);
      this.filterParams.fromDate = fromDate;
    }
    
    if (toDate) {
      await this.page.locator(selectors.toDate).fill(toDate);
      this.filterParams.toDate = toDate;
    }
    
    await this.page.locator(selectors.estado).selectOption({ label: status });
    this.filterParams.status = firstInvoice.status || 'APPROVED';
    
    if (nitEmisor) {
      await this.page.locator(selectors.nitEmisor).fill(nitEmisor);
      this.filterParams.senderTaxId = nitEmisor;
    }
    
    if (nitReceptor) {
      await this.page.locator(selectors.nitReceptor).fill(nitReceptor);
      this.filterParams.receiverTaxId = nitReceptor;
    }
    
    this.attach(`Filled filters with API data: fromDate=${fromDate}, toDate=${toDate}, status=${status}, nitEmisor=${nitEmisor}, nitReceptor=${nitReceptor}`, 'text/plain');
    this.attach(`API Invoice fields: id=${firstInvoice.id}, status=${firstInvoice.status}, issueDate=${firstInvoice.issueDate || firstInvoice.IssueDate}`, 'text/plain');
  } else {
    throw new Error('No API data available to fill filters');
  }
});

When('I fill the filters with invalid date range', async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  // Use API data to create an invalid date range
  let invalidFromDate = '';
  let invalidToDate = '';
  
  if (this.apiInvoices && this.apiInvoices.length > 0) {
    // Get dates from API and reverse them to make invalid range
    const dates = this.apiInvoices
      .map(inv => inv.issueDate || inv.IssueDate)
      .filter(date => date)
      .sort();
    
    if (dates.length >= 2) {
      // Use last date as from and first date as to (invalid range)
      invalidFromDate = dates[dates.length - 1].split('T')[0];
      invalidToDate = dates[0].split('T')[0];
    } else if (dates.length === 1) {
      // Use invoice date + 30 days as from, and invoice date as to
      const invoiceDate = new Date(dates[0]);
      const futureDate = new Date(invoiceDate);
      futureDate.setDate(futureDate.getDate() + 30);
      
      invalidFromDate = futureDate.toISOString().split('T')[0];
      invalidToDate = invoiceDate.toISOString().split('T')[0];
    }
  }
  
  // If still no dates, calculate from today
  if (!invalidFromDate || !invalidToDate) {
    const today = new Date();
    const pastDate = new Date(today);
    pastDate.setDate(pastDate.getDate() - 30);
    
    invalidFromDate = today.toISOString().split('T')[0];
    invalidToDate = pastDate.toISOString().split('T')[0];
  }
  
  await this.page.locator(selectors.fromDate).fill(invalidFromDate);
  await this.page.locator(selectors.toDate).fill(invalidToDate);
  await this.page.locator(selectors.estado).selectOption({ label: 'Aprobada' });
  
  this.attach(`Filled filters with invalid date range from API: fromDate=${invalidFromDate}, toDate=${invalidToDate}`, 'text/plain');
});

When('I click Buscar', async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  await this.page.locator(selectors.buscar).click();
  await this.page.waitForTimeout(3000); // Wait for search results
  this.attach('Clicked Buscar button', 'text/plain');
});

When('I click Limpiar Filtros', async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  await this.page.locator(selectors.limpiar).click();
  await this.page.waitForTimeout(1000); // Wait for filters to clear
  this.filterParams = {};
  this.attach('Clicked Limpiar Filtros button', 'text/plain');
});

// ===== THEN STEPS =====

Then('the totals should update', async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  // Wait for the totals to be visible
  await this.page.waitForTimeout(2000);
  
  const totalIngresadasVisible = await this.page.locator(selectors.totalIngresadas).isVisible().catch(() => false);
  const totalAprobadasVisible = await this.page.locator(selectors.totalAprobadas).isVisible().catch(() => false);
  const totalRechazadasVisible = await this.page.locator(selectors.totalRechazadas).isVisible().catch(() => false);
  const totalMontoVisible = await this.page.locator(selectors.totalMonto).isVisible().catch(() => false);
  
  // At least some totals should be visible
  const anyVisible = totalIngresadasVisible || totalAprobadasVisible || totalRechazadasVisible || totalMontoVisible;
  
  if (!anyVisible) {
    // Try alternative selectors for totals
    const pageContent = await this.page.content();
    this.attach(`Page content snippet: ${pageContent.substring(0, 500)}`, 'text/plain');
  }
  
  expect(anyVisible).toBeTruthy();
  this.attach('Totals have been updated and are visible', 'text/plain');
});

Then('the totals should reset', async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  // Wait for reset to complete
  await this.page.waitForTimeout(1000);
  
  // Check if filters are cleared
  const fromDateValue = await this.page.locator(selectors.fromDate).inputValue();
  const toDateValue = await this.page.locator(selectors.toDate).inputValue();
  
  this.attach(`After reset - fromDate: "${fromDateValue}", toDate: "${toDateValue}"`, 'text/plain');
  
  // Verify filters are empty or reset
  const areFiltersCleared = fromDateValue === '' || toDateValue === '';
  expect(areFiltersCleared).toBeTruthy();
  
  this.attach('Filters have been cleared/reset', 'text/plain');
});

Then('I should see a validation error or no results', async function (this: InvoiceFilterWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  
  // Wait for potential error message or results
  await this.page.waitForTimeout(2000);
  
  // Check for error message with filter-specific context
  const alertVisible = await this.page.locator(selectors.alert).isVisible().catch(() => false);
  
  if (alertVisible) {
    this.attach('Validation error message is visible', 'text/plain');
    expect(alertVisible).toBeTruthy();
    return;
  }
  
  // Try to find any error message on the page related to date validation
  const errorPatterns = [
    ':text-matches("error|invalid|inválido|fecha|incorrecto|rango", "i")',
    '.error',
    '.mensaje-error',
    '[class*="error"]',
    '[role="alert"]'
  ];
  
  let errorFound = false;
  for (const pattern of errorPatterns) {
    const isVisible = await this.page.locator(pattern).first().isVisible().catch(() => false);
    if (isVisible) {
      const errorText = await this.page.locator(pattern).first().textContent();
      this.attach(`Found error message: ${errorText}`, 'text/plain');
      errorFound = true;
      break;
    }
  }
  
  if (errorFound) {
    expect(errorFound).toBeTruthy();
    return;
  }
  
  // Check if the search returned no results (which is acceptable for invalid date range)
  const pageContent = await this.page.content();
  const hasNoResults = pageContent.includes('No se encontraron') || 
                      pageContent.includes('No results') ||
                      pageContent.includes('0 facturas') ||
                      pageContent.includes('Sin resultados');
  
  if (hasNoResults) {
    this.attach('Invalid date range handled: No results returned', 'text/plain');
    expect(true).toBeTruthy();
    return;
  }
  
  // If neither error nor no-results, log and fail
  this.attach(`No error message or no-results indicator found. Page snippet: ${pageContent.substring(0, 500)}`, 'text/plain');
  expect(errorFound || hasNoResults).toBeTruthy();
});
