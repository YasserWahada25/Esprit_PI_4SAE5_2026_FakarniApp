# 🚀 Quick Reference Card - Fakarni App Fixes

## ⚡ 30-Second Deploy

```bash
# 1. Add Google Maps API key to .env
GOOGLE_MAPS_API_KEY=YOUR_KEY_HERE

# 2. Run deployment (Windows)
deploy-fixes.bat

# OR (Linux/Mac)
chmod +x deploy-fixes.sh && ./deploy-fixes.sh

# 3. Wait 60 seconds, then open
http://localhost:4200
```

---

## 📚 Documentation Quick Links

| Need | Read This |
|------|-----------|
| **Quick Start** | `START-HERE.md` |
| **What Changed** | `FIXES-SUMMARY.md` |
| **How to Test** | `COMPLETE-FIXES-GUIDE.md` |
| **Step-by-Step** | `DEPLOYMENT-CHECKLIST.md` |
| **Technical Details** | `FIXES-IMPLEMENTATION-PLAN.md` |

---

## 🔧 Common Commands

### Deployment
```bash
# Stop all
docker compose down

# Rebuild specific services
docker compose build group-service post-service activite-educative-service geofencing-service

# Start all
docker compose up -d

# Check status
docker compose ps
```

### Monitoring
```bash
# View all logs
docker compose logs --tail=50

# Follow specific service
docker compose logs -f group-service

# Check Eureka
http://localhost:8762
```

### Troubleshooting
```bash
# Restart service
docker compose restart [service-name]

# Rebuild and restart
docker compose build [service-name]
docker compose up -d [service-name]

# Check volumes
docker volume ls | grep fakarni

# Full reset
docker compose down -v
docker compose up -d
```

---

## 🎯 What Was Fixed

| Service | Issue | Fix |
|---------|-------|-----|
| **Post** | Images don't save | ✅ Security config updated |
| **Group** | 403 Forbidden | ✅ Security config updated |
| **Activite** | Images don't load | ✅ Image serving added |
| **Activite** | No map | ✅ Maps API configured |
| **Geofencing** | No map | ✅ Maps API configured |
| **Geofencing** | No users | ✅ User selection added |

---

## 🔗 New API Endpoints

### Geofencing
```bash
GET /api/geofencing/users
GET /api/geofencing/users/by-role?role=PATIENT
GET /api/geofencing/config/maps-api-key
```

### Activite-Educative
```bash
GET /api/activities/uploads/activities/{filename}
GET /api/activities/uploads/questions/{filename}
GET /api/activities/config/maps-api-key
```

---

## ✅ Quick Test

```bash
# 1. Check containers
docker compose ps
# Expected: 28 containers UP

# 2. Check Eureka
curl http://localhost:8762
# Expected: All services registered

# 3. Test Post creation
curl -X POST http://localhost:8090/api/posts \
  -H "Content-Type: application/json" \
  -d '{"content":"Test","imageUrl":"data:image/png;base64,..."}'
# Expected: 201 Created

# 4. Test Group creation
curl -X POST http://localhost:8090/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","description":"Test","groupType":"PUBLIC"}'
# Expected: 201 Created

# 5. Test Maps API key
curl http://localhost:8090/api/geofencing/config/maps-api-key
# Expected: {"apiKey":"YOUR_KEY"}
```

---

## 🐛 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| **Maps not loading** | Check `.env` has `GOOGLE_MAPS_API_KEY` |
| **403 errors** | Rebuild: `docker compose build [service]` |
| **Images not showing** | Check volume: `docker volume ls` |
| **Service not starting** | Check logs: `docker compose logs [service]` |
| **Port conflict** | Stop conflicting app or change port |

---

## 📊 Success Checklist

- [ ] 28 containers running
- [ ] All services in Eureka
- [ ] Frontend loads (http://localhost:4200)
- [ ] Can create post with image
- [ ] Can create group
- [ ] Activity images display
- [ ] Maps load in Geofencing
- [ ] Maps load in Activities
- [ ] User dropdown works

---

## 🔑 Required Configuration

### .env File
```env
# Required for maps
GOOGLE_MAPS_API_KEY=YOUR_ACTUAL_API_KEY_HERE

# Already configured
MAIL_USERNAME_USER=mohamadrayen.jbili@esprit.tn
MAIL_PASSWORD_USER=ueivocwsiczztvem
GOOGLE_CLIENT_ID=968599520946-llp69cv61a73f9457lpedn7m4tflrr2t.apps.googleusercontent.com
FACEBOOK_APP_ID=1270980888473415
JWT_SECRET=ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=
```

---

## 🌐 Important URLs

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:4200 |
| **API Gateway** | http://localhost:8090 |
| **Eureka Dashboard** | http://localhost:8762 |
| **phpMyAdmin** | http://localhost:8086 |
| **SonarQube** | http://localhost:9000 |

---

## 📞 Get Help

1. **Check logs**: `docker compose logs -f [service]`
2. **Read docs**: `START-HERE.md` → `COMPLETE-FIXES-GUIDE.md`
3. **Check status**: `docker compose ps`
4. **Verify Eureka**: http://localhost:8762

---

## 🎯 Files You Need

### To Deploy
- ✅ `.env` (add Google Maps API key)
- ✅ `deploy-fixes.bat` (Windows)
- ✅ `deploy-fixes.sh` (Linux/Mac)

### To Read
- ✅ `START-HERE.md` (start here!)
- ✅ `COMPLETE-FIXES-GUIDE.md` (comprehensive)
- ✅ `DEPLOYMENT-CHECKLIST.md` (step-by-step)

---

## ⏱️ Time Estimates

| Task | Time |
|------|------|
| Get Google Maps API key | 5 min |
| Add key to .env | 1 min |
| Run deployment script | 3 min |
| Wait for startup | 1 min |
| Test features | 5 min |
| **Total** | **~15 min** |

---

## 🎉 You're Ready!

1. Add Google Maps API key to `.env`
2. Run `deploy-fixes.bat` or `./deploy-fixes.sh`
3. Wait 60 seconds
4. Open http://localhost:4200
5. Test everything
6. Done! 🚀

---

**Last Updated**: May 3, 2026
**Version**: 1.0
**Status**: Ready to Deploy ✅

---

**Print this page for quick reference during deployment!**
