# Start & Stop Guide - Fakarni Application

## 🛑 Stopping the Application

### Stop All Containers
```bash
docker compose down
```

This will:
- Stop all 26 containers (15 services + 11 databases)
- Remove containers
- Keep all data (volumes are preserved)

### Stop Without Removing Containers
```bash
docker compose stop
```

Use this if you want to start quickly later without recreating containers.

---

## 🚀 Starting the Application

### Quick Start (Recommended)
```bash
# Navigate to project directory
cd C:\Users\jbili\OneDrive\Bureau\Fakarni_App

# Start all services
docker compose up -d
```

**That's it!** Wait 30-60 seconds for all services to start, then open http://localhost:4200

### What Happens When You Start
1. **Databases start first** (MySQL, MongoDB) - ~10 seconds
2. **Eureka Server starts** - ~15 seconds
3. **API Gateway starts** - ~20 seconds
4. **All microservices start** - ~30 seconds
5. **Frontend starts** - ~5 seconds

**Total startup time**: ~60 seconds

---

## ✅ Verify Everything is Running

### Check Container Status
```bash
docker ps
```

You should see **26 containers** running:
- 1 Frontend (nginx)
- 1 Eureka Server
- 1 API Gateway
- 12 Microservices
- 10 MySQL databases
- 1 MongoDB
- 1 phpMyAdmin

### Quick Health Check
```bash
# Check if all services are up
docker compose ps

# Should show all services as "Up" or "Up (healthy)"
```

### Test in Browser
1. Open http://localhost:4200 (Frontend)
2. Open http://localhost:8762 (Eureka - should show registered services)
3. Open http://localhost:8086 (phpMyAdmin)

If all three load, everything is working! ✅

---

## 🔄 Restart After Changes

### If You Modified Code
```bash
# Rebuild and restart specific service
docker compose build <service-name>
docker compose up -d <service-name>

# Example: Rebuild frontend
docker compose build frontend
docker compose up -d frontend

# Example: Rebuild user service
docker compose build user-service
docker compose up -d user-service
```

### If You Modified docker-compose.yml or .env
```bash
# Restart all services with new config
docker compose down
docker compose up -d
```

### If You Modified Only Configuration Files (application.properties)
```bash
# Just restart the affected service
docker compose restart user-service
```

---

## 📋 Common Scenarios

### Scenario 1: Daily Development
```bash
# Morning - Start everything
docker compose up -d

# Work on your project...

# Evening - Stop everything
docker compose down
```

### Scenario 2: Testing After Code Changes
```bash
# Rebuild and restart changed service
docker compose build user-service
docker compose up -d user-service

# Check logs
docker logs fakarni_user_service --tail 50 -f
```

### Scenario 3: Fresh Start (Clear All Data)
```bash
# Stop and remove everything including volumes
docker compose down -v

# Start fresh
docker compose up -d

# Note: This deletes all database data!
```

### Scenario 4: Quick Restart (Containers Already Exist)
```bash
# If you used "docker compose stop" before
docker compose start

# Much faster than "docker compose up -d"
```

---

## 🐛 Troubleshooting

### Services Won't Start
```bash
# Check what's wrong
docker compose ps
docker compose logs

# Restart specific service
docker compose restart <service-name>
```

### Port Already in Use
```bash
# Find what's using the port
netstat -ano | findstr :4200
netstat -ano | findstr :8090

# Kill the process or change port in docker-compose.yml
```

### Database Connection Errors
```bash
# Wait for databases to be healthy
docker compose ps

# Check database logs
docker logs fakarni_mongo --tail 50
docker logs fakarni_db_user --tail 50

# Restart database
docker compose restart mongodb
```

### Frontend Shows Blank Page
```bash
# Check frontend logs
docker logs fakarni_frontend --tail 50

# Restart frontend
docker compose restart frontend

# Clear browser cache and reload
```

---

## 📊 Monitoring Commands

### View Logs
```bash
# All services
docker compose logs -f

# Specific service
docker logs fakarni_user_service -f

# Last 50 lines
docker logs fakarni_api_gateway --tail 50

# Multiple services
docker compose logs -f frontend user-service api-gateway
```

