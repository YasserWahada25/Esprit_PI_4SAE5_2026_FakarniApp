# 🔧 Fakarni App - Fixes Implementation Plan

## Issues to Fix

### 1. ✅ Post-Service - Image Upload
**Status**: Already Working (Base64 storage)
- Images are stored as LONGTEXT (Base64) in database
- No file system storage needed
- **Action**: Verify frontend is sending Base64 correctly

### 2. 🔴 Group-Service - 403 Forbidden Error
**Problem**: Security requires JWT authentication
**Root Cause**: SecurityConfig requires authentication for all requests except /actuator/**
**Solution**: 
- Update SecurityConfig to allow public access to group creation
- OR ensure frontend sends JWT token in Authorization header

### 3. 🔴 Activite-Educative-Service - Images Not Loading
**Problem**: Images uploaded but not accessible
**Root Cause**: Need image serving endpoint and proper file storage
**Solution**:
- Add image serving endpoint
- Configure proper upload directory in Docker
- Add volume mapping in docker-compose.yml

### 4. 🔴 Activite-Educative-Service - Map API Not Showing
**Problem**: No map API key configured
**Solution**:
- Add Google Maps API key to .env
- Configure in application properties
- Pass to frontend via environment

### 5. 🔴 Geofencing-Service - Map Not Showing
**Problem**: Same as #4 - No map API key
**Solution**: Same as #4

### 6. 🔴 Geofencing-Service - No User Selection
**Problem**: No endpoint to get users for zone tracking
**Solution**:
- Add UserClient Feign client (already exists!)
- Add endpoint to get all users
- Filter by role (patients/caregivers)

## Implementation Order

1. Fix Group-Service 403 error (Security Config)
2. Add Google Maps API key configuration
3. Fix Activite-Educative image serving
4. Add Geofencing user selection endpoint
5. Update docker-compose.yml with volumes
6. Rebuild and test all containers

## Files to Modify

### Backend
- `backend/group/src/main/java/com/alzheimer/group_service/config/SecurityConfig.java`
- `backend/Geofencing-Service/src/main/java/tn/SoftCare/Geofencing/Controller/GeofencingController.java`
- `backend/Geofencing-Service/src/main/java/tn/SoftCare/Geofencing/Client/UserClient.java`
- `backend/activite-educative-service/src/main/java/com/alzheimer/activite_educative_service/controllers/ActiviteEducativeController.java`
- `.env` (add Google Maps API key)
- `docker-compose.yml` (add volumes for images)

### Frontend
- Check API calls include Authorization header
- Verify Google Maps API key usage
