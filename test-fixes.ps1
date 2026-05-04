# Fakarni App Fixes Test Script
# PowerShell version

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Testing Fakarni App Fixes" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if all containers are running
Write-Host "Test 1: Checking container status..." -ForegroundColor Yellow
$running = (docker compose ps --format json | ConvertFrom-Json | Where-Object { $_.State -eq "running" }).Count
$total = 29

if ($running -eq $total) {
    Write-Host "✅ All $total containers are running" -ForegroundColor Green
} else {
    Write-Host "❌ Only $running/$total containers are running" -ForegroundColor Red
}
Write-Host ""

# Test 2: Check if Gateway is accessible
Write-Host "Test 2: Checking Gateway accessibility..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Gateway is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Gateway is not accessible" -ForegroundColor Red
}
Write-Host ""

# Test 3: Check if Eureka is accessible
Write-Host "Test 3: Checking Eureka Dashboard..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8762" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($response.Content -match "Eureka") {
        Write-Host "✅ Eureka Dashboard is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Eureka Dashboard is not accessible" -ForegroundColor Red
}
Write-Host ""

# Test 4: Check if Frontend is accessible
Write-Host "Test 4: Checking Frontend..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:4200" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Frontend is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Frontend is not accessible" -ForegroundColor Red
}
Write-Host ""

# Test 5: Check if activite-educative-service is registered
Write-Host "Test 5: Checking service registration..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8762/eureka/apps" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($response.Content -match "ACTIVITE-EDUCATIVE-SERVICE") {
        Write-Host "✅ Activite Educative Service is registered" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Activite Educative Service not yet registered (may need more time)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Could not check service registration" -ForegroundColor Yellow
}
Write-Host ""

# Test 6: Check if detection-maladie-service is registered
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8762/eureka/apps" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($response.Content -match "DETECTION-MALADIE-SERVICE") {
        Write-Host "✅ Detection Maladie Service is registered" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Detection Maladie Service not yet registered (may need more time)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Could not check service registration" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor White
Write-Host "1. Open http://localhost:4200 in your browser" -ForegroundColor White
Write-Host "2. Login as aidant to test geofencing maps" -ForegroundColor White
Write-Host "3. Navigate to Educational Events to test activity images" -ForegroundColor White
Write-Host "4. Create a post with an image to test post images" -ForegroundColor White
Write-Host "5. Go to Medical → Detection to test detection service" -ForegroundColor White
Write-Host ""
Write-Host "For detailed testing instructions, see DEPLOYMENT-COMPLETE.md" -ForegroundColor Cyan
Write-Host ""
