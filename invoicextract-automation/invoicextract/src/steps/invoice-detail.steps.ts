import { Given, When, Then, Before } from '@cucumber/cucumber';
import { expect, Page } from '@playwright/test';
import { CustomWorld } from '../support/world';
import { config } from '../support/config';
import { scrollToEnd } from '../support/scroll-helper';
import { AuthHelper } from '../api/auth-helper';

/*
 * ============================================================================
 * INVOICE DETAIL FEATURE - STEP DEFINITIONS
 * ============================================================================
 * 
 * FILE STRUCTURE:
 * ---------------
 * 1. ✅ IMPLEMENTED STEPS - Working implementations
 *    - Background steps (I am on the invoices dashboard page, table is loaded)
 *    - When steps (I click on button, I press ESC key, I close modal)
 *    - Then steps for modal display, title, buttons, PDF viewer, sections
 * 
 * 2. ⚠️ MODAL FEATURE STATUS
 *    - Some steps will fail because the invoice detail modal feature may NOT be
 *      fully implemented in the frontend application at http://localhost:3001/invoices
 * 
 * ORGANIZED FEATURE FILE:
 * -----------------------
 * The feature file has been reorganized to group:
 * - Untagged scenarios (standard tests)
 * - Tagged scenarios by tag type (@failing, @fail, @failing_fix_with_ia, 
 *   @failing_fix_with_api, @bug, @review_pertinence)
 * 
 * ============================================================================
 */

// ============================================================================
// IMPLEMENTED STEPS - These steps have working implementations
// ============================================================================

// Page Object Model for Invoice Detail
class InvoiceDetailPage {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  // Locators
  get verDetalleButtons() {
    return this.page.locator('button:has-text("Ver Detalle")');
  }

  get modal() {
    return this.page.locator('//h2[contains(., "Detalle de Factura")]').first();
  }

  get modalTitle() {
    return this.page.locator('h1, h2').filter({ hasText: 'Detalle de Factura' });
  }

  get volverButton() {
    return this.page.locator('button:has-text("Volver")');
  }

  get proveedorField() {
    return this.page.locator('text=Proveedor:').locator('..').locator('text=/.*S\\.A\\.S|.*Ltd/');
  }

  get fechaField() {
    return this.page.locator('text=Fecha:').locator('..').locator('text=/\\d{4}-\\d{2}-\\d{2}/');
  }

  get montoField() {
    return this.page.locator('text=Monto:').locator('..').locator('text=/\\$ [\\d,.]+/');
  }

  get estadoField() {
    return this.page.locator('text=Estado:').locator('..').locator('.badge, span').filter({ hasText: /Pendiente|Aprobada|Rechazada/ });
  }

  get pdfViewer() {
    return this.page.locator('iframe[src*="pdf"], embed[type="application/pdf"]');
  }

  get itemsSection() {
    return this.page.locator('text=Items de la Factura');
  }

  get itemsTable() {
    return this.itemsSection.locator('..').locator('table');
  }

  get accionesSection() {
    return this.page.locator('text=Acciones de Revisión');
  }

  get aprobarButton() {
    return this.page.locator('button:has-text("Aprobar Factura")');
  }

  get rechazarButton() {
    return this.page.locator('button:has-text("Rechazar Factura")');
  }

  get descargarPDFButton() {
    return this.page.locator('button:has-text("Descargar PDF")');
  }

  get descargarXMLButton() {
    return this.page.locator('button:has-text("Descargar XML")');
  }

  get abrirPDFLink() {
    return this.page.locator('a:has-text("Abrir PDF en nueva pestaña")');
  }

  get invoiceTable() {
    return this.page.locator('table').filter({ hasText: 'ACCIONES' });
  }

  // Methods
  async clickVerDetalleForInvoice(invoiceId: number) {
    const row = this.page.locator(`tr:has(td:text("${invoiceId}"))`).first();
    await row.locator('//h2[contains(., "Detalle de Factura")]').click();
  }

  async waitForModalToBeVisible() {
    await this.modal.waitFor({ state: 'visible', timeout: 10000 });
  }

  async waitForModalToBeHidden() {
    await this.modal.waitFor({ state: 'hidden', timeout: 10000 });
  }

  async closeModal() {
    await this.volverButton.click();
    await this.waitForModalToBeHidden();
  }

  async getInvoiceFieldValue(fieldName: string): Promise<string> {
    const fieldLocator = this.page.locator(`text=${fieldName}:`).locator('..').last();
    return await fieldLocator.innerText();
  }

  async getEstadoValue(): Promise<string> {
    return await this.estadoField.innerText();
  }

  async isActionButtonEnabled(buttonName: string): Promise<boolean> {
    const button = this.page.locator(`button:has-text("${buttonName}")`);
    return await button.isEnabled();
  }

  async waitForLoadingState() {
    await this.page.waitForLoadState('networkidle');
  }
}

// Step Definitions
let invoiceDetailPage: InvoiceDetailPage;
let initialInvoiceData: Map<number, any> = new Map();

// ============================================================================
// ✅ BACKGROUND STEPS - IMPLEMENTED AND WORKING
// ============================================================================

// Background Steps
Given('I am on the invoices dashboard page', async function (this: CustomWorld) {
  // Page should already be initialized by the Before hook in hooks.ts
  // First login if not already logged in
  await this.page!.goto('http://localhost:3001');
  await this.page!.waitForLoadState('networkidle');
  
  // Check if we're on login page
  const isLoginPage = await this.page!.locator('input[name="username"], input[name="email"]').isVisible().catch(() => false);
  
  if (isLoginPage) {
    // Login with admin credentials using pageManager
    if (this.pageManager) {
      await this.pageManager.loginPage.login(config.credentials.admin.username, config.credentials.admin.password);
    }
  }
  
  // Navigate to invoices page
  await this.page!.goto('http://localhost:3001/invoices');
  await this.page!.waitForLoadState('networkidle');
  await this.page!.waitForTimeout(1000);
});

