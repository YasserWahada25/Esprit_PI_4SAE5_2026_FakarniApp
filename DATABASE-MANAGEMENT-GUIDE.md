# Database Management & Image Fix Guide

## Date: May 3, 2026

---

## ✅ Objective 1: Database Management Tools

### Added Tools

#### 1. **Mongo Express** - MongoDB Web UI
- **URL**: http://localhost:8091
- **Username**: `admin`
- **Password**: `admin`
- **Purpose**: View and manage MongoDB data (users, sessions, etc.)

#### 2. **Adminer** - MySQL Web UI
- **URL**: http://localhost:8092
- **Purpose**: Alternative to PHPMyAdmin for MySQL databases
- **Login Details**:
  - **System**: MySQL
  - **Server**: Choose from dropdown (db-session, db-tracking, db-geofencing, etc.)
  - **Username**: `root`
  - **Password**: `root`
  - **Database**: (leave empty or select specific database)

#### 3. **PHPMyAdmin** (Already Existing)
- **URL**: http://localhost:8086
- **Purpose**: Manage all MySQL databases
- **Login**: `root` / `root`

---

## ✅ Objective 2: Image Upload Fix

### Problem
Images were stored locally in `C:\Users\jbili\OneDrive\Bureau\Fakarni_App\backend\activite-educative-service\uploads` but the Docker container couldn't access them because it was using a Docker volume instead of a bind mount.

### Solution
Changed the `activite-educative-service` volume configuration from:
```yaml
# Before (Docker volume - isolated from host)
volumes:
  - fakarni_activite_uploads:/app/uploads
```

To:
```yaml
# After (Bind mount - shares host directory)
volumes:
  - ./backend/activite-educative-service/uploads:/app/uploads
```

### Result
✅ Container now has direct access to local uploads directory  
✅ Images uploaded before containerization are now visible  
✅ New images will be saved to the same local directory  
✅ Images persist even if container is removed  

---

## 📁 Current Images

### Activities
Located in: `backend/activite-educative-service/uploads/activities/`
- `0886d55e282a4e8ab6b0cc5e2f3a8a7a.jpg`
- `0ee2924b777c484c9993b8b7a015261a.jpg`
- `7aab352307f4433e9c2b62170065f7a5.jpg`
- `89cdc780e3994107aec48125a51de95c.jpg`
- `ad21ec8623dd473d8d4cf42af43ac35a.jpg`

### Questions (Quizzes)
Located in: `backend/activite-educative-service/uploads/questions/`
- `5d702b41d00744b69749fd1d757da639.jpg`

### Access URLs
All images are now accessible via:
- **Direct**: `http://localhost:8084/uploads/activities/[filename]`
- **Gateway**: `http://localhost:8090/uploads/activities/[filename]`
- **Frontend**: `http://localhost:4200/uploads/activities/[filename]`

---

## 🗄️ Database Access Guide

### MongoDB Databases (via Mongo Express)

1. Open http://localhost:8091
2. Login with `admin` / `admin`
3. You'll see databases like:
   - `admin` - MongoDB admin database
   - `config` - MongoDB config
   - `fakarni_db` - Your application database (if exists)
   - `test` - Test database

4. Click on a database to see collections
5. Click on a collection to view documents

### MySQL Databases (via Adminer or PHPMyAdmin)

#### Using Adminer (http://localhost:8092)
1. Select **System**: MySQL
2. Select **Server**: 
   - `db-session` - User sessions, authentication
   - `db-tracking` - GPS tracking data
   - `db-geofencing` - Geofencing zones and alerts
   - `db-activite` - Educational activities
   - `db-detection` - MRI detection results
   - `db-dossier` - Medical records
   - `db-event` - Events
   - `db-group` - Groups
   - `db-post` - Posts
   - `db-suivi` - Engagement tracking
3. Username: `root`
4. Password: `root`
5. Click **Login**

#### Using PHPMyAdmin (http://localhost:8086)
1. Server dropdown shows all databases
2. Username: `root`
3. Password: `root`
4. Click **Go**

---

## 🔍 Database Schema Overview

### MongoDB Collections (Typical)
- `users` - User accounts
- `sessions` - Active sessions
- `tokens` - JWT tokens

### MySQL Databases

#### db_session
- `users` - User profiles
- `roles` - User roles
- `permissions` - Access permissions

#### db_activite
- `activite_educative` - Educational activities
- `questions` - Quiz questions
- `game_sessions` - Game play sessions

#### db_geofencing
- `zones` - Safe/danger zones
- `alerts` - Geofencing alerts
- `patient_positions` - GPS positions

#### db_detection
- `analyse_irm` - MRI analysis results
- `predictions` - AI predictions

#### db_dossier
- `dossier_medical` - Medical records
- `analyses` - Medical analyses

