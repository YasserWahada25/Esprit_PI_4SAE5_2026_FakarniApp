# Démarre les services minimaux pour connexion/inscription via la Gateway (8090).
# Prérequis : Docker Desktop, exécuter depuis ce dossier : .\scripts\start-backend-auth-stack.ps1

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "Démarrage : mongodb, eureka-server, user-service, api-gateway..." -ForegroundColor Cyan
docker compose up -d mongodb eureka-server user-service api-gateway

if ($LASTEXITCODE -ne 0) {
    Write-Host "docker compose a échoué. Vérifiez Docker et le fichier docker-compose.yml." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Attendez 30 à 90 secondes que les services Spring s'enregistrent dans Eureka." -ForegroundColor Yellow
Write-Host "Frontend : depuis le dossier frontend, npm start (avec proxy)." -ForegroundColor Green
Write-Host "Test rapide Gateway : Invoke-WebRequest http://localhost:8090/actuator/health -UseBasicParsing" -ForegroundColor Green
