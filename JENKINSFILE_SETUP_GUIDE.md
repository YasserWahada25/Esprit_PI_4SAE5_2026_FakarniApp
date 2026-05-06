# 🚀 Jenkinsfile Setup Guide - Pipeline from SCM

## ✅ What We Have

**38 Jenkinsfiles deployed to 19 services:**
- Each service has `Jenkinsfile` (CI Pipeline)
- Each service has `Jenkinsfile.cd` (CD Pipeline)

---

## 📁 File Structure

```
backend/
├── Tracking-Service/
│   ├── Jenkinsfile          ← CI Pipeline
│   ├── Jenkinsfile.cd       ← CD Pipeline
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── Geofencing-Service/
│   ├── Jenkinsfile
│   ├── Jenkinsfile.cd
│   └── ...
├── Chat-Service/
│   ├── Jenkinsfile
│   ├── Jenkinsfile.cd
│   └── ...
└── ... (16 more services)
```

---

## 🎯 Jenkins Job Creation - Using Pipeline from SCM

### Step 1: Commit Jenkinsfiles to Git

```bash
# Add all Jenkinsfiles
git add backend/*/Jenkinsfile backend/*/Jenkinsfile.cd

# Commit
git commit -m "feat: add CI/CD Jenkinsfiles for all services"

# Push to GitHub
git push origin main
```

### Step 2: Create CI Job in Jenkins

1. **Open Jenkins**: http://localhost:8085
2. **Click**: "New Item"
3. **Enter name**: `tracking-service-CI` (example)
4. **Select**: "Pipeline"
5. **Click**: "OK"

#### Configure CI Job:

**General:**
- Description: `CI Pipeline for Tracking Service`

**Build Triggers:**
- ☑️ **Poll SCM**
- Schedule: `H/5 * * * *`

**Pipeline:**
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git`
- Credentials: `github-credentials`
- Branch: `*/main`
- **Script Path**: `backend/Tracking-Service/Jenkinsfile`

**Click**: "Save"

---

### Step 3: Create CD Job in Jenkins

1. **Click**: "New Item"
2. **Enter name**: `tracking-service-CD`
3. **Select**: "Pipeline"
4. **Click**: "OK"

#### Configure CD Job:

**General:**
- Description: `CD Pipeline for Tracking Service`
- ☑️ **This project is parameterized**
  - Add Parameter: **String Parameter**
    - Name: `IMAGE_TAG`
    - Default Value: `latest`

**Pipeline:**
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git`
- Credentials: `github-credentials`
- Branch: `*/main`
- **Script Path**: `backend/Tracking-Service/Jenkinsfile.cd`

**Click**: "Save"

---

## 📋 All Services - Script Paths

### Infrastructure Services:

| Service | CI Script Path | CD Script Path |
|---------|---------------|----------------|
| eureka-service | `backend/Eureka-Service/Jenkinsfile` | `backend/Eureka-Service/Jenkinsfile.cd` |
| gateway-service | `backend/Gateway-Service/Jenkinsfile` | `backend/Gateway-Service/Jenkinsfile.cd` |

### Core Services:

| Service | CI Script Path | CD Script Path |
|---------|---------------|----------------|
| tracking-service | `backend/Tracking-Service/Jenkinsfile` | `backend/Tracking-Service/Jenkinsfile.cd` |
| geofencing-service | `backend/Geofencing-Service/Jenkinsfile` | `backend/Geofencing-Service/Jenkinsfile.cd` |
| chat-service | `backend/Chat_Service/Jenkinsfile` | `backend/Chat_Service/Jenkinsfile.cd` |

### Business Services:

| Service | CI Script Path | CD Script Path |
|---------|---------------|----------------|
| event-service | `backend/Event-Service/Jenkinsfile` | `backend/Event-Service/Jenkinsfile.cd` |
| post-service | `backend/Post-Service/Jenkinsfile` | `backend/Post-Service/Jenkinsfile.cd` |
| notification-service | `backend/Notification-Service/Jenkinsfile` | `backend/Notification-Service/Jenkinsfile.cd` |
| paiement-service | `backend/Paiement-Service/Jenkinsfile` | `backend/Paiement-Service/Jenkinsfile.cd` |
| pharmacie-service | `backend/Pharmacie-Service/Jenkinsfile` | `backend/Pharmacie-Service/Jenkinsfile.cd` |
| rendez-vous-service | `backend/Rendez-Vous-Service/Jenkinsfile` | `backend/Rendez-Vous-Service/Jenkinsfile.cd` |
| dossier-medical-service | `backend/Dossier_Medical-service/Jenkinsfile` | `backend/Dossier_Medical-service/Jenkinsfile.cd` |
| detection-maladie-service | `backend/Detection_Maladie-Service/Jenkinsfile` | `backend/Detection_Maladie-Service/Jenkinsfile.cd` |
| activite-educative-service | `backend/activite-educative-service/Jenkinsfile` | `backend/activite-educative-service/Jenkinsfile.cd` |
| video-service | `backend/Video-Service/Jenkinsfile` | `backend/Video-Service/Jenkinsfile.cd` |

### Additional Services:

| Service | CI Script Path | CD Script Path |
|---------|---------------|----------------|
| session-service | `backend/session-service/Jenkinsfile` | `backend/session-service/Jenkinsfile.cd` |
| suivi-engagement-service | `backend/suivi-engagement-service/Jenkinsfile` | `backend/suivi-engagement-service/Jenkinsfile.cd` |
| meeting-insights-service | `backend/meeting-insights-service/Jenkinsfile` | `backend/meeting-insights-service/Jenkinsfile.cd` |
| group-service | `backend/group/Jenkinsfile` | `backend/group/Jenkinsfile.cd` |

---

## ⚡ Quick Job Creation Template

