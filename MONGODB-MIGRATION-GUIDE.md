# MongoDB Data Migration Guide

## Problem
Your existing users from local MongoDB are not accessible in Docker because:
1. Local MongoDB service is not running (port 27017)
2. Docker MongoDB is a fresh instance with only test users

## Solution Options

### Option 1: Migrate Data from Local MongoDB (Recommended)

If you have existing users in your local MongoDB that you want to keep:

#### Step 1: Start Local MongoDB
```bash
# Windows Service
net start MongoDB

# Or start MongoDB manually if installed standalone
```

#### Step 2: Run Migration Script
```powershell
.\migrate-mongodb-data.ps1
```

This script will:
- ✅ Export all data from local MongoDB (`rayen` database)
- ✅ Backup existing Docker MongoDB data
- ✅ Import your local data to Docker MongoDB
- ✅ Verify the migration

#### Step 3: Test Login
Open http://localhost:4200 and login with your existing credentials.

---

### Option 2: Start Fresh (Quick)

If you don't need old data, just create new users:

1. Open http://localhost:4200
2. Click "Sign Up"
3. Create a new account
4. Login with the new credentials

---

### Option 3: Manual Export/Import

If the script doesn't work, do it manually:

#### Export from Local MongoDB
```bash
# Make sure local MongoDB is running
mongodump --uri="mongodb://localhost:27017/rayen" --out="backup"
```

#### Import to Docker MongoDB
```bash
# Copy backup to container
docker cp backup/rayen fakarni_mongo:/tmp/restore

# Drop existing database
docker exec fakarni_mongo mongosh -u admin -p admin --authenticationDatabase admin rayen --eval "db.dropDatabase()"

# Restore data
docker exec fakarni_mongo mongorestore -u admin -p admin --authenticationDatabase admin --db=rayen /tmp/restore
```

#### Verify
```bash
docker exec fakarni_mongo mongosh -u admin -p admin --authenticationDatabase admin rayen --eval "db.users.countDocuments()"
```

---

## Current Docker MongoDB Status

Database: `rayen`  
Collections: `users`, `sessions`  
Current users: 2 (test users created after Docker setup)

---

## Troubleshooting

### "Cannot connect to local MongoDB"
- Local MongoDB service is not running
- Start it with: `net start MongoDB` (Windows)
- Or check if MongoDB is installed

### "mongodump command not found"
- MongoDB Database Tools not installed
- Download from: https://www.mongodb.com/try/download/database-tools
- Or use mongosh export commands

### "Authentication failed"
- Docker MongoDB credentials: `admin:admin`
- These are set in `.env` file

### "Database already exists"
- The script backs up existing data before importing
- Backup location: `mongodb-docker-backup/`

---

## Database Connection Details

### Local MongoDB (Development)
```
URI: mongodb://localhost:27017/rayen
Database: rayen
Auth: None (local development)
```

### Docker MongoDB (Containerized)
```
URI: mongodb://admin:admin@localhost:27018/rayen?authSource=admin
Database: rayen
Username: admin
Password: admin
Port: 27018 (mapped from container's 27017)
```

### User Service Configuration
The User Service automatically connects to the correct MongoDB based on profile:
- **Local profile**: `mongodb://localhost:27017/rayen`
- **Docker profile**: `mongodb://admin:admin@mongodb:27017/rayen?authSource=admin`

---

## Next Steps

1. **If you have existing users**: Run `.\migrate-mongodb-data.ps1`
2. **If starting fresh**: Just create new accounts at http://localhost:4200
3. **Test OAuth**: Google Sign-In button should now be visible
4. **Test Email**: Password reset should send emails

All OAuth and email configurations are now properly set up! 🎉
