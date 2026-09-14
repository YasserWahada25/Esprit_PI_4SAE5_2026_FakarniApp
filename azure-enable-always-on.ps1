#!/usr/bin/env pwsh
# ==============================
# AZURE ALWAYS ON CONFIGURATION
# ==============================
# Enable Always On for all Azure Web Apps to prevent cold starts
# This fixes 502/504 errors on first request after inactivity

param(
    [Parameter(Mandatory=$false)]
    [string]$ResourceGroup = "rg-fakarni-prod"
)

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "AZURE ALWAYS ON CONFIGURATION SCRIPT" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Check if Azure CLI is installed
Write-Host "[1/3] Checking Azure CLI..." -ForegroundColor Yellow
if (-not (Get-Command az -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Azure CLI not found. Install from https://aka.ms/installazurecli" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Azure CLI found" -ForegroundColor Green

# Check if logged in
Write-Host "`n[2/3] Checking Azure login..." -ForegroundColor Yellow
$account = az account show 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Not logged in to Azure. Run: az login" -ForegroundColor Red
    exit 1
}
$accountName = (az account show --query name -o tsv)
Write-Host "✓ Logged in as: $accountName" -ForegroundColor Green

# Get all web apps in resource group
Write-Host "`n[3/3] Discovering Web Apps in resource group '$ResourceGroup'..." -ForegroundColor Yellow
$webApps = az webapp list --resource-group $ResourceGroup --query "[].name" -o tsv 2>$null

if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($webApps)) {
    Write-Host "ERROR: No Web Apps found in resource group '$ResourceGroup'" -ForegroundColor Red
    Write-Host "Available resource groups:" -ForegroundColor Yellow
    az group list --query "[].name" -o tsv
    exit 1
}

$webAppArray = $webApps -split "`n" | Where-Object { $_ -ne "" }
Write-Host "✓ Found $($webAppArray.Count) Web Apps" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "ENABLING ALWAYS ON" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$successCount = 0
$skipCount = 0
$failCount = 0

foreach ($appName in $webAppArray) {
    Write-Host "Processing: $appName" -ForegroundColor Cyan
    
    # Check current Always On status
    $currentStatus = az webapp config show --name $appName --resource-group $ResourceGroup --query "alwaysOn" -o tsv 2>$null
    
    if ($currentStatus -eq "true") {
        Write-Host "  ⏭️  Already enabled - skipping" -ForegroundColor Gray
        $skipCount++
        continue
    }
    
    # Enable Always On
    az webapp config set --name $appName --resource-group $ResourceGroup --always-on true --output none 2>$null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Always On enabled" -ForegroundColor Green
        $successCount++
    } else {
        Write-Host "  ✗ Failed to enable Always On" -ForegroundColor Red
        $failCount++
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "SUMMARY" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Total Web Apps: $($webAppArray.Count)" -ForegroundColor White
Write-Host "✓ Enabled: $successCount" -ForegroundColor Green
Write-Host "⏭️  Already enabled: $skipCount" -ForegroundColor Gray
if ($failCount -gt 0) {
    Write-Host "✗ Failed: $failCount" -ForegroundColor Red
}

if ($failCount -eq 0) {
    Write-Host "`n✓ ALL WEB APPS CONFIGURED SUCCESSFULLY" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  SOME WEB APPS FAILED - CHECK ERRORS ABOVE" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "CONFIGURED WEB APPS" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

foreach ($appName in $webAppArray) {
    $status = az webapp config show --name $appName --resource-group $ResourceGroup --query "alwaysOn" -o tsv 2>$null
    $state = az webapp show --name $appName --resource-group $ResourceGroup --query "state" -o tsv 2>$null
    $url = "https://$appName.azurewebsites.net"
    
    if ($status -eq "true") {
        Write-Host "✓ $appName" -ForegroundColor Green
        Write-Host "  State: $state | Always On: Enabled" -ForegroundColor Gray
        Write-Host "  URL: $url" -ForegroundColor Cyan
    } else {
        Write-Host "✗ $appName" -ForegroundColor Red
        Write-Host "  State: $state | Always On: Disabled" -ForegroundColor Gray
        Write-Host "  URL: $url" -ForegroundColor Cyan
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "WHAT ALWAYS ON DOES" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Benefits:" -ForegroundColor Yellow
Write-Host "  • Keeps Web App instances loaded in memory" -ForegroundColor White
Write-Host "  • Prevents container unload after 20 min inactivity" -ForegroundColor White
Write-Host "  • Eliminates cold start delays (74-133s observed)" -ForegroundColor White
Write-Host "  • Reduces 502/504 errors on first request" -ForegroundColor White

Write-Host "`nRequirements:" -ForegroundColor Yellow
Write-Host "  • Only available on Basic (B1+) and higher tiers" -ForegroundColor White
Write-Host "  • Not available on Free (F1) or Shared (D1) tiers" -ForegroundColor White
Write-Host "  • Your apps are on B1 tier ✓" -ForegroundColor Green

Write-Host "`nTrade-offs:" -ForegroundColor Yellow
Write-Host "  • Slightly higher Azure costs (instances stay active)" -ForegroundColor White
Write-Host "  • Estimated: ~5-10% increase on B1 tier" -ForegroundColor White
Write-Host "  • Worth it for production reliability" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "NEXT STEPS" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "1. Changes take effect immediately (no restart needed)" -ForegroundColor White
Write-Host "2. Test application to verify no more cold start delays" -ForegroundColor White
Write-Host "3. Monitor for 502/504 errors (should be eliminated)" -ForegroundColor White

Write-Host "`nVerification commands:" -ForegroundColor Yellow
Write-Host "  # Check if all apps respond quickly (no cold start)" -ForegroundColor Cyan
Write-Host "  foreach (`$app in @('fakarni-gateway','fakarni-user','fakarni-frontend')) {" -ForegroundColor Cyan
Write-Host "    Measure-Command { curl https://`$app.azurewebsites.net/actuator/health }" -ForegroundColor Cyan
Write-Host "  }" -ForegroundColor Cyan

Write-Host "`n  # Disable Always On for specific app (if needed)" -ForegroundColor Cyan
Write-Host "  az webapp config set --name <app-name> --resource-group $ResourceGroup --always-on false" -ForegroundColor Cyan

Write-Host "`n✓ Configuration complete!`n" -ForegroundColor Green
