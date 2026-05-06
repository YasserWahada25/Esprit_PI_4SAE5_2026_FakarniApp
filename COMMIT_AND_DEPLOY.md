# ⚡ COMMIT & DEPLOY - Quick Action Guide

## 🎯 Current Status: 75% Complete

✅ JaCoCo configured
✅ SonarQube ready
✅ 38 Jenkinsfiles generated
⏳ **NEXT: Commit to Git and create Jenkins jobs**

---

## 🚀 STEP 1: COMMIT JENKINSFILES (5 minutes)

### Copy and paste these commands:

```bash
# Navigate to project root
cd C:\Users\jbili\OneDrive\Bureau\Fakarni_App

# Check what will be committed
git status

# Add all Jenkinsfiles
git add backend/*/Jenkinsfile backend/*/Jenkinsfile.cd

# Verify files are staged
git status

# Commit
git commit -m "feat: add CI/CD Jenkinsfiles for all 19 microservices

- Add Jenkinsfile (CI) to each service
- Add Jenkinsfile.cd (CD) to each service
- Configure Maven, JaCoCo, SonarQube, Docker
- Setup automated deployment pipeline
- Total: 38 Jenkinsfiles for 19 services"

# Push to GitHub
git push origin main
```

### Verify on GitHub:
```
https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp/tree/main/backend
```

Check that each service has:
- ✅ Jenkinsfile
- ✅ Jenkinsfile.cd

---

## 🚀 STEP 2: CREATE JENKINS JOBS (30-45 minutes)

### A. Start with Infrastructure Services

#### 1. Eureka-Service-CI

**Jenkins UI:**
1. Click "New Item"
2. Name: `eureka-service-CI`
3. Type: Pipeline → OK

**Configuration:**
```
Description: CI Pipeline for Eureka Service Discovery

Build Triggers:
☑️ Poll SCM
Schedule: H/5 * * * *

Pipeline:
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
Credentials: github-credentials
Branch: */main
Script Path: backend/Eureka-Service/Jenkinsfile
```

Click **Save**

---

#### 2. Eureka-Service-CD

**Jenkins UI:**
1. Click "New Item"
2. Name: `eureka-service-CD`
3. Type: Pipeline → OK

**Configuration:**
```
Description: CD Pipeline for Eureka Service Discovery

☑️ This project is parameterized
Add Parameter: String Parameter
  Name: IMAGE_TAG
  Default Value: latest

Pipeline:
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
Credentials: github-credentials
Branch: */main
Script Path: backend/Eureka-Service/Jenkinsfile.cd
```

Click **Save**

---

#### 3. Gateway-Service-CI

**Jenkins UI:**
1. Click "New Item"
2. Name: `gateway-service-CI`
3. Type: Pipeline → OK

**Configuration:**
```
Description: CI Pipeline for API Gateway

Build Triggers:
☑️ Poll SCM
Schedule: H/5 * * * *

Pipeline:
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
Credentials: github-credentials
Branch: */main
Script Path: backend/Gateway-Service/Jenkinsfile
```

Click **Save**

---

#### 4. Gateway-Service-CD

**Jenkins UI:**
1. Click "New Item"
2. Name: `gateway-service-CD`
3. Type: Pipeline → OK

**Configuration:**
```
Description: CD Pipeline for API Gateway

☑️ This project is parameterized
Add Parameter: String Parameter
  Name: IMAGE_TAG
  Default Value: latest

Pipeline:
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
Credentials: github-credentials
Branch: */main
Script Path: backend/Gateway-Service/Jenkinsfile.cd
```

Click **Save**

---

### B. Test Infrastructure Services

```bash
# In Jenkins, build eureka-service-CI
# Wait for completion
# Verify CD is triggered

# Check container
docker ps | grep fakarni_eureka_service

# Check health
curl http://localhost:8762/actuator/health

# Build gateway-service-CI
# Wait for completion

# Check container
docker ps | grep fakarni_gateway_service

# Check health
curl http://localhost:8080/actuator/health
```

---

### C. Create Remaining 34 Jobs

**Use the same pattern for all remaining services:**

