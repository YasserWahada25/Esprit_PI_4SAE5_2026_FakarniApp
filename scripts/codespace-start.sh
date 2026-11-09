#!/usr/bin/env bash
# Relance la stack Fakarni après un redémarrage du Codespace, dans l'ordre
# bases -> Eureka -> services. Utilise "docker compose start" : aucun conteneur
# n'est recréé (les images reconstruites localement sont conservées).
#   bash scripts/codespace-start.sh
set -u
cd "$(dirname "$0")/.."

DBS="mongodb db-tracking db-geofencing db-activite db-detection db-dossier db-event db-group db-post db-suivi db-session"

echo "==> Attente du démon Docker"
until docker info >/dev/null 2>&1; do sleep 2; done

echo "==> 1/4 Bases de données"
docker compose start $DBS
for s in $DBS; do
  c=$(docker compose ps -q "$s")
  [ -z "$c" ] && { echo "   $s : conteneur absent, ignoré"; continue; }
  printf "   %-15s" "$s"
  for _ in $(seq 1 60); do
    st=$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$c")
    [ "$st" = healthy ] || [ "$st" = running ] && break
    sleep 3
  done
  echo "$st"
done

echo "==> 2/4 Eureka"
docker compose start eureka-server
until curl -sf localhost:8762/actuator/health >/dev/null 2>&1 || curl -sf localhost:8762/ >/dev/null 2>&1; do sleep 3; done
echo "   Eureka prêt"

echo "==> 3/4 Gateway, microservices, frontend"
docker compose start

# GitHub repasse les ports en privé au redémarrage : l'app mobile a besoin de la gateway publique.
if [ -n "${CODESPACE_NAME:-}" ] && command -v gh >/dev/null 2>&1; then
  gh codespace ports visibility 8090:public 4300:public -c "$CODESPACE_NAME" >/dev/null 2>&1 \
    && echo "   Ports 8090 (gateway) et 4300 (web) publics" \
    || echo "   Ports 8090 et 4300 : passez-les en Public dans l'onglet Ports"
fi

echo "==> 4/4 Attente de l'enregistrement dans Eureka (2 min max)"
for _ in $(seq 1 24); do
  n=$(curl -s -H "Accept: application/json" localhost:8762/eureka/apps | grep -o '"app":"[^"]*"' | sort -u | wc -l)
  printf "\r   %s services enregistrés" "$n"
  [ "$n" -ge 15 ] && break
  sleep 5
done
echo
curl -s -H "Accept: application/json" localhost:8762/eureka/apps | grep -o '"app":"[^"]*"' | sort -u
echo
docker compose ps -a --format "table {{.Name}}\t{{.Status}}"
