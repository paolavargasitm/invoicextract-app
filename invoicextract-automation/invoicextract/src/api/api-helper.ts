import { APIRequestContext, APIResponse } from '@playwright/test';

export class ApiHelper {
  private context: APIRequestContext;

  constructor(context: APIRequestContext) {
    this.context = context;
  }

  /**
   * Perform GET request
   */
  async get(endpoint: string, options?: any): Promise<APIResponse> {
    return await this.context.get(endpoint, options);
  }

  /**
   * Perform POST request
   */
  async post(endpoint: string, data?: any, options?: any): Promise<APIResponse> {
    return await this.context.post(endpoint, {
      data,
      ...options
    });
  }

  /**
   * Perform PUT request
   */
  async put(endpoint: string, data?: any, options?: any): Promise<APIResponse> {
    return await this.context.put(endpoint, {
      data,
      ...options
    });
  }

  /**
   * Perform PATCH request
   */
  async patch(endpoint: string, data?: any, options?: any): Promise<APIResponse> {
    return await this.context.patch(endpoint, {
      data,
      ...options
    });
  }

  /**
   * Perform DELETE request
   */
  async delete(endpoint: string, options?: any): Promise<APIResponse> {
    return await this.context.delete(endpoint, options);
  }

  /**
   * Get response body as JSON
   */
  async getJsonResponse(response: APIResponse): Promise<any> {
    return await response.json();
  }

  /**
   * Get response body as text
   */
  async getTextResponse(response: APIResponse): Promise<string> {
    return await response.text();
  }

  /**
   * Check if response is OK (status 200-299)
   */
  isResponseOk(response: APIResponse): boolean {
    return response.ok();
  }

  /**
   * Get response status
   */
  getResponseStatus(response: APIResponse): number {
    return response.status();
  }

  /**
   * Get response headers
   */
  getResponseHeaders(response: APIResponse): { [key: string]: string } {
    return response.headers();
  }
}