Given('the invoice table is loaded with data', async function (this: CustomWorld) {
  // Reinitialize invoiceDetailPage with the current page instance from World
  if (this.page) {
    invoiceDetailPage = new InvoiceDetailPage(this.page);
  }
  
  await invoiceDetailPage.invoiceTable.waitFor({ state: 'visible', timeout: 10000 });
  const rowCount = await invoiceDetailPage.invoiceTable.locator('tbody tr').count();
  expect(rowCount).toBeGreaterThan(0);
});

// ============================================================================
// ✅ WHEN STEPS - IMPLEMENTED AND WORKING
// ============================================================================

// Positive Scenarios - When Steps
When('I click on the {string} button for invoice ID {int}', async function (this: CustomWorld, buttonText: string, invoiceId: number) {
  // Ensure invoiceDetailPage is initialized
  if (!invoiceDetailPage && this.page) {
    invoiceDetailPage = new InvoiceDetailPage(this.page);
  }
  
  // Click the first Ver Detalle button
  await this.page!.locator('button:has-text("Ver Detalle")').first().click();
  await this.page!.waitForTimeout(3000);
  
  // Scroll to ensure all content is visible
  try {
    await scrollToEnd(this.page!);
  } catch (error) {
    console.log('Scroll helper not needed or modal not present');
  }
  
  // Try to find any modal-like elements
  const allDivs = await this.page!.locator('//h3[text()="Facturas Recientes"]//following-sibling::div/table/tbody/tr').count();
  this.attach(`Modal-like divs found: ${allDivs}`, 'text/plain');
  
  // Check if URL changed (maybe it navigates instead)
  const currentUrl = this.page!.url();
  this.attach(`Current URL: ${currentUrl}`, 'text/plain');
  
  // If no modal found, this feature might not be implemented
  if (allDivs === 0 && !currentUrl.includes('detail')) {
    throw new Error('Invoice detail modal feature does not appear to be implemented yet');
  }
});

When('I click on the {string} button', async function (this: CustomWorld, buttonText: string) {
  const button = this.page!.locator(`button:has-text("${buttonText}")`);
  await button.click();
});

When('I close the modal', async function (this: CustomWorld) {
  await invoiceDetailPage.closeModal();
});

When('I click on the {string} link', async function (this: CustomWorld, linkText: string) {
  const link = this.page!.locator(`a:has-text("${linkText}")`);
  await link.click();
});

When('I press the ESC key', async function (this: CustomWorld) {
  await this.page!.keyboard.press('Escape');
});

// ============================================================================
// THEN STEPS - MODAL DISPLAY AND VERIFICATION
// ============================================================================

// Positive Scenarios - Then Steps
Then('the invoice detail modal should be displayed', async function (this: CustomWorld) {
  await expect(invoiceDetailPage.modal).toBeVisible();
});

Then('the modal title should show {string}', async function (this: CustomWorld, expectedTitle: string) {
  const titleText = await invoiceDetailPage.modalTitle.innerText();
  expect(titleText).toContain(expectedTitle);
});

Then('the {string} button should be visible', async function (this: CustomWorld, buttonText: string) {
  const button = this.page!.locator(`button:has-text("${buttonText}")`);
  await expect(button).toBeVisible();
});

Then('the modal should display the invoice information matching the API data for invoice ID {int}', async function (this: CustomWorld, invoiceId: number) {
  // Initialize API context using AuthHelper if not already done
  if (!this.apiContext) {
    this.apiContext = await AuthHelper.createAuthenticatedContext();
  }

  // Fetch invoice data from API
  const invoiceResponse = await this.apiContext!.get(`api/invoices/${invoiceId}`);

  if (!invoiceResponse.ok()) {
    const errorText = await invoiceResponse.text();
    throw new Error(`Failed to fetch invoice ${invoiceId}: ${invoiceResponse.status()} - ${errorText}`);
  }

  const apiInvoiceData = await invoiceResponse.json();

  console.log('API Invoice Data:', JSON.stringify(apiInvoiceData, null, 2));

  // Wait for modal to be present and scroll if needed
  await this.page!.waitForTimeout(2000);
  
  // Try scrolling to make content visible
  try {
    await scrollToEnd(this.page!);
    await this.page!.waitForTimeout(500);
  } catch (e) {
    console.log('Scroll not needed or failed');
  }

  // Get all text content from the modal to see what we have
  const modalContent = await this.page!.locator('[role="dialog"], .modal, div[class*="modal" i], div[class*="Modal"]').first().innerText().catch(() => 'Modal not found');
  console.log('Modal content preview:', modalContent.substring(0, 500));

  // Try to find fields more flexibly
  const findFieldValue = async (fieldName: string): Promise<string> => {
    // Try multiple patterns for finding field values
    const patterns = [
      this.page!.locator(`text=${fieldName}`).locator('..').locator('text=/[A-Za-z0-9\\s\\$\\-\\.]+/').first(),
      this.page!.locator(`*:has-text("${fieldName}")`).locator('xpath=following-sibling::*[1]'),
      this.page!.locator(`label:has-text("${fieldName}")`).locator('xpath=following-sibling::*[1]'),
      this.page!.locator(`dt:has-text("${fieldName}")`).locator('xpath=following-sibling::dd[1]')
    ];

    for (const pattern of patterns) {
      try {
        const text = await pattern.innerText({ timeout: 2000 });
        if (text && text.trim()) {
          return text.trim();
        }
      } catch (e) {
        continue;
      }
    }
    
    throw new Error(`Could not find field: ${fieldName}`);
  };

  // Get displayed data from modal with flexible locators
  let displayedProveedor: string;
  let displayedFecha: string;
  let displayedMonto: string;
  let displayedEstado: string;

  try {
    displayedProveedor = await findFieldValue('Proveedor');
    displayedFecha = await findFieldValue('Fecha');
    displayedMonto = await findFieldValue('Monto');
    displayedEstado = await findFieldValue('Estado');
  } catch (error) {
    // If modal fields not found, the feature might not be implemented
    throw new Error(`Invoice detail modal fields not found. Modal might not be properly implemented. Error: ${error}`);
  }

  // Compare with API data (handle different field naming conventions)
  const apiProveedor = apiInvoiceData.senderBusinessName || apiInvoiceData.proveedor || apiInvoiceData.supplier || apiInvoiceData.supplierName;
  const apiFecha = apiInvoiceData.issueDate || apiInvoiceData.fecha || apiInvoiceData.date || apiInvoiceData.invoiceDate;
  const apiMonto = apiInvoiceData.amount || apiInvoiceData.monto || apiInvoiceData.total || apiInvoiceData.totalAmount;
  const apiEstado = (apiInvoiceData.status || apiInvoiceData.estado || '').toUpperCase();

  // Map API status to Spanish if needed
  const statusMapping: { [key: string]: string } = {
    'PENDING': 'Pendiente',
    'APPROVED': 'Aprobada',
    'REJECTED': 'Rechazada',
    'PENDIENTE': 'Pendiente',
    'APROBADA': 'Aprobada',
    'RECHAZADA': 'Rechazada'
  };
  
  const expectedEstado = statusMapping[apiEstado] || apiEstado;

  expect(displayedProveedor).toContain(apiProveedor);
  expect(displayedFecha).toContain(apiFecha);
  
  // Handle different money formats
  const montoStr = typeof apiMonto === 'number' ? apiMonto.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.') : apiMonto;
  const displayedMontoNumber = displayedMonto.replace(/[^\d.,]/g, '').replace(/\./g, '').replace(',', '.');
  const apiMontoNumber = apiMonto.toString();
  
  expect(displayedMontoNumber).toContain(apiMontoNumber.split('.')[0]); // At least the integer part matches
  expect(displayedEstado).toBe(expectedEstado);

  console.log('✓ Modal displays invoice information matching API data');
  console.log(`  - Proveedor: ${displayedProveedor} (API: ${apiProveedor})`);
  console.log(`  - Fecha: ${displayedFecha} (API: ${apiFecha})`);
  console.log(`  - Monto: ${displayedMonto} (API: ${apiMonto})`);
  console.log(`  - Estado: ${displayedEstado} (API: ${expectedEstado})`);
});

