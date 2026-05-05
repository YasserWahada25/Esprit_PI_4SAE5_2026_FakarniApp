# 🔧 Complete SonarQube Fix Guide

## Current Issue
```
[ERROR] SonarQube server [http://fakarni_sonarqube:9000] can not be reached
Status returned by url [http://fakarni_sonarqube:9000/batch/index] is not valid: [400]
```

## Root Causes
1. ✅ **Network connectivity**: FIXED - Jenkins can reach SonarQube
2. ❌ **Authentication**: Missing SonarQube token in Jenkins
3. ❌ **Plugin version**: Old Maven plugin incompatible with SonarQube 10.x

## What I Fixed

### 1. Updated `backend/User-Service/pom.xml`
Added explicit SonarQube Maven plugin version compatible with SonarQube 10.x:

```xml
<properties>
    ...
    <sonar.version>5.0.0.4389</sonar.version>
</properties>

<build>
    <plugins>
        ...
        <!-- SonarQube Maven Plugin -->
        <plugin>
            <groupId>org.sonarsource.scanner.maven</groupId>
            <artifactId>sonar-maven-plugin</artifactId>
            <version>${sonar.version}</version>
        </plugin>
    </plugins>
</build>
```

## What You Need to Do

### Step 1: Generate SonarQube Token

1. **Open SonarQube**: http://localhost:9000

2. **Login**:
   - Username: `admin`
   - Password: `admin`
   - (You may be prompted to change the password on first login)

3. **Generate Token**:
   - Click your profile icon (top right) → **My Account**
   - Click **Security** tab
   - Under **Generate Tokens**:
     - Name: `jenkins`
     - Type: **Global Analysis Token**
     - Expires in: **No expiration** (or 90 days)
   - Click **Generate**
   - **⚠️ COPY THE TOKEN IMMEDIATELY** (you won't see it again!)

### Step 2: Add Token to Jenkins

1. **Open Jenkins**: http://localhost:8085

2. **Add Credential**:
   - Go to: **Manage Jenkins** → **Credentials**
   - Click: **(global)** domain
   - Click: **Add Credentials**
   - Fill in:
     - **Kind**: Secret text
     - **Scope**: Global
     - **Secret**: [PASTE YOUR SONARQUBE TOKEN HERE]
     - **ID**: `sonarqube-token`
     - **Description**: SonarQube Authentication Token
   - Click: **Create**

### Step 3: Configure SonarQube Server in Jenkins

1. **In Jenkins**, go to: **Manage Jenkins** → **System** (or **Configure System**)

2. **Scroll down** to: **SonarQube servers** section

3. **Configure**:
   - **Name**: `SonarQube` (must match the name in your pipeline)
   - **Server URL**: `http://fakarni_sonarqube:9000`
   - **Server authentication token**: Select `sonarqube-token` from dropdown
   
4. **Click**: Save

### Step 4: Commit and Push Changes

```bash
# Commit the pom.xml changes
git add backend/User-Service/pom.xml
git commit -m "fix: Update SonarQube Maven plugin to 5.0.0 for compatibility with SonarQube 10.x"
git push origin main
```

### Step 5: Re-run Pipeline

Go to Jenkins and re-run your `user-service-CI` pipeline. It should now work! 🎉

## Verification

After the pipeline runs, you should see:

```
✅ SonarQube analysis completed
✅ Quality Gate passed
```

And you can view the analysis results at:
http://localhost:9000/dashboard?id=user-service

## Troubleshooting

### If you still get 400 error:

1. **Check SonarQube is fully started**:
   ```bash
   docker logs fakarni_sonarqube | grep "SonarQube is operational"
   ```

2. **Verify token is valid**:
   - Go to SonarQube → My Account → Security
   - Check if the `jenkins` token exists

3. **Test connection from Jenkins container**:
   ```bash
   docker exec fakarni_jenkins curl -H "Authorization: Bearer YOUR_TOKEN" http://fakarni_sonarqube:9000/api/server/version
   ```

### If you get 401 Unauthorized:

- The token is incorrect or expired
- Regenerate the token in SonarQube
- Update the credential in Jenkins

### If you get connection refused:

- Check both containers are running:
  ```bash
  docker ps | grep -E "jenkins|sonarqube"
  ```

- Check they're on the same network:
  ```bash
  docker network inspect fakarni_app_fakarni-net
  ```

## Quick Reference

| Service | URL | Default Credentials |
|---------|-----|---------------------|
| SonarQube | http://localhost:9000 | admin / admin |
| Jenkins | http://localhost:8085 | (your setup) |

## Files Modified

- ✅ `backend/User-Service/pom.xml` - Added SonarQube plugin version
- ✅ `setup-sonarqube-token.sh` - Helper script (optional)
- ✅ `SONARQUBE_FIX_COMPLETE.md` - This guide

## Alternative: Use SonarQube 9.9 LTS

If you prefer not to update the Maven plugin, you can downgrade SonarQube:

```yaml
# In docker-compose.yml, change:
sonarqube:
  image: sonarqube:9.9-community  # Instead of 10-community
```

Then restart:
```bash
docker-compose down sonarqube
docker-compose up -d sonarqube
```

**Note**: SonarQube 10.x is recommended for better features and security.
