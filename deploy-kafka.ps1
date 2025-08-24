# ==========================
# Script: deploy-kafka.ps1
# Propósito: Crear contenedores para Kafka y Zookeeper
# ==========================

$RESOURCE_GROUP="invoiceextract-rg"
$VNET_NAME="invoiceextract-vnet"
$SUBNET_NAME="invoiceextract-subnet"

Write-Host "==> Desplegando Zookeeper..."
az container create --resource-group $RESOURCE_GROUP --name zookeeper `
  --image wurstmeister/zookeeper --ports 2181 `
  --vnet $VNET_NAME --subnet $SUBNET_NAME

Write-Host "==> Desplegando Kafka..."
az container create --resource-group $RESOURCE_GROUP --name kafka `
  --image wurstmeister/kafka --ports 9092 `
  --environment-variables KAFKA_ADVERTISED_HOST_NAME=kafka `
  --vnet $VNET_NAME --subnet $SUBNET_NAME
