/**
 * Common configuration for test environments
 * Can be imported and used across test files
 */

export const config = {
  // Base URLs for InvoiceExtract Application
  baseUrl: process.env.BASE_URL || 'http://localhost:3001',
  frontendUrl: process.env.FRONTEND_URL || 'http://localhost:3001',
  keycloakBaseUrl: process.env.KEYCLOAK_BASE_URL || 'http://localhost:8085',
  apiBaseUrl: process.env.API_BASE_URL || 'https://api.example.com',

  // Application URLs
  urls: {
    home: 'http://localhost:3001/',
    emailConfig: 'http://localhost:3001/email-config',
    invoiceDashboard: 'http://localhost:3001/invoices',
    mapping: 'http://localhost:3001/mapping',
    erpConfig: 'http://localhost:3001/erp-config',
  },

  // Test Credentials by Role
  credentials: {
    admin: {
      username: process.env.ADMIN_USERNAME || 'admin',
      password: process.env.ADMIN_PASSWORD || 'admin123'
    },
    finance: {
      username: process.env.FINANCE_USERNAME || 'finance',
      password: process.env.FINANCE_PASSWORD || 'finance123'
    },
    technician: {
      username: process.env.TECHNICIAN_USERNAME || 'technician',
      password: process.env.TECHNICIAN_PASSWORD || 'tech123'
    }
  },

  // Helper function to get credentials by role
  getCredentialsByRole(role: string): { username: string; password: string } {
    const normalizedRole = role.toLowerCase();
    if (normalizedRole === 'admin' || normalizedRole === 'administrator') {
      return this.credentials.admin;
    } else if (normalizedRole === 'finance') {
      return this.credentials.finance;
    } else if (normalizedRole === 'technician' || normalizedRole === 'tech') {
      return this.credentials.technician;
    } else {
      throw new Error(`Unknown role: ${role}. Valid roles are: admin, finance, technician`);
    }
  },

  // Browser settings
  browser: {
    headless: process.env.HEADLESS !== 'false',
    slowMo: parseInt(process.env.SLOW_MO || '0'),
    viewport: {
      width: 1920,
      height: 1080
    },
    timeout: parseInt(process.env.TIMEOUT || '60000')
  },

  // Test settings
  test: {
    defaultTimeout: 60000,
    retries: 1,
    screenshotOnFailure: true
  },

  // API settings
  api: {
    timeout: parseInt(process.env.API_TIMEOUT || '30000'),
    retries: 2
  },

  // Report settings
  report: {
    outputDir: 'reports',
    screenshotDir: 'reports/screenshots',
    videoDir: 'reports/videos'
  },

  // Test data paths
  testData: {
    invoices: 'test-data/invoices',
    documents: 'test-data/documents',
    fixtures: 'test-data/fixtures'
  }
};

export default config;
