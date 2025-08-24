# ==========================
# Script: deploy-mysql-adminer.ps1
# Propósito: Desplegar MySQL, Adminer y Liquibase en Azure Container Apps
# ==========================

$ErrorActionPreference = "Stop"

# -----------------------------
# Importar variables del init
# -----------------------------
. .\init-env.ps1

# Variables adicionales
$EnvName = "invoicextract-ca-env"
$WorkspaceName = "invoicextract-law"
$MysqlAppName = "invoicextract-mysql"
$MysqlRootPassword = "root"
$MysqlDbName = "invoices"
$AdminerName = "invoicextract-adminer"
$LiquibaseName = "invoicextract-liquibase"
$LiquibaseImage = "$ACR_NAME.azurecr.io/invoiceextract-liquibase:latest"

# -----------------------------
# Desplegar MySQL
# -----------------------------
Write-Host "=== Deploy MySQL Container App ===" -ForegroundColor Cyan

$mysqlExists = $false
try {
    az containerapp show -g "$RESOURCE_GROUP" -n "$MysqlAppName" --only-show-errors | Out-Null
    $mysqlExists = $true
} catch { }

if ($mysqlExists) {
    Write-Host "Deleting existing MySQL container app..."
    az containerapp delete -g "$RESOURCE_GROUP" -n "$MysqlAppName" --yes
    Start-Sleep -Seconds 10
}

az containerapp create `
  -g "$RESOURCE_GROUP" `
  -n "$MysqlAppName" `
  --environment "$EnvName" `
  --image "mysql:8.1" `
  --cpu "1" `
  --memory "2Gi" `
  --min-replicas 1 `
  --max-replicas 1 `
  --env-vars `
    MYSQL_ROOT_PASSWORD="$MysqlRootPassword" `
    MYSQL_DATABASE="$MysqlDbName" `
  --ingress 'internal' | Out-Null

Write-Host "MySQL deployed internally: $MysqlAppName" -ForegroundColor Green

# -----------------------------
# Desplegar Adminer
# -----------------------------
Write-Host "=== Deploy Adminer Container App ===" -ForegroundColor Cyan

$adminerExists = $false
try {
    az containerapp show -g "$RESOURCE_GROUP" -n "$AdminerName" --only-show-errors | Out-Null
    $adminerExists = $true
} catch { }

if ($adminerExists) {
    Write-Host "Deleting existing Adminer container app..."
    az containerapp delete -g "$RESOURCE_GROUP" -n "$AdminerName" --yes
    Start-Sleep -Seconds 10
}

az containerapp create `
  -g "$RESOURCE_GROUP" `
  -n "$AdminerName" `
  --environment "$EnvName" `
  --image "adminer:latest" `
  --cpu "0.5" `
  --memory "1Gi" `
  --min-replicas 1 `
  --max-replicas 1 `
  --ingress 'external' `
  --target-port 8080 | Out-Null

$AdminerFqdn = az containerapp show -g "$RESOURCE_GROUP" -n "$AdminerName" --query properties.configuration.ingress.fqdn -o tsv
Write-Host "Adminer deployed and accessible at: https://$AdminerFqdn" -ForegroundColor Green

# -----------------------------
# Esperar a que MySQL inicialice
# -----------------------------
Write-Host "Waiting 45 seconds for MySQL to initialize..." -ForegroundColor Cyan
Start-Sleep -Seconds 45

# -----------------------------
# Desplegar Liquibase
# -----------------------------
Write-Host "=== Deploy Liquibase Container App ===" -ForegroundColor Cyan

# Obtener credenciales del ACR
$AcrLoginServer = az acr show -n "$ACR_NAME" --query loginServer -o tsv
$acrUser = az acr credential show -n "$ACR_NAME" --query username -o tsv
$acrPass = az acr credential show -n "$ACR_NAME" --query passwords[0].value -o tsv

$liquibaseExists = $false
try {
    az containerapp show -g "$RESOURCE_GROUP" -n "$LiquibaseName" --only-show-errors | Out-Null
    $liquibaseExists = $true
} catch { }

if ($liquibaseExists) {
    Write-Host "Deleting existing Liquibase container app..."
    az containerapp delete -g "$RESOURCE_GROUP" -n "$LiquibaseName" --yes
    Start-Sleep -Seconds 10
}

$LiquibaseUrl = "jdbc:mysql://${MysqlAppName}:3306/$MysqlDbName"

Write-Host "Liquibase URL (internal MySQL): $LiquibaseUrl" -ForegroundColor Cyan

az containerapp create `
  -g "$RESOURCE_GROUP" `
  -n "$LiquibaseName" `
  --environment "$EnvName" `
  --image "$LiquibaseImage" `
  --cpu "0.5" `
  --memory "1Gi" `
  --registry-server "$AcrLoginServer" `
  --registry-username "$acrUser" `
  --registry-password "$acrPass" `
  --min-replicas 1 `
  --max-replicas 1 `
  --env-vars `
    LIQUIBASE_COMMAND_URL="$LiquibaseUrl" `
    LIQUIBASE_COMMAND_USERNAME="root" `
    LIQUIBASE_COMMAND_PASSWORD="$MysqlRootPassword" | Out-Null

Write-Host "Liquibase container app deployed: $LiquibaseName" -ForegroundColor Green
Write-Host "Use this container to run migrations pointing to MySQL: $LiquibaseUrl"
Write-Host "=== Deployment Completed ===" -ForegroundColor Green