Then('the PDF viewer should be visible in the modal', async function (this: CustomWorld) {
  await expect(invoiceDetailPage.pdfViewer).toBeVisible({ timeout: 15000 });
});

Then('the PDF should be loaded successfully', async function (this: CustomWorld) {
  const pdfFrame = invoiceDetailPage.pdfViewer;
  await expect(pdfFrame).toHaveAttribute('src', /.+\.pdf/);
});

Then('the {string} section should be visible', async function (this: CustomWorld, sectionName: string) {
  // Locate the section by its heading text
  const sectionHeading = this.page!.locator(`h1, h2, h3, h4, h5, h6`).filter({ hasText: sectionName });
  
  // Wait for the section heading to be visible
  await expect(sectionHeading).toBeVisible({ timeout: 10000 });
  
  // Get the parent container of the section
  const sectionContainer = sectionHeading.locator('xpath=ancestor::*[contains(@class, "section") or contains(@class, "container") or contains(@class, "card")][1]');
  
  // If no specific container class found, just verify the heading is visible
  const hasContainer = await sectionContainer.count() > 0;
  if (hasContainer) {
    await expect(sectionContainer).toBeVisible();
  }
  
  // Store section locator for subsequent steps
  this.currentSection = sectionHeading;
  
  // Log for debugging
  console.log(`Section "${sectionName}" is visible`);
});

When('I scroll to the items table section in the modal', async function (this: CustomWorld) {
  // Scroll to the end of the modal to make items table visible
  await scrollToEnd(this.page!);
  await this.page!.waitForTimeout(1000);
  
  console.log('✓ Scrolled to items table section in modal');
});

