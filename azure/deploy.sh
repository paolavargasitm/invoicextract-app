#!/bin/bash
# Azure Container Apps Deployment Script for InvoiceExtract Stack (Bash version)
# This script deploys the complete docker-compose stack to Azure Container Apps

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if required parameters are provided
if [ $# -lt 2 ]; then
    print_error "Usage: $0 <resource-group-name> <location> [encryption-secret-key] [mysql-root-password] [subscription-id] [app-name]"
    print_info "Example: $0 invoicextract-rg 'East US' 'MyEncryptionKey123456789012345678901234567890' 'root'"
    exit 1
fi

RESOURCE_GROUP_NAME=$1
LOCATION=$2
ENCRYPTION_SECRET_KEY=${3:-"ThisIsASecretKey1234567890123456"}
MYSQL_ROOT_PASSWORD=${4:-"root"}
SUBSCRIPTION_ID=${5:-""}
APP_NAME=${6:-"invoicextract"}

print_info "🚀 Starting Azure deployment for InvoiceExtract application..."

# Login to Azure (if not already logged in)
if ! az account show &> /dev/null; then
    print_info "📝 Please login to Azure..."
    az login
fi

# Set subscription if provided
if [ -n "$SUBSCRIPTION_ID" ]; then
    print_info "🔄 Setting subscription to: $SUBSCRIPTION_ID"
    az account set --subscription "$SUBSCRIPTION_ID"
fi

# Create resource group if it doesn't exist
print_info "📦 Creating resource group: $RESOURCE_GROUP_NAME"
if ! az group show --name "$RESOURCE_GROUP_NAME" &> /dev/null; then
    az group create --name "$RESOURCE_GROUP_NAME" --location "$LOCATION"
    print_status "Resource group created successfully"
else
    print_warning "Resource group already exists"
fi

# Create Azure Container Registry
ACR_NAME="${APP_NAME}acr$(shuf -i 1000-9999 -n 1)"
print_info "📦 Creating Azure Container Registry: $ACR_NAME"

if ! az acr show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP_NAME" &> /dev/null; then
    az acr create --resource-group "$RESOURCE_GROUP_NAME" --name "$ACR_NAME" --sku Basic --location "$LOCATION"
    print_status "ACR created successfully"
else
    print_warning "ACR already exists"
fi

# Get ACR login server
ACR_LOGIN_SERVER=$(az acr show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP_NAME" --query "loginServer" --output tsv)

# Build and push Docker image
print_info "🔨 Building Docker image..."
cd ../
docker build -t "$ACR_LOGIN_SERVER/invoicextract:latest" -f invoicextract-backend/Dockerfile .

print_info "📤 Pushing image to ACR..."
az acr login --name "$ACR_NAME"
docker push "$ACR_LOGIN_SERVER/invoicextract:latest"

# Update parameters file
print_info "📝 Updating deployment parameters..."
cd azure/
cat > parameters.json << EOF
{
  "\$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentParameters.json#",
  "contentVersion": "1.0.0.0",
  "parameters": {
    "appName": {
      "value": "$APP_NAME"
    },
    "location": {
      "value": "$LOCATION"
    },
    "mysqlRootPassword": {
      "value": "$MYSQL_ROOT_PASSWORD"
    },
    "encryptionSecretKey": {
      "value": "$ENCRYPTION_SECRET_KEY"
    }
  }
}
EOF

# Deploy Azure Container Apps using Bicep template
print_info "☁️ Deploying Azure Container Apps..."
DEPLOYMENT_OUTPUT=$(az deployment group create \
    --resource-group "$RESOURCE_GROUP_NAME" \
    --template-file container-apps.bicep \
    --parameters @parameters.json \
    --output json)

if [ $? -eq 0 ]; then
    print_status "Azure resources deployed successfully!"
    
    # Extract outputs
    WEB_APP_URL=$(echo "$DEPLOYMENT_OUTPUT" | jq -r '.properties.outputs.webAppUrl.value')
    MYSQL_SERVER_FQDN=$(echo "$DEPLOYMENT_OUTPUT" | jq -r '.properties.outputs.mysqlServerFqdn.value')
    KEY_VAULT_NAME=$(echo "$DEPLOYMENT_OUTPUT" | jq -r '.properties.outputs.keyVaultName.value')
    
    # Display deployment information
    echo -e "\n${CYAN}📋 Deployment Information:${NC}"
    print_status "🌐 Web App URL: $WEB_APP_URL"
    print_status "🗄️ MySQL Server: $MYSQL_SERVER_FQDN"
    print_status "🔐 Key Vault: $KEY_VAULT_NAME"
    print_status "📦 Resource Group: $RESOURCE_GROUP_NAME"
    
    # Update Web App with ACR image
    print_info "🔄 Updating Web App with container image..."
    WEB_APP_NAME="${APP_NAME}-app"
    az webapp config container set \
        --name "$WEB_APP_NAME" \
        --resource-group "$RESOURCE_GROUP_NAME" \
        --docker-custom-image-name "$ACR_LOGIN_SERVER/invoicextract:latest" \
        --docker-registry-server-url "https://$ACR_LOGIN_SERVER"
    
    echo -e "\n${GREEN}🎉 Deployment completed successfully!${NC}"
    echo -e "${CYAN}🌐 Your application will be available at: $WEB_APP_URL/invoicextract${NC}"
    echo -e "${CYAN}📚 Swagger UI: $WEB_APP_URL/invoicextract/swagger-ui/index.html${NC}"
    
else
    print_error "Deployment failed!"
    exit 1
fi

print_status "✅ Azure deployment completed successfully! 🎉"
