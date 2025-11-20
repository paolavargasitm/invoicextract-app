#!/bin/bash
set -e

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
# Get admin access token
ACCESS_TOKEN=$(docker exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin \
  --config /tmp/kcadm.config > /dev/null 2>&1 && \
  docker exec keycloak cat /tmp/kcadm.config | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

echo "Checking if realm already exists..."
# Check if realm exists
REALM_EXISTS=$(docker exec keycloak /opt/keycloak/bin/kcadm.sh get realms/invoices \
  --config /tmp/kcadm.config 2>&1 | grep -c "Resource not found" || true)

if [ "$REALM_EXISTS" -gt 0 ]; then
  echo "Importing realm from invoicextract-realm.json..."
  # Import the realm
  docker exec keycloak /opt/keycloak/bin/kcadm.sh create realms \
    --config /tmp/kcadm.config \
    -f /tmp/invoicextract-realm.json
  echo "Realm imported successfully!"
else
  echo "Realm 'invoices' already exists, skipping import."
fi

echo "Keycloak realm setup complete!"
echo "CLIENT_ID: invoices-backend"
echo "CLIENT_SECRET: TlPOfnP8P30SdR6bRl3WtJSNqM6ojdhA"
