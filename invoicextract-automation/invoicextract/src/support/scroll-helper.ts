import { Page } from '@playwright/test';

/**
 * Scroll to the end of the invoice details visor (outer container)
 */
export async function scrollToEnd(page: Page): Promise<void> {
  // Focus on the modal content area
  await page.click('text=Detalle de Factura');
  
  // Scroll to the bottom using keyboard shortcut
  await page.keyboard.press('Meta+ArrowDown'); // Mac: cmd+down, Windows/Linux: use 'Control+End'
  
  // Wait for scroll to complete
  await page.waitForTimeout(500);
}

/**
 * Scroll to the top of the invoice details visor (outer container)
 */
export async function scrollToTop(page: Page): Promise<void> {
  // Focus on the modal content area
  await page.click('text=Detalle de Factura');
  
  // Scroll to the top using keyboard shortcut
  await page.keyboard.press('Meta+ArrowUp'); // Mac: cmd+up, Windows/Linux: use 'Control+Home'
  
  // Wait for scroll to complete
  await page.waitForTimeout(500);
}
