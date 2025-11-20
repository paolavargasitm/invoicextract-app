const reporter = require('cucumber-html-reporter');
const fs = require('fs');
const path = require('path');

// Ensure reports directory exists
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

const options = {
  theme: 'bootstrap',
  jsonFile: reportFile,
  output: path.join(reportsDir, 'report-standalone.html'),
  reportSuiteAsScenarios: true,
  scenarioTimestamp: true,
  launchReport: false,
  metadata: {
    'App Version': '1.0.0',
    'Test Environment': 'TEST',
    'Browser': 'Chromium',
    'Platform': process.platform,
    'Node Version': process.version,
    'Executed': new Date().toLocaleString()
  },
  brandTitle: 'Invoice Extract - Test Report',
  name: 'Invoice Extract Automation Tests'
};

reporter.generate(options);

const fileSize = fs.statSync(options.output).size;
console.log('\n✅ Single-file report generated successfully!');
console.log(`📄 Report location: ${options.output}`);
console.log(`📦 File size: ${(fileSize / 1024).toFixed(2)} KB`);
console.log('\n💡 This is a standalone HTML file that can be easily shared.');
console.log('   It contains all styles and data inline - no external dependencies needed.\n');
