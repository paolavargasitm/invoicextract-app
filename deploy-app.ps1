# ==========================
# Script: deploy-app.ps1
# Propósito: Crear contenedor para la aplicación principal
# ==========================

$RESOURCE_GROUP="invoiceextract-rg"
$ACR_NAME=$(az acr list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv)
$VNET_NAME="invoiceextract-vnet"
$SUBNET_NAME="invoiceextract-subnet"

Write-Host "==> Desplegando aplicación..."
az container create --resource-group $RESOURCE_GROUP --name invoiceextract-app `
  --image $ACR_NAME.azurecr.io/invoiceextract-app:latest `
  --ports 8081 `
  --vnet $VNET_NAME --subnet $SUBNET_NAME
