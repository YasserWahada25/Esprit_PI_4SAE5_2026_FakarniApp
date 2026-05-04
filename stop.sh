#!/bin/bash

# 🛑 Script d'arrêt - Fakarni
# Usage: ./stop.sh [--clean]

set -e

echo "🛑 Arrêt de l'architecture Fakarni..."
echo ""

if [ "$1" == "--clean" ]; then
    echo "⚠️  Mode CLEAN : suppression des volumes (données perdues)"
    read -p "Êtes-vous sûr ? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker compose down -v
        echo "✅ Containers et volumes supprimés"
    else
        echo "❌ Annulé"
    fi
else
    docker compose down
    echo "✅ Containers arrêtés (données conservées)"
fi

echo ""
echo "📊 Containers restants:"
docker ps -a | grep fakarni || echo "Aucun"
echo ""
