#!/bin/bash

# Script to get a fresh API token from the application
# This token expires after some time, so run this script when API tests fail with 401/404

echo "🔑 Getting fresh API token..."
echo ""
echo "⚠️  NOTE: The Keycloak client 'invoices-frontend' is configured for implicit flow,"
echo "   so we cannot get the token via direct password grant."
echo ""
echo "📋 To get a fresh token manually:"
echo "   1. Open your browser and login to: http://localhost:3001"
echo "   2. Open Browser DevTools (F12)"
echo "   3. Go to Application/Storage -> Local Storage"
echo "   4. Find the 'token' or 'access_token' key"
echo "   5. Copy the token value"
echo "   6. Update the API_TOKEN in your .env file"
echo ""
echo "OR extract it from the Authorization header in Network tab:"
echo "   1. Login to http://localhost:3001"
echo "   2. Open DevTools -> Network tab"
echo "   3. Make any API call (refresh the page)"
echo "   4. Click on any /api/ request"
echo "   5. Look at Request Headers -> Authorization: Bearer <TOKEN>"
echo "   6. Copy the token and update .env"
echo ""
echo "💡 Alternatively, if the API doesn't require auth for certain endpoints,"
echo "   you can comment out the API_TOKEN in .env"
