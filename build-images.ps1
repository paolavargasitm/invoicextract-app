# ==========================
# Script: build-images.ps1
# Propósito: Construir imágenes Docker y subirlas al ACR
# ==========================

$RESOURCE_GROUP="invoiceextract-rg"
$ACR_NAME=$(az acr list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv)

if ([string]::IsNullOrWhiteSpace($ACR_NAME)) {
    Write-Error "No se pudo obtener el nombre del ACR. Verifica que exista en el grupo $RESOURCE_GROUP"
    exit 1
}

$REGISTRY="$ACR_NAME.azurecr.io"

Write-Host "==> Construyendo imágenes locales..."
docker build -t $REGISTRY/invoiceextract-app:latest ./invoicextract-backend
docker build -t $REGISTRY/invoiceextract-liquibase:latest -f Dockerfile.liquibase .

Write-Host "==> Logueando en ACR..."
az acr login --name $ACR_NAME

Write-Host "==> Subiendo imágenes al ACR..."
docker push $REGISTRY/invoiceextract-app:latest
docker push $REGISTRY/invoiceextract-liquibase:latest

Write-Host "==> Imágenes subidas correctamente a $REGISTRY"
