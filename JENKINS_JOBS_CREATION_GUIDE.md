# 🚀 Jenkins Jobs Creation Guide - ALL SERVICES

## ✅ What We Have Done

1. ✅ **Generated 38 Pipeline Files**:
   - 19 CI Pipelines in `pipelines/ci/`
   - 19 CD Pipelines in `pipelines/cd/`

2. ✅ **Services Covered** (19 total):
   - tracking-service
   - geofencing-service
   - chat-service
   - event-service
   - post-service
   - notification-service
   - paiement-service
   - pharmacie-service
   - rendez-vous-service
   - dossier-medical-service
   - detection-maladie-service
   - activite-educative-service
   - video-service
   - eureka-service
   - gateway-service
   - session-service
   - suivi-engagement-service
   - meeting-insights-service
   - group-service

---

## 🎯 Quick Start - Create All Jobs in Jenkins

### Option 1: Manual Creation (Recommended for First Time)

#### Step 1: Access Jenkins
```
URL: http://localhost:8085
```

#### Step 2: Create CI Jobs (19 jobs)

For each service, follow these steps:

1. **Click**: "New Item"
2. **Enter name**: `{service-name}-CI` (e.g., `tracking-service-CI`)
3. **Select**: "Pipeline"
4. **Click**: "OK"

5. **Configure**:
   - **Description**: `CI Pipeline for {Service-Name}`
   - **Build Triggers**: 
     - ☑️ Poll SCM
     - Schedule: `H/5 * * * *`
   - **Pipeline**:
     - Definition: `Pipeline script`
     - Script: Copy from `pipelines/ci/{service-name}-CI.groovy`

6. **Click**: "Save"

#### Step 3: Create CD Jobs (19 jobs)

For each service:

1. **Click**: "New Item"
2. **Enter name**: `{service-name}-CD` (e.g., `tracking-service-CD`)
3. **Select**: "Pipeline"
4. **Click**: "OK"

5. **Configure**:
   - **Description**: `CD Pipeline for {Service-Name}`
   - **This project is parameterized**: ☑️
     - Add Parameter: **String Parameter**
       - Name: `IMAGE_TAG`
       - Default Value: `latest`
   - **Pipeline**:
     - Definition: `Pipeline script`
     - Script: Copy from `pipelines/cd/{service-name}-CD.groovy`

6. **Click**: "Save"

---

### Option 2: Automated Creation (Using Jenkins CLI)

#### Prerequisites:
```bash
# Download Jenkins CLI
curl -O http://localhost:8085/jnlpJars/jenkins-cli.jar

# Test connection
java -jar jenkins-cli.jar -s http://localhost:8085/ -auth admin:admin who-am-i
```

#### Create All Jobs:
```bash
# Run the automated script
python pipelines/create-jenkins-jobs.py
```

---

## 📋 Services Priority Order

### Phase 1: Infrastructure (MUST BE FIRST)
1. ✅ **eureka-service** - Service Discovery
2. ✅ **gateway-service** - API Gateway

### Phase 2: Core Services
3. ✅ **user-service** (Already done)
4. ✅ **tracking-service**
5. ✅ **geofencing-service**
6. ✅ **chat-service**

### Phase 3: Business Services
7. ✅ **event-service**
8. ✅ **post-service**
9. ✅ **notification-service**
10. ✅ **paiement-service**
11. ✅ **pharmacie-service**
12. ✅ **rendez-vous-service**
13. ✅ **dossier-medical-service**
14. ✅ **detection-maladie-service**
15. ✅ **activite-educative-service**
16. ✅ **video-service**

### Phase 4: Additional Services
17. ✅ **session-service**
18. ✅ **suivi-engagement-service**
19. ✅ **meeting-insights-service**
20. ✅ **group-service**

---

## 🧪 Testing Strategy

### Test 1: Infrastructure Services First
```bash
# Start Eureka
# In Jenkins: Build eureka-service-CI

# Wait for completion, then start Gateway
# In Jenkins: Build gateway-service-CI
```

### Test 2: Test One Microservice
```bash
# Pick any service (e.g., tracking-service)
# In Jenkins: Build tracking-service-CI

# Verify:
# 1. CI completes successfully
# 2. CD is triggered automatically
# 3. Container is deployed
# 4. Health check passes
```

### Test 3: Batch Test All Services
```bash
# Trigger all CI pipelines at once
# Monitor Jenkins dashboard for failures
```

---

## 🔍 Verification Checklist

### For Each Service:

