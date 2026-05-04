@echo off
REM 🚀 Script de démarrage rapide - Fakarni (Windows)
REM Usage: start.bat

echo 🚀 Démarrage de l'architecture Fakarni...
echo.

REM Vérifier Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker n'est pas installé
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker n'est pas démarré
    exit /b 1
)

echo ✅ Docker est prêt
echo.

REM Vérifier le fichier .env
if not exist .env (
    echo ⚠️  Fichier .env non trouvé
    echo Veuillez créer un fichier .env
)

echo 📦 Arrêt des containers existants...
docker compose down

echo.
echo 🔨 Build et démarrage des services...
echo ⏱️  Cela peut prendre 10-15 minutes la première fois...
echo.

docker compose up --build -d

echo.
echo ⏳ Attente du démarrage des services...
timeout /t 10 /nobreak >nul

echo.
echo 📊 État des services:
docker compose ps

echo.
echo ✅ Architecture démarrée !
echo.
echo 🌐 URLs disponibles:
echo    - Frontend:    http://localhost:4200
echo    - Eureka:      http://localhost:8762
echo    - Gateway:     http://localhost:8090
echo    - PhpMyAdmin:  http://localhost:8086
echo.
echo 📝 Voir les logs:
echo    docker compose logs -f
echo.
echo 🛑 Arrêter tout:
echo    docker compose down
echo.

pause
