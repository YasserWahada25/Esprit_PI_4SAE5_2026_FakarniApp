# ✅ Deployment Checklist - Fakarni App Fixes

## Pre-Deployment

### 1. Environment Setup
- [ ] `.env` file exists in project root
- [ ] `GOOGLE_MAPS_API_KEY` added to `.env`
- [ ] Google Maps API key is valid and active
- [ ] Maps JavaScript API enabled in Google Cloud Console
- [ ] Geocoding API enabled in Google Cloud Console

### 2. Docker Environment
- [ ] Docker Desktop is running
- [ ] Docker Compose is installed
- [ ] Sufficient disk space (at least 10GB free)
- [ ] No port conflicts (4200, 8090, 8762, etc.)

### 3. Code Changes
- [ ] All modified files are saved
- [ ] No uncommitted changes (optional)
- [ ] Backup of current working version (optional)

---

## Deployment Steps

### Phase 1: Stop Services
- [ ] Run: `docker compose down`
- [ ] Verify all containers stopped
- [ ] Check no orphan containers: `docker ps -a`

### Phase 2: Build Services
- [ ] Run: `docker compose build group-service`
- [ ] Run: `docker compose build post-service`
- [ ] Run: `docker compose build activite-educative-service`
- [ ] Run: `docker compose build geofencing-service`
- [ ] Verify all builds successful (no errors)

### Phase 3: Start Services
- [ ] Run: `docker compose up -d`
- [ ] Wait 60 seconds for startup
- [ ] Check all containers running: `docker compose ps`
- [ ] Verify 28 containers are UP

### Phase 4: Service Registration
- [ ] Open Eureka: http://localhost:8762
- [ ] Verify all services registered:
  - [ ] USER-SERVICE
  - [ ] TRACKING-SERVICE
  - [ ] GEOFENCING-SERVICE
  - [ ] ACTIVITE-EDUCATIVE-SERVICE
  - [ ] CHAT-SERVICE
  - [ ] SESSION-SERVICE
  - [ ] DETECTION-MALADIE-SERVICE
  - [ ] DOSSIER-MEDICAL-SERVICE
  - [ ] EVENT-SERVICE
  - [ ] GROUP-SERVICE
  - [ ] MEETING-INSIGHTS-SERVICE
  - [ ] POST-SERVICE
  - [ ] SUIVI-ENGAGEMENT-SERVICE

---

## Post-Deployment Testing

### Test 1: Post-Service
- [ ] Open frontend: http://localhost:4200
- [ ] Navigate to Posts section
- [ ] Create new post with text only
- [ ] Create new post with image (Base64)
- [ ] Verify post appears in list
- [ ] Verify image displays correctly
- [ ] Edit post
- [ ] Delete post
- [ ] **Result**: ✅ No 403 errors, images work

### Test 2: Group-Service
- [ ] Navigate to Groups section
- [ ] Click "Create Group"
- [ ] Fill in group details:
  - Name: "Test Group"
  - Description: "Testing fixes"
  - Type: PUBLIC
- [ ] Submit form
- [ ] Verify group created (no 403 error)
- [ ] Add member to group
- [ ] Update group details
- [ ] Delete group
- [ ] **Result**: ✅ No 403 errors

### Test 3: Activite-Educative Images
- [ ] Navigate to Activities section
- [ ] Click "Create Activity"
- [ ] Fill in activity details
- [ ] Upload thumbnail image
- [ ] Submit form
- [ ] Verify activity created
- [ ] Check image displays in activity card
- [ ] Open activity details
- [ ] Verify image loads correctly
- [ ] Test image URL directly: `/uploads/activities/{filename}`
- [ ] **Result**: ✅ Images upload and display

### Test 4: Activite-Educative Maps
- [ ] Navigate to Events section
- [ ] Click "Create Event"
- [ ] Verify Google Maps loads
- [ ] Click on map to select location
- [ ] Verify coordinates captured
- [ ] Submit event
- [ ] **Result**: ✅ Maps load and work

### Test 5: Geofencing Maps
- [ ] Navigate to Geofencing section
- [ ] Click "Create Zone"
- [ ] Verify Google Maps loads
- [ ] Click on map to set center point
- [ ] Adjust radius
- [ ] Verify zone preview shows on map
- [ ] **Result**: ✅ Maps load and work

### Test 6: Geofencing User Selection
- [ ] In "Create Zone" form
- [ ] Open "Select Patient" dropdown
- [ ] Verify list of patients appears
- [ ] Select a patient
- [ ] Open "Select Caregiver" dropdown
- [ ] Verify list of caregivers appears
- [ ] Select a caregiver
- [ ] Submit zone
- [ ] **Result**: ✅ User dropdowns populated

---

## API Endpoint Testing

### Post-Service Endpoints
```bash
# Test 1: Create post
curl -X POST http://localhost:8090/api/posts \
  -H "Content-Type: application/json" \
  -d '{"content":"Test post","imageUrl":"data:image/png;base64,..."}'
```
- [ ] Response: 201 Created
- [ ] No 403 error

