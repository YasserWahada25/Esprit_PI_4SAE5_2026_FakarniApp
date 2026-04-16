#!/bin/bash

echo "=== Test Post-Service Authentication ==="
echo ""

# 1. Login to get token
echo "1. Logging in to get token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}')

echo "Login response: $LOGIN_RESPONSE"
echo ""

# Extract token (assuming the response has accessToken field)
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Failed to get token. Please check if user exists and credentials are correct."
    exit 1
fi

echo "✅ Token obtained: ${TOKEN:0:50}..."
echo ""

# 2. Test Post-Service directly (bypass Gateway)
echo "2. Testing Post-Service directly (port 8069)..."
DIRECT_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET http://localhost:8069/api/posts \
  -H "Authorization: Bearer $TOKEN")

HTTP_STATUS=$(echo "$DIRECT_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
RESPONSE_BODY=$(echo "$DIRECT_RESPONSE" | sed '/HTTP_STATUS/d')

echo "HTTP Status: $HTTP_STATUS"
echo "Response: $RESPONSE_BODY"
echo ""

# 3. Test via Gateway
echo "3. Testing via Gateway (port 8090)..."
GATEWAY_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET http://localhost:8090/api/posts \
  -H "Authorization: Bearer $TOKEN")

HTTP_STATUS=$(echo "$GATEWAY_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
RESPONSE_BODY=$(echo "$GATEWAY_RESPONSE" | sed '/HTTP_STATUS/d')

echo "HTTP Status: $HTTP_STATUS"
echo "Response: $RESPONSE_BODY"
echo ""

# 4. Decode JWT to check content
echo "4. JWT Token Content:"
echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | python3 -m json.tool 2>/dev/null || echo "Could not decode token"
echo ""

echo "=== Test Complete ==="
