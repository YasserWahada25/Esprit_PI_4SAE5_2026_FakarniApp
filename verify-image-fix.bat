@echo off
echo 🔍 VERIFYING IMAGE FIX
echo =====================
echo.

REM Check Docker
echo [1/6] Checking Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running
    goto :end
) else (
    echo ✅ Docker is running
)
echo.

REM Check containers
echo [2/6] Checking containers...
docker ps --format "{{.Names}}" | findstr "fakarni_activite_service" >nul
if errorlevel 1 (
    echo ❌ activite-educative-service is NOT running
    echo    Run: docker-compose up -d
    goto :end
) else (
    echo ✅ activite-educative-service is running
)

docker ps --format "{{.Names}}" | findstr "fakarni_api_gateway" >nul
if errorlevel 1 (
    echo ❌ api-gateway is NOT running
    goto :end
) else (
    echo ✅ api-gateway is running
)

docker ps --format "{{.Names}}" | findstr "fakarni_frontend" >nul
if errorlevel 1 (
    echo ❌ frontend is NOT running
    goto :end
) else (
    echo ✅ frontend is running
)
echo.

REM Check upload directory
echo [3/6] Checking upload directory...
if exist "backend\activite-educative-service\uploads\activities" (
    echo ✅ Upload directory exists
    dir /b "backend\activite-educative-service\uploads\activities\*.jpg" "backend\activite-educative-service\uploads\activities\*.png" 2>nul | find /c /v "" > temp.txt
    set /p count=<temp.txt
    del temp.txt
    echo    Found images in directory
) else (
    echo ⚠️  Upload directory not found
)
echo.

REM Check service health
echo [4/6] Checking service health...
timeout /t 2 /nobreak >nul
curl -s http://localhost:8084/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ⚠️  activite-educative-service health check failed (may still be starting)
) else (
    echo ✅ activite-educative-service is healthy
)
echo.

REM Check image access
echo [5/6] Testing image access...
curl -s -o nul -w "%%{http_code}" http://localhost:8084/uploads/activities/4e345308e58b4ff292e629a994d342f7.png > temp.txt 2>&1
set /p code=<temp.txt
del temp.txt
if "%code%"=="200" (
    echo ✅ Direct image access: OK
) else (
    echo ⚠️  Direct image access: HTTP %code% (service may still be starting)
)

curl -s -o nul -w "%%{http_code}" http://localhost:8090/uploads/activities/4e345308e58b4ff292e629a994d342f7.png > temp.txt 2>&1
set /p code=<temp.txt
del temp.txt
if "%code%"=="200" (
    echo ✅ Gateway routing: OK
) else (
    echo ⚠️  Gateway routing: HTTP %code% (service may still be starting)
)
echo.

REM Check API
echo [6/6] Testing activities API...
curl -s http://localhost:8090/api/activities >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Activities API not responding (service may still be starting)
) else (
    echo ✅ Activities API is responding
)
echo.

echo ========================================
echo 🎉 VERIFICATION COMPLETE
echo ========================================
echo.
echo 📝 Next Steps:
echo    1. If all checks passed: Open http://localhost:4200/educational/activities
echo    2. If some checks failed: Wait 1-2 minutes and run this script again
echo    3. If still failing: Check logs with: docker logs fakarni_activite_service
echo.

:end
pause
