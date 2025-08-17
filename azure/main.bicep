// Azure Bicep template for InvoiceExtract multi-container deployment
// This template deploys the complete docker-compose stack to Azure Container Apps

@description('The name of the application')
param appName string = 'invoicextract'

@description('The location for all resources')
param location string = resourceGroup().location

@description('MySQL root password')
@secure()
param mysqlRootPassword string = 'root'

@description('Encryption secret key for the application')
@secure()
param encryptionSecretKey string

// Variables
var appServicePlanName = '${appName}-plan'
var webAppName = '${appName}-app'
var mysqlServerName = '${appName}-mysql-${uniqueString(resourceGroup().id)}'
var keyVaultName = '${appName}-kv-${uniqueString(resourceGroup().id)}'

// App Service Plan
resource appServicePlan 'Microsoft.Web/serverfarms@2022-03-01' = {
  name: appServicePlanName
  location: location
  sku: {
    name: appServicePlanSku
  }
  kind: 'linux'
  properties: {
    reserved: true
  }
}

// Azure Database for MySQL
resource mysqlServer 'Microsoft.DBforMySQL/servers@2017-12-01' = {
  name: mysqlServerName
  location: location
  sku: {
    name: mysqlSku
    tier: 'Basic'
    capacity: 1
  }
  properties: {
    administratorLogin: mysqlAdminLogin
    administratorLoginPassword: mysqlAdminPassword
    version: '8.0'
    sslEnforcement: 'Enabled'
    minimalTlsVersion: 'TLS1_2'
    infrastructureEncryption: 'Disabled'
    publicNetworkAccess: 'Enabled'
  }
}

// MySQL Database
resource mysqlDatabase 'Microsoft.DBforMySQL/servers/databases@2017-12-01' = {
  parent: mysqlServer
  name: databaseName
  properties: {
    charset: 'utf8mb4'
    collation: 'utf8mb4_unicode_ci'
  }
}

// MySQL Firewall Rule for Azure Services
resource mysqlFirewallRule 'Microsoft.DBforMySQL/servers/firewallRules@2017-12-01' = {
  parent: mysqlServer
  name: 'AllowAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

// Key Vault for storing secrets
resource keyVault 'Microsoft.KeyVault/vaults@2022-07-01' = {
  name: keyVaultName
  location: location
  properties: {
    sku: {
      family: 'A'
      name: 'standard'
    }
    tenantId: subscription().tenantId
    accessPolicies: [
      {
        tenantId: subscription().tenantId
        objectId: webApp.identity.principalId
        permissions: {
          secrets: [
            'get'
            'list'
          ]
        }
      }
    ]
    enabledForTemplateDeployment: true
    enableSoftDelete: true
    softDeleteRetentionInDays: 7
  }
}

// Store database connection string in Key Vault
resource dbConnectionStringSecret 'Microsoft.KeyVault/vaults/secrets@2022-07-01' = {
  parent: keyVault
  name: 'database-connection-string'
  properties: {
    value: 'jdbc:mysql://${mysqlServer.properties.fullyQualifiedDomainName}:3306/${databaseName}?useSSL=true&requireSSL=true&serverTimezone=UTC'
  }
}

// Store database password in Key Vault
resource dbPasswordSecret 'Microsoft.KeyVault/vaults/secrets@2022-07-01' = {
  parent: keyVault
  name: 'database-password'
  properties: {
    value: mysqlAdminPassword
  }
}

// Store encryption key in Key Vault
resource encryptionKeySecret 'Microsoft.KeyVault/vaults/secrets@2022-07-01' = {
  parent: keyVault
  name: 'encryption-secret-key'
  properties: {
    value: encryptionSecretKey
  }
}

// Web App
resource webApp 'Microsoft.Web/sites@2022-03-01' = {
  name: webAppName
  location: location
  kind: 'app,linux,container'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: appServicePlan.id
    siteConfig: {
      linuxFxVersion: 'DOCKER|invoicextract:latest'
      alwaysOn: true
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
      appSettings: [
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'azure'
        }
        {
          name: 'SPRING_DATASOURCE_URL'
          value: '@Microsoft.KeyVault(SecretUri=${dbConnectionStringSecret.properties.secretUri})'
        }
        {
          name: 'SPRING_DATASOURCE_USERNAME'
          value: mysqlAdminLogin
        }
        {
          name: 'SPRING_DATASOURCE_PASSWORD'
          value: '@Microsoft.KeyVault(SecretUri=${dbPasswordSecret.properties.secretUri})'
        }
        {
          name: 'ENCRYPTION_SECRET_KEY'
          value: '@Microsoft.KeyVault(SecretUri=${encryptionKeySecret.properties.secretUri})'
        }
        {
          name: 'SPRING_JPA_HIBERNATE_DDL_AUTO'
          value: 'validate'
        }
        {
          name: 'SPRING_LIQUIBASE_ENABLED'
          value: 'true'
        }
        {
          name: 'WEBSITES_PORT'
          value: '8080'
        }
        {
          name: 'WEBSITES_ENABLE_APP_SERVICE_STORAGE'
          value: 'false'
        }
      ]
    }
    httpsOnly: true
  }
}

// Outputs
output webAppUrl string = 'https://${webApp.properties.defaultHostName}'
output mysqlServerFqdn string = mysqlServer.properties.fullyQualifiedDomainName
output keyVaultName string = keyVault.name
output resourceGroupName string = resourceGroup().name
