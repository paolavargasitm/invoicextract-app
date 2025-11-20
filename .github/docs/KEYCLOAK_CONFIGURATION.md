# Keycloak Configuration for InvoiceExtract

## Overview
The InvoiceExtract application uses Keycloak for authentication and authorization. This document explains how Keycloak is configured in CI/CD and local environments.

## Client Credentials

The `invoices-backend` client is pre-configured in the `invoicextract-realm.json` file with the following credentials:

```
CLIENT_ID: invoices-backend
CLIENT_SECRET: TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA
```

## CI/CD Configuration (GitHub Actions)

### Automatic Realm Import

The E2E testing workflow automatically imports the realm configuration from `invoicextract-realm.json` to ensure consistent client credentials across all test runs.

**Workflow Steps:**
1. Docker Compose mounts `invoicextract-realm.json` at `/tmp/invoicextract-realm.json` in the Keycloak container
2. After Keycloak starts, the import script (`.github/scripts/import-keycloak-realm.sh`) runs
3. The script uses Keycloak Admin CLI (`kcadm.sh`) to import the realm
4. The realm includes all pre-configured clients with their secrets

**Benefits:**
- ✅ CLIENT_SECRET remains consistent across all workflow runs
- ✅ No need to manually configure Keycloak in CI/CD
- ✅ Tests can use hardcoded credentials from .env files
- ✅ Idempotent - safe to run multiple times

## Local Development Configuration

### Option 1: Use Realm Import (Recommended)

Mount the realm file in your local `docker-compose.yml`:

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:26.0
  volumes:
    - ./invoicextract-realm.json:/tmp/invoicextract-realm.json:ro
```

Then run the import script after starting containers:

```bash
docker-compose up -d
chmod +x .github/scripts/import-keycloak-realm.sh
.github/scripts/import-keycloak-realm.sh
```

### Option 2: Manual Configuration

If you need to configure Keycloak manually:

1. Access Keycloak admin console: http://localhost:8085
2. Login with admin/admin
3. Create realm `invoices`
4. Create client `invoices-backend`:
   - Client ID: `invoices-backend`
   - Client authentication: ON (confidential)
   - Service accounts roles: ON
   - Valid redirect URIs: `/*`
   - Web origins: `/*`
5. Go to Credentials tab and set:
   - Client Secret: `TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA`

## Environment Variables

Update your `.env` file with the following Keycloak configuration:

```bash
# Keycloak Configuration
KEYCLOAK_URL=http://localhost:8085
KEYCLOAK_REALM=invoices
CLIENT_ID=invoices-backend
CLIENT_SECRET=TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA

# Admin credentials for test automation
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin
```

## Test Automation Configuration

The E2E tests in `invoicextract-automation/invoicextract/` use the following environment variables:

```bash
# From .env file
KEYCLOAK_URL=http://localhost:8085
CLIENT_ID=invoices-backend
CLIENT_SECRET=TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin
```

These values are automatically loaded by the test framework from the `.env` file.

## Troubleshooting

### CLIENT_SECRET Mismatch

**Symptom:** Authentication fails with "invalid_client" or "unauthorized" errors

**Solution:**
1. Check that the Keycloak realm was imported correctly:
   ```bash
   docker exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
     --server http://localhost:8080 --realm master --user admin --password admin
   docker exec keycloak /opt/keycloak/bin/kcadm.sh get realms/invoices
   ```

2. Verify the client secret:
   ```bash
   docker exec keycloak /opt/keycloak/bin/kcadm.sh get clients \
     -r invoices --fields 'clientId,secret'
   ```

3. If the secret doesn't match, re-import the realm:
   ```bash
   .github/scripts/import-keycloak-realm.sh
   ```

### Realm Not Found

**Symptom:** Tests fail with "realm not found" errors

**Solution:**
1. Verify Keycloak is running:
   ```bash
   curl http://localhost:8085/realms/master/.well-known/openid-configuration
   ```

2. Check if realm exists:
   ```bash
   docker exec keycloak /opt/keycloak/bin/kcadm.sh get realms
   ```

3. Import the realm if missing:
   ```bash
   .github/scripts/import-keycloak-realm.sh
   ```

### Container Health Issues

**Symptom:** Keycloak container is unhealthy or not starting

**Solution:**
1. Check container logs:
   ```bash
   docker logs keycloak
   ```

2. Verify database connectivity:
   ```bash
   docker logs keycloak-db
   docker exec keycloak-db psql -U keycloak -d keycloak -c "SELECT 1"
   ```

3. Restart with fresh volumes:
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

## Security Considerations

### Production Environments

**⚠️ WARNING:** The CLIENT_SECRET in this documentation is for **development and testing only**.

For production deployments:

1. **Generate new secrets:**
   ```bash
   openssl rand -base64 32
   ```

2. **Use environment variables** instead of hardcoded secrets

3. **Store secrets securely** using:
   - GitHub Secrets for CI/CD
   - AWS Secrets Manager
   - HashiCorp Vault
   - Kubernetes Secrets

4. **Rotate secrets regularly** (every 90 days recommended)

5. **Use different secrets** for each environment (dev, staging, prod)

### GitHub Secrets Configuration

For production workflows, configure GitHub Secrets:

```yaml
env:
  CLIENT_SECRET: ${{ secrets.KEYCLOAK_CLIENT_SECRET }}
```

Then add the secret in your repository:
Settings → Secrets and variables → Actions → New repository secret

## Realm Configuration Details

The `invoicextract-realm.json` file includes:

### Clients
- `invoices-backend` - Backend API client (confidential)
- `invoices-frontend` - Frontend SPA client (public)
- `invoices-mappings` - Mapping service client (confidential)

### Users
Pre-configured test users with roles and permissions

### Roles
Application-specific roles for authorization

### Identity Providers
Optional external authentication providers

## References

- [Keycloak Admin CLI Documentation](https://www.keycloak.org/docs/latest/server_admin/#the-admin-cli)
- [Keycloak Docker Documentation](https://www.keycloak.org/server/containers)
- [Keycloak Import/Export](https://www.keycloak.org/server/importExport)
