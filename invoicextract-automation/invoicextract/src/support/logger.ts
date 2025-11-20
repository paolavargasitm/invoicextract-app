import { World } from '@cucumber/cucumber';

/**
 * Logger utility for Cucumber tests
 * Logs comparison results between expected and actual values to the Cucumber report
 */
export class TestLogger {
  private world: World;

  constructor(world: World) {
    this.world = world;
  }

  /**
   * Logs a comparison between expected and actual values
   * @param field - The field name being compared
   * @param expected - The expected value
   * @param actual - The actual value
   * @param passed - Whether the comparison passed
   */
  logComparison(field: string, expected: any, actual: any, passed: boolean): void {
    const status = passed ? '✅ PASS' : '❌ FAIL';
    const message = `
╔═══════════════════════════════════════════════════════════════
║ ${status} - Field: ${field}
╠═══════════════════════════════════════════════════════════════
║ Expected: ${this.formatValue(expected)}
║ Actual:   ${this.formatValue(actual)}
╚═══════════════════════════════════════════════════════════════
`;
    this.world.attach(message, 'text/plain');
  }

  /**
   * Logs a response validation summary
   * @param totalChecks - Total number of checks performed
   * @param passedChecks - Number of checks that passed
   * @param failedChecks - Number of checks that failed
   */
  logValidationSummary(totalChecks: number, passedChecks: number, failedChecks: number): void {
    const message = `
╔═══════════════════════════════════════════════════════════════
║ VALIDATION SUMMARY
╠═══════════════════════════════════════════════════════════════
║ Total Checks:  ${totalChecks}
║ Passed:        ${passedChecks} ✅
║ Failed:        ${failedChecks} ❌
║ Success Rate:  ${totalChecks > 0 ? ((passedChecks / totalChecks) * 100).toFixed(2) : 0}%
╚═══════════════════════════════════════════════════════════════
`;
    this.world.attach(message, 'text/plain');
  }

  /**
   * Logs the full response body for debugging
   * @param response - The response object to log
   */
  logResponse(response: any): void {
    const message = `
╔═══════════════════════════════════════════════════════════════
║ RESPONSE BODY
╠═══════════════════════════════════════════════════════════════
${JSON.stringify(response, null, 2)}
╚═══════════════════════════════════════════════════════════════
`;
    this.world.attach(message, 'text/plain');
  }

  /**
   * Logs the expected response structure
   * @param expected - The expected response structure
   */
  logExpected(expected: any): void {
    const message = `
╔═══════════════════════════════════════════════════════════════
║ EXPECTED RESPONSE STRUCTURE
╠═══════════════════════════════════════════════════════════════
${JSON.stringify(expected, null, 2)}
╚═══════════════════════════════════════════════════════════════
`;
    this.world.attach(message, 'text/plain');
  }

  /**
   * Logs general information
   * @param title - Title of the log
   * @param message - Message to log
   */
  logInfo(title: string, message: string): void {
    const logMessage = `
╔═══════════════════════════════════════════════════════════════
║ ${title}
╠═══════════════════════════════════════════════════════════════
${message}
╚═══════════════════════════════════════════════════════════════
`;
    this.world.attach(logMessage, 'text/plain');
  }

  /**
   * Formats a value for display in logs
   * @param value - Value to format
   * @returns Formatted string representation
   */
  private formatValue(value: any): string {
    if (value === null) return 'null';
    if (value === undefined) return 'undefined';
    if (typeof value === 'object') {
      return JSON.stringify(value, null, 2);
    }
    return String(value);
  }

  /**
   * Logs an error with details
   * @param error - Error message or object
   */
  logError(error: string | Error): void {
    const errorMessage = error instanceof Error ? error.message : error;
    const message = `
╔═══════════════════════════════════════════════════════════════
║ ❌ ERROR
╠═══════════════════════════════════════════════════════════════
${errorMessage}
╚═══════════════════════════════════════════════════════════════
`;
    this.world.attach(message, 'text/plain');
  }
}
