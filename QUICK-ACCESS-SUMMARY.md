# 🚀 Quick Access Summary - Fakarni App

## Date: May 3, 2026

---

## ✅ Objectives Completed

### 1. Database Management Tools Added ✅
- **Mongo Express** for MongoDB
- **Adminer** for MySQL

### 2. Image Upload Issue Fixed ✅
- Local uploads directory now mounted in container
- All existing images now accessible

---

## 🌐 Access URLs

### Application
| Service | URL | Credentials |
|---------|-----|-------------|
| **Frontend** | http://localhost:4200 | Your app login |
| **API Gateway** | http://localhost:8090 | - |
| **Eureka Dashboard** | http://localhost:8762 | - |

### Database Management
| Tool | URL | Username | Password | Purpose |
|------|-----|----------|----------|---------|
| **Mongo Express** | **http://localhost:8091** | `admin` | `admin` | View MongoDB data |
| **Adminer** | **http://localhost:8092** | `root` | `root` | View MySQL data |
| **PHPMyAdmin** | http://localhost:8086 | `root` | `root` | View MySQL data |

### Image Access
| Type | Example URL |
|------|-------------|
| Activities | http://localhost:8090/uploads/activities/0886d55e282a4e8ab6b0cc5e2f3a8a7a.jpg |
| Questions | http://localhost:8090/uploads/questions/5d702b41d00744b69749fd1d757da639.jpg |

---

## 📊 How to Use Database Tools

### Mongo Express (MongoDB)
1. Open: http://localhost:8091
2. Login: `admin` / `admin`
3. Click on database name to explore
4. Click on collection to view documents
5. **Use Cases**:
   - View user accounts
   - Check session data
   - Verify authentication tokens

### Adminer (MySQL)
1. Open: http://localhost:8092
2. Fill in:
   - **System**: MySQL
   - **Server**: Select from dropdown:
     - `db-session` - Users, auth
     - `db-activite` - Activities, quizzes
     - `db-geofencing` - GPS zones
     - `db-detection` - MRI analysis
     - `db-dossier` - Medical records
     - `db-event` - Events
     - `db-post` - Posts
     - `db-group` - Groups
     - `db-tracking` - GPS tracking
     - `db-suivi` - Engagement
   - **Username**: `root`
   - **Password**: `root`
3. Click **Login**
4. **Use Cases**:
   - Browse table data
   - Run SQL queries
   - Export data
   - Check relationships

### PHPMyAdmin (MySQL - Alternative)
1. Open: http://localhost:8086
2. Select server from dropdown
3. Login: `root` / `root`
4. Same functionality as Adminer but different UI

---

## 🖼️ Image Upload Status

### Current Images (6 total)

**Activities (5 images)**:
```
✅ 0886d55e282a4e8ab6b0cc5e2f3a8a7a.jpg
✅ 0ee2924b777c484c9993b8b7a015261a.jpg
✅ 7aab352307f4433e9c2b62170065f7a5.jpg
✅ 89cdc780e3994107aec48125a51de95c.jpg
✅ ad21ec8623dd473d8d4cf42af43ac35a.jpg
```

**Questions (1 image)**:
```
✅ 5d702b41d00744b69749fd1d757da639.jpg
```

### Local Directory
All images are stored in:
```
C:\Users\jbili\OneDrive\Bureau\Fakarni_App\backend\activite-educative-service\uploads\
```

### Container Access
Container can now access local directory:
```bash
docker exec fakarni_activite_service ls /app/uploads/activities/
```

---

## 🧪 Quick Tests

### Test 1: View MongoDB Data
```
1. Open http://localhost:8091
2. Login with admin/admin
3. Click on your database
4. Verify you can see collections
```

### Test 2: View MySQL Data
```
1. Open http://localhost:8092
2. Select MySQL system
3. Choose db-session server
4. Login with root/root
5. Browse tables
```

