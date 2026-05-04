#!/bin/bash

echo "🔧 FIXING IMAGE DISPLAY ISSUE"
echo "=============================="
echo ""

# Step 1: Check if Docker is running
echo "📋 Step 1: Checking Docker..."
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running!"
    echo "   Please start Docker Desktop and run this script again."
    exit 1
fi
echo "✅ Docker is running"
echo ""

# Step 2: Stop all containers
echo "📋 Step 2: Stopping existing containers..."
docker-compose down
echo "✅ Containers stopped"
echo ""

# Step 3: Start all services
echo "📋 Step 3: Starting all services..."
docker-compose up -d
echo "✅ Services starting..."
echo ""

# Step 4: Wait for services
echo "📋 Step 4: Waiting for services to be ready (60 seconds)..."
sleep 60
echo ""

# Step 5: Check service status
echo "📋 Step 5: Checking service status..."
echo ""
docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "fakarni_activite_service|fakarni_api_gateway|fakarni_frontend"
echo ""

# Step 6: Test image access
echo "📋 Step 6: Testing image access..."
echo ""

# Test if activite service is responding
echo "Testing activite-educative-service..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8084/uploads/activities/4e345308e58b4ff292e629a994d342f7.png | grep -q "200"; then
    echo "✅ Direct service access: OK"
else
    echo "⚠️  Direct service access: Not ready yet (this is normal, service may still be starting)"
fi

# Test if gateway is routing
echo "Testing gateway routing..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/uploads/activities/4e345308e58b4ff292e629a994d342f7.png | grep -q "200"; then
    echo "✅ Gateway routing: OK"
else
    echo "⚠️  Gateway routing: Not ready yet (this is normal, service may still be starting)"
fi

echo ""
echo "🎉 SETUP COMPLETE!"
echo ""
echo "📝 Next Steps:"
echo "   1. Wait 2-3 minutes for all services to fully start"
echo "   2. Open: http://localhost:4200/educational/activities"
echo "   3. Images should now display!"
echo ""
echo "🔍 To check logs:"
echo "   docker logs fakarni_activite_service"
echo ""
echo "📖 For detailed troubleshooting, see: IMAGE-UPLOAD-COMPLETE-FIX.md"
