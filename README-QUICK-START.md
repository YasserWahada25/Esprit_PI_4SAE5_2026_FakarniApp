# Fakarni Application - Quick Start Guide

## 🚀 Start the Application

```bash
cd C:\Users\jbili\OneDrive\Bureau\Fakarni_App
docker compose up -d
```

Wait 60 seconds, then open: **http://localhost:4200**

## 🛑 Stop the Application

```bash
docker compose down
```

## ✅ What's Running

- **28 containers total**
  - 1 Frontend (Angular + nginx)
  - 1 Eureka Server (service registry)
  - 1 API Gateway
  - 14 Microservices
  - 11 MySQL databases
  - 1 MongoDB
  - 1 phpMyAdmin

## 🔗 Access URLs

| Service | URL |
|---------|-----|
| **Main App** | http://localhost:4200 |
| Eureka Dashboard | http://localhost:8762 |
| API Gateway | http://localhost:8090 |
| phpMyAdmin | http://localhost:8086 |

## ✨ All Features Working

- ✅ User registration & login
- ✅ Google & Facebook OAuth
- ✅ Password reset (email + page)
- ✅ Session management (virtual & video)
- ✅ Real-time chat (WebSocket)
- ✅ Location tracking & geofencing
- ✅ Educational activities & games
- ✅ Disease detection
- ✅ Medical records
- ✅ Events & groups
- ✅ Social posts
- ✅ Engagement tracking
- ✅ Meeting insights

## 📋 Quick Commands

```bash
# Check status
docker ps

# View logs
docker logs fakarni_user_service -f

# Restart service
docker compose restart user-service

# Rebuild after code changes
docker compose build user-service
docker compose up -d user-service
```

## 🐛 Troubleshooting

### Services not starting?
```bash
docker compose ps
docker logs <container-name>
```

### Port already in use?
```bash
netstat -ano | findstr :4200
# Kill the process or change port in docker-compose.yml
```

### Need fresh start?
```bash
docker compose down -v  # Deletes all data!
docker compose up -d
```

## 📚 Documentation

- **START-STOP-GUIDE.md** - Detailed start/stop instructions
- **ALL-SERVICES-FIXED.md** - Complete service list and fixes
- **FINAL-STATUS.md** - Full application status
- **QUICK-REFERENCE.md** - Command reference

## 🎯 Test Checklist

1. Open http://localhost:4200
2. Create new account
3. Login
4. Test Google Sign-In
5. Test password reset
6. Browse all features

## 🎉 Everything is Working!

Your complete microservices application is ready to test.

**No IDE required** - everything runs in Docker!
