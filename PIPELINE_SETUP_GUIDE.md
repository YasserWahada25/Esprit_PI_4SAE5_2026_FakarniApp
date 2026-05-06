# 🚀 Complete CI/CD Pipeline Setup Guide

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [CI/CD Architecture](#cicd-architecture)
3. [Setup Steps](#setup-steps)
4. [Create Pipelines](#create-pipelines)
5. [Test Pipelines](#test-pipelines)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 Prerequisites

### 1. Verify Docker Network
```bash
# Check if network exists
docker network ls | grep fakarni-net

# If not, create it
docker network create fakarni-net
```

### 2. Verify Jenkins Docker Access
```bash
# Test if Jenkins can run Docker commands
docker exec fakarni_jenkins docker ps

# If this fails, Jenkins needs Docker socket access
```

### 3. Start Required Infrastructure
```bash
# Start MongoDB and Eureka (required for User Service)
docker-compose up -d mongodb eureka-server

# Verify they're running
docker ps | grep -E "mongodb|eureka"
```

### 4. Verify Jenkins is Running
```bash
# Check Jenkins status
docker ps | grep jenkins

# Access Jenkins UI
# URL: http://localhost:8085
```

---

## 🏗️ CI/CD Architecture

### **CI Pipeline (Continuous Integration)**
```
┌─────────────────────────────────────────────────────────┐
│                    CI PIPELINE                          │
├─────────────────────────────────────────────────────────┤
│ 1. 📥 Checkout       → Clone code from GitHub          │
│ 2. 🔨 Build          → Maven compile                    │
│ 3. 🧪 Test           → Unit tests + JaCoCo coverage     │
│ 4. 📊 SonarQube      → Code quality analysis            │
│ 5. 🚦 Quality Gate   → Pass/Fail decision               │
│ 6. 📦 Package        → Create JAR file                  │
│ 7. 🐳 Docker Build   → Build Docker image               │
│ 8. 📤 Docker Push    → Push to Docker Hub ← ARTIFACT    │
│ 9. 🚀 Trigger CD     → Start deployment                 │
└─────────────────────────────────────────────────────────┘
```

### **CD Pipeline (Continuous Deployment)**
```
┌─────────────────────────────────────────────────────────┐
│                    CD PIPELINE                          │
├─────────────────────────────────────────────────────────┤
│ 1. 📥 Pull Image     → Get from Docker Hub              │
│ 2. 🛑 Stop Old       → Stop existing container          │
│ 3. 🚀 Deploy         → Run new container                │
│ 4. ⏳ Wait           → Allow startup time                │
│ 5. 🏥 Health Check   → Verify service is healthy        │
│ 6. 📊 Status         → Show container info              │
│ 7. 🧹 Cleanup        → Remove old images                │
└─────────────────────────────────────────────────────────┘
```

### **Why This Separation?**

**CI (Build & Test)**:
- ✅ Produces a **versioned artifact** (Docker image)
- ✅ Artifact is stored in **Docker Hub** (registry)
- ✅ Can be deployed to **multiple environments**
- ✅ **One build**, many deployments

**CD (Deploy)**:
- ✅ **Pulls** the artifact (doesn't build)
- ✅ Can deploy to **dev, staging, prod**
- ✅ Fast deployment (no build time)
- ✅ **Rollback** by deploying older image tag

---

## 🛠️ Setup Steps

### Step 1: Create CI Pipeline

1. **Go to Jenkins**: http://localhost:8085
2. **Click**: "New Item"
3. **Enter name**: `user-service-CI`
4. **Select**: "Pipeline"
5. **Click**: "OK"

#### Configure CI Pipeline:

**General Section**:
- Description: `CI Pipeline for User Service - Build, Test, SonarQube, Docker Build & Push`

**Build Triggers**:
- ☑️ **Poll SCM**
- Schedule: `H/5 * * * *` (checks GitHub every 5 minutes)

**Pipeline Section**:
- Definition: `Pipeline script`
- Script: **Copy from `PIPELINE_USER_SERVICE_CI.groovy`**

**Click**: "Save"

---

### Step 2: Create CD Pipeline

1. **Go to Jenkins Dashboard**
2. **Click**: "New Item"
3. **Enter name**: `user-service-CD`
4. **Select**: "Pipeline"
5. **Click**: "OK"

#### Configure CD Pipeline:

**General Section**:
- Description: `CD Pipeline for User Service - Pull Image & Deploy`
- ☑️ **This project is parameterized**
  - Add Parameter: **String Parameter**
    - Name: `IMAGE_TAG`
    - Default Value: `latest`
    - Description: `Docker image tag to deploy`

**Pipeline Section**:
- Definition: `Pipeline script`
- Script: **Copy from `PIPELINE_USER_SERVICE_CD.groovy`**

**Click**: "Save"

---

## 🧪 Test Pipelines

### Test 1: Manual CI Trigger

1. Go to `user-service-CI` pipeline
2. Click **"Build Now"**
3. Watch the stages execute:
   - ✅ Checkout
   - ✅ Build
   - ✅ Test
   - ✅ SonarQube Analysis
   - ✅ Quality Gate
   - ✅ Package
   - ✅ Docker Build
   - ✅ Docker Push
   - ✅ Trigger CD

4. **Expected Result**: 
   - CI completes successfully
   - CD pipeline starts automatically
   - Docker image pushed to Docker Hub

### Test 2: Verify CD Execution

1. Go to `user-service-CD` pipeline
2. You should see it running (triggered by CI)
3. Watch the stages:
   - ✅ Pull Docker Image
   - ✅ Stop Old Container
   - ✅ Deploy New Container
   - ✅ Wait for Startup
   - ✅ Health Check
   - ✅ Container Status
   - ✅ Cleanup

4. **Expected Result**:
   - Container deployed successfully
   - Health check passes (HTTP 200)
   - Service accessible on port 8081

### Test 3: Verify Deployment

```bash
# Check if container is running
docker ps | grep fakarni_user_service

# Check container logs
docker logs fakarni_user_service

# Test health endpoint
curl http://localhost:8081/actuator/health

# Expected response:
# {"status":"UP"}
```

### Test 4: Automatic Trigger (Git Push)

1. Make a small change in code:
```bash
# Edit a file
echo "// Test CI/CD" >> backend/User-Service/src/main/java/com/example/Test.java

# Commit and push
git add .
git commit -m "test: trigger CI/CD pipeline"
git push origin main
```

2. Wait 5 minutes (Poll SCM interval)
3. Jenkins will automatically:
   - Detect the change
   - Trigger CI pipeline
   - CI triggers CD pipeline
   - Service gets redeployed

---

## 🔍 Monitoring

### Jenkins Dashboard
- **CI Pipeline**: http://localhost:8085/job/user-service-CI/
- **CD Pipeline**: http://localhost:8085/job/user-service-CD/

### SonarQube
- **URL**: http://localhost:9000
- **Project**: user-service

### Docker Hub
- **Repository**: https://hub.docker.com/r/didou2505/fakarni-user-service

### Service Health
- **Health Check**: http://localhost:8081/actuator/health
- **Eureka Dashboard**: http://localhost:8762

---

## 🐛 Troubleshooting

### Issue 1: Jenkins Can't Access Docker

**Symptom**: `docker: command not found` in pipeline

**Solution**:
```bash
# Stop Jenkins
docker-compose down jenkins

# Restart with Docker socket mounted
docker-compose up -d jenkins

# Verify
docker exec fakarni_jenkins docker ps
```

### Issue 2: Network Not Found

**Symptom**: `network fakarni-net not found`

**Solution**:
```bash
# Create network
docker network create fakarni-net

# Verify
docker network ls | grep fakarni-net
```

### Issue 3: Quality Gate Fails

**Symptom**: Pipeline stops at Quality Gate stage

**Solution**:
1. Go to SonarQube: http://localhost:9000
2. Login (admin/admin)
3. Check project quality gate settings
4. Adjust thresholds if needed
5. Or temporarily disable: Remove Quality Gate stage from pipeline

### Issue 4: Health Check Fails

**Symptom**: Health check returns 000 or 404

**Solution**:
```bash
# Check if container is running
docker ps | grep fakarni_user_service

# Check container logs
docker logs fakarni_user_service

# Check if Eureka is running
docker ps | grep eureka

# Check if MongoDB is running
docker ps | grep mongodb

# Restart dependencies
docker-compose up -d mongodb eureka-server
```

### Issue 5: Port Already in Use

**Symptom**: `port 8081 is already allocated`

**Solution**:
```bash
# Find what's using the port
netstat -ano | findstr :8081

# Stop the old container
docker stop fakarni_user_service
docker rm fakarni_user_service

# Or kill the process using the port
taskkill /PID <PID> /F
```

### Issue 6: Docker Push Fails

**Symptom**: `unauthorized: authentication required`

**Solution**:
1. Verify Docker Hub credentials in Jenkins
2. Go to: Manage Jenkins → Credentials
3. Check `dockerhub-credentials` exists
4. Username: `didou2505`
5. Password: (your Docker Hub password)

### Issue 7: CD Not Triggered

**Symptom**: CI completes but CD doesn't start

**Solution**:
1. Check if `user-service-CD` pipeline exists
2. Check CI pipeline logs for trigger errors
3. Manually trigger CD to test:
   - Go to `user-service-CD`
   - Click "Build with Parameters"
   - IMAGE_TAG: `latest`
   - Click "Build"

---

## 📊 Pipeline Logs

### View CI Logs
```bash
# Jenkins UI
http://localhost:8085/job/user-service-CI/lastBuild/console

# Or via Docker
docker logs fakarni_jenkins
```

### View CD Logs
```bash
# Jenkins UI
http://localhost:8085/job/user-service-CD/lastBuild/console
```

### View Container Logs
```bash
# User Service logs
docker logs -f fakarni_user_service

# Last 100 lines
docker logs --tail 100 fakarni_user_service
```

---

## ✅ Success Criteria

### CI Pipeline Success:
- ✅ All tests pass
- ✅ Code coverage > 0%
- ✅ SonarQube analysis completes
- ✅ Quality Gate passes
- ✅ Docker image built
- ✅ Image pushed to Docker Hub
- ✅ CD pipeline triggered

### CD Pipeline Success:
- ✅ Image pulled from Docker Hub
- ✅ Old container stopped
- ✅ New container deployed
- ✅ Health check returns HTTP 200
- ✅ Service registered in Eureka
- ✅ Service accessible on port 8081

---

## 🎯 Next Steps

After User Service pipelines work:

1. **Replicate for other services**:
   - Copy CI/CD scripts
   - Change SERVICE_NAME, SERVICE_PATH, ports
   - Create new pipelines in Jenkins

2. **Add more services**:
   - Tracking Service (port 9011)
   - Geofencing Service (port 9012)
   - Chat Service (port 8070)
   - etc.

3. **Enhance pipelines**:
   - Add email notifications
   - Add Slack notifications
   - Add deployment approvals
   - Add rollback mechanism

---

## 📚 Reference

### Pipeline Files:
- `PIPELINE_USER_SERVICE_CI.groovy` - CI Pipeline script
- `PIPELINE_USER_SERVICE_CD.groovy` - CD Pipeline script

### Documentation:
- `CICD_SETUP_GUIDE.md` - Complete CI/CD setup
- `JENKINS_CREDENTIALS_COMPLETE_LIST.md` - All credentials

### Ports:
- Jenkins: 8085
- SonarQube: 9000
- User Service: 8081
- Eureka: 8762
- MongoDB: 27018

---

**🎉 You're ready to create the pipelines! Follow the steps above and let me know if you encounter any issues.**
