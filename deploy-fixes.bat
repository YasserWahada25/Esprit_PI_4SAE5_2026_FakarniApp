@echo off
REM Fakarni App - Deploy All Fixes Script (Windows)
REM This script rebuilds and restarts all modified services

echo.
echo ============================================
echo   Fakarni App - Deploying All Fixes
echo ============================================
echo.

echo [Step 1] Stopping all containers...
docker compose down

echo.
echo [Step 2] Rebuilding modified services...
echo   - group-service
echo   - post-service
echo   - activite-educative-service
echo   - geofencing-service
docker compose build group-service post-service activite-educative-service geofencing-service

if errorlevel 1 (
    echo [ERROR] Build failed! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo [Step 3] Starting all services...
docker compose up -d

if errorlevel 1 (
    echo [ERROR] Failed to start services! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo [Step 4] Waiting for services to start (60 seconds)...
timeout /t 60 /nobreak

echo.
echo ============================================
echo   Deployment Complete!
echo ============================================
echo.
echo [Status] Checking service status...
docker compose ps

echo.
echo ============================================
echo   Next Steps:
echo ============================================
echo 1. Check Eureka Dashboard: http://localhost:8762
echo 2. Open Frontend: http://localhost:4200
echo 3. Test the fixes:
echo    - Create a post with image
echo    - Create a group
echo    - Upload activity images
echo    - Open Geofencing with map (Leaflet)
echo.
echo View logs with: docker compose logs -f [service-name]
echo.
echo All fixes deployed successfully!
echo Maps use Leaflet (no API key needed)
echo.
pause