### CI Job Template:
```
Name: {service-name}-CI
Type: Pipeline
Build Triggers: Poll SCM (H/5 * * * *)
Pipeline:
  - Definition: Pipeline script from SCM
  - SCM: Git
  - Repository: https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
  - Credentials: github-credentials
  - Branch: */main
  - Script Path: backend/{Service-Directory}/Jenkinsfile
```

### CD Job Template:
```
Name: {service-name}-CD
Type: Pipeline
Parameters: IMAGE_TAG (String, default: latest)
Pipeline:
  - Definition: Pipeline script from SCM
  - SCM: Git
  - Repository: https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
  - Credentials: github-credentials
  - Branch: */main
  - Script Path: backend/{Service-Directory}/Jenkinsfile.cd
```

---

## 🎯 Priority Order for Job Creation

### Phase 1: Infrastructure (MUST BE FIRST)
1. eureka-service-CI
2. eureka-service-CD
3. gateway-service-CI
4. gateway-service-CD

### Phase 2: Test One Service
5. tracking-service-CI
6. tracking-service-CD

### Phase 3: All Remaining Services
7-38. Create remaining 32 jobs (16 services × 2 jobs)

---

## 🧪 Testing

### Test 1: Eureka Service
```bash
# 1. Trigger eureka-service-CI in Jenkins
# 2. Watch pipeline execute
# 3. Verify CD is triggered
# 4. Check container:
docker ps | grep fakarni_eureka_service

# 5. Check health:
curl http://localhost:8762/actuator/health
```

### Test 2: Gateway Service
```bash
# 1. Trigger gateway-service-CI
# 2. Wait for completion
# 3. Check container:
docker ps | grep fakarni_gateway_service

# 4. Check health:
curl http://localhost:8080/actuator/health
```

### Test 3: Tracking Service
```bash
# 1. Trigger tracking-service-CI
# 2. Wait for completion
# 3. Check container:
docker ps | grep fakarni_tracking_service

# 4. Check health:
curl http://localhost:9011/actuator/health
```

---

## ✅ Advantages of Pipeline from SCM

### 1. **Version Control**
- Jenkinsfiles are in Git
- Track changes over time
- Easy rollback

### 2. **Code Review**
- Review pipeline changes via Pull Requests
- Team collaboration

### 3. **Consistency**
- Same pipeline for all environments
- No manual copy-paste errors

### 4. **Automatic Updates**
- Jenkins pulls latest Jenkinsfile from Git
- No need to update jobs manually

### 5. **Backup**
- Jenkinsfiles backed up in Git
- Easy disaster recovery

---

## 🔄 Updating Pipelines

### To update a pipeline:

1. **Edit Jenkinsfile** in your local repository:
```bash
# Edit the file
code backend/Tracking-Service/Jenkinsfile

# Commit changes
git add backend/Tracking-Service/Jenkinsfile
git commit -m "fix: update tracking service pipeline"
git push origin main
```

2. **Jenkins automatically uses the new version** on next build!

---

## 🐛 Troubleshooting

### Issue: Jenkins can't find Jenkinsfile
**Solution**: Verify Script Path is correct
```
Correct: backend/Tracking-Service/Jenkinsfile
Wrong: Tracking-Service/Jenkinsfile
Wrong: backend/Tracking-Service/
```

### Issue: Git credentials not working
**Solution**: 
1. Go to Manage Jenkins → Credentials
2. Verify `github-credentials` exists
3. Test with: `git ls-remote https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git`

### Issue: Jenkinsfile not found in repository
**Solution**: 
```bash
# Verify file exists
ls backend/Tracking-Service/Jenkinsfile

# If not, commit and push
git add backend/Tracking-Service/Jenkinsfile
git commit -m "add: Jenkinsfile for tracking service"
git push origin main
```

---

## 📊 Verification Checklist

### Before Creating Jobs:
- [ ] All Jenkinsfiles committed to Git
- [ ] Jenkinsfiles pushed to GitHub
- [ ] Jenkins can access GitHub repository
- [ ] GitHub credentials configured in Jenkins

### After Creating Jobs:
- [ ] 38 jobs created in Jenkins
- [ ] All jobs use "Pipeline script from SCM"
- [ ] Script paths are correct
- [ ] CI jobs have Poll SCM configured
- [ ] CD jobs have IMAGE_TAG parameter

### After Testing:
- [ ] Infrastructure services deployed
- [ ] At least one microservice deployed
- [ ] All health checks passing
- [ ] Docker images in Docker Hub
- [ ] SonarQube analysis completed

---

## 🚀 Next Steps

1. **Commit Jenkinsfiles to Git** ✅ (Do this first!)
2. **Create Jenkins jobs** (30-45 minutes)
3. **Test infrastructure services** (10 minutes)
4. **Deploy all services** (20 minutes)
5. **Setup monitoring** (Prometheus + Grafana)
6. **Kubernetes deployment** (Phase 4)

---

## 📞 Quick Commands

### Commit Jenkinsfiles:
```bash
git add backend/*/Jenkinsfile backend/*/Jenkinsfile.cd
git commit -m "feat: add CI/CD Jenkinsfiles for all services"
git push origin main
```

### Verify Jenkinsfiles:
```bash
# Count Jenkinsfiles
find backend -name "Jenkinsfile" | wc -l  # Should be 19
find backend -name "Jenkinsfile.cd" | wc -l  # Should be 19

# List all Jenkinsfiles
find backend -name "Jenkinsfile" -o -name "Jenkinsfile.cd"
```

### Check Jenkins:
```bash
# Access Jenkins
start http://localhost:8085

# Check Jenkins logs
docker logs -f fakarni_jenkins
```

---

**🎉 You're ready! Commit the Jenkinsfiles and start creating Jenkins jobs!**

