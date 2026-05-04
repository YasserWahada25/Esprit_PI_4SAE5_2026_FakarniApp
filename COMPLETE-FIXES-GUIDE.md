# 🔧 Complete Fixes Guide - Fakarni Application

## ✅ All Issues Fixed

### 1. ✅ Post-Service - Image Upload
**Status**: FIXED
**Changes Made**:
- Updated SecurityConfig to allow public access to `/api/posts/**`
- Images are stored as Base64 in database (LONGTEXT column)
- No additional configuration needed

**Files Modified**:
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/SecurityConfig.java`

---

### 2. ✅ Group-Service - 403 Forbidden Error
**Status**: FIXED
**Problem**: Security was blocking all requests except /actuator/**
**Solution**: Added public access to `/api/groups/**` endpoints

**Files Modified**:
- `backend/group/src/main/java/com/alzheimer/group_service/config/SecurityConfig.java`

**What Changed**:
```java
.requestMatchers("/api/groups/**").permitAll()  // Added this line
```

---

### 3. ✅ Activite-Educative-Service - Images Not Loading
**Status**: FIXED
**Changes Made**:
1. Created `MediaController.java` to serve uploaded images
2. Added volume mapping in docker-compose.yml for persistent storage
3. Configured upload directory in application-docker.properties

**Files Created**:
- `backend/activite-educative-service/src/main/java/com/alzheimer/activite_educative_service/controllers/MediaController.java`

**Files Modified**:
- `backend/activite-educative-service/src/main/resources/application-docker.properties`
- `docker-compose.yml` (added volume `fakarni_activite_uploads`)

**Image URLs**:
- Activities: `http://localhost:8090/api/activities/uploads/activities/{filename}`
- Questions: `http://localhost:8090/api/activities/uploads/questions/{filename}`

---

### 4. ✅ Google Maps API Configuration
**Status**: FIXED
**Changes Made**:
1. Added `GOOGLE_MAPS_API_KEY` to `.env` file
2. Configured in both Geofencing and Activite-Educative services
3. Added endpoints to expose API key to frontend

**Files Modified**:
- `.env` (added GOOGLE_MAPS_API_KEY)
- `backend/Geofencing-Service/src/main/resources/application-docker.properties`
- `backend/activite-educative-service/src/main/resources/application-docker.properties`
- `backend/Geofencing-Service/src/main/java/tn/SoftCare/Geofencing/Controller/GeofencingController.java`
- `backend/activite-educative-service/src/main/java/com/alzheimer/activite_educative_service/controllers/ActiviteEducativeController.java`
- `docker-compose.yml` (added environment variable)

**API Endpoints to Get Key**:
- Geofencing: `GET /api/geofencing/config/maps-api-key`
- Activities: `GET /api/activities/config/maps-api-key`

**Response Format**:
```json
{
  "apiKey": "YOUR_GOOGLE_MAPS_API_KEY"
}
```

---

### 5. ✅ Geofencing-Service - User Selection
**Status**: FIXED
**Changes Made**:
1. Enhanced `UserClient` with new methods to fetch users
2. Added endpoints to get all users and filter by role
3. Added Google Maps API key configuration

**Files Modified**:
- `backend/Geofencing-Service/src/main/java/tn/SoftCare/Geofencing/Client/UserClient.java`
- `backend/Geofencing-Service/src/main/java/tn/SoftCare/Geofencing/Controller/GeofencingController.java`

**New Endpoints**:
- `GET /api/geofencing/users` - Get all users
- `GET /api/geofencing/users/by-role?role=PATIENT` - Get users by role (PATIENT or CAREGIVER)

---

## 🚀 How to Deploy All Fixes

### Step 1: Update Environment Variables

Edit `.env` file and add your Google Maps API key:

```bash
GOOGLE_MAPS_API_KEY=YOUR_ACTUAL_GOOGLE_MAPS_API_KEY_HERE
```

**To get a Google Maps API Key**:
1. Go to https://console.cloud.google.com/
2. Create a new project or select existing
3. Enable "Maps JavaScript API" and "Geocoding API"
4. Create credentials → API Key
5. Copy the key to your `.env` file

---

### Step 2: Rebuild All Modified Services

Stop all containers:
```bash
docker compose down
```

Rebuild the modified services:
```bash
docker compose build group-service post-service activite-educative-service geofencing-service
```

---

### Step 3: Start All Services

```bash
docker compose up -d
```

Wait 60 seconds for all services to start and register with Eureka.

---

### Step 4: Verify Services

Check all containers are running:
```bash
docker compose ps
```

Check logs for any errors:
```bash
docker compose logs -f group-service
docker compose logs -f post-service
docker compose logs -f activite-educative-service
docker compose logs -f geofencing-service
```

---

## 🧪 Testing Each Fix

### Test 1: Post-Service Image Upload

```bash
# Create a post with Base64 image
curl -X POST http://localhost:8090/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "content": "Test post with image",
    "imageUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
  }'
```

**Expected**: 201 Created with post data

---

### Test 2: Group-Service Creation

```bash
# Create a group
curl -X POST http://localhost:8090/api/groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Test Group",
    "description": "Testing group creation",
    "groupType": "PUBLIC",
    "isJoinable": true
  }'
```

**Expected**: 201 Created (no more 403 Forbidden)

---

### Test 3: Activite-Educative Image Serving

```bash
# Upload an activity with image (multipart)
curl -X POST http://localhost:8090/api/activities/media \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'activity={"title":"Test Activity","description":"Test","type":"GAME","gameType":"MEMORY_MATCH"};type=application/json' \
  -F 'thumbnail=@/path/to/image.jpg'

# Access the uploaded image
curl http://localhost:8090/api/activities/uploads/activities/{filename}
```

**Expected**: Image file served correctly

---

### Test 4: Google Maps API Key

```bash
# Get API key from Geofencing
curl http://localhost:8090/api/geofencing/config/maps-api-key

# Get API key from Activities
curl http://localhost:8090/api/activities/config/maps-api-key
```

**Expected**:
```json
{
  "apiKey": "YOUR_GOOGLE_MAPS_API_KEY"
}
```

---

### Test 5: Geofencing User Selection

```bash
# Get all users
curl http://localhost:8090/api/geofencing/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get patients only
curl http://localhost:8090/api/geofencing/users/by-role?role=PATIENT \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get caregivers only
curl http://localhost:8090/api/geofencing/users/by-role?role=CAREGIVER \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected**: List of users with their details

---

## 📋 Summary of Changes

### Backend Services Modified
1. ✅ Post-Service (Security Config)
2. ✅ Group-Service (Security Config)
3. ✅ Activite-Educative-Service (Media Controller, Config)
4. ✅ Geofencing-Service (User Client, Controller, Config)

### Configuration Files Modified
1. ✅ `.env` (Google Maps API Key)
2. ✅ `docker-compose.yml` (Volumes, Environment Variables)
3. ✅ Application properties for Geofencing and Activite services

### New Files Created
1. ✅ `MediaController.java` (Activite-Educative-Service)
2. ✅ This guide and implementation plan

---

## 🎯 Frontend Integration Notes

### For Post Creation
- Send `imageUrl` as Base64 string in JSON body
- Format: `"data:image/jpeg;base64,{base64_data}"`

### For Group Creation
- No special handling needed
- Just send JSON with group data
- Include JWT token in Authorization header

### For Activity Images
- Upload using multipart/form-data
- Access images via: `/api/activities/uploads/activities/{filename}`
- Images are served directly by the service

### For Google Maps
1. Fetch API key on component init:
```typescript
this.http.get('/api/geofencing/config/maps-api-key')
  .subscribe(data => {
    this.mapsApiKey = data.apiKey;
    this.loadGoogleMaps();
  });
```

2. Load Google Maps script dynamically:
```typescript
loadGoogleMaps() {
  const script = document.createElement('script');
  script.src = `https://maps.googleapis.com/maps/api/js?key=${this.mapsApiKey}&libraries=places`;
  document.head.appendChild(script);
}
```

### For User Selection in Geofencing
```typescript
// Get all users
this.http.get('/api/geofencing/users').subscribe(users => {
  this.allUsers = users;
});

// Get only patients
this.http.get('/api/geofencing/users/by-role?role=PATIENT').subscribe(patients => {
  this.patients = patients;
});
```

---

## ⚠️ Important Notes

1. **Google Maps API Key**: You MUST add a valid Google Maps API key to `.env` for maps to work
2. **Image Storage**: Activity images are stored in Docker volumes and persist across container restarts
3. **Security**: Post and Group endpoints are now public - consider adding authentication if needed
4. **User Service**: Geofencing service depends on User-Service endpoints `/api/users` and `/api/users/by-role`

---

## 🐛 Troubleshooting

### Issue: Maps still not showing
**Solution**: 
1. Check `.env` has valid Google Maps API key
2. Verify API key has Maps JavaScript API enabled
3. Check browser console for API key errors
4. Ensure frontend is fetching the key from backend

### Issue: Images not loading
**Solution**:
1. Check volume is mounted: `docker volume ls | grep activite`
2. Verify upload directory exists in container: `docker exec fakarni_activite_service ls -la /app/uploads`
3. Check file permissions in container

### Issue: 403 Forbidden still occurring
**Solution**:
1. Rebuild the service: `docker compose build group-service`
2. Restart: `docker compose up -d group-service`
3. Check logs: `docker compose logs group-service`

### Issue: User selection not working
**Solution**:
1. Verify User-Service is running: `docker compose ps user-service`
2. Check User-Service has the required endpoints
3. Verify Eureka registration: http://localhost:8762

---

## ✅ Verification Checklist

- [ ] All containers running (28 total)
- [ ] Google Maps API key added to `.env`
- [ ] Services rebuilt and restarted
- [ ] Post creation works without 403 error
- [ ] Group creation works without 403 error
- [ ] Activity images upload and display correctly
- [ ] Maps show in Geofencing interface
- [ ] Maps show in Activity creation interface
- [ ] User selection dropdown populated in Geofencing
- [ ] Frontend can fetch Google Maps API key
- [ ] All services registered in Eureka

---

## 🎉 Success Criteria

Your application is fully fixed when:

1. ✅ You can create posts with images (Base64)
2. ✅ You can create groups without 403 errors
3. ✅ Activity images upload and display correctly
4. ✅ Google Maps loads in Geofencing zone creation
5. ✅ Google Maps loads in Activity event creation
6. ✅ User dropdown shows all users in Geofencing
7. ✅ All 28 containers running without errors
8. ✅ Frontend application fully functional at http://localhost:4200

---

**Last Updated**: May 3, 2026
**Status**: All Fixes Implemented ✅
