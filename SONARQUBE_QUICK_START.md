# 🚀 SonarQube Quick Start - 3 Steps

## The Problem
Your pipeline fails because **Jenkins cannot authenticate with SonarQube**.

## The Solution (5 minutes)

### ✅ Step 1: I Fixed the Code
I updated `backend/User-Service/pom.xml` with the correct SonarQube plugin version (5.0.0) compatible with SonarQube 10.x.

**You need to commit this change:**
```bash
git add backend/User-Service/pom.xml
git commit -m "fix: Update SonarQube Maven plugin for compatibility"
git push
```

### 🔐 Step 2: Generate SonarQube Token

1. Open: http://localhost:9000
2. Login: `admin` / `admin`
3. Go to: **Profile Icon** → **My Account** → **Security**
4. Generate Token:
   - Name: `jenkins`
   - Type: Global Analysis Token
   - Click **Generate**
5. **COPY THE TOKEN** (you won't see it again!)

### 🔧 Step 3: Add Token to Jenkins

1. Open: http://localhost:8085
2. Go to: **Manage Jenkins** → **Credentials** → **(global)** → **Add Credentials**
3. Fill in:
   - Kind: **Secret text**
   - Secret: **[PASTE YOUR TOKEN]**
   - ID: `sonarqube-token`
   - Description: SonarQube Token
4. Click **Create**

5. Go to: **Manage Jenkins** → **System**
6. Find: **SonarQube servers**
7. Configure:
   - Name: `SonarQube`
   - Server URL: `http://fakarni_sonarqube:9000`
   - Token: Select `sonarqube-token`
8. Click **Save**

### 🎉 Step 4: Re-run Pipeline

Your pipeline should now work!

---

## Need More Details?

- 📖 Full guide: `SONARQUBE_FIX_COMPLETE.md`
- 🔧 Helper script: `setup-sonarqube-token.sh`

## Quick Links

- SonarQube: http://localhost:9000
- Jenkins: http://localhost:8085