### Test 3: View Images
```
1. Open http://localhost:8090/uploads/activities/0886d55e282a4e8ab6b0cc5e2f3a8a7a.jpg
2. Image should display
3. Check Network tab: Status 200 OK
```

### Test 4: Upload New Image
```
1. Login to frontend as admin
2. Go to Educational Content → Create Activity
3. Upload an image
4. Check it appears in: backend/activite-educative-service/uploads/activities/
5. Verify it displays in the activity
```

---

## 🐳 Container Status

### Check All Containers
```bash
docker ps
```

**Expected**: 31 containers running (29 original + 2 new)

### New Containers
```
✅ fakarni_mongo_express (port 8091)
✅ fakarni_adminer (port 8092)
```

### Modified Container
```
✅ fakarni_activite_service (now uses bind mount)
```

---

## 🔧 Common Commands

### View Logs
```bash
# Mongo Express
docker-compose logs -f mongo-express

# Adminer
docker-compose logs -f adminer

# Activite Service
docker-compose logs -f activite-educative-service
```

### Restart Services
```bash
# Restart database tools
docker-compose restart mongo-express adminer

# Restart activite service
docker-compose restart activite-educative-service

# Restart all
docker-compose restart
```

### Stop/Start
```bash
# Stop all
docker-compose down

# Start all
docker-compose up -d
```

---

## 📁 Important Directories

### Uploads (Local)
```
C:\Users\jbili\OneDrive\Bureau\Fakarni_App\backend\activite-educative-service\uploads\
├── activities\
│   ├── 0886d55e282a4e8ab6b0cc5e2f3a8a7a.jpg
│   ├── 0ee2924b777c484c9993b8b7a015261a.jpg
│   ├── 7aab352307f4433e9c2b62170065f7a5.jpg
│   ├── 89cdc780e3994107aec48125a51de95c.jpg
│   └── ad21ec8623dd473d8d4cf42af43ac35a.jpg
└── questions\
    └── 5d702b41d00744b69749fd1d757da639.jpg
```

### Uploads (Container)
```
/app/uploads/
├── activities/
└── questions/
```

---

## ✅ Success Checklist

```
✅ Mongo Express running on port 8091
✅ Adminer running on port 8092
✅ Activite service using local uploads directory
✅ All 6 images accessible via gateway
✅ Container can read/write to local directory
✅ Database tools accessible and working
✅ Images display in frontend
```

---

## 🎯 What You Can Do Now

### 1. Explore Your Data
- **MongoDB**: Check user accounts, sessions
- **MySQL**: Browse all tables, run queries
- **Export**: Backup your data

### 2. Manage Images
- **View**: All existing images are accessible
- **Upload**: New images save to local directory
- **Backup**: Copy uploads folder to backup location

### 3. Debug Issues
- **Logs**: View real-time logs for all services
- **Data**: Verify data integrity in databases
- **Images**: Check image paths and accessibility

### 4. Development
- **Test**: Upload new images and verify
- **Query**: Run SQL queries to test features
- **Monitor**: Watch logs for errors

---

## 📞 Need Help?

### Images Not Showing?
1. Check: http://localhost:8090/uploads/activities/[filename]
2. Verify: `docker exec fakarni_activite_service ls /app/uploads/activities/`
3. Restart: `docker-compose restart activite-educative-service api-gateway`

### Database Tools Not Loading?
1. Check: `docker ps | grep -E "mongo-express|adminer"`
2. Logs: `docker-compose logs mongo-express adminer`
3. Restart: `docker-compose restart mongo-express adminer`

### Port Conflicts?
1. Stop: `docker-compose down`
2. Check: `netstat -ano | findstr "8091 8092"`
3. Start: `docker-compose up -d`

---

**Status**: ✅ All objectives completed  
**Date**: May 3, 2026  
**Containers**: 31/31 running  
**Images**: 6/6 accessible  
**Database Tools**: 2/2 working
