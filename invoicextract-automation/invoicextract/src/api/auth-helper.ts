import { request, APIRequestContext } from '@playwright/test';
import * as dotenv from 'dotenv';

dotenv.config();

export interface TokenResponse {
  access_token: string;
  expires_in: number;
  refresh_expires_in: number;
  token_type: string;
  scope?: string;
}

/**
 * Helper class for Keycloak authentication
 */
export class AuthHelper {
  private static tokenCache: { token: string; expiresAt: number } | null = null;

  /**
   * Gets a valid access token, either from cache or by requesting a new one
   * @returns Access token string
   */
  static async getAccessToken(): Promise<string> {
    // Check if we have a valid cached token
    if (this.tokenCache && Date.now() < this.tokenCache.expiresAt) {
      return this.tokenCache.token;
    }

    // Request a new token
    const token = await this.requestNewToken();
    return token;
  }

  /**
   * Requests a new access token from Keycloak using client credentials
   * @returns Access token string
   */
  private static async requestNewToken(): Promise<string> {
    // Use KEYCLOAK_TOKEN_URL if available (for matching backend's issuer in CI/CD)
    // Otherwise fall back to KEYCLOAK_BASE_URL for local development
    const keycloakUrl = process.env.KEYCLOAK_TOKEN_URL || process.env.KEYCLOAK_BASE_URL || 'http://localhost:8085';
    const clientId = process.env.CLIENT_ID || 'invoices-backend';
    const clientSecret = process.env.CLIENT_SECRET;

    if (!clientSecret) {
      throw new Error('CLIENT_SECRET not configured in .env file');
    }

    const tokenEndpoint = `${keycloakUrl}/realms/invoicextract/protocol/openid-connect/token`;

    // Create a temporary API context for the token request
    const apiContext = await request.newContext();

    try {
      const response = await apiContext.post(tokenEndpoint, {
        form: {
          grant_type: 'client_credentials',
          client_id: clientId,
          client_secret: clientSecret
        }
      });

      if (!response.ok()) {
        const errorBody = await response.text();
        throw new Error(
          `Failed to get access token. Status: ${response.status()}, Body: ${errorBody}`
        );
      }

      const tokenResponse: TokenResponse = await response.json();

      // Cache the token (subtract 60 seconds for safety margin)
      const expiresIn = (tokenResponse.expires_in - 60) * 1000;
      this.tokenCache = {
        token: tokenResponse.access_token,
        expiresAt: Date.now() + expiresIn
      };

      return tokenResponse.access_token;
    } finally {
      await apiContext.dispose();
    }
  }

  /**
   * Clears the token cache, forcing a new token on next request
   */
  static clearTokenCache(): void {
    this.tokenCache = null;
  }

  /**
   * Creates an authenticated API request context
   * @returns Authenticated APIRequestContext
   */
  static async createAuthenticatedContext(): Promise<APIRequestContext> {
    const token = await this.getAccessToken();
    const baseURL = process.env.API_BASE_URL || 'http://localhost:8080/invoicextract';
    
    // Do NOT add trailing slash - Playwright's APIRequestContext handles URL joining correctly
    // when endpoints start with '/' (which they should)
    const normalizedBaseURL = baseURL.endsWith('/') ? baseURL.slice(0, -1) : baseURL;

    return await request.newContext({
      baseURL: normalizedBaseURL,
      extraHTTPHeaders: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }
}
