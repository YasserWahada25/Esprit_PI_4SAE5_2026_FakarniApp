# 🚀 DEPLOY NOW - Quick Commands

## ⚡ Fastest Way to Deploy

### Windows
```bash
deploy-fixes.bat
```

### Linux/Mac
```bash
chmod +x deploy-fixes.sh && ./deploy-fixes.sh
```

**That's it!** Wait 60 seconds, then open http://localhost:4200

---

## 📋 Manual Deployment (3 Commands)

```bash
# 1. Stop
docker compose down

# 2. Rebuild
docker compose build group-service post-service activite-educative-service geofencing-service

# 3. Start
docker compose up -d
```

Wait 60 seconds, then test!

---

## ✅ Quick Test

Open these URLs:

1. **Frontend**: http://localhost:4200
2. **Eureka**: http://localhost:8762
3. **Test**: Create a post, create a group, upload images

---

## 🐛 If Something Goes Wrong

```bash
# Check logs
docker compose logs -f [service-name]

# Restart everything
docker compose down
docker compose up -d

# Check status
docker compose ps
```

---

## 🎯 What to Expect

- ✅ 28 containers running
- ✅ All services in Eureka
- ✅ No 403 errors
- ✅ Images work
- ✅ Maps work (Leaflet - no API key needed)
- ✅ User dropdown works

---

## 📞 Need Help?

Read: **SIMPLE-DEPLOY-GUIDE.md**

---

**Total Time**: 5 minutes
**Complexity**: Simple
**API Keys**: None needed ✅

**Deploy now!** 🚀
