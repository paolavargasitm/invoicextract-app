const report = require('multiple-cucumber-html-reporter');
const fs = require('fs');
const path = require('path');

// Ensure reports directory exists (relative to project root)
const reportsDir = path.join(__dirname, '../../reports');
if (!fs.existsSync(reportsDir)) {
  fs.mkdirSync(reportsDir, { recursive: true });
}

// Check if cucumber-report.json exists
const reportFile = path.join(reportsDir, 'cucumber-report.json');
if (!fs.existsSync(reportFile)) {
  console.error('No cucumber-report.json found. Please run tests first.');
  console.error(`Looking for: ${reportFile}`);
  process.exit(1);
}

report.generate({
  jsonDir: reportsDir,
  reportPath: reportsDir,
  reportName: 'Cucumber Test Report',
  pageTitle: 'Invoice Extract - Test Report',
  displayDuration: true,
  displayReportTime: true,
  metadata: {
    browser: {
      name: 'chromium',
      version: 'latest'
    },
    device: 'Local test machine',
    platform: {
      name: process.platform,
      version: process.version
    }
  },
  customData: {
    title: 'Run Info',
    data: [
      { label: 'Project', value: 'Invoice Extract' },
      { label: 'Environment', value: 'Test' },
      { label: 'Execution Time', value: new Date().toISOString() }
    ]
  }
});

console.log('Report generated successfully!');
console.log(`Report location: ${path.join(reportsDir, 'index.html')}`);
