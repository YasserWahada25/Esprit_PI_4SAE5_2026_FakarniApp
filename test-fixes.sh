#!/bin/bash

echo "================================"
echo "Testing Fakarni App Fixes"
echo "================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Check if all containers are running
echo "Test 1: Checking container status..."
RUNNING=$(docker compose ps --format json | jq -r '.State' | grep -c "running")
TOTAL=29

if [ "$RUNNING" -eq "$TOTAL" ]; then
    echo -e "${GREEN}✅ All $TOTAL containers are running${NC}"
else
    echo -e "${RED}❌ Only $RUNNING/$TOTAL containers are running${NC}"
fi
echo ""

# Test 2: Check if Gateway is accessible
echo "Test 2: Checking Gateway accessibility..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/actuator/health | grep -q "200"; then
    echo -e "${GREEN}✅ Gateway is accessible${NC}"
else
    echo -e "${RED}❌ Gateway is not accessible${NC}"
fi
echo ""

# Test 3: Check if Eureka is accessible
echo "Test 3: Checking Eureka Dashboard..."
if curl -s http://localhost:8762 | grep -q "Eureka"; then
    echo -e "${GREEN}✅ Eureka Dashboard is accessible${NC}"
else
    echo -e "${RED}❌ Eureka Dashboard is not accessible${NC}"
fi
echo ""

# Test 4: Check if Frontend is accessible
echo "Test 4: Checking Frontend..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:4200 | grep -q "200"; then
    echo -e "${GREEN}✅ Frontend is accessible${NC}"
else
    echo -e "${RED}❌ Frontend is not accessible${NC}"
fi
echo ""

# Test 5: Check if activite-educative-service is registered
echo "Test 5: Checking service registration..."
if curl -s http://localhost:8762/eureka/apps | grep -q "ACTIVITE-EDUCATIVE-SERVICE"; then
    echo -e "${GREEN}✅ Activite Educative Service is registered${NC}"
else
    echo -e "${YELLOW}⚠️  Activite Educative Service not yet registered (may need more time)${NC}"
fi
echo ""

# Test 6: Check if detection-maladie-service is registered
if curl -s http://localhost:8762/eureka/apps | grep -q "DETECTION-MALADIE-SERVICE"; then
    echo -e "${GREEN}✅ Detection Maladie Service is registered${NC}"
else
    echo -e "${YELLOW}⚠️  Detection Maladie Service not yet registered (may need more time)${NC}"
fi
echo ""

echo "================================"
echo "Test Summary"
echo "================================"
echo ""
echo "Next steps:"
echo "1. Open http://localhost:4200 in your browser"
echo "2. Login as aidant to test geofencing maps"
echo "3. Navigate to Educational Events to test activity images"
echo "4. Create a post with an image to test post images"
echo "5. Go to Medical → Detection to test detection service"
echo ""
echo "For detailed testing instructions, see DEPLOYMENT-COMPLETE.md"
echo ""
