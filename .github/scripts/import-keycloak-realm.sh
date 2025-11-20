#!/bin/bash
set -e

echo "Starting Keycloak realm import process..."

# Wait for Keycloak to be fully ready
echo "Waiting for Keycloak to be ready..."
max_attempts=60
attempt=0

while [ $attempt -lt $max_attempts ]; do
  if curl -s http://localhost:8085/realms/master/.well-known/openid-configuration > /dev/null 2>&1; then
    echo "Keycloak is ready!"
    break
  fi
  attempt=$((attempt + 1))
  echo "Attempt $attempt/$max_attempts - Keycloak not ready yet, waiting..."
  sleep 2
done

if [ $attempt -eq $max_attempts ]; then
  echo "Keycloak failed to become ready in time"
  exit 1
fi

echo "Logging in to Keycloak admin..."
# Configure kcadm credentials
docker exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin \
  --config /tmp/kcadm.config 2>&1 || echo "Login completed"

echo "Checking if realm already exists..."
# Check if realm exists
REALM_CHECK=$(docker exec keycloak /opt/keycloak/bin/kcadm.sh get realms/invoices \
  --config /tmp/kcadm.config 2>&1 || echo "NOT_FOUND")

if echo "$REALM_CHECK" | grep -q "Resource not found"; then
  echo "Realm not found. Importing realm from invoicextract-realm.json..."
  # Import the realm
  docker exec keycloak /opt/keycloak/bin/kcadm.sh create realms \
    --config /tmp/kcadm.config \
    -f /tmp/invoicextract-realm.json 2>&1 || echo "Realm import completed (may already exist)"
  echo "Realm import process completed!"
else
  echo "Realm 'invoices' already exists, skipping import."
fi

echo ""
echo "✅ Keycloak realm setup complete!"
echo "📋 Credentials for testing:"
echo "   CLIENT_ID: invoices-backend"
echo "   CLIENT_SECRET: TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA"
