# Fakarni Application Stop Script
# Run this script to stop all services

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  Stopping Fakarni Application" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# Check if Docker is running
Write-Host "[1/2] Checking Docker..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not running!" -ForegroundColor Red
    Write-Host "Services are already stopped." -ForegroundColor Gray
    exit 0
}

# Count running containers
$runningContainers = (docker ps --filter "name=fakarni_" --format "{{.Names}}" | Measure-Object).Count
Write-Host "📊 Currently running: $runningContainers containers" -ForegroundColor Cyan

# Stop all services
Write-Host ""
Write-Host "[2/2] Stopping all services..." -ForegroundColor Yellow
docker compose down

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to stop services" -ForegroundColor Red
    exit 1
}

Write-Host "✅ All services stopped" -ForegroundColor Green

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ Application Stopped Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Notes:" -ForegroundColor Cyan
Write-Host "  • All containers have been stopped and removed" -ForegroundColor White
Write-Host "  • Your data is preserved in Docker volumes" -ForegroundColor White
Write-Host "  • To start again, run: .\start.ps1" -ForegroundColor White
Write-Host ""
Write-Host "To delete all data (fresh start):" -ForegroundColor Yellow
Write-Host "  docker compose down -v" -ForegroundColor Red
Write-Host ""
