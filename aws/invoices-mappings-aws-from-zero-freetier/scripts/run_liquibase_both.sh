#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Uso: $0 <terraform-outputs.json>"
  echo "Ejemplo: terraform output -json > terraform/outputs-dev.json"
  exit 1
fi

OUT_JSON="$1"
if [ ! -f "$OUT_JSON" ]; then
  echo "No existe $OUT_JSON"
  exit 1
fi

JDBC_BASE=$(jq -r '.jdbc_url.value' "$OUT_JSON")
DB_USER=$(jq -r '.db_username.value' "$OUT_JSON")

# jdbc_url apunta a /invoices por defecto; derivamos una URL base sin el nombre de DB
BASE_HOST=$(echo "$JDBC_BASE" | sed -E 's#(jdbc:mysql://[^/]+)/[^?]+(.*)#\1\2#')

read -s -p "DB password (usuario ${DB_USER}): " DB_PASSWORD
echo

# 1) Bootstrap: crear DB mappings (conectamos a invoices y ejecutamos SQL CREATE DATABASE IF NOT EXISTS)
export LB_URL="jdbc:mysql://${BASE_HOST}/invoices"
export LB_USERNAME="$DB_USER"
export LB_PASSWORD="$DB_PASSWORD"

echo "Bootstrap: crear DB mappings en $LB_URL"
docker run --rm -v "$(pwd)/liquibase:/liquibase/changelog" -w /liquibase   -e LB_URL -e LB_USERNAME -e LB_PASSWORD   liquibase/liquibase:latest   --defaultsFile=liquibase.properties   --changeLogFile=bootstrap/db.changelog-bootstrap.yaml   update

# 2) Aplicar changelogs de invoices
echo "Aplicando changelogs de INVOICES…"
docker run --rm -v "$(pwd)/liquibase:/liquibase/changelog" -w /liquibase   -e LB_URL -e LB_USERNAME -e LB_PASSWORD   liquibase/liquibase:latest   --defaultsFile=liquibase.properties   --changeLogFile=changelog/db.changelog-master.yaml   update

# 3) Aplicar changelogs de mappings (cambiando la DB del JDBC)
export LB_URL="jdbc:mysql://${BASE_HOST}/mappings"

echo "Aplicando changelogs de MAPPINGS…"
docker run --rm -v "$(pwd)/liquibase:/liquibase/changelog" -w /liquibase   -e LB_URL -e LB_USERNAME -e LB_PASSWORD   liquibase/liquibase:latest   --defaultsFile=liquibase.properties   --changeLogFile=mappings/changelog/db.changelog-master.yaml   update

echo "Listo: invoices + mappings creadas/actualizadas."