### Group-Service Endpoints
```bash
# Test 2: Create group
curl -X POST http://localhost:8090/api/groups \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","description":"Test","groupType":"PUBLIC"}'
```
- [ ] Response: 201 Created
- [ ] No 403 error

### Geofencing Endpoints
```bash
# Test 3: Get users
curl http://localhost:8090/api/geofencing/users

# Test 4: Get patients
curl http://localhost:8090/api/geofencing/users/by-role?role=PATIENT

# Test 5: Get Maps API key
curl http://localhost:8090/api/geofencing/config/maps-api-key
```
- [ ] Users list returned
- [ ] Patients list returned
- [ ] API key returned

### Activite-Educative Endpoints
```bash
# Test 6: Get Maps API key
curl http://localhost:8090/api/activities/config/maps-api-key

# Test 7: Access image
curl http://localhost:8090/api/activities/uploads/activities/{filename}
```
- [ ] API key returned
- [ ] Image file served

---

## Log Verification

### Check for Errors
```bash
# Check all services
docker compose logs --tail=50

# Check specific services
docker compose logs -f group-service
docker compose logs -f post-service
docker compose logs -f activite-educative-service
docker compose logs -f geofencing-service
```

### Look for:
- [ ] No ERROR level logs
- [ ] No stack traces
- [ ] No connection refused errors
- [ ] Services registered with Eureka
- [ ] Database connections successful

---

## Performance Verification

### Container Health
```bash
docker compose ps
```
- [ ] All containers show "Up" status
- [ ] No containers restarting
- [ ] No containers in "Exit" state

### Resource Usage
```bash
docker stats --no-stream
```
- [ ] CPU usage < 80% per container
- [ ] Memory usage reasonable
- [ ] No containers using excessive resources

### Database Connections
```bash
# Check MySQL containers
docker exec fakarni_db_group mysql -uroot -proot -e "SHOW PROCESSLIST;"
docker exec fakarni_db_post mysql -uroot -proot -e "SHOW PROCESSLIST;"
```
- [ ] Connections established
- [ ] No connection errors

---

## Frontend Verification

### Browser Console
- [ ] Open browser DevTools (F12)
- [ ] Navigate to Console tab
- [ ] Check for errors
- [ ] **Expected**: No red errors

### Network Tab
- [ ] Open Network tab
- [ ] Perform actions (create post, group, etc.)
- [ ] Check API calls
- [ ] **Expected**: All calls return 200/201, no 403/500

### Application Functionality
- [ ] Login works
- [ ] Navigation works
- [ ] All pages load
- [ ] No broken images
- [ ] Maps display correctly
- [ ] Forms submit successfully

---

## Security Verification

### API Access
- [ ] Public endpoints accessible without auth
- [ ] Protected endpoints require JWT
- [ ] No sensitive data exposed in logs
- [ ] API keys not visible in frontend source

### CORS Configuration
- [ ] Frontend can call backend APIs
- [ ] No CORS errors in console
- [ ] Credentials handled correctly

---

## Rollback Plan (If Needed)

### If Deployment Fails:

1. **Stop new containers**:
```bash
docker compose down
```

2. **Restore previous version**:
```bash
git checkout HEAD~1  # If using git
# OR restore from backup
```

3. **Rebuild and restart**:
```bash
docker compose build
docker compose up -d
```

4. **Verify old version works**

---

## Success Criteria

### All Must Pass:
- ✅ All 28 containers running
- ✅ All services registered in Eureka
- ✅ No errors in logs
- ✅ Frontend accessible
- ✅ Post creation works with images
- ✅ Group creation works (no 403)
- ✅ Activity images upload and display
- ✅ Maps load in Geofencing
- ✅ Maps load in Activities
- ✅ User selection works in Geofencing
- ✅ No errors in browser console
- ✅ All API endpoints respond correctly

---

## Post-Deployment Tasks

### Documentation
- [ ] Update team on changes
- [ ] Document any issues encountered
- [ ] Update deployment notes

### Monitoring
- [ ] Set up log monitoring (optional)
- [ ] Monitor resource usage
- [ ] Check for any delayed errors

### Backup
- [ ] Backup database volumes
- [ ] Backup uploaded images
- [ ] Document current configuration

---

## Sign-Off

### Deployment Information
- **Date**: _______________
- **Time**: _______________
- **Deployed By**: _______________
- **Version**: Fixes v1.0

### Verification
- [ ] All tests passed
- [ ] No critical errors
- [ ] Team notified
- [ ] Documentation updated

### Approval
- **Approved By**: _______________
- **Signature**: _______________
- **Date**: _______________

---

## Notes

### Issues Encountered:
```
[Write any issues here]
```

### Resolutions:
```
[Write resolutions here]
```

### Additional Changes:
```
[Write any additional changes here]
```

---

**Deployment Status**: ⬜ Not Started | ⬜ In Progress | ⬜ Complete | ⬜ Failed

**Overall Result**: ⬜ Success | ⬜ Partial Success | ⬜ Failed

---

**Last Updated**: May 3, 2026
**Document Version**: 1.0
