// Load environment variables from .env file
require('dotenv').config();

module.exports = {
  default: {
    require: ['src/steps/**/*.ts', 'src/support/**/*.ts'],
    requireModule: ['ts-node/register'],
    format: [
      'progress-bar',
      'html:reports/cucumber-html-report.html',
      'json:reports/cucumber-report.json'
    ],
    formatOptions: {
      snippetInterface: 'async-await'
    },
    worldParameters: {
      headless: false,
      slowMo: 0
    },
    publishQuiet: true
  }
};
