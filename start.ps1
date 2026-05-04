# Fakarni Application Startup Script
# Run this script to start all services

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Fakarni Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "[1/5] Checking Docker..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not running!" -ForegroundColor Red
    Write-Host "Please start Docker Desktop first." -ForegroundColor Yellow
    exit 1
}

# Start all services
Write-Host ""
Write-Host "[2/5] Starting all services..." -ForegroundColor Yellow
docker compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start services" -ForegroundColor Red
    exit 1
}

Write-Host "✅ All services started" -ForegroundColor Green

# Wait for services to initialize
Write-Host ""
Write-Host "[3/5] Waiting for services to initialize..." -ForegroundColor Yellow
Write-Host "⏳ Databases starting... (10s)" -ForegroundColor Gray
Start-Sleep -Seconds 10
Write-Host "⏳ Eureka & Gateway starting... (15s)" -ForegroundColor Gray
Start-Sleep -Seconds 15
Write-Host "⏳ Microservices starting... (15s)" -ForegroundColor Gray
Start-Sleep -Seconds 15
Write-Host "✅ Services should be ready" -ForegroundColor Green

# Check service status
Write-Host ""
Write-Host "[4/5] Checking service status..." -ForegroundColor Yellow
$runningContainers = (docker ps --format "{{.Names}}" | Measure-Object).Count
Write-Host "✅ Running containers: $runningContainers/26" -ForegroundColor Green

# Open browser
Write-Host ""
Write-Host "[5/5] Opening application..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

Write-Host "🌐 Opening Frontend: http://localhost:4200" -ForegroundColor Cyan
Start-Process "http://localhost:4200"

Start-Sleep -Seconds 2
Write-Host "🌐 Opening Eureka Dashboard: http://localhost:8762" -ForegroundColor Cyan
Start-Process "http://localhost:8762"

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ Application Started Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Access URLs:" -ForegroundColor Cyan
Write-Host "  • Frontend:    http://localhost:4200" -ForegroundColor White
Write-Host "  • Eureka:      http://localhost:8762" -ForegroundColor White
Write-Host "  • API Gateway: http://localhost:8090" -ForegroundColor White
Write-Host "  • phpMyAdmin:  http://localhost:8086" -ForegroundColor White
Write-Host ""
Write-Host "Useful Commands:" -ForegroundColor Cyan
Write-Host "  • View logs:   docker logs fakarni_user_service -f" -ForegroundColor White
Write-Host "  • Check status: docker ps" -ForegroundColor White
Write-Host "  • Stop all:    docker compose down" -ForegroundColor White
Write-Host ""
Write-Host "Happy testing! 🚀" -ForegroundColor Green
Write-Host ""
