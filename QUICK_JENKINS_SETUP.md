# ⚡ QUICK JENKINS SETUP - Copy & Paste Guide

## 🎯 Goal: Create 38 Jenkins Jobs in 30 Minutes

---

## 📋 STEP-BY-STEP CHECKLIST

### ✅ Prerequisites (5 minutes)

```bash
# 1. Start infrastructure
docker-compose up -d jenkins sonarqube eureka-server mongodb

# 2. Wait for Jenkins to start (check logs)
docker logs -f fakarni_jenkins
# Wait for: "Jenkins is fully up and running"

# 3. Access Jenkins
# URL: http://localhost:8085
# Login: admin / admin (or your password)
```

### ✅ Verify Jenkins Configuration (2 minutes)

1. **Go to**: Manage Jenkins → Global Tool Configuration
2. **Verify**:
   - ✅ Maven 3.9 is configured
   - ✅ JDK 21 is configured

3. **Go to**: Manage Jenkins → Credentials
4. **Verify**:
   - ✅ `github-credentials` exists
   - ✅ `dockerhub-credentials` exists

5. **Go to**: Manage Jenkins → Configure System
6. **Verify**:
   - ✅ SonarQube server configured

---

## 🚀 CREATE JOBS - FAST METHOD

### Priority 1: Infrastructure Services (10 minutes)

#### 1. Eureka-Service-CI
```
Name: eureka-service-CI
Type: Pipeline
Build Triggers: ☑️ Poll SCM → H/5 * * * *
Pipeline: Copy from pipelines/ci/eureka-service-CI.groovy
```

#### 2. Eureka-Service-CD
```
Name: eureka-service-CD
Type: Pipeline
Parameters: ☑️ String Parameter
  - Name: IMAGE_TAG
  - Default: latest
Pipeline: Copy from pipelines/cd/eureka-service-CD.groovy
```

#### 3. Gateway-Service-CI
```
Name: gateway-service-CI
Type: Pipeline
Build Triggers: ☑️ Poll SCM → H/5 * * * *
Pipeline: Copy from pipelines/ci/gateway-service-CI.groovy
```

#### 4. Gateway-Service-CD
```
Name: gateway-service-CD
Type: Pipeline
Parameters: ☑️ String Parameter (IMAGE_TAG, default: latest)
Pipeline: Copy from pipelines/cd/gateway-service-CD.groovy
```

---

### Priority 2: Test One Microservice (5 minutes)

#### 5. Tracking-Service-CI
```
Name: tracking-service-CI
Type: Pipeline
Build Triggers: ☑️ Poll SCM → H/5 * * * *
Pipeline: Copy from pipelines/ci/tracking-service-CI.groovy
```

#### 6. Tracking-Service-CD
```
Name: tracking-service-CD
Type: Pipeline
Parameters: ☑️ String Parameter (IMAGE_TAG, default: latest)
Pipeline: Copy from pipelines/cd/tracking-service-CD.groovy
```

---

### Priority 3: Remaining Services (15 minutes)

**Repeat the same pattern for:**

7-8. geofencing-service (CI + CD)
9-10. chat-service (CI + CD)
11-12. event-service (CI + CD)
13-14. post-service (CI + CD)
15-16. notification-service (CI + CD)
17-18. paiement-service (CI + CD)
19-20. pharmacie-service (CI + CD)
21-22. rendez-vous-service (CI + CD)
23-24. dossier-medical-service (CI + CD)
25-26. detection-maladie-service (CI + CD)
27-28. activite-educative-service (CI + CD)
29-30. video-service (CI + CD)
31-32. session-service (CI + CD)
33-34. suivi-engagement-service (CI + CD)
35-36. meeting-insights-service (CI + CD)
37-38. group-service (CI + CD)

---

## 📝 JOB CREATION TEMPLATE

### For CI Jobs:
```
1. Click "New Item"
2. Name: {service-name}-CI
3. Type: Pipeline
4. Click OK

Configuration:
- Description: CI Pipeline for {Service-Name}
- Build Triggers:
  ☑️ Poll SCM
  Schedule: H/5 * * * *
  
- Pipeline:
  Definition: Pipeline script
  Script: [Copy from pipelines/ci/{service-name}-CI.groovy]
  
5. Click Save
```

### For CD Jobs:
```
1. Click "New Item"
2. Name: {service-name}-CD
3. Type: Pipeline
4. Click OK

Configuration:
- Description: CD Pipeline for {Service-Name}
- ☑️ This project is parameterized
  Add Parameter: String Parameter
    Name: IMAGE_TAG
    Default Value: latest
    
- Pipeline:
  Definition: Pipeline script
  Script: [Copy from pipelines/cd/{service-name}-CD.groovy]
  
5. Click Save
```

---

## 🧪 TESTING PHASE

