# GitHub Secrets Configuration

This document describes the secrets required for the GitHub Actions workflows.

## Required Secrets

### `KEYCLOAK_CLIENT_SECRET`

**Description:** The client secret for the `invoices-backend` client in Keycloak, used for API authentication during E2E tests.

**Value:** `TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA`

**How to set it up:**

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `KEYCLOAK_CLIENT_SECRET`
5. Secret: `TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA`
6. Click **Add secret**

## Fallback Behavior

The workflow is configured with a fallback value, so if the secret is not set, it will use the default value from the realm configuration. However, for security best practices, **you should set the secret in GitHub**.

## Security Notes

- Never commit secrets directly in code or configuration files
- The `.env` files in the repository should not contain real secrets
- GitHub Actions masks secrets automatically in logs
- Secrets are only available to workflows running in your repository

## Local Development

For local development, create a `.env` file in `invoicextract-automation/invoicextract/` with the following content:

```env
# Keycloak Client Credentials for API Authentication
CLIENT_ID=invoices-backend
CLIENT_SECRET=TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA
```

**Important:** Make sure `.env` is in your `.gitignore` to prevent accidentally committing it.