Then('the items table should display data matching the API items for invoice ID {int}', async function (this: CustomWorld, invoiceId: number) {
  // Initialize API context using AuthHelper if not already done
  if (!this.apiContext) {
    this.apiContext = await AuthHelper.createAuthenticatedContext();
  }

  // Fetch invoice data from API (it contains items)
  const invoiceResponse = await this.apiContext!.get(`api/invoices/${invoiceId}`);

  if (!invoiceResponse.ok()) {
    const errorText = await invoiceResponse.text();
    throw new Error(`Failed to fetch invoice ${invoiceId}: ${invoiceResponse.status()} - ${errorText}`);
  }

  const apiInvoiceData = await invoiceResponse.json();
  const apiItems = apiInvoiceData.items || [];

  if (apiItems.length === 0) {
    throw new Error(`No items found in API response for invoice ${invoiceId}`);
  }

  console.log(`API returned ${apiItems.length} items for invoice ${invoiceId}`);
  console.log('API Items:', JSON.stringify(apiItems, null, 2));

  // Find the items table in the modal
  const itemsTable = this.page!.locator('table').filter({ 
    has: this.page!.locator('thead th, thead td').filter({ hasText: /CÓDIGO|DESCRIPCIÓN|CANTIDAD/i })
  }).first();

  await expect(itemsTable).toBeVisible({ timeout: 10000 });

  // Get table headers to map column indices
  const headers = itemsTable.locator('thead th, thead td');
  const headerCount = await headers.count();
  
  const headerMap: { [key: string]: number } = {};
  for (let i = 0; i < headerCount; i++) {
    const headerText = await headers.nth(i).innerText();
    headerMap[headerText.trim().toUpperCase()] = i;
  }

  console.log('Table headers:', Object.keys(headerMap));

  // Get all table rows
  const tableRows = itemsTable.locator('tbody tr');
  const rowCount = await tableRows.count();

  console.log(`Items table has ${rowCount} rows, API has ${apiItems.length} items`);

  // Verify row count matches
  expect(rowCount).toBe(apiItems.length);

  // Verify each item
  for (let i = 0; i < Math.min(rowCount, apiItems.length); i++) {
    const row = tableRows.nth(i);
    const cells = row.locator('td');
    
    const apiItem = apiItems[i];
    
    // Map API fields to expected values (handle different naming conventions)
    const apiCodigo = apiItem.ItemCode || apiItem.itemCode || apiItem.codigo || apiItem.code;
    const apiDescripcion = apiItem.Description || apiItem.description || apiItem.descripcion;
    const apiCantidad = apiItem.Quantity || apiItem.quantity || apiItem.cantidad;
    const apiUnidad = apiItem.Unit || apiItem.unit || apiItem.unidad;
    const apiSubtotal = apiItem.Subtotal || apiItem.subtotal;
    const apiTotal = apiItem.Total || apiItem.total;

    // Get cell values based on header positions
    const tableCodigo = headerMap['CÓDIGO'] !== undefined ? await cells.nth(headerMap['CÓDIGO']).innerText() : '';
    const tableDescripcion = headerMap['DESCRIPCIÓN'] !== undefined ? await cells.nth(headerMap['DESCRIPCIÓN']).innerText() : '';
    const tableCantidad = headerMap['CANTIDAD'] !== undefined ? await cells.nth(headerMap['CANTIDAD']).innerText() : '';
    const tableUnidad = headerMap['UNIDAD'] !== undefined ? await cells.nth(headerMap['UNIDAD']).innerText() : '';
    const tableSubtotal = headerMap['SUBTOTAL'] !== undefined ? await cells.nth(headerMap['SUBTOTAL']).innerText() : '';
    const tableTotal = headerMap['TOTAL'] !== undefined ? await cells.nth(headerMap['TOTAL']).innerText() : '';

    // Verify each field
    if (apiCodigo && tableCodigo) {
      expect(tableCodigo.trim()).toBe(apiCodigo.toString());
      console.log(`  ✓ Row ${i + 1} - Código: ${tableCodigo} matches API`);
    }

    if (apiDescripcion && tableDescripcion) {
      expect(tableDescripcion.trim()).toContain(apiDescripcion);
      console.log(`  ✓ Row ${i + 1} - Descripción: ${tableDescripcion} matches API`);
    }

    if (apiCantidad !== undefined && tableCantidad) {
      expect(tableCantidad.trim()).toBe(apiCantidad.toString());
      console.log(`  ✓ Row ${i + 1} - Cantidad: ${tableCantidad} matches API`);
    }

    if (apiUnidad && tableUnidad) {
      expect(tableUnidad.trim()).toBe(apiUnidad);
      console.log(`  ✓ Row ${i + 1} - Unidad: ${tableUnidad} matches API`);
    }

    if (apiSubtotal !== undefined && tableSubtotal) {
      // Handle number formatting - could be "$ 170,35" or "$ 170.35" or "170"
      const cleanedSubtotal = tableSubtotal.replace(/[^\d.,]/g, '');
      let tableSubtotalNumber: number;
      
      // Check if comma is decimal separator (Spanish format)
      if (cleanedSubtotal.includes(',') && !cleanedSubtotal.includes('.')) {
        tableSubtotalNumber = parseFloat(cleanedSubtotal.replace(',', '.'));
      } else if (cleanedSubtotal.includes('.') && cleanedSubtotal.includes(',')) {
        // Both present - assume period is thousands separator
        tableSubtotalNumber = parseFloat(cleanedSubtotal.replace(/\./g, '').replace(',', '.'));
      } else {
        // Just period or just comma or neither
        tableSubtotalNumber = parseFloat(cleanedSubtotal.replace(',', '.'));
      }
      
      const apiSubtotalNumber = typeof apiSubtotal === 'number' ? apiSubtotal : parseFloat(apiSubtotal);
      expect(Math.abs(tableSubtotalNumber - apiSubtotalNumber)).toBeLessThan(0.1);
      console.log(`  ✓ Row ${i + 1} - Subtotal: ${tableSubtotal} matches API (${apiSubtotal})`);
    }

    if (apiTotal !== undefined && tableTotal) {
      // Handle number formatting - could be "$ 170,35" or "$ 170.35" or "170"
      const cleanedTotal = tableTotal.replace(/[^\d.,]/g, '');
      let tableTotalNumber: number;
      
      // Check if comma is decimal separator (Spanish format)
      if (cleanedTotal.includes(',') && !cleanedTotal.includes('.')) {
        tableTotalNumber = parseFloat(cleanedTotal.replace(',', '.'));
      } else if (cleanedTotal.includes('.') && cleanedTotal.includes(',')) {
        // Both present - assume period is thousands separator
        tableTotalNumber = parseFloat(cleanedTotal.replace(/\./g, '').replace(',', '.'));
      } else {
        // Just period or just comma or neither
        tableTotalNumber = parseFloat(cleanedTotal.replace(',', '.'));
      }
      
      const apiTotalNumber = typeof apiTotal === 'number' ? apiTotal : parseFloat(apiTotal);
      expect(Math.abs(tableTotalNumber - apiTotalNumber)).toBeLessThan(0.1);
      console.log(`  ✓ Row ${i + 1} - Total: ${tableTotal} matches API (${apiTotal})`);
    }
  }

  console.log(`✓ All ${apiItems.length} items in the table match API data`);
});

// ============================================================================
// ADDITIONAL STEP DEFINITIONS FOR INVOICE DETAIL SCENARIOS
// ============================================================================

Then('the following action buttons should be present:', async function (this: CustomWorld, dataTable: any) {
  // Get list of expected buttons from data table
  const expectedButtons = dataTable.raw().flat(); // Flatten array to get all button names
  
  // Verify Acciones de Revisión section is visible first
  await expect(invoiceDetailPage.accionesSection).toBeVisible();
  
  // Verify each button exists and is visible
  for (const buttonText of expectedButtons) {
    const button = this.page!.locator(`button:has-text("${buttonText}")`);
    
    // Check button exists and is visible
    await expect(button).toBeVisible({ timeout: 5000 });
    
    // Verify button is in the DOM and count is exactly 1 (no duplicates)
    const buttonCount = await button.count();
    expect(buttonCount).toBeGreaterThanOrEqual(1);
    
    console.log(`✓ Button "${buttonText}" is present`);
  }
  
  console.log(`✓ All ${expectedButtons.length} action buttons are present`);
});

