#!/usr/bin/env pwsh
# ==============================
# AZURE GATEWAY CONFIGURATION
# ==============================
# Configure Gateway-Service to route to public Azure Web App URLs
# This fixes 502 Bad Gateway errors caused by internal lb:// URIs
# being non-routable between Azure Web Apps without VNet

param(
    [Parameter(Mandatory=$false)]
    [string]$ResourceGroup = "rg-fakarni-prod",
    
    [Parameter(Mandatory=$false)]
    [string]$GatewayAppName = "fakarni-gateway"
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "AZURE GATEWAY CONFIGURATION SCRIPT" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Check if Azure CLI is installed
Write-Host "[1/4] Checking Azure CLI..." -ForegroundColor Yellow
if (-not (Get-Command az -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Azure CLI not found. Install from https://aka.ms/installazurecli" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Azure CLI found" -ForegroundColor Green

# Check if logged in
Write-Host "`n[2/4] Checking Azure login..." -ForegroundColor Yellow
$account = az account show 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Not logged in to Azure. Run: az login" -ForegroundColor Red
    exit 1
}
$accountName = (az account show --query name -o tsv)
Write-Host "✓ Logged in as: $accountName" -ForegroundColor Green

# Check if Gateway app exists
Write-Host "`n[3/4] Checking if Gateway app exists..." -ForegroundColor Yellow
$appExists = az webapp show --name $GatewayAppName --resource-group $ResourceGroup 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Gateway app '$GatewayAppName' not found in resource group '$ResourceGroup'" -ForegroundColor Red
    Write-Host "Available apps:" -ForegroundColor Yellow
    az webapp list --resource-group $ResourceGroup --query "[].name" -o tsv
    exit 1
}
Write-Host "✓ Gateway app found: $GatewayAppName" -ForegroundColor Green

# Configure Gateway app settings
Write-Host "`n[4/4] Configuring Gateway routes to public HTTPS URLs..." -ForegroundColor Yellow

# Set Spring Profile to activate application-azure.properties
Write-Host "  → Setting SPRING_PROFILES_ACTIVE=azure" -ForegroundColor Cyan
az webapp config appsettings set `
    --name $GatewayAppName `
    --resource-group $ResourceGroup `
    --settings SPRING_PROFILES_ACTIVE=azure `
    --output none

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to set Spring profile" -ForegroundColor Red
    exit 1
}

# Set route URIs (these override application-azure.properties if needed)
Write-Host "  → Configuring route URIs for all services" -ForegroundColor Cyan
az webapp config appsettings set `
    --name $GatewayAppName `
    --resource-group $ResourceGroup `
    --settings `
        GATEWAY_ROUTES_USER_URI=https://fakarni-user.azurewebsites.net `
        GATEWAY_ROUTES_USER_AUTH_URI=https://fakarni-user.azurewebsites.net `
        GATEWAY_ROUTES_SESSION_URI=https://fakarni-session.azurewebsites.net `
        GATEWAY_ROUTES_EVENT_URI=https://fakarni-event.azurewebsites.net `
        GATEWAY_ROUTES_POST_URI=https://fakarni-post.azurewebsites.net `
        GATEWAY_ROUTES_GROUP_URI=https://fakarni-group.azurewebsites.net `
        GATEWAY_ROUTES_CHAT_URI=https://fakarni-chat.azurewebsites.net `
        GATEWAY_ROUTES_CHAT_WS_URI=wss://fakarni-chat.azurewebsites.net `
        GATEWAY_ROUTES_MEETING_INSIGHTS_URI=https://fakarni-meeting.azurewebsites.net `
        GATEWAY_DIRECT_ACTIVITE_EDUCATIVE_SERVICE_URI=https://fakarni-activite.azurewebsites.net `
    --output none

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to set route URIs" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Route URIs configured" -ForegroundColor Green

# Restart Gateway to apply changes
Write-Host "`n[5/5] Restarting Gateway-Service..." -ForegroundColor Yellow
az webapp restart --name $GatewayAppName --resource-group $ResourceGroup --output none

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to restart Gateway" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Gateway restarted successfully" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "✓ GATEWAY CONFIGURATION COMPLETE" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Wait 2-3 minutes for Gateway to fully restart" -ForegroundColor White
Write-Host "  2. Test user registration: https://fakarni-frontend.azurewebsites.net" -ForegroundColor White
Write-Host "  3. Check Gateway logs if issues persist:" -ForegroundColor White
Write-Host "     az webapp log tail --name $GatewayAppName --resource-group $ResourceGroup" -ForegroundColor Cyan

Write-Host "`nConfigured routes:" -ForegroundColor Yellow
Write-Host "  • /api/users → https://fakarni-user.azurewebsites.net" -ForegroundColor White
Write-Host "  • /session → https://fakarni-session.azurewebsites.net" -ForegroundColor White
Write-Host "  • /api/events → https://fakarni-event.azurewebsites.net" -ForegroundColor White
Write-Host "  • /api/posts → https://fakarni-post.azurewebsites.net" -ForegroundColor White
Write-Host "  • /api/groups → https://fakarni-group.azurewebsites.net" -ForegroundColor White
Write-Host "  • /api/messages → https://fakarni-chat.azurewebsites.net" -ForegroundColor White
Write-Host "  • /api/meet → https://fakarni-meeting.azurewebsites.net" -ForegroundColor White
Write-Host "  • /api/activities → https://fakarni-activite.azurewebsites.net" -ForegroundColor White

Write-Host "`n⚠️  NOTE: Hardcoded lb:// routes still need code changes:" -ForegroundColor Yellow
Write-Host "  • /api/detection → Detection_Maladie-Service" -ForegroundColor White
Write-Host "  • /api/dossiers → Dossier_Medical-Service" -ForegroundColor White
Write-Host "  • /api/engagement → SUIVI-ENGAGEMENT-SERVICE" -ForegroundColor White
Write-Host "  • /api/tracking → TRACKING-SERVICE" -ForegroundColor White
Write-Host "  • /api/geofencing → GEOFENCING-SERVICE" -ForegroundColor White
Write-Host "  These will work once deployed with Eureka registration.`n" -ForegroundColor White