| Service | CI Job Name | CD Job Name | Script Path |
|---------|-------------|-------------|-------------|
| tracking-service | tracking-service-CI | tracking-service-CD | backend/Tracking-Service/Jenkinsfile |
| geofencing-service | geofencing-service-CI | geofencing-service-CD | backend/Geofencing-Service/Jenkinsfile |
| chat-service | chat-service-CI | chat-service-CD | backend/Chat_Service/Jenkinsfile |
| event-service | event-service-CI | event-service-CD | backend/Event-Service/Jenkinsfile |
| post-service | post-service-CI | post-service-CD | backend/Post-Service/Jenkinsfile |
| notification-service | notification-service-CI | notification-service-CD | backend/Notification-Service/Jenkinsfile |
| paiement-service | paiement-service-CI | paiement-service-CD | backend/Paiement-Service/Jenkinsfile |
| pharmacie-service | pharmacie-service-CI | pharmacie-service-CD | backend/Pharmacie-Service/Jenkinsfile |
| rendez-vous-service | rendez-vous-service-CI | rendez-vous-service-CD | backend/Rendez-Vous-Service/Jenkinsfile |
| dossier-medical-service | dossier-medical-service-CI | dossier-medical-service-CD | backend/Dossier_Medical-service/Jenkinsfile |
| detection-maladie-service | detection-maladie-service-CI | detection-maladie-service-CD | backend/Detection_Maladie-Service/Jenkinsfile |
| activite-educative-service | activite-educative-service-CI | activite-educative-service-CD | backend/activite-educative-service/Jenkinsfile |
| video-service | video-service-CI | video-service-CD | backend/Video-Service/Jenkinsfile |
| session-service | session-service-CI | session-service-CD | backend/session-service/Jenkinsfile |
| suivi-engagement-service | suivi-engagement-service-CI | suivi-engagement-service-CD | backend/suivi-engagement-service/Jenkinsfile |
| meeting-insights-service | meeting-insights-service-CI | meeting-insights-service-CD | backend/meeting-insights-service/Jenkinsfile |
| group-service | group-service-CI | group-service-CD | backend/group/Jenkinsfile |

---

## 🚀 STEP 3: DEPLOY ALL SERVICES (20 minutes)

### Trigger All CI Pipelines:

In Jenkins dashboard:
1. Click on each CI job
2. Click "Build Now"
3. Watch the pipeline execute
4. CD will be triggered automatically

### Monitor Deployment:

```bash
# Watch containers start
watch -n 2 'docker ps --filter "name=fakarni_" --format "table {{.Names}}\t{{.Status}}"'

# Or on Windows PowerShell:
while ($true) { docker ps --filter "name=fakarni_" --format "table {{.Names}}\t{{.Status}}"; Start-Sleep -Seconds 2; Clear-Host }
```

### Verify All Services:

```bash
# Count running containers
docker ps --filter "name=fakarni_" | wc -l
# Should be 19 (or more with infrastructure)

# Check Eureka dashboard
start http://localhost:8762
# Should show all registered services

# Check health endpoints
curl http://localhost:8762/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:9011/actuator/health  # Tracking
curl http://localhost:9012/actuator/health  # Geofencing
curl http://localhost:8070/actuator/health  # Chat
# ... etc
```

---

## ✅ SUCCESS CHECKLIST

### After Commit:
- [ ] All Jenkinsfiles committed to Git
- [ ] All Jenkinsfiles visible on GitHub
- [ ] No uncommitted changes: `git status`

### After Job Creation:
- [ ] 38 Jenkins jobs created (19 CI + 19 CD)
- [ ] All jobs use "Pipeline script from SCM"
- [ ] All CI jobs have Poll SCM configured
- [ ] All CD jobs have IMAGE_TAG parameter
- [ ] Infrastructure services (Eureka, Gateway) tested

### After Deployment:
- [ ] All 19 containers running
- [ ] All health checks passing (HTTP 200)
- [ ] All services registered in Eureka
- [ ] Docker images in Docker Hub
- [ ] SonarQube projects created

---

## 🎯 FINAL VERIFICATION

### Check Everything:

```bash
# 1. Jenkins jobs
# Open: http://localhost:8085
# Verify: 38 jobs visible

# 2. Docker containers
docker ps --filter "name=fakarni_"
# Should show 19+ containers

# 3. Eureka dashboard
# Open: http://localhost:8762
# Verify: All services registered

# 4. SonarQube projects
# Open: http://localhost:9000
# Verify: 19 projects with analysis

# 5. Docker Hub
# Open: https://hub.docker.com/u/didou2505
# Verify: 19 repositories with images
```

---

## 🐛 QUICK TROUBLESHOOTING

### Jenkinsfile not found:
```bash
# Verify file exists
ls backend/Tracking-Service/Jenkinsfile

# Check if committed
git log --oneline -1

# If not pushed, push again
git push origin main
```

### Jenkins can't access GitHub:
```
1. Manage Jenkins → Credentials
2. Check 'github-credentials' exists
3. Test: git ls-remote https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
```

### Container not starting:
```bash
# Check logs
docker logs fakarni_{service}_service

# Check if Eureka is running
docker ps | grep eureka

# Restart container
docker restart fakarni_{service}_service
```

---

## 📊 TIME ESTIMATE

| Task | Time |
|------|------|
| Commit Jenkinsfiles | 5 min |
| Create 4 infrastructure jobs | 10 min |
| Test infrastructure | 5 min |
| Create 34 remaining jobs | 25 min |
| Deploy all services | 15 min |
| Verification | 5 min |
| **TOTAL** | **~60 min** |

---

## 🎉 COMPLETION

When done, you'll have:

✅ **38 Jenkinsfiles** in Git
✅ **38 Jenkins Jobs** configured
✅ **19 Docker Containers** running
✅ **19 Services** in Eureka
✅ **19 Docker Images** in Docker Hub
✅ **19 SonarQube Projects** analyzed
✅ **Full CI/CD Pipeline** operational

---

**🚀 START NOW: Copy the commit commands and execute!**

