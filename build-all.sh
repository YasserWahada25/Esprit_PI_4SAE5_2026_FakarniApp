#!/bin/bash

# Script de build complet - Fakarni

echo "========================================"
echo " BUILD COMPLET - FAKARNI"
echo "========================================"
echo ""

echo "Verification Docker..."
if ! command -v docker &> /dev/null; then
    echo "ERREUR: Docker n'est pas installe"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "ERREUR: Docker n'est pas demarre"
    exit 1
fi

echo "Docker OK"
echo ""

echo "========================================"
echo " NETTOYAGE (optionnel)"
echo "========================================"
read -p "Nettoyer les anciennes images ? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Nettoyage en cours..."
    docker compose down
    docker system prune -f
    echo "Nettoyage termine"
fi
echo ""

echo "========================================"
echo " BUILD DES IMAGES"
echo "========================================"
echo ""
echo "Temps estime: 10-15 minutes"
echo ""

docker compose build

if [ $? -ne 0 ]; then
    echo ""
    echo "========================================"
    echo " ERREUR DE BUILD"
    echo "========================================"
    echo ""
    echo "Consultez BUILD-FIX.md pour les solutions"
    echo ""
    exit 1
fi

echo ""
echo "========================================"
echo " BUILD TERMINE AVEC SUCCES"
echo "========================================"
echo ""

echo "Verification des images creees:"
docker images | grep fakarni

echo ""
echo "========================================"
echo " PROCHAINE ETAPE"
echo "========================================"
echo ""
echo "Pour demarrer tous les services:"
echo "  docker compose up -d"
echo ""
echo "Pour voir les logs:"
echo "  docker compose logs -f"
echo ""
echo "URLs importantes:"
echo "  - Frontend:  http://localhost:4200"
echo "  - Eureka:    http://localhost:8762"
echo "  - Gateway:   http://localhost:8090"
echo ""