#### db_event
- `events` - Educational events
- `participants` - Event participants

#### db_post
- `posts` - Social posts
- `comments` - Post comments
- `likes` - Post likes

#### db_group
- `groups` - User groups
- `members` - Group members

#### db_tracking
- `tracking_data` - GPS tracking history

#### db_suivi
- `engagement_metrics` - User engagement data

---

## 🧪 Testing

### Test Image Access
1. Open browser: http://localhost:8090/uploads/activities/0886d55e282a4e8ab6b0cc5e2f3a8a7a.jpg
2. **Expected**: Image displays correctly
3. Check browser Network tab: Status should be `200 OK`

### Test Database Access
1. **MongoDB**: http://localhost:8091 → Should show databases
2. **MySQL (Adminer)**: http://localhost:8092 → Should show login form
3. **MySQL (PHPMyAdmin)**: http://localhost:8086 → Should show databases

### Test Image Upload
1. Login as admin
2. Go to Educational Content → Create Activity
3. Upload a new image
4. **Expected**: Image saves to `backend/activite-educative-service/uploads/activities/`
5. **Expected**: Image displays in the activity

---

## 🐳 Docker Commands

### View Container Logs
```bash
# Activite service logs
docker-compose logs -f activite-educative-service

# Mongo Express logs
docker-compose logs -f mongo-express

# Adminer logs
docker-compose logs -f adminer
```

### Restart Services
```bash
# Restart activite service
docker-compose restart activite-educative-service

# Restart database tools
docker-compose restart mongo-express adminer
```

### Check Container Status
```bash
docker ps | grep -E "mongo-express|adminer|activite"
```

### Access Container Shell
```bash
# Check uploads inside container
docker exec -it fakarni_activite_service ls -la /app/uploads/activities/

# Access container bash
docker exec -it fakarni_activite_service bash
```

---

## 📊 Port Summary

| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| Frontend | 4200 | http://localhost:4200 | Angular app |
| Gateway | 8090 | http://localhost:8090 | API Gateway |
| Eureka | 8762 | http://localhost:8762 | Service Discovery |
| Activite Service | 8084 | http://localhost:8084 | Educational content |
| PHPMyAdmin | 8086 | http://localhost:8086 | MySQL management |
| **Mongo Express** | **8091** | **http://localhost:8091** | **MongoDB management** |
| **Adminer** | **8092** | **http://localhost:8092** | **MySQL management** |

---

## 🔧 Troubleshooting

### Images Still Not Showing?

1. **Check container can see files**:
   ```bash
   docker exec fakarni_activite_service ls -la /app/uploads/activities/
   ```

2. **Check gateway routing**:
   ```bash
   docker-compose logs api-gateway | grep uploads
   ```

3. **Test direct access**:
   - Service: http://localhost:8084/uploads/activities/[filename]
   - Gateway: http://localhost:8090/uploads/activities/[filename]

4. **Restart services**:
   ```bash
   docker-compose restart activite-educative-service api-gateway
   ```

### Database Tools Not Loading?

1. **Check containers are running**:
   ```bash
   docker ps | grep -E "mongo-express|adminer"
   ```

2. **Check logs**:
   ```bash
   docker-compose logs mongo-express
   docker-compose logs adminer
   ```

3. **Restart**:
   ```bash
   docker-compose restart mongo-express adminer
   ```

### Port Already in Use?

If you see "port is already allocated":
```bash
# Stop all containers
docker-compose down

# Start again
docker-compose up -d
```

---

## 📝 Files Modified

1. `docker-compose.yml`
   - Added `mongo-express` service (port 8091)
   - Added `adminer` service (port 8092)
   - Changed `activite-educative-service` volume from Docker volume to bind mount
   - Commented out `fakarni_activite_uploads` volume definition

---

## ✅ Success Criteria

- ✅ Mongo Express accessible at http://localhost:8091
- ✅ Adminer accessible at http://localhost:8092
- ✅ Images accessible via http://localhost:8090/uploads/activities/[filename]
- ✅ Container can see local uploads directory
- ✅ New uploads save to local directory
- ✅ All database tools working

---

## 🎯 Next Steps

1. **Explore MongoDB data**:
   - Open http://localhost:8091
   - Check user collections
   - Verify session data

2. **Explore MySQL data**:
   - Open http://localhost:8092
   - Connect to each database
   - Review table structures

3. **Test image uploads**:
   - Create new activity with image
   - Verify image appears in local directory
   - Verify image displays in frontend

4. **Backup important data**:
   - Export MongoDB collections
   - Export MySQL databases
   - Keep uploads directory backed up

---

**Implementation Date**: May 3, 2026  
**Status**: ✅ Complete  
**Containers Added**: 2 (mongo-express, adminer)  
**Images Fixed**: 6 images now accessible