### Test 1: Eureka Service (5 minutes)
```bash
# 1. In Jenkins, click "Build Now" on eureka-service-CI
# 2. Watch the pipeline stages execute
# 3. Verify CD is triggered automatically
# 4. Check container is running:
docker ps | grep fakarni_eureka_service

# 5. Check health:
curl http://localhost:8762/actuator/health
```

### Test 2: Gateway Service (5 minutes)
```bash
# 1. Build gateway-service-CI
# 2. Wait for CD to complete
# 3. Check container:
docker ps | grep fakarni_gateway_service

# 4. Check health:
curl http://localhost:8080/actuator/health
```

### Test 3: One Microservice (5 minutes)
```bash
# 1. Build tracking-service-CI
# 2. Wait for CD to complete
# 3. Check container:
docker ps | grep fakarni_tracking_service

# 4. Check health:
curl http://localhost:9011/actuator/health
```

### Test 4: Batch Build All (10 minutes)
```bash
# In Jenkins, trigger all CI jobs
# Monitor for failures
# Fix any issues
```

---

## 🎯 QUICK VERIFICATION COMMANDS

### Check All Containers:
```bash
docker ps --filter "name=fakarni_" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### Count Running Services:
```bash
docker ps --filter "name=fakarni_" | wc -l
```

### Check All Health Endpoints:
```bash
# Eureka
curl -s http://localhost:8762/actuator/health | jq .

# Gateway
curl -s http://localhost:8080/actuator/health | jq .

# Tracking
curl -s http://localhost:9011/actuator/health | jq .
```

### Check Eureka Dashboard:
```bash
# Open in browser
start http://localhost:8762

# Should show all registered services
```

### Check SonarQube Projects:
```bash
# Open in browser
start http://localhost:9000

# Should show all analyzed projects
```

### Check Docker Hub Images:
```bash
# List local images
docker images | grep fakarni

# Should show all built images with build numbers
```

---

## 🐛 COMMON ISSUES & QUICK FIXES

### Issue: Jenkins job fails at checkout
```
Fix: Verify github-credentials in Jenkins
Manage Jenkins → Credentials → github-credentials
```

### Issue: Docker build fails
```bash
# Check if Dockerfile exists
ls backend/{Service-Name}/Dockerfile

# Test build manually
cd backend/{Service-Name}
docker build -t test .
```

### Issue: SonarQube connection fails
```
Fix: Verify SonarQube is running
docker ps | grep sonarqube

Fix: Check SonarQube configuration in Jenkins
Manage Jenkins → Configure System → SonarQube servers
```

### Issue: Docker push fails
```
Fix: Verify dockerhub-credentials
Manage Jenkins → Credentials → dockerhub-credentials
Username: didou2505
Password: [your Docker Hub password]
```

### Issue: CD not triggered
```
Fix: Verify CD job name matches in CI pipeline
CI pipeline should trigger: {service-name}-CD
CD job name should be: {service-name}-CD
```

### Issue: Health check fails
```bash
# Check container logs
docker logs fakarni_{service}_service

# Check if Eureka is running
docker ps | grep eureka

# Check if MongoDB is running
docker ps | grep mongodb
```

---

## 📊 SUCCESS CHECKLIST

After creating all jobs:

- [ ] 38 jobs visible in Jenkins dashboard
- [ ] All CI jobs have green checkmark (or ready to build)
- [ ] All CD jobs have parameter configured
- [ ] Eureka service deployed and healthy
- [ ] Gateway service deployed and healthy
- [ ] At least one microservice deployed and healthy
- [ ] Docker images in Docker Hub
- [ ] SonarQube projects created
- [ ] All containers running: `docker ps | grep fakarni_`

---

## 🚀 FINAL DEPLOYMENT

### Deploy All Services at Once:
```bash
# In Jenkins, select all CI jobs
# Click "Build" on each one
# Or use Jenkins CLI to trigger all:

for service in tracking geofencing chat event post notification paiement pharmacie rendez-vous dossier-medical detection-maladie activite-educative video session suivi-engagement meeting-insights group; do
  echo "Building $service-service-CI"
  # Trigger via Jenkins UI or CLI
done
```

### Monitor Deployment:
```bash
# Watch containers start
watch -n 2 'docker ps --filter "name=fakarni_" --format "table {{.Names}}\t{{.Status}}"'

# Check Eureka dashboard
start http://localhost:8762
```

---

## 🎉 COMPLETION

When all done, you should have:

✅ **38 Jenkins Jobs** (19 CI + 19 CD)
✅ **19 Docker Containers** running
✅ **19 Services** registered in Eureka
✅ **19 Docker Images** in Docker Hub
✅ **19 SonarQube Projects** with analysis
✅ **All Health Checks** passing

---

**⏱️ Total Time: ~30-45 minutes**
**🎯 Next Phase: Kubernetes Deployment**

