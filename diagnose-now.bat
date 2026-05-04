@echo off
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║         🔍 IMAGE DISPLAY DIAGNOSTIC TOOL                   ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Check Docker
echo [CHECK 1] Docker Status
echo ─────────────────────────────────────────────────────────────
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ PROBLEM: Docker is NOT running
    echo 💡 FIX: Start Docker Desktop
    echo.
    goto :end
) else (
    echo ✅ Docker is running
)
echo.

REM Check containers
echo [CHECK 2] Container Status
echo ─────────────────────────────────────────────────────────────
set "containers_running=0"

docker ps --format "{{.Names}}" | findstr "fakarni_activite_service" >nul
if errorlevel 1 (
    echo ❌ PROBLEM: activite-educative-service is NOT running
    set "containers_running=1"
) else (
    echo ✅ activite-educative-service is running
)

docker ps --format "{{.Names}}" | findstr "fakarni_api_gateway" >nul
if errorlevel 1 (
    echo ❌ PROBLEM: api-gateway is NOT running
    set "containers_running=1"
) else (
    echo ✅ api-gateway is running
)

docker ps --format "{{.Names}}" | findstr "fakarni_frontend" >nul
if errorlevel 1 (
    echo ❌ PROBLEM: frontend is NOT running
    set "containers_running=1"
) else (
    echo ✅ frontend is running
)

docker ps --format "{{.Names}}" | findstr "fakarni_eureka_server" >nul
if errorlevel 1 (
    echo ❌ PROBLEM: eureka-server is NOT running
    set "containers_running=1"
) else (
    echo ✅ eureka-server is running
)

if "%containers_running%"=="1" (
    echo.
    echo 💡 FIX: Run this command:
    echo    docker-compose up -d
    echo.
    goto :end
)
echo.

REM Check upload directory
echo [CHECK 3] Upload Directory
echo ─────────────────────────────────────────────────────────────
if exist "backend\activite-educative-service\uploads\activities" (
    echo ✅ Upload directory exists
    echo    Location: backend\activite-educative-service\uploads\activities
    echo.
    echo    Files found:
    dir /b "backend\activite-educative-service\uploads\activities" 2>nul | findstr /R ".*" >nul
    if errorlevel 1 (
        echo    ⚠️  No files in directory
    ) else (
        dir /b "backend\activite-educative-service\uploads\activities" | findstr /R ".*"
    )
) else (
    echo ❌ PROBLEM: Upload directory does not exist
    echo 💡 FIX: Create directory or upload an image from admin panel
)
echo.

REM Check service health
echo [CHECK 4] Service Health
echo ─────────────────────────────────────────────────────────────
echo Waiting for services to respond...
timeout /t 3 /nobreak >nul

curl -s http://localhost:8084/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️  activite-educative-service: Not responding
    echo    This is normal if services just started (wait 2-3 minutes)
) else (
    echo ✅ activite-educative-service: Healthy
)

curl -s http://localhost:8090/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️  api-gateway: Not responding
    echo    This is normal if services just started (wait 2-3 minutes)
) else (
    echo ✅ api-gateway: Healthy
)
echo.

REM Check image access
echo [CHECK 5] Image Access Test
echo ─────────────────────────────────────────────────────────────
echo Testing with sample image: 4e345308e58b4ff292e629a994d342f7.png
echo.

REM Test direct service access
curl -s -o nul -w "%%{http_code}" http://localhost:8084/uploads/activities/4e345308e58b4ff292e629a994d342f7.png > temp_code.txt 2>&1
set /p direct_code=<temp_code.txt
del temp_code.txt

if "%direct_code%"=="200" (
    echo ✅ Direct access: HTTP 200 OK
) else if "%direct_code%"=="404" (
    echo ❌ PROBLEM: Direct access: HTTP 404 Not Found
    echo 💡 FIX: Image file may not exist in uploads directory
) else if "%direct_code%"=="503" (
    echo ❌ PROBLEM: Direct access: HTTP 503 Service Unavailable
    echo 💡 FIX: Service is not running or not ready
) else (
    echo ⚠️  Direct access: HTTP %direct_code%
)

REM Test gateway routing
curl -s -o nul -w "%%{http_code}" http://localhost:8090/uploads/activities/4e345308e58b4ff292e629a994d342f7.png > temp_code.txt 2>&1
set /p gateway_code=<temp_code.txt
del temp_code.txt

if "%gateway_code%"=="200" (
    echo ✅ Gateway routing: HTTP 200 OK
) else if "%gateway_code%"=="404" (
    echo ❌ PROBLEM: Gateway routing: HTTP 404 Not Found
    echo 💡 FIX: Gateway routing may be misconfigured
) else if "%gateway_code%"=="503" (
    echo ❌ PROBLEM: Gateway routing: HTTP 503 Service Unavailable
    echo 💡 FIX: Service is not registered with Eureka yet (wait 2-3 minutes)
) else (
    echo ⚠️  Gateway routing: HTTP %gateway_code%
)
echo.

REM Check API
echo [CHECK 6] Activities API
echo ─────────────────────────────────────────────────────────────
curl -s http://localhost:8090/api/activities >nul 2>&1
if errorlevel 1 (
    echo ❌ PROBLEM: Activities API not responding
    echo 💡 FIX: Service may still be starting (wait 2-3 minutes)
) else (
    echo ✅ Activities API is responding
)
echo.

REM Final diagnosis
echo ╔════════════════════════════════════════════════════════════╗
echo ║                    🎯 DIAGNOSIS COMPLETE                   ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

if "%containers_running%"=="1" (
    echo 🔴 MAIN ISSUE: Docker services are NOT running
    echo.
    echo 📝 TO FIX:
    echo    1. Run: docker-compose up -d
    echo    2. Wait 2-3 minutes for services to start
    echo    3. Run this diagnostic again
    echo.
    echo 🚀 QUICK FIX:
    echo    Run: fix-images-now.bat
) else (
    echo 🟢 All services are running!
    echo.
    echo 📝 NEXT STEPS:
    echo    1. If services just started, wait 2-3 minutes
    echo    2. Open: http://localhost:4200/educational/activities
    echo    3. Images should display
    echo.
    echo 🔍 TO CHECK LOGS:
    echo    docker logs fakarni_activite_service
)
echo.

:end
pause