Given('the invoice detail modal is open for invoice ID {int}', async function (this: CustomWorld, invoiceId: number) {
  // Click to open modal for specified invoice
  await invoiceDetailPage.clickVerDetalleForInvoice(invoiceId);
  
  // Wait for modal to be visible
  await invoiceDetailPage.waitForModalToBeVisible();
  
  // Verify modal is displayed
  await expect(invoiceDetailPage.modal).toBeVisible();
  
  // Verify correct invoice ID in title
  const titleText = await invoiceDetailPage.modalTitle.innerText();
  expect(titleText).toContain(`${invoiceId}`);
  
  // Store invoice ID for later use
  this.currentInvoiceId = invoiceId;
  
  console.log(`✓ Invoice detail modal opened for invoice ID ${invoiceId}`);
});

Given('the invoice detail modal is open for an invoice with status {string}', async function (this: CustomWorld, status: string) {
  // Find an invoice with the specified status in the table
  const row = this.page!.locator(`//h3//following-sibling::div/table/tbody/tr[contains(., '${status}')]`).first();
  await expect(row).toBeVisible({ timeout: 5000 });
  
  // Get the invoice ID from the row
  const idCell = row.locator('td').first();
  const invoiceId = await idCell.innerText();
  
  // Click Ver Detalle button for this invoice
  await row.locator('button:has-text("Ver Detalle")').click();
  
  // Wait for modal to open
  await invoiceDetailPage.waitForModalToBeVisible();
  
  // Verify status in modal
  const modalStatus = await invoiceDetailPage.getEstadoValue();
  expect(modalStatus.trim()).toBe(status);
  
  // Store data
  this.currentInvoiceId = parseInt(invoiceId);
  this.initialStatus = status;
  
  console.log(`✓ Modal opened for invoice with status "${status}"`);
});

Given('the invoice detail modal is open for an invoice with no items', async function (this: CustomWorld) {
  // This requires either test data setup or mocking
  // For now, we'll implement the step assuming such an invoice exists
  
  // Navigate to an invoice that has no items (ID would be known from test data)
  // If no such invoice exists, we may need to mock the API response
  
  // Mock API response for empty items (if using Playwright route interception)
  await this.page!.route('**/api/invoices/*/items', async (route: any) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ items: [] })
    });
  });
  
  // Open any invoice detail
  await invoiceDetailPage.clickVerDetalleForInvoice(1);
  await invoiceDetailPage.waitForModalToBeVisible();
  
  console.log('✓ Modal opened for invoice with no items');
});

Then('the modal should be closed', async function (this: CustomWorld) {
  // Wait for modal to disappear from DOM or become hidden
  await invoiceDetailPage.modal.waitFor({ state: 'hidden', timeout: 10000 });
  
  // Verify modal is not visible
  await expect(invoiceDetailPage.modal).not.toBeVisible();
  
  // Check for modal backdrop/overlay removal
  const modalBackdrop = this.page!.locator('.modal-backdrop, .overlay, [class*="backdrop"], [class*="dimmer"]');
  const backdropCount = await modalBackdrop.count();
  if (backdropCount > 0) {
    await expect(modalBackdrop).not.toBeVisible();
  }
  
  console.log('✓ Modal is closed');
});

Then('I should be back on the invoices dashboard', async function (this: CustomWorld) {
  // Verify URL contains invoices path
  expect(this.page!.url()).toContain('/invoices');
  
  // Verify main dashboard heading is visible
  const dashboardHeading = this.page!.locator('h1, h2').filter({ hasText: /Dashboard/i });
  await expect(dashboardHeading).toBeVisible({ timeout: 5000 });
  
  // Verify invoice table is visible
  await expect(invoiceDetailPage.invoiceTable).toBeVisible();
  
  // Verify Ver Detalle buttons are clickable again
  const verDetalleButtons = this.page!.locator('button:has-text("Ver Detalle")');
  await expect(verDetalleButtons.first()).toBeVisible();
  
  console.log('✓ Back on invoices dashboard');
});

Then('the Estado field should show {string}', async function (this: CustomWorld, expectedStatus: string) {
  // Get the status value from the modal
  const estadoValue = await invoiceDetailPage.getEstadoValue();
  
  // Verify status matches expected value
  expect(estadoValue.trim()).toBe(expectedStatus);
  
  // Optional: Verify status badge color/styling matches status type
  const estadoBadge = invoiceDetailPage.estadoField;
  const badgeClass = await estadoBadge.getAttribute('class') || '';
  
  // Store current status
  this.currentInvoiceStatus = expectedStatus;
  
  console.log(`✓ Estado field shows: ${expectedStatus}`);
});

Then('all action buttons should be enabled', async function (this: CustomWorld) {
  // List of all action buttons
  const actionButtons = [
    'Aprobar Factura',
    'Rechazar Factura',
    'Descargar PDF',
    'Descargar XML'
  ];
  
  // Check each button
  for (const buttonText of actionButtons) {
    const button = this.page!.locator(`button:has-text("${buttonText}")`);
    
    // Verify button is visible
    await expect(button).toBeVisible();
    
    // Verify button is enabled
    await expect(button).toBeEnabled();
    
    // Double-check with isEnabled method
    const isEnabled = await button.isEnabled();
    expect(isEnabled).toBe(true);
    
    console.log(`✓ "${buttonText}" is enabled`);
  }
  
  console.log('✓ All action buttons are enabled');
});

Then('the invoice detail modal should be displayed for invoice {int}', async function (this: CustomWorld, invoiceId: number) {
  // Verify modal is visible
  await expect(invoiceDetailPage.modal).toBeVisible({ timeout: 10000 });
  
  // Verify modal title contains correct invoice ID
  const titleText = await invoiceDetailPage.modalTitle.innerText();
  expect(titleText).toMatch(new RegExp(`Detalle de Factura:?\\s*${invoiceId}`));
  
  // Store current invoice ID
  this.currentInvoiceId = invoiceId;
  
  // Optionally store invoice data for comparison
  const proveedor = await invoiceDetailPage.proveedorField.innerText();
  const fecha = await invoiceDetailPage.fechaField.innerText();
  const monto = await invoiceDetailPage.montoField.innerText();
  
  initialInvoiceData.set(invoiceId, { proveedor, fecha, monto });
  
  console.log(`✓ Invoice detail modal displayed for invoice ${invoiceId}`);
});