### Check Resource Usage
```bash
# CPU and memory usage
docker stats

# Disk usage
docker system df
```

### Check Networks
```bash
# List networks
docker network ls

# Inspect fakarni network
docker network inspect fakarni_app_fakarni-net
```

---

## 🎯 Quick Reference

| Command | What It Does |
|---------|--------------|
| `docker compose up -d` | Start all services |
| `docker compose down` | Stop and remove containers |
| `docker compose stop` | Stop containers (keep them) |
| `docker compose start` | Start stopped containers |
| `docker compose restart <name>` | Restart one service |
| `docker compose ps` | Show service status |
| `docker ps` | Show running containers |
| `docker logs <name>` | View service logs |
| `docker compose build <name>` | Rebuild service |

---

## ⚡ Pro Tips

### 1. Create a Startup Script
Create `start.ps1`:
```powershell
Write-Host "Starting Fakarni Application..." -ForegroundColor Cyan
docker compose up -d
Write-Host "Waiting for services to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Write-Host "Opening application..." -ForegroundColor Green
Start-Process "http://localhost:4200"
Write-Host "Opening Eureka dashboard..." -ForegroundColor Green
Start-Process "http://localhost:8762"
Write-Host "Done! Application is ready." -ForegroundColor Green
```

Run with: `.\start.ps1`

### 2. Create a Stop Script
Create `stop.ps1`:
```powershell
Write-Host "Stopping Fakarni Application..." -ForegroundColor Yellow
docker compose down
Write-Host "All services stopped." -ForegroundColor Green
```

Run with: `.\stop.ps1`

### 3. Check Health Before Testing
```bash
# Quick health check
docker compose ps | Select-String "Up"

# Should show all services as "Up"
```

### 4. Keep Logs Clean
```bash
# Clear old logs
docker compose down
docker system prune -f

# Start fresh
docker compose up -d
```

---

## 🎓 Best Practices

### ✅ DO
- Always use `docker compose up -d` (detached mode)
- Wait 30-60 seconds after starting before testing
- Check logs if something doesn't work
- Use `docker compose down` when done for the day
- Keep Docker Desktop running

### ❌ DON'T
- Don't use `docker compose down -v` unless you want to delete data
- Don't stop Docker Desktop while containers are running
- Don't modify files inside containers (modify source and rebuild)
- Don't run multiple instances of the same service

---

## 📝 Your Typical Workflow

### Morning Routine
```bash
# 1. Open terminal in project directory
cd C:\Users\jbili\OneDrive\Bureau\Fakarni_App

# 2. Start all services
docker compose up -d

# 3. Wait a minute
# (Get coffee ☕)

# 4. Open browser
# http://localhost:4200

# 5. Start testing!
```

### Evening Routine
```bash
# 1. Stop all services
docker compose down

# 2. Close Docker Desktop (optional)
```

### After Code Changes
```bash
# 1. Rebuild changed service
docker compose build <service-name>

# 2. Restart it
docker compose up -d <service-name>

# 3. Check logs
docker logs fakarni_<service-name> --tail 50 -f

# 4. Test changes
```

---

## 🚀 Next Time You Want to Test

**Just run these 2 commands:**

```bash
cd C:\Users\jbili\OneDrive\Bureau\Fakarni_App
docker compose up -d
```

**Wait 60 seconds, then open:** http://localhost:4200

**That's it!** Everything will start automatically with all your data preserved.

---

## 📞 Need Help?

Check these files:
- **QUICK-REFERENCE.md** - Quick commands
- **FINAL-STATUS.md** - Complete status and features
- **DOCKER-GUIDE-PHASE1.md** - Detailed Docker guide
- **TROUBLESHOOTING.md** - Common issues (if created)

---

## ✅ Summary

**To Stop:**
```bash
docker compose down
```

**To Start Next Time:**
```bash
docker compose up -d
```

**To Test:**
```
http://localhost:4200
```

**Simple as that!** 🎉
