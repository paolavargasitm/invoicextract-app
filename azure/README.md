# Azure Deployment Guide for InvoiceExtract

This directory contains all the necessary files and configurations to deploy the InvoiceExtract application to Microsoft Azure using Azure App Service and Azure Database for MySQL.

## 🏗️ Architecture Overview

The Azure deployment includes:

- **Azure App Service**: Hosts the Spring Boot application
- **Azure Database for MySQL**: Managed MySQL database service
- **Azure Container Registry (ACR)**: Stores the Docker container image
- **Azure Key Vault**: Securely stores secrets and connection strings
- **Azure Resource Manager**: Infrastructure as Code using Bicep templates

## 📁 Files Structure

```
azure/
├── main.bicep              # Main Bicep template for Azure resources
├── parameters.json         # Deployment parameters
├── app-service.yaml        # Kubernetes-style configuration (optional)
├── deploy.ps1             # PowerShell deployment script
├── deploy.sh              # Bash deployment script
└── README.md              # This file
```

## 🚀 Quick Deployment

### Prerequisites

1. **Azure CLI** installed and configured
2. **Docker** installed and running
3. **Azure subscription** with appropriate permissions
4. **PowerShell** (for Windows) or **Bash** (for Linux/macOS)

### Option 1: PowerShell Deployment (Windows)

```powershell
# Navigate to the azure directory
cd azure

# Run the deployment script
.\deploy.ps1 -ResourceGroupName "invoicextract-rg" `
             -Location "East US" `
             -MySqlAdminPassword "YourSecurePassword123!" `
             -EncryptionSecretKey "YourEncryptionKey123456789012345678901234567890"
```

### Option 2: Bash Deployment (Linux/macOS)

```bash
# Navigate to the azure directory
cd azure

# Make the script executable
chmod +x deploy.sh

# Run the deployment script
./deploy.sh "invoicextract-rg" "East US" "YourSecurePassword123!" "YourEncryptionKey123456789012345678901234567890"
```

### Option 3: Manual Azure CLI Deployment

```bash
# 1. Create resource group
az group create --name invoicextract-rg --location "East US"

# 2. Deploy resources using Bicep template
az deployment group create \
    --resource-group invoicextract-rg \
    --template-file main.bicep \
    --parameters @parameters.json

# 3. Build and push Docker image (after creating ACR)
docker build -t invoicextractacr.azurecr.io/invoicextract:latest -f ../invoicextract-backend/Dockerfile ../
az acr login --name invoicextractacr
docker push invoicextractacr.azurecr.io/invoicextract:latest
```

## ⚙️ Configuration Parameters

### Required Parameters

| Parameter | Description | Example |
|-----------|-------------|---------|
| `ResourceGroupName` | Azure resource group name | `invoicextract-rg` |
| `Location` | Azure region | `East US` |
| `MySqlAdminPassword` | MySQL administrator password | `SecurePassword123!` |
| `EncryptionSecretKey` | Application encryption key | `32-character-key` |

### Optional Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `AppName` | Application name prefix | `invoicextract` |
| `SubscriptionId` | Azure subscription ID | Current subscription |
| `AppServicePlanSku` | App Service plan SKU | `B1` |
| `MySqlSku` | MySQL server SKU | `B_Gen5_1` |

## 🔐 Security Configuration

### Key Vault Integration

The deployment automatically creates an Azure Key Vault to store:

- Database connection strings
- Database passwords
- Application encryption keys
- Other sensitive configuration

### SSL/TLS Configuration

- **HTTPS Only**: App Service is configured to redirect HTTP to HTTPS
- **MySQL SSL**: Database connections use SSL encryption
- **TLS 1.2**: Minimum TLS version enforced

## 🗄️ Database Configuration

### Azure Database for MySQL

- **Version**: MySQL 8.0
- **SSL Enforcement**: Enabled
- **Backup Retention**: 7 days (configurable)
- **Geo-Redundant Backup**: Available for higher tiers

### Database Schema

The application uses Liquibase for database schema management:

- **Initial Schema**: Created automatically on first deployment
- **Migrations**: Handled by Liquibase changesets
- **Validation**: Schema validation enabled in production

## 📊 Monitoring and Logging

### Application Insights (Optional)

Enable Application Insights for:

- Performance monitoring
- Error tracking
- Custom metrics
- Distributed tracing

### Health Checks

The application exposes health check endpoints:

- **Liveness**: `/invoicextract/actuator/health/liveness`
- **Readiness**: `/invoicextract/actuator/health/readiness`
- **General Health**: `/invoicextract/actuator/health`

## 🌐 Access URLs

After successful deployment, your application will be available at:

- **Main Application**: `https://{app-name}-app.azurewebsites.net/invoicextract`
- **Swagger UI**: `https://{app-name}-app.azurewebsites.net/invoicextract/swagger-ui/index.html`
- **Health Check**: `https://{app-name}-app.azurewebsites.net/invoicextract/actuator/health`

## 🔧 Troubleshooting

### Common Issues

1. **Container Registry Access**
   ```bash
   # Ensure ACR admin user is enabled
   az acr update --name {acr-name} --admin-enabled true
   ```

2. **Database Connection Issues**
   ```bash
   # Check firewall rules
   az mysql server firewall-rule list --resource-group {rg} --server-name {server}
   ```

3. **Key Vault Access**
   ```bash
   # Verify App Service managed identity has access
   az keyvault set-policy --name {kv-name} --object-id {app-identity} --secret-permissions get list
   ```

### Logs and Diagnostics

```bash
# View App Service logs
az webapp log tail --name {app-name} --resource-group {rg}

# Download log files
az webapp log download --name {app-name} --resource-group {rg}
```

## 💰 Cost Optimization

### Recommended SKUs by Environment

| Environment | App Service Plan | MySQL Server | Estimated Monthly Cost |
|-------------|------------------|--------------|----------------------|
| Development | B1 | B_Gen5_1 | ~$50-70 USD |
| Staging | S1 | GP_Gen5_2 | ~$150-200 USD |
| Production | P1V2 | GP_Gen5_4 | ~$300-400 USD |

### Cost-Saving Tips

1. **Auto-scaling**: Configure based on actual usage
2. **Reserved Instances**: For production workloads
3. **Dev/Test Pricing**: Use Azure Dev/Test subscriptions
4. **Resource Cleanup**: Delete unused resources

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy to Azure
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to Azure
        run: |
          cd azure
          ./deploy.sh "${{ secrets.RESOURCE_GROUP }}" \
                     "East US" \
                     "${{ secrets.MYSQL_PASSWORD }}" \
                     "${{ secrets.ENCRYPTION_KEY }}"
```

## 📞 Support

For issues related to:

- **Azure Resources**: Check Azure Portal diagnostics
- **Application Issues**: Review application logs
- **Database Issues**: Check MySQL server logs
- **Deployment Issues**: Verify Bicep template parameters

## 🔗 Useful Links

- [Azure App Service Documentation](https://docs.microsoft.com/en-us/azure/app-service/)
- [Azure Database for MySQL Documentation](https://docs.microsoft.com/en-us/azure/mysql/)
- [Azure Bicep Documentation](https://docs.microsoft.com/en-us/azure/azure-resource-manager/bicep/)
- [Azure Key Vault Documentation](https://docs.microsoft.com/en-us/azure/key-vault/)
