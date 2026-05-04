# Fakarni Application Status Check Script
# Run this script to check the status of all services

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Fakarni Application Status" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "🐳 Docker Status:" -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "  ✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Docker is not running!" -ForegroundColor Red
    Write-Host "  Please start Docker Desktop first." -ForegroundColor Yellow
    exit 1
}

# Count containers
Write-Host ""
Write-Host "📊 Container Status:" -ForegroundColor Yellow
$allContainers = (docker ps -a --filter "name=fakarni_" --format "{{.Names}}" | Measure-Object).Count
$runningContainers = (docker ps --filter "name=fakarni_" --format "{{.Names}}" | Measure-Object).Count

Write-Host "  Total containers: $allContainers" -ForegroundColor White
Write-Host "  Running: $runningContainers" -ForegroundColor Green
Write-Host "  Stopped: $($allContainers - $runningContainers)" -ForegroundColor $(if ($allContainers -eq $runningContainers) { "Gray" } else { "Yellow" })

# Show service status
Write-Host ""
Write-Host "🔧 Service Status:" -ForegroundColor Yellow
docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}" 2>$null

# Check key services
Write-Host ""
Write-Host "🌐 Key Services:" -ForegroundColor Yellow

$services = @(
    @{Name="Frontend"; Container="fakarni_frontend"; URL="http://localhost:4200"},
    @{Name="API Gateway"; Container="fakarni_api_gateway"; URL="http://localhost:8090"},
    @{Name="Eureka"; Container="fakarni_eureka_server"; URL="http://localhost:8762"},
    @{Name="User Service"; Container="fakarni_user_service"; URL="http://localhost:8081"},
    @{Name="MongoDB"; Container="fakarni_mongo"; URL="localhost:27018"},
    @{Name="phpMyAdmin"; Container="fakarni_phpmyadmin"; URL="http://localhost:8086"}
)

foreach ($service in $services) {
    $status = docker ps --filter "name=$($service.Container)" --format "{{.Status}}" 2>$null
    if ($status) {
        Write-Host "  ✅ $($service.Name.PadRight(15)) - Running - $($service.URL)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $($service.Name.PadRight(15)) - Stopped" -ForegroundColor Red
    }
}

# Database health
Write-Host ""
Write-Host "💾 Database Status:" -ForegroundColor Yellow
$mysqlCount = (docker ps --filter "name=fakarni_db_" --format "{{.Names}}" | Measure-Object).Count
$mongoStatus = docker ps --filter "name=fakarni_mongo" --format "{{.Status}}" 2>$null

Write-Host "  MySQL databases: $mysqlCount/10 running" -ForegroundColor $(if ($mysqlCount -eq 10) { "Green" } else { "Yellow" })
if ($mongoStatus) {
    Write-Host "  MongoDB: Running" -ForegroundColor Green
} else {
    Write-Host "  MongoDB: Stopped" -ForegroundColor Red
}

# Quick health check
Write-Host ""
Write-Host "🏥 Quick Health Check:" -ForegroundColor Yellow

# Check frontend
try {
    $response = Invoke-WebRequest -Uri "http://localhost:4200" -TimeoutSec 2 -UseBasicParsing 2>$null
    Write-Host "  ✅ Frontend responding" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Frontend not responding" -ForegroundColor Red
}

# Check Eureka
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8762" -TimeoutSec 2 -UseBasicParsing 2>$null
    Write-Host "  ✅ Eureka responding" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Eureka not responding" -ForegroundColor Red
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($runningContainers -eq 26) {
    Write-Host "  ✅ All Systems Operational!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "🚀 Ready to test: http://localhost:4200" -ForegroundColor Cyan
} elseif ($runningContainers -gt 0) {
    Write-Host "  ⚠️  Some Services Not Running" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "💡 Try restarting: .\start.ps1" -ForegroundColor Cyan
} else {
    Write-Host "  ❌ Application Not Running" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "💡 Start application: .\start.ps1" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "📝 Useful Commands:" -ForegroundColor Cyan
Write-Host "  • Start:  .\start.ps1" -ForegroundColor White
Write-Host "  • Stop:   .\stop.ps1" -ForegroundColor White
Write-Host "  • Logs:   docker logs fakarni_user_service -f" -ForegroundColor White
Write-Host "  • Restart: docker compose restart <service-name>" -ForegroundColor White
Write-Host ""
