# Test Data Directory

This directory contains test data files used in automated tests.

## Contents

- Sample invoices (PDF, images)
- Mock data files (JSON, CSV)
- Test fixtures

## Adding Test Data

1. Place test files in this directory
2. Reference them in your feature files
3. Use relative paths in step definitions

Example:
```typescript
const filePath = path.join(__dirname, '../test-data', 'sample-invoice.pdf');
```

## Naming Convention

- Use descriptive names: `valid-invoice-001.pdf`
- Include test type: `invalid-format.txt`
- Keep files small for faster test execution
