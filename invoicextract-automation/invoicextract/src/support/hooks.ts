import { Before, After, Status, setDefaultTimeout } from '@cucumber/cucumber';
import { CustomWorld } from './world';

// Set default timeout to 60 seconds
setDefaultTimeout(60 * 1000);

Before(async function (this: CustomWorld, { pickle }) {
  // Check if this is an API test by looking at the feature file name or tags
  const isApiTest = pickle.uri?.includes('api-') || 
                    pickle.tags?.some(tag => tag.name === '@api');
  
  this.isApiTest = isApiTest;
  
  // Only initialize browser for UI tests
  if (!isApiTest) {
    await this.initBrowser();
  }
  // API context is initialized on-demand in the API steps
});

After(async function (this: CustomWorld, { result, pickle }) {
  // Take screenshot on failure (only for UI tests with a page)
  if (result?.status === Status.FAILED && this.page) {
    const screenshot = await this.page.screenshot({ fullPage: true });
    await this.attach(screenshot, 'image/png');
  }
  
  // Cleanup resources
  await this.cleanup();
});
