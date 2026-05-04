# MongoDB Data Migration Script
# This script exports data from local MongoDB and imports to Docker MongoDB

Write-Host "=== MongoDB Data Migration ===" -ForegroundColor Cyan

# Configuration
$LOCAL_MONGO_URI = "mongodb://localhost:27017"
$DOCKER_CONTAINER = "fakarni_mongo"
$DOCKER_MONGO_USER = "admin"
$DOCKER_MONGO_PASS = "admin"
$DATABASE_NAME = "rayen"
$BACKUP_DIR = "mongodb-backup"

# Step 1: Check if local MongoDB is running
Write-Host "`n[1/5] Checking local MongoDB..." -ForegroundColor Yellow
try {
    $localCheck = mongosh "$LOCAL_MONGO_URI/$DATABASE_NAME" --quiet --eval "db.stats()" 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Local MongoDB is not running on localhost:27017" -ForegroundColor Red
        Write-Host "Please start your local MongoDB service first:" -ForegroundColor Yellow
        Write-Host "  - Windows Service: net start MongoDB" -ForegroundColor Gray
        Write-Host "  - Or start MongoDB manually" -ForegroundColor Gray
        exit 1
    }
    Write-Host "✅ Local MongoDB is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Cannot connect to local MongoDB" -ForegroundColor Red
    exit 1
}

# Step 2: Export data from local MongoDB
Write-Host "`n[2/5] Exporting data from local MongoDB..." -ForegroundColor Yellow
if (Test-Path $BACKUP_DIR) {
    Remove-Item -Recurse -Force $BACKUP_DIR
}
New-Item -ItemType Directory -Path $BACKUP_DIR | Out-Null

mongodump --uri="$LOCAL_MONGO_URI/$DATABASE_NAME" --out="$BACKUP_DIR"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to export data" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Data exported to $BACKUP_DIR" -ForegroundColor Green

# Step 3: Check Docker MongoDB
Write-Host "`n[3/5] Checking Docker MongoDB..." -ForegroundColor Yellow
$dockerCheck = docker exec $DOCKER_CONTAINER mongosh -u $DOCKER_MONGO_USER -p $DOCKER_MONGO_PASS --authenticationDatabase admin --quiet --eval "db.version()" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker MongoDB is not running" -ForegroundColor Red
    Write-Host "Please start Docker containers: docker compose up -d" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Docker MongoDB is running" -ForegroundColor Green

# Step 4: Backup existing Docker data (optional)
Write-Host "`n[4/5] Backing up existing Docker data..." -ForegroundColor Yellow
$dockerBackupDir = "mongodb-docker-backup"
if (Test-Path $dockerBackupDir) {
    Remove-Item -Recurse -Force $dockerBackupDir
}
docker exec $DOCKER_CONTAINER mongodump -u $DOCKER_MONGO_USER -p $DOCKER_MONGO_PASS --authenticationDatabase admin --db=$DATABASE_NAME --out=/tmp/backup 2>&1 | Out-Null
docker cp "${DOCKER_CONTAINER}:/tmp/backup" $dockerBackupDir 2>&1 | Out-Null
Write-Host "✅ Docker data backed up to $dockerBackupDir" -ForegroundColor Green

# Step 5: Import data to Docker MongoDB
Write-Host "`n[5/5] Importing data to Docker MongoDB..." -ForegroundColor Yellow

# Copy backup to container
docker cp "$BACKUP_DIR/$DATABASE_NAME" "${DOCKER_CONTAINER}:/tmp/restore"

# Drop existing database and restore
docker exec $DOCKER_CONTAINER mongosh -u $DOCKER_MONGO_USER -p $DOCKER_MONGO_PASS --authenticationDatabase admin $DATABASE_NAME --quiet --eval "db.dropDatabase()" 2>&1 | Out-Null

# Restore data
docker exec $DOCKER_CONTAINER mongorestore -u $DOCKER_MONGO_USER -p $DOCKER_MONGO_PASS --authenticationDatabase admin --db=$DATABASE_NAME /tmp/restore

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to import data" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Data imported successfully" -ForegroundColor Green

# Verify
Write-Host "`n=== Verification ===" -ForegroundColor Cyan
$userCount = docker exec $DOCKER_CONTAINER mongosh -u $DOCKER_MONGO_USER -p $DOCKER_MONGO_PASS --authenticationDatabase admin $DATABASE_NAME --quiet --eval "db.users.countDocuments()" 2>&1
Write-Host "Users in Docker MongoDB: $userCount" -ForegroundColor Green

Write-Host "`n✅ Migration completed successfully!" -ForegroundColor Green
Write-Host "You can now login with your existing users at http://localhost:4200" -ForegroundColor Cyan
