#!/bin/bash

# SonarQube Token Setup Script
# This script helps you generate a SonarQube token for Jenkins

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔐 SonarQube Token Setup for Jenkins"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if SonarQube is running
if ! docker ps | grep -q fakarni_sonarqube; then
    echo "❌ SonarQube container is not running!"
    echo "   Start it with: docker start fakarni_sonarqube"
    exit 1
fi

echo "✅ SonarQube is running"
echo ""

# Wait for SonarQube to be ready
echo "⏳ Waiting for SonarQube to be ready..."
for i in {1..30}; do
    if docker exec fakarni_sonarqube curl -s http://localhost:9000/api/server/version > /dev/null 2>&1; then
        echo "✅ SonarQube is ready!"
        break
    fi
    echo "   Attempt $i/30..."
    sleep 2
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 STEP 1: Generate SonarQube Token"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Open SonarQube in your browser:"
echo "   👉 http://localhost:9000"
echo ""
echo "2. Login with default credentials:"
echo "   Username: admin"
echo "   Password: admin"
echo "   (You may be asked to change the password)"
echo ""
echo "3. Generate a token:"
echo "   - Click on your profile (top right) → My Account"
echo "   - Go to: Security tab"
echo "   - Under 'Generate Tokens':"
echo "     • Name: jenkins"
echo "     • Type: Global Analysis Token"
echo "     • Expires in: No expiration (or 90 days)"
echo "   - Click 'Generate'"
echo "   - COPY THE TOKEN (you won't see it again!)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 STEP 2: Add Token to Jenkins"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Open Jenkins:"
echo "   👉 http://localhost:8085"
echo ""
echo "2. Add the token as a credential:"
echo "   - Go to: Manage Jenkins → Credentials"
echo "   - Click: (global) domain"
echo "   - Click: Add Credentials"
echo "   - Fill in:"
echo "     • Kind: Secret text"
echo "     • Scope: Global"
echo "     • Secret: [PASTE YOUR SONARQUBE TOKEN]"
echo "     • ID: sonarqube-token"
echo "     • Description: SonarQube Authentication Token"
echo "   - Click: Create"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 STEP 3: Configure SonarQube Server in Jenkins"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. In Jenkins, go to: Manage Jenkins → System"
echo ""
echo "2. Find: SonarQube servers section"
echo ""
echo "3. Configure (or verify):"
echo "   • Name: SonarQube"
echo "   • Server URL: http://fakarni_sonarqube:9000"
echo "   • Server authentication token: sonarqube-token"
echo ""
echo "4. Click: Save"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 STEP 4: Re-run Your Pipeline"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Your pipeline should now work! 🎉"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Need help? Check these URLs:"
echo "  • SonarQube: http://localhost:9000"
echo "  • Jenkins: http://localhost:8085"
echo ""
