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

JDBC_URL=$(jq -r '.jdbc_url.value' "$OUT_JSON")
DB_USER=$(jq -r '.db_username.value' "$OUT_JSON")

read -s -p "DB password (usuario ${DB_USER}): " DB_PASSWORD
echo

export LB_URL="$JDBC_URL"
export LB_USERNAME="$DB_USER"
export LB_PASSWORD="$DB_PASSWORD"

echo "Liquibase update → $LB_URL"
docker run --rm -v "$(pwd)/liquibase:/liquibase/changelog" -w /liquibase   -e LB_URL -e LB_USERNAME -e LB_PASSWORD   liquibase/liquibase:latest   --defaultsFile=liquibase.properties   update

echo "OK."
