# ==========================
# Script: deploy-mysql-adminer.ps1
# Propósito: Desplegar MySQL y Adminer en Azure Container Apps (MySQL expuesto para pruebas)
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

# -----------------------------
# Login Azure
# -----------------------------
Write-Host "=== Azure Login Check ===" -ForegroundColor Cyan
az account show > $null 2>&1
if ($LASTEXITCODE -ne 0) { az login | Out-Null }

# -----------------------------
# Crear Log Analytics Workspace
# -----------------------------
Write-Host "=== Create Log Analytics Workspace ===" -ForegroundColor Cyan
az monitor log-analytics workspace create -g "$RESOURCE_GROUP" -n "$WorkspaceName" -l "$LOCATION" --retention-time 30 | Out-Null
$WorkspaceId = az monitor log-analytics workspace show -g "$RESOURCE_GROUP" -n "$WorkspaceName" --query customerId -o tsv
$WorkspaceKey = az monitor log-analytics.workspace.get-shared-keys -g "$RESOURCE_GROUP" -n "$WorkspaceName" --query primarySharedKey -o tsv

# -----------------------------
# Crear Container Apps Environment
# -----------------------------
Write-Host "=== Create Container Apps Environment ===" -ForegroundColor Cyan
az containerapp env create `
  -g "$RESOURCE_GROUP" `
  -n "$EnvName" `
  -l "$LOCATION" `
  --logs-workspace-id "$WorkspaceId" `
  --logs-workspace-key "$WorkspaceKey" | Out-Null

# -----------------------------
# Desplegar MySQL como Container App externo
# -----------------------------
Write-Host "=== Deploy MySQL Container App (external) ===" -ForegroundColor Cyan

$mysqlExists = $false
try {
    az containerapp show -g "$RESOURCE_GROUP" -n "$MysqlAppName" --only-show-errors | Out-Null
    $mysqlExists = $true
} catch {
    Write-Host "MySQL container app does not exist. Will create a new one..."
}

if ($mysqlExists) {
    Write-Host "Deleting existing MySQL container app..."
    az containerapp delete -g "$RESOURCE_GROUP" -n "$MysqlAppName" --yes
    Start-Sleep -Seconds 10
}

az containerapp create `
  -g "$RESOURCE_GROUP" `
  -n "$MysqlAppName" `
  --environment "$EnvName" `
  --image "mysql:8" `
  --ingress "external" `
  --target-port "3306" `
  --min-replicas "1" `
  --max-replicas "1" `
  --cpu "1" `
  --memory "2Gi" `
  --env-vars `
    MYSQL_ROOT_PASSWORD="$MysqlRootPassword" `
    MYSQL_DATABASE="$MysqlDbName" | Out-Null

$MysqlFqdn = az containerapp show -g "$RESOURCE_GROUP" -n "$MysqlAppName" --query properties.configuration.ingress.fqdn -o tsv
Write-Host "MySQL external FQDN: $MysqlFqdn" -ForegroundColor Yellow

# -----------------------------
# Esperar a que MySQL esté listo
# -----------------------------
Write-Host "Waiting for MySQL to be ready..."
Start-Sleep -Seconds 30  # espera inicial
do {
    try {
        docker run --rm mysql:8 mysqladmin ping -h "$MysqlFqdn" -u root -p"$MysqlRootPassword" | Out-Null
        $ready = $true
    } catch {
        Write-Host "MySQL not ready yet, waiting 10s..."
        Start-Sleep -Seconds 10
        $ready = $false
    }
} until ($ready)

# -----------------------------
# Desplegar Adminer como Container App externo (imagen oficial)
# -----------------------------
Write-Host "=== Deploy Adminer Container App (external, official image) ===" -ForegroundColor Cyan

$adminerExists = $false
try {
    az containerapp show -g "$RESOURCE_GROUP" -n "$AdminerName" --only-show-errors | Out-Null
    $adminerExists = $true
} catch {
    Write-Host "Adminer container app does not exist. Will create a new one..."
}

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
  --ingress "external" `
  --target-port "8080" `
  --min-replicas "1" `
  --max-replicas "1" `
  --env-vars `
    ADMINER_DEFAULT_SERVER="$MysqlFqdn" `
    ADMINER_DEFAULT_USER="root" `
    ADMINER_DEFAULT_PASSWORD="$MysqlRootPassword" | Out-Null

$AdminerUrl = az containerapp show -g "$RESOURCE_GROUP" -n "$AdminerName" --query properties.configuration.ingress.fqdn -o tsv
Write-Host "Adminer URL: https://$AdminerUrl" -ForegroundColor Yellow
Write-Host "Use host: $MysqlFqdn, user: root, pass: $MysqlRootPassword, db: $MysqlDbName" -ForegroundColor DarkGray

# -----------------------------
# Desplegar Liquibase como Container App (imagen personalizada en ACR)
# -----------------------------
$REGISTRY="$ACR_NAME.azurecr.io"

$MysqlFqdn = az containerapp show -g "$RESOURCE_GROUP" -n "$MysqlAppName" --query properties.configuration.ingress.fqdn -o tsv

$LiquibaseName = "invoicextract-liquibase"
$LiquibaseImage = "$REGISTRY/invoiceextract-liquibase:latest"

$LiquibaseUrl = "jdbc:mysql://invoicextract-mysql:3306/$MysqlDbName"
Write-Host "Liquibase URL: $LiquibaseUrl" -ForegroundColor Cyan
Write-Host "=== Deploy Liquibase Container App ===" -ForegroundColor Cyan

# Obtener credenciales del ACR
$AcrLoginServer = az acr show -n "$ACR_NAME" --query loginServer -o tsv
$acrUser = az acr credential show -n "$ACR_NAME" --query username -o tsv
$acrPass = az acr credential show -n "$ACR_NAME" --query passwords[0].value -o tsv

$liquibaseExists = $false
try {
    az containerapp show -g "$RESOURCE_GROUP" -n "$LiquibaseName" --only-show-errors | Out-Null
    $liquibaseExists = $true
} catch {
    Write-Host "Liquibase container app does not exist. Will create a new one..."
}

if ($liquibaseExists) {
    Write-Host "Deleting existing Liquibase container app..."
    az containerapp delete -g "$RESOURCE_GROUP" -n "$LiquibaseName" --yes
    Start-Sleep -Seconds 10
}

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
  --min-replicas "1" `
  --max-replicas "1" `
  --env-vars `
    LIQUIBASE_COMMAND_URL="$LiquibaseUrl" `
    LIQUIBASE_COMMAND_USERNAME="root" `
    LIQUIBASE_COMMAND_PASSWORD="$MysqlRootPassword" | Out-Null

Write-Host "Liquibase container app deployed: $LiquibaseName" -ForegroundColor Green
Write-Host "Use this container to run migrations pointing to MySQL: $LiquibaseUrl"
Write-Host "Use this container to run migrations pointing to MySQL: $MysqlFqdn"
Write-Host "=== Deployment Completed ===" -ForegroundColor Green
