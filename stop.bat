@echo off
REM 🛑 Script d'arrêt - Fakarni (Windows)
REM Usage: stop.bat [clean]

echo 🛑 Arrêt de l'architecture Fakarni...
echo.

if "%1"=="clean" (
    echo ⚠️  Mode CLEAN : suppression des volumes ^(données perdues^)
    set /p confirm="Êtes-vous sûr ? (y/N) "
    if /i "%confirm%"=="y" (
        docker compose down -v
        echo ✅ Containers et volumes supprimés
    ) else (
        echo ❌ Annulé
    )
) else (
    docker compose down
    echo ✅ Containers arrêtés ^(données conservées^)
)

echo.
echo 📊 Containers restants:
docker ps -a | findstr fakarni
echo.

pause
