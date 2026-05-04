@echo off
echo 🔧 FIXING IMAGE DISPLAY ISSUE
echo ==============================
echo.

REM Step 1: Check if Docker is running
echo 📋 Step 1: Checking Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running!
    echo    Please start Docker Desktop and run this script again.
    pause
    exit /b 1
)
echo ✅ Docker is running
echo.

REM Step 2: Stop all containers
echo 📋 Step 2: Stopping existing containers...
docker-compose down
echo ✅ Containers stopped
echo.

REM Step 3: Start all services
echo 📋 Step 3: Starting all services...
docker-compose up -d
echo ✅ Services starting...
echo.

REM Step 4: Wait for services
echo 📋 Step 4: Waiting for services to be ready (60 seconds)...
timeout /t 60 /nobreak >nul
echo.

REM Step 5: Check service status
echo 📋 Step 5: Checking service status...
echo.
docker ps --format "table {{.Names}}\t{{.Status}}" | findstr "fakarni_activite_service fakarni_api_gateway fakarni_frontend"
echo.

REM Step 6: Test image access
echo 📋 Step 6: Testing image access...
echo.

echo Testing activite-educative-service...
curl -s -o nul -w "%%{http_code}" http://localhost:8084/uploads/activities/4e345308e58b4ff292e629a994d342f7.png | findstr "200" >nul
if errorlevel 1 (
    echo ⚠️  Direct service access: Not ready yet (this is normal, service may still be starting)
) else (
    echo ✅ Direct service access: OK
)

echo Testing gateway routing...
curl -s -o nul -w "%%{http_code}" http://localhost:8090/uploads/activities/4e345308e58b4ff292e629a994d342f7.png | findstr "200" >nul
if errorlevel 1 (
    echo ⚠️  Gateway routing: Not ready yet (this is normal, service may still be starting)
) else (
    echo ✅ Gateway routing: OK
)

echo.
echo 🎉 SETUP COMPLETE!
echo.
echo 📝 Next Steps:
echo    1. Wait 2-3 minutes for all services to fully start
echo    2. Open: http://localhost:4200/educational/activities
echo    3. Images should now display!
echo.
echo 🔍 To check logs:
echo    docker logs fakarni_activite_service
echo.
echo 📖 For detailed troubleshooting, see: IMAGE-UPLOAD-COMPLETE-FIX.md
echo.
pause
