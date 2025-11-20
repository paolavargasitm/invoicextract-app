#!/bin/bash

# Script to update backend with latest changes and rebuild Docker containers
# Usage: ./scripts/update-backend.sh

set -e  # Exit on error

echo "🔄 Starting backend update process..."

# Step 1: Check current status
echo ""
echo "📊 Current Git Status:"
git status --short

# Step 2: Ask to stash changes if needed
if ! git diff-index --quiet HEAD --; then
    echo ""
    read -p "❓ You have uncommitted changes. Stash them? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git stash save "WIP: auto-stash before updating backend $(date '+%Y-%m-%d %H:%M:%S')"
        echo "✅ Changes stashed"
    fi
fi

# Step 3: Pull latest from main
echo ""
echo "📥 Pulling latest changes from main branch..."
CURRENT_BRANCH=$(git branch --show-current)
git checkout main
git pull origin main

# Step 4: Return to feature branch if needed
if [ "$CURRENT_BRANCH" != "main" ]; then
    echo ""
    echo "🔀 Switching back to $CURRENT_BRANCH..."
    git checkout "$CURRENT_BRANCH"
    
    read -p "❓ Merge main into $CURRENT_BRANCH? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git merge main
        echo "✅ Merged main into $CURRENT_BRANCH"
    fi
fi

# Step 5: Find docker-compose file
echo ""
echo "🐳 Looking for docker-compose file..."

if [ -f "docker-compose.yml" ]; then
    COMPOSE_FILE="docker-compose.yml"
elif [ -f "../docker-compose.yml" ]; then
    cd ..
    COMPOSE_FILE="docker-compose.yml"
else
    echo "❌ docker-compose.yml not found!"
    echo "Please navigate to the directory containing docker-compose.yml and run:"
    echo "  docker-compose down && docker-compose build --no-cache && docker-compose up -d"
    exit 1
fi

echo "✅ Found $COMPOSE_FILE"

# Step 6: Stop containers
echo ""
echo "🛑 Stopping Docker containers..."
docker-compose down

# Step 7: Pull latest images
echo ""
echo "📥 Pulling latest Docker images..."
docker-compose pull || echo "⚠️  Pull failed or no images to pull (using local build)"

# Step 8: Rebuild containers
echo ""
echo "🔨 Rebuilding containers from scratch (this may take a few minutes)..."
docker-compose build --no-cache

# Step 9: Start containers
echo ""
echo "🚀 Starting containers..."
docker-compose up -d

# Step 10: Wait for startup
echo ""
echo "⏳ Waiting for services to start (30 seconds)..."
sleep 30

# Step 11: Show logs
echo ""
echo "📋 Backend logs (last 20 lines):"
docker logs invoicextract-app --tail 20

# Step 12: Test API endpoint
echo ""
echo "🧪 Testing API endpoint..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/invoicextract/api/invoices || echo "000")

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo "✅ API endpoint is accessible (requires authentication)"
elif [ "$HTTP_CODE" = "200" ]; then
    echo "✅ API endpoint is accessible"
else
    echo "⚠️  API returned HTTP $HTTP_CODE"
    echo "The backend may still be starting up. Check logs with:"
    echo "  docker logs invoicextract-app -f"
fi

echo ""
echo "✨ Update process complete!"
echo ""
echo "Next steps:"
echo "  1. Verify services are running:"
echo "     docker ps"
echo ""
echo "  2. Check backend logs:"
echo "     docker logs invoicextract-app -f"
echo ""
echo "  3. Run API tests:"
echo "     npm test -- --tags \"@api\""
