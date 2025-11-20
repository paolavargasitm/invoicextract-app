import * as fs from 'fs';
import * as path from 'path';

/**
 * Generic helper class to load and filter test data from JSON files
 */
export class TestDataHelper {
  /**
   * Loads test data from a JSON file and filters by testCaseId
   * @param fileName - Name of the JSON file in test-data folder (e.g., 'invoices.json')
   * @param testCaseId - The testCaseId to filter by
   * @returns The matching test data object
   * @throws Error if file not found or testCaseId not found
   */
  static getTestData<T>(fileName: string, testCaseId: string): T {
    const testDataPath = path.resolve(__dirname, '../../test-data', fileName);
    
    if (!fs.existsSync(testDataPath)) {
      throw new Error(`Test data file not found: ${testDataPath}`);
    }

    const fileContent = fs.readFileSync(testDataPath, 'utf-8');
    const testDataArray = JSON.parse(fileContent);

    if (!Array.isArray(testDataArray)) {
      throw new Error(`Test data file ${fileName} must contain an array of test data objects`);
    }

    const testData = testDataArray.find((item: any) => item.testCaseId === testCaseId);

    if (!testData) {
      const availableIds = testDataArray.map((item: any) => item.testCaseId).join(', ');
      throw new Error(
        `Test data with testCaseId '${testCaseId}' not found in ${fileName}.\n` +
        `Available test case IDs: ${availableIds}`
      );
    }

    // Remove testCaseId from the returned object as it's only for filtering
    const { testCaseId: _, ...dataWithoutTestCaseId } = testData;
    
    // Make DocumentNumber unique by appending timestamp (for invoices.json)
    if (fileName === 'invoices.json' && dataWithoutTestCaseId.hasOwnProperty('DocumentNumber')) {
      const timestamp = Date.now();
      const data = dataWithoutTestCaseId as any;
      data.DocumentNumber = `${data.DocumentNumber}-${timestamp}`;
    }
    
    return dataWithoutTestCaseId as T;
  }

  /**
   * Loads all test data from a JSON file
   * @param fileName - Name of the JSON file in test-data folder
   * @returns Array of all test data objects
   */
  static getAllTestData<T>(fileName: string): T[] {
    const testDataPath = path.resolve(__dirname, '../../test-data', fileName);
    
    if (!fs.existsSync(testDataPath)) {
      throw new Error(`Test data file not found: ${testDataPath}`);
    }

    const fileContent = fs.readFileSync(testDataPath, 'utf-8');
    return JSON.parse(fileContent);
  }

  /**
   * Lists all available test case IDs from a test data file
   * @param fileName - Name of the JSON file in test-data folder
   * @returns Array of test case IDs
   */
  static listTestCaseIds(fileName: string): string[] {
    const testData = this.getAllTestData<any>(fileName);
    return testData.map((item: any) => item.testCaseId);
  }
}