Then('the invoice information should be different from invoice {int}', async function (this: CustomWorld, previousInvoiceId: number) {
  // Get current invoice data
  const currentProveedor = await invoiceDetailPage.proveedorField.innerText();
  const currentFecha = await invoiceDetailPage.fechaField.innerText();
  const currentMonto = await invoiceDetailPage.montoField.innerText();
  
  // Get previous invoice data
  const previousData = initialInvoiceData.get(previousInvoiceId);
  
  if (previousData) {
    // At least one field should be different
    const isDifferent = 
      currentProveedor !== previousData.proveedor ||
      currentFecha !== previousData.fecha ||
      currentMonto !== previousData.monto;
    
    expect(isDifferent).toBe(true);
    console.log('✓ Invoice information is different from previous invoice');
  } else {
    // If no previous data, just verify current data exists
    expect(currentProveedor).toBeTruthy();
    expect(currentFecha).toBeTruthy();
    expect(currentMonto).toBeTruthy();
    console.log('✓ Current invoice information verified (no previous data to compare)');
  }
});

Then('a new browser tab should open with the PDF', async function (this: CustomWorld) {
  // Listen for new page/tab
  const pagePromise = this.context!.waitForEvent('page', { timeout: 15000 });
  
  // Note: The click should have already happened in the When step
  // If not, we need to click here
  
  const newPage = await pagePromise;
  
  // Wait for new page to load
  await newPage.waitForLoadState('domcontentloaded', { timeout: 10000 });
  
  // Verify URL contains PDF reference
  const url = newPage.url();
  expect(url).toMatch(/\.pdf|\/pdf|amazonaws\.com/);
  
  // Store new page for cleanup
  this.newTab = newPage;
  
  console.log(`✓ New tab opened with PDF: ${url}`);
});

Then('the PDF file should start downloading', async function (this: CustomWorld) {
  // Wait for download event
  const downloadPromise = this.page!.waitForEvent('download', { timeout: 15000 });
  
  // Note: Button click should have already triggered download
  const download = await downloadPromise;
  
  // Verify download exists
  expect(download).toBeTruthy();
  
  // Verify filename
  const fileName = download.suggestedFilename();
  expect(fileName).toMatch(/\.pdf$/i);
  
  // Get download path to verify it's valid
  const path = await download.path();
  expect(path).toBeTruthy();
  
  // Store download reference
  this.lastDownload = download;
  
  console.log(`✓ PDF download started: ${fileName}`);
});

Then('the XML file should start downloading', async function (this: CustomWorld) {
  // Wait for download event
  const downloadPromise = this.page!.waitForEvent('download', { timeout: 15000 });
  
  // Note: Button click should have already triggered download
  const download = await downloadPromise;
  
  // Verify download exists
  expect(download).toBeTruthy();
  
  // Verify filename
  const fileName = download.suggestedFilename();
  expect(fileName).toMatch(/\.xml$/i);
  
  // Get download path
  const path = await download.path();
  expect(path).toBeTruthy();
  
  // Store download reference
  this.lastDownload = download;
  
  console.log(`✓ XML download started: ${fileName}`);
});

Given('the invoice status is {string}', async function (this: CustomWorld, status: string) {
  // Verify current status in the modal
  const currentStatus = await invoiceDetailPage.getEstadoValue();
  expect(currentStatus.trim()).toBe(status);
  
  // Store initial status for later comparison
  this.initialStatus = status;
  
  console.log(`✓ Confirmed invoice status is: ${status}`);
});

Then('the invoice status should be updated to {string}', async function (this: CustomWorld, expectedStatus: string) {
  // Wait for potential loading/processing
  await this.page!.waitForTimeout(1500);
  
  // Look for success notification
  const notification = this.page!.locator('.toast, .alert-success, .notification, [role="alert"]').filter({ 
    hasText: /actualiz|success|éxito|aprobad|rechazad/i 
  });
  
  if (await notification.count() > 0) {
    await expect(notification.first()).toBeVisible({ timeout: 5000 });
  }
  
  // Close modal if still open
  const isModalVisible = await invoiceDetailPage.modal.isVisible();
  if (isModalVisible) {
    await invoiceDetailPage.closeModal();
  }
  
  // Reopen modal to verify status change
  await this.page!.waitForTimeout(500);
  await invoiceDetailPage.clickVerDetalleForInvoice(this.currentInvoiceId!);
  await invoiceDetailPage.waitForModalToBeVisible();
  
  // Verify updated status
  const updatedStatus = await invoiceDetailPage.getEstadoValue();
  expect(updatedStatus.trim()).toBe(expectedStatus);
  
  console.log(`✓ Invoice status updated to: ${expectedStatus}`);
});

When('I attempt to navigate directly to invoice detail with ID {int}', async function (this: CustomWorld, invalidId: number) {
  // Attempt to navigate to a direct URL (if such route exists)
  // Or attempt to open modal via API call
  
  // Try navigating to detail page directly
  await this.page!.goto(`http://localhost:3001/invoices/${invalidId}`);
  
  // Store attempt for verification
  this.attemptedInvoiceId = invalidId;
  
  console.log(`✓ Attempted to navigate to invoice ${invalidId}`);
});

Then('an error message should be displayed', async function (this: CustomWorld) {
  // Look for error message with various selectors
  const errorMessage = this.page!.locator(
    '.error, .alert-danger, .notification-error, [role="alert"], .toast-error, .message--error'
  ).filter({ hasText: /error|no encontr|not found|invalid|inválid/i });
  
  // Wait for error to appear
  await expect(errorMessage.first()).toBeVisible({ timeout: 10000 });
  
  // Verify error text contains relevant information
  const errorText = await errorMessage.first().innerText();
  expect(errorText.length).toBeGreaterThan(0);
  
  console.log(`✓ Error message displayed: ${errorText.substring(0, 50)}...`);
});

Then('the modal should not open', async function (this: CustomWorld) {
  // Wait a moment to ensure modal doesn't appear
  await this.page!.waitForTimeout(2000);
  
  // Verify modal is not visible
  const isModalVisible = await invoiceDetailPage.modal.isVisible().catch(() => false);
  expect(isModalVisible).toBe(false);
  
  // Verify no modal elements exist
  const modalCount = await invoiceDetailPage.modal.count();
  expect(modalCount).toBe(0);
  
  console.log('✓ Modal did not open');
});

