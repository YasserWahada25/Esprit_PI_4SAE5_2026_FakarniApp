#!/bin/bash

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 Testing Pipeline Prerequisites"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test 1: Check containers are running
echo "1️⃣  Checking containers..."
if docker ps | grep -q fakarni_jenkins; then
    echo "   ✅ Jenkins is running"
else
    echo "   ❌ Jenkins is NOT running"
    exit 1
fi

if docker ps | grep -q fakarni_sonarqube; then
    echo "   ✅ SonarQube is running"
else
    echo "   ❌ SonarQube is NOT running"
    exit 1
fi

echo ""

# Test 2: Check Jenkins can reach SonarQube
echo "2️⃣  Testing Jenkins → SonarQube connectivity..."
HTTP_CODE=$(docker exec fakarni_jenkins curl -s -o /dev/null -w "%{http_code}" http://172.18.0.2:9000/api/server/version)
if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✅ Jenkins can reach SonarQube (HTTP $HTTP_CODE)"
else
    echo "   ❌ Jenkins cannot reach SonarQube (HTTP $HTTP_CODE)"
    exit 1
fi

echo ""

# Test 3: Check Jenkins configuration
echo "3️⃣  Checking Jenkins SonarQube configuration..."
if docker exec fakarni_jenkins cat /var/jenkins_home/hudson.plugins.sonar.SonarGlobalConfiguration.xml | grep -q "172.18.0.2:9000"; then
    echo "   ✅ SonarQube URL is configured correctly"
else
    echo "   ❌ SonarQube URL is NOT configured correctly"
    exit 1
fi

if docker exec fakarni_jenkins cat /var/jenkins_home/hudson.plugins.sonar.SonarGlobalConfiguration.xml | grep -q "sonarqube-token"; then
    echo "   ✅ SonarQube credential is configured"
else
    echo "   ❌ SonarQube credential is NOT configured"
    exit 1
fi

echo ""

# Test 4: Check credential exists
echo "4️⃣  Checking Jenkins credentials..."
if docker exec fakarni_jenkins cat /var/jenkins_home/credentials.xml | grep -q "sonarqube-token"; then
    echo "   ✅ sonarqube-token credential exists"
else
    echo "   ❌ sonarqube-token credential does NOT exist"
    exit 1
fi

echo ""

# Test 5: Check pom.xml has correct plugin
echo "5️⃣  Checking Maven SonarQube plugin..."
if grep -q "sonar.version" backend/User-Service/pom.xml; then
    echo "   ✅ SonarQube plugin version is defined in pom.xml"
else
    echo "   ❌ SonarQube plugin version is NOT defined in pom.xml"
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ ALL TESTS PASSED!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🎯 Next Step: Re-run your Jenkins pipeline"
echo ""
echo "   1. Open Jenkins: http://localhost:8085"
echo "   2. Go to: user-service-CI job"
echo "   3. Click: Build Now"
echo ""
echo "⚠️  Note: If the pipeline still fails with authentication error,"
echo "   the SonarQube token might be expired. You'll need to:"
echo "   - Generate a new token in SonarQube (http://localhost:9000)"
echo "   - Update the 'sonarqube-token' credential in Jenkins"
echo ""
