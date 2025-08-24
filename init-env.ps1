# ==========================
# Script: init-env.ps1
# Propósito: Crear recursos iniciales en Azure
# ==========================

$RESOURCE_GROUP="invoiceextract-rg"
$LOCATION="eastus"
$ACR_NAME="invoiceextractacr"   # nombre fijo
$VNET_NAME="invoiceextract-vnet"
$SUBNET_NAME="invoiceextract-subnet"
$ENV_FILE = ".\env-variables.ps1"

Write-Host "==> Creando grupo de recursos..."
az group create --name $RESOURCE_GROUP --location $LOCATION | Out-Null

Write-Host "==> Creando Azure Container Registry (ACR)..."
az acr create --resource-group $RESOURCE_GROUP `
              --name $ACR_NAME `
              --sku Basic `
              --admin-enabled true | Out-Null

az acr login --name $ACR_NAME

Write-Host "==> Creando red virtual..."
az network vnet create --resource-group $RESOURCE_GROUP `
                       --name $VNET_NAME `
                       --address-prefix 10.0.0.0/16 `
                       --subnet-name $SUBNET_NAME `
                       --subnet-prefix 10.0.1.0/24 | Out-Null

Write-Host "==> Delegando subnet para contenedores (ACI)..."
az network vnet subnet update `
  --resource-group $RESOURCE_GROUP `
  --vnet-name $VNET_NAME `
  --name $SUBNET_NAME `
  --delegations Microsoft.ContainerInstance/containerGroups | Out-Null

Write-Host "==> Guardando variables de entorno en $ENV_FILE..."
Set-Content -Path $ENV_FILE -Value @"
`$RESOURCE_GROUP='$RESOURCE_GROUP'
`$LOCATION='$LOCATION'
`$ACR_NAME='$ACR_NAME'
`$VNET_NAME='$VNET_NAME'
`$SUBNET_NAME='$SUBNET_NAME'
"@

Write-Host "==> Recursos iniciales creados con éxito"
Write-Host "==> Ejecuta 'deploy-mysql-adminer.ps1' después"
