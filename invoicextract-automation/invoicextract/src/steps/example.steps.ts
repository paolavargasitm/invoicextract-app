import { Given, Then } from '@cucumber/cucumber';
import { expect } from '@playwright/test';
import { CustomWorld } from '../support/world';

Given('I navigate to {string}', async function (this: CustomWorld, url: string) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  await this.page.goto(url);
});

Then('the page title should contain {string}', async function (this: CustomWorld, expectedTitle: string) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  const title = await this.page.title();
  expect(title).toContain(expectedTitle);
});

Then('the page should be loaded', async function (this: CustomWorld) {
  if (!this.page) {
    throw new Error('Page not initialized');
  }
  await this.page.waitForLoadState('domcontentloaded');
  const url = this.page.url();
  expect(url).toBeTruthy();
});
