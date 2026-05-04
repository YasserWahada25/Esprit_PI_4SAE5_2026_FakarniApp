#!/bin/bash

# 🚀 Script de démarrage rapide - Fakarni
# Usage: ./start.sh

set -e

echo "🚀 Démarrage de l'architecture Fakarni..."
echo ""

# Vérifier Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "❌ Docker n'est pas démarré"
    exit 1
fi

echo "✅ Docker est prêt"
echo ""

# Vérifier le fichier .env
if [ ! -f .env ]; then
    echo "⚠️  Fichier .env non trouvé"
    echo "Création d'un fichier .env par défaut..."
    cp .env.example .env 2>/dev/null || echo "Veuillez créer un fichier .env"
fi

echo "📦 Arrêt des containers existants..."
docker compose down

echo ""
echo "🔨 Build et démarrage des services..."
echo "⏱️  Cela peut prendre 10-15 minutes la première fois..."
echo ""

docker compose up --build -d

echo ""
echo "⏳ Attente du démarrage des services..."
sleep 10

echo ""
echo "📊 État des services:"
docker compose ps

echo ""
echo "✅ Architecture démarrée !"
echo ""
echo "🌐 URLs disponibles:"
echo "   - Frontend:    http://localhost:4200"
echo "   - Eureka:      http://localhost:8762"
echo "   - Gateway:     http://localhost:8090"
echo "   - PhpMyAdmin:  http://localhost:8086"
echo ""
echo "📝 Voir les logs:"
echo "   docker compose logs -f"
echo ""
echo "🛑 Arrêter tout:"
echo "   docker compose down"
echo ""
