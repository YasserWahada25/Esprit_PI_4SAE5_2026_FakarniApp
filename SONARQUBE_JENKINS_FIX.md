# SonarQube Jenkins Connection Fix

## Problem
Jenkins pipeline fails with:
```
[ERROR] SonarQube server [http://localhost:9000] can not be reached
[ERROR] Failed to connect to localhost/[0:0:0:0:0:0:0:1]:9000: Connection refused
```

## Root Cause
Jenkins is running inside a Docker container and cannot access SonarQube via `localhost`. 
Both containers are on the same Docker network (`fakarni_app_fakarni-net`), so they need to communicate using container names.

## Verification
✅ SonarQube is running: `fakarni_sonarqube` on port 9000
✅ Jenkins is running: `fakarni_jenkins` on port 8085
✅ Both are on the same network: `fakarni_app_fakarni-net`
✅ Jenkins CAN reach SonarQube using container name

## Solution

### Step 1: Update Jenkins SonarQube Configuration

1. Open Jenkins: http://localhost:8085
2. Go to: **Manage Jenkins** → **System** (or **Configure System**)
3. Scroll down to: **SonarQube servers** section
4. Update the **Server URL**:
   - ❌ OLD: `http://localhost:9000`
   - ✅ NEW: `http://fakarni_sonarqube:9000`
5. Click **Save**

### Step 2: Verify SonarQube Token

Make sure you have a valid SonarQube authentication token configured:

1. In the same **SonarQube servers** section
2. Check **Server authentication token** is set
3. If not, create one:
   - Go to SonarQube: http://localhost:9000
   - Login (default: admin/admin)
   - Go to: **My Account** → **Security** → **Generate Tokens**
   - Generate a token named `jenkins`
   - Copy the token
   - Back in Jenkins, add it as a **Secret text** credential
   - Select it in the SonarQube server configuration

### Step 3: Re-run the Pipeline

After updating the configuration, re-run your Jenkins pipeline. It should now successfully connect to SonarQube.

## Alternative: Use Docker Compose Service Name

If you're using docker-compose, you can also use the service name defined in your `docker-compose.yml`:
- Check your docker-compose file for the SonarQube service name
- Use that name instead: `http://<service-name>:9000`

## Testing Connection

To test if Jenkins can reach SonarQube:

```bash
# From your host machine
docker exec fakarni_jenkins curl -I http://fakarni_sonarqube:9000

# Should return HTTP response (even if 400, it means connection works)
```

## Quick Commands

```bash
# Check running containers
docker ps

# Check SonarQube logs
docker logs fakarni_sonarqube --tail 50

# Check Jenkins logs
docker logs fakarni_jenkins --tail 50

# Restart Jenkins (if needed)
docker restart fakarni_jenkins

# Restart SonarQube (if needed)
docker restart fakarni_sonarqube
```

## Expected Result

After the fix, your pipeline should show:
```
✅ SonarQube analysis completed
✅ Quality Gate passed
```