#### CI Pipeline:
- [ ] Checkout succeeds
- [ ] Build completes
- [ ] Tests pass
- [ ] JaCoCo coverage generated
- [ ] SonarQube analysis completes
- [ ] JAR file created
- [ ] Docker image built
- [ ] Image pushed to Docker Hub
- [ ] CD pipeline triggered

#### CD Pipeline:
- [ ] Image pulled from Docker Hub
- [ ] Old container stopped
- [ ] New container deployed
- [ ] Container is running
- [ ] Health check passes (HTTP 200)
- [ ] Service registered in Eureka

#### Docker Verification:
```bash
# Check container is running
docker ps | grep fakarni_{service}

# Check logs
docker logs fakarni_{service}_service

# Test health endpoint
curl http://localhost:{port}/actuator/health
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "Job already exists"
**Solution**: Job name must be unique. Use exact names from the guide.

### Issue 2: Pipeline script too long
**Solution**: 
1. Use "Pipeline script from SCM" instead
2. Or keep using "Pipeline script" (Jenkins supports large scripts)

### Issue 3: Docker build fails
**Solution**:
```bash
# Check if Dockerfile exists
ls backend/{Service-Name}/Dockerfile

# Test build manually
cd backend/{Service-Name}
docker build -t test .
```

### Issue 4: SonarQube connection fails
**Solution**:
```bash
# Check SonarQube is running
docker ps | grep sonarqube

# Check SonarQube URL in Jenkins
# Manage Jenkins → Configure System → SonarQube servers
```

### Issue 5: Port already in use
**Solution**:
```bash
# Find and stop conflicting container
docker ps | grep {port}
docker stop {container_name}
```

---

## 📊 Monitoring All Services

### Jenkins Dashboard
```
http://localhost:8085
```

### SonarQube Dashboard
```
http://localhost:9000
```

### Eureka Dashboard
```
http://localhost:8762
```

### Check All Containers
```bash
# List all Fakarni containers
docker ps --filter "name=fakarni_" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Count running services
docker ps --filter "name=fakarni_" | wc -l
```

### Check All Health Endpoints
```bash
# Create a script to check all services
for port in 8081 9011 9012 8070 8086 8084 8083 8087 8088 8089 8090 8091 8092 8093 8094 8095 8096 8097; do
  echo "Port $port: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:$port/actuator/health)"
done
```

---

## 🎯 Success Criteria

### All Services Deployed:
- [ ] 19 CI pipelines created in Jenkins
- [ ] 19 CD pipelines created in Jenkins
- [ ] All CI pipelines run successfully
- [ ] All CD pipelines run successfully
- [ ] All containers running
- [ ] All health checks passing
- [ ] All services registered in Eureka
- [ ] Gateway can route to all services

### SonarQube:
- [ ] 19 projects created in SonarQube
- [ ] All projects have code coverage data
- [ ] All quality gates configured

### Docker Hub:
- [ ] 19 repositories created
- [ ] All images pushed successfully
- [ ] Images tagged with build numbers

---

## 📈 Next Steps After All Jobs Created

### 1. Setup Monitoring
- Deploy Prometheus
- Deploy Grafana
- Configure dashboards

### 2. Setup Notifications
- Configure email notifications
- Configure Slack notifications
- Configure webhook notifications

### 3. Kubernetes Deployment
- Create Kubernetes manifests
- Deploy to AKS or Minikube
- Configure ingress

### 4. Production Readiness
- Add deployment approvals
- Add rollback mechanisms
- Add blue-green deployment
- Add canary deployment

---

## 🚀 Quick Commands Reference

### Start All Infrastructure:
```bash
docker-compose up -d mongodb mysql eureka-server gateway sonarqube jenkins
```

### Check Jenkins:
```bash
docker logs -f fakarni_jenkins
```

### Check SonarQube:
```bash
docker logs -f fakarni_sonarqube
```

### Restart Jenkins:
```bash
docker-compose restart jenkins
```

### Clean Up All Containers:
```bash
docker stop $(docker ps -q --filter "name=fakarni_")
docker rm $(docker ps -aq --filter "name=fakarni_")
```

---

## 📞 Need Help?

### Check Logs:
```bash
# Jenkins logs
docker logs fakarni_jenkins

# Service logs
docker logs fakarni_{service}_service

# All logs
docker-compose logs -f
```

### Verify Configuration:
```bash
# Check network
docker network ls | grep fakarni-net

# Check volumes
docker volume ls | grep fakarni

# Check images
docker images | grep fakarni
```

---

**🎉 You're ready to create all Jenkins jobs! Start with infrastructure services (Eureka, Gateway) then proceed with the rest.**