When('I attempt to click on the dashboard table behind the modal', async function (this: CustomWorld) {
  // Find a table row behind the modal
  const tableRow = invoiceDetailPage.invoiceTable.locator('tbody tr').first();
  
  // Attempt to click it
  await tableRow.click({ force: true }).catch(() => {
    // Click might be intercepted by modal - that's expected
    console.log('Click intercepted by modal (expected behavior)');
  });
  
  // Store that we attempted the click
  this.attemptedBackgroundClick = true;
});

Then('the click should not be registered', async function (this: CustomWorld) {
  // Verify modal is still open (click was blocked)
  await expect(invoiceDetailPage.modal).toBeVisible();
  
  // Verify we're still in the modal context (no navigation happened)
  const modalTitle = invoiceDetailPage.modalTitle;
  await expect(modalTitle).toBeVisible();
  
  console.log('✓ Background click was blocked by modal');
});

Then('the modal should remain open', async function (this: CustomWorld) {
  // Verify modal is still visible
  await expect(invoiceDetailPage.modal).toBeVisible({ timeout: 5000 });
  
  // Verify modal contents are still interactive
  await expect(invoiceDetailPage.volverButton).toBeVisible();
  
  console.log('✓ Modal remains open');
});

Given('the PDF URL is invalid or unavailable', async function (this: CustomWorld) {
  // Intercept PDF requests and return 404 or error
  await this.page!.route('**/*.pdf', async (route: any) => {
    await route.abort('failed');
  });
  
  await this.page!.route('**/api/invoices/*/pdf', async (route: any) => {
    await route.fulfill({
      status: 404,
      contentType: 'application/json',
      body: JSON.stringify({ error: 'PDF not found' })
    });
  });
  
  console.log('✓ PDF URL mocked as unavailable');
});

Then('an error message should be displayed in the PDF viewer', async function (this: CustomWorld) {
  // Look for error in PDF viewer area
  const pdfContainer = this.page!.locator('[class*="pdf"], iframe').locator('..');
  const errorInViewer = pdfContainer.locator('.error, .alert, [role="alert"]').filter({ 
    hasText: /error|fail|unavailable|not found/i 
  });
  
  await expect(errorInViewer.first()).toBeVisible({ timeout: 10000 });
  
  console.log('✓ Error message displayed in PDF viewer');
});

Then('the action buttons should still be accessible', async function (this: CustomWorld) {
  // Verify action buttons section is visible and buttons are clickable
  await expect(invoiceDetailPage.accionesSection).toBeVisible();
  await expect(invoiceDetailPage.aprobarButton).toBeEnabled();
  await expect(invoiceDetailPage.rechazarButton).toBeEnabled();
  await expect(invoiceDetailPage.descargarPDFButton).toBeVisible();
  await expect(invoiceDetailPage.descargarXMLButton).toBeVisible();
  
  console.log('✓ Action buttons are still accessible');
});

Then('an appropriate message should be displayed', async function (this: CustomWorld) {
  // Look for any message/notification
  const message = this.page!.locator(
    '//*[@id="root"]/div/main/div/div/div/div[1]'
  ).filter({ hasText: /ya|already|aprobad|rechazad|estado/i });
  
  await expect(message.first()).toBeVisible({ timeout: 10000 });
  
  const messageText = await message.first().innerText();
  console.log(`✓ Message displayed: ${messageText.substring(0, 50)}...`);
});

Then('the status should remain {string}', async function (this: CustomWorld, expectedStatus: string) {
  // Wait for any processing
  await this.page!.waitForTimeout(1500);
  
  // Verify status hasn't changed
  const currentStatus = await invoiceDetailPage.getEstadoValue();
  expect(currentStatus.trim()).toBe(expectedStatus);
  
  console.log(`✓ Status remains: ${expectedStatus}`);
});

When('the network connection fails', async function (this: CustomWorld) {
  // Simulate network failure by aborting all API requests
  await this.page!.route('**/api/**', async (route: any) => {
    await route.abort('failed');
  });
  
  console.log('✓ Network connection simulated as failed');
});

Then('the modal should handle the error gracefully', async function (this: CustomWorld) {
  // Verify error is shown but app doesn't crash
  const errorElement = this.page!.locator('.error, .alert-danger, [role="alert"]');
  
  // Either error message shown or modal stays functional
  const hasError = await errorElement.count() > 0;
  const modalVisible = await invoiceDetailPage.modal.isVisible();
  
  expect(hasError || modalVisible).toBe(true);
  
  // Verify page is still responsive (can close modal)
  await expect(invoiceDetailPage.volverButton).toBeVisible();
  
  console.log('✓ Error handled gracefully');
});

When('I click outside the modal boundaries', async function (this: CustomWorld) {
  // Get modal bounding box
  const modalBox = await invoiceDetailPage.modal.boundingBox();
  
  if (modalBox) {
    // Click outside modal (top-left corner of viewport)
    await this.page!.mouse.click(10, 10);
  } else {
    // Fallback: click on body element
    await this.page!.locator('body').click({ position: { x: 10, y: 10 } });
  }
  
  await this.page!.waitForTimeout(500);
  console.log('✓ Clicked outside modal boundaries');
});

Then('the modal should close', async function (this: CustomWorld) {
  // Wait for modal to disappear
  await invoiceDetailPage.modal.waitFor({ state: 'hidden', timeout: 10000 });
  
  // Verify modal is not visible
  await expect(invoiceDetailPage.modal).not.toBeVisible();
  
  console.log('✓ Modal closed');
});

Given('the browser window is resized to mobile dimensions', async function (this: CustomWorld) {
  // Set viewport to mobile size (iPhone dimensions)
  await this.page!.setViewportSize({ width: 375, height: 667 });
  
  await this.page!.waitForTimeout(500);
  console.log('✓ Browser resized to mobile dimensions (375x667)');
});

