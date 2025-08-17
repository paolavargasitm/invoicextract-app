# Azure Container Apps Deployment Script for InvoiceExtract Stack
# This PowerShell script deploys the complete docker-compose stack to Azure Container Apps

param(
    [Parameter(Mandatory=$true)]
    [string]$ResourceGroupName,
    
    [Parameter(Mandatory=$true)]
    [string]$Location = "East US",
    
    [Parameter(Mandatory=$false)]
    [string]$SubscriptionId,
    
    [Parameter(Mandatory=$false)]
    [string]$AppName = "invoicextract",
    
    [Parameter(Mandatory=$false)]
    [string]$MySqlRootPassword = "root",
    
    [Parameter(Mandatory=$true)]
    [string]$EncryptionSecretKey
)

# Set error action preference
$ErrorActionPreference = "Stop"

Write-Host "🚀 Starting Azure deployment for InvoiceExtract application..." -ForegroundColor Green

# Login to Azure (if not already logged in)
try {
    $context = Get-AzContext
    if (-not $context) {
        Write-Host "📝 Please login to Azure..." -ForegroundColor Yellow
        Connect-AzAccount
    }
} catch {
    Write-Host "📝 Please login to Azure..." -ForegroundColor Yellow
    Connect-AzAccount
}

# Set subscription if provided
if ($SubscriptionId) {
    Write-Host "🔄 Setting subscription to: $SubscriptionId" -ForegroundColor Blue
    Set-AzContext -SubscriptionId $SubscriptionId
}

# Create resource group if it doesn't exist
Write-Host "📦 Creating resource group: $ResourceGroupName" -ForegroundColor Blue
try {
    $rg = Get-AzResourceGroup -Name $ResourceGroupName -ErrorAction SilentlyContinue
    if (-not $rg) {
        New-AzResourceGroup -Name $ResourceGroupName -Location $Location
        Write-Host "✅ Resource group created successfully" -ForegroundColor Green
    } else {
        Write-Host "ℹ️ Resource group already exists" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Failed to create resource group: $_" -ForegroundColor Red
    exit 1
}

# Build and push Docker image to Azure Container Registry (ACR)
Write-Host "🐳 Building and pushing Docker image..." -ForegroundColor Blue

# Create ACR if it doesn't exist
$acrName = "$AppName" + "acr" + (Get-Random -Maximum 9999)
try {
    $acr = Get-AzContainerRegistry -ResourceGroupName $ResourceGroupName -Name $acrName -ErrorAction SilentlyContinue
    if (-not $acr) {
        Write-Host "📦 Creating Azure Container Registry: $acrName" -ForegroundColor Blue
        New-AzContainerRegistry -ResourceGroupName $ResourceGroupName -Name $acrName -Sku Basic -Location $Location
        Write-Host "✅ ACR created successfully" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Failed to create ACR: $_" -ForegroundColor Red
    exit 1
}

# Get ACR login server
$acrLoginServer = (Get-AzContainerRegistry -ResourceGroupName $ResourceGroupName -Name $acrName).LoginServer

# Build and push Docker image
Write-Host "🔨 Building Docker image..." -ForegroundColor Blue
Set-Location -Path "../"
docker build -t "$acrLoginServer/invoicextract:latest" -f invoicextract-backend/Dockerfile .

# Login to ACR and push image
Write-Host "📤 Pushing image to ACR..." -ForegroundColor Blue
az acr login --name $acrName
docker push "$acrLoginServer/invoicextract:latest"

# Update parameters with values
$parametersPath = "azure/parameters.json"
$parameters = Get-Content $parametersPath | ConvertFrom-Json
$parameters.parameters.appName.value = $AppName
$parameters.parameters.location.value = $Location
$parameters.parameters.mysqlRootPassword.value = $MySqlRootPassword
$parameters.parameters.encryptionSecretKey.value = $EncryptionSecretKey

# Save updated parameters
$parameters | ConvertTo-Json -Depth 10 | Set-Content $parametersPath

# Deploy Azure Container Apps using Bicep template
Write-Host "☁️ Deploying Azure Container Apps..." -ForegroundColor Blue
try {
    $deployment = New-AzResourceGroupDeployment `
        -ResourceGroupName $ResourceGroupName `
        -TemplateFile "azure/container-apps.bicep" `
        -TemplateParameterFile "azure/parameters.json" `
        -Verbose

    if ($deployment.ProvisioningState -eq "Succeeded") {
        Write-Host "✅ Azure resources deployed successfully!" -ForegroundColor Green
        
        # Display deployment outputs
        Write-Host "`n📋 Deployment Information:" -ForegroundColor Cyan
        Write-Host "🌐 Web App URL: $($deployment.Outputs.webAppUrl.Value)" -ForegroundColor Green
        Write-Host "🗄️ MySQL Server: $($deployment.Outputs.mysqlServerFqdn.Value)" -ForegroundColor Green
        Write-Host "🔐 Key Vault: $($deployment.Outputs.keyVaultName.Value)" -ForegroundColor Green
        Write-Host "📦 Resource Group: $($deployment.Outputs.resourceGroupName.Value)" -ForegroundColor Green
        
        # Update Web App with ACR image
        Write-Host "🔄 Updating Web App with container image..." -ForegroundColor Blue
        $webAppName = "$AppName-app"
        Set-AzWebApp -ResourceGroupName $ResourceGroupName -Name $webAppName -ContainerImageName "$acrLoginServer/invoicextract:latest"
        
        Write-Host "`n🎉 Deployment completed successfully!" -ForegroundColor Green
        Write-Host "🌐 Your application will be available at: $($deployment.Outputs.webAppUrl.Value)/invoicextract" -ForegroundColor Cyan
        Write-Host "📚 Swagger UI: $($deployment.Outputs.webAppUrl.Value)/invoicextract/swagger-ui/index.html" -ForegroundColor Cyan
        
    } else {
        Write-Host "❌ Deployment failed with state: $($deployment.ProvisioningState)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Deployment failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n✅ Azure deployment completed successfully! 🎉" -ForegroundColor Green