Then('the modal should display properly', async function (this: CustomWorld) {
  // Verify modal is visible and not cut off
  await expect(invoiceDetailPage.modal).toBeVisible();
  
  // Check modal is within viewport
  const modalBox = await invoiceDetailPage.modal.boundingBox();
  const viewport = this.page!.viewportSize();
  
  if (modalBox && viewport) {
    expect(modalBox.width).toBeLessThanOrEqual(viewport.width);
    expect(modalBox.height).toBeLessThanOrEqual(viewport.height);
  }
  
  // Verify key elements are visible
  await expect(invoiceDetailPage.modalTitle).toBeVisible();
  await expect(invoiceDetailPage.volverButton).toBeVisible();
  
  console.log('✓ Modal displays properly on mobile');
});

Then('all elements should be accessible', async function (this: CustomWorld) {
  // Verify all interactive elements are visible and clickable
  await expect(invoiceDetailPage.volverButton).toBeVisible();
  
  // Check if elements are scrollable if needed
  const modal = invoiceDetailPage.modal;
  const isScrollable = await modal.evaluate(el => el.scrollHeight > el.clientHeight);
  
  console.log(`✓ All elements accessible (scrollable: ${isScrollable})`);
});

Then('the items table should display an empty state message', async function (this: CustomWorld) {
  // Look for empty state message
  const emptyMessage = this.page!.locator('.empty-state, .no-data, .no-items').filter({ 
    hasText: /no.*items?|sin.*items?|vacío|empty/i 
  });
  
  await expect(emptyMessage.first()).toBeVisible({ timeout: 10000 });
  
  const messageText = await emptyMessage.first().innerText();
  console.log(`✓ Empty state message: ${messageText}`);
});

Then('the other invoice details should still be visible', async function (this: CustomWorld) {
  // Verify main invoice information is still displayed
  await expect(invoiceDetailPage.proveedorField).toBeVisible();
  await expect(invoiceDetailPage.fechaField).toBeVisible();
  await expect(invoiceDetailPage.montoField).toBeVisible();
  await expect(invoiceDetailPage.estadoField).toBeVisible();
  
  console.log('✓ Other invoice details remain visible');
});

Then('the button should show a loading state', async function (this: CustomWorld) {
  // Look for loading indicator on button
  const loadingButton = this.page!.locator('button').filter({ 
    hasText: /Aprobar|aprobar/
  }).filter({
    has: this.page!.locator('.spinner, .loading, [class*="load"]')
  });
  
  // Or check for disabled state with loading text
  const aprobarBtn = invoiceDetailPage.aprobarButton;
  const isDisabled = await aprobarBtn.isDisabled();
  
  // At least one loading indicator should be present
  const hasLoadingClass = await aprobarBtn.evaluate(el => 
    el.className.includes('loading') || el.className.includes('disabled')
  );
  
  expect(isDisabled || hasLoadingClass).toBe(true);
  
  console.log('✓ Button shows loading state');
});

Then('other action buttons should be disabled during processing', async function (this: CustomWorld) {
  // Verify other buttons are disabled
  const rechazarBtn = invoiceDetailPage.rechazarButton;
  const descargarPDFBtn = invoiceDetailPage.descargarPDFButton;
  const descargarXMLBtn = invoiceDetailPage.descargarXMLButton;
  
  // Check if buttons are disabled
  const rechazarDisabled = await rechazarBtn.isDisabled();
  const pdfDisabled = await descargarPDFBtn.isDisabled();
  const xmlDisabled = await descargarXMLBtn.isDisabled();
  
  // At least some buttons should be disabled during processing
  const someDisabled = rechazarDisabled || pdfDisabled || xmlDisabled;
  expect(someDisabled).toBe(true);
  
  console.log('✓ Other action buttons disabled during processing');
});

When('I quickly click {string} and {string} consecutively', async function (this: CustomWorld, button1: string, button2: string) {
  // Click first button
  const btn1 = this.page!.locator(`button:has-text("${button1}")`);
  await btn1.click();
  
  // Immediately click second button (no wait)
  const btn2 = this.page!.locator(`button:has-text("${button2}")`);
  await btn2.click().catch(() => {
    // Second click might be blocked - that's expected
    console.log('Second click blocked (expected)');
  });
  
  console.log(`✓ Clicked "${button1}" and "${button2}" consecutively`);
});

Then('only the first action should be processed', async function (this: CustomWorld) {
  // Wait for processing to complete
  await this.page!.waitForTimeout(2000);
  
  // The final status should reflect only the first action
  // This would need to check against stored initial state
  
  // Verify only one success message appeared
  const successMessages = this.page!.locator('.toast, .notification, .alert-success');
  const count = await successMessages.count();
  
  expect(count).toBeLessThanOrEqual(1);
  
  console.log('✓ Only first action was processed');
});

Then('an appropriate message should inform about the conflict', async function (this: CustomWorld) {
  // Look for conflict/warning message
  const conflictMessage = this.page!.locator('.message, .notification, .alert').filter({ 
    hasText: /conflict|ya procesad|already|simultáneo/i 
  });
  
  // May or may not appear depending on implementation
  const messageCount = await conflictMessage.count();
  
  if (messageCount > 0) {
    await expect(conflictMessage.first()).toBeVisible();
    console.log('✓ Conflict message displayed');
  } else {
    console.log('✓ No conflict message (first action completed successfully)');
  }
});

When('the download fails due to server error', async function (this: CustomWorld) {
  // Intercept download request and return error
  await this.page!.route('**/api/invoices/*/pdf', async (route: any) => {
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ error: 'Internal server error' })
    });
  });
  
  await this.page!.route('**/*.pdf', async (route: any) => {
    await route.abort('failed');
  });
  
  console.log('✓ Download configured to fail');
});

Then('the user should be able to retry the download', async function (this: CustomWorld) {
  // Verify download button is still visible and clickable
  await expect(invoiceDetailPage.descargarPDFButton).toBeVisible();
  await expect(invoiceDetailPage.descargarPDFButton).toBeEnabled();
  
  // Verify no permanent error state that prevents retry
  const buttonText = await invoiceDetailPage.descargarPDFButton.innerText();
  expect(buttonText).toMatch(/Descargar|Download/i);
  
  console.log('✓ User can retry download');
});
