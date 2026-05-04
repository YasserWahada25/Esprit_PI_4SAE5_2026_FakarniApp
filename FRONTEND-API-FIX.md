# Frontend API Connection Fix

## Problem
The frontend was calling `http://localhost:4200/auth/login` (itself) instead of the API Gateway at `http://localhost:8090`.

## Root Cause
- Frontend environment files had empty `apiUrl` and `apiBaseUrl`
- No nginx proxy rules to forward API requests to the backend

## Solution Applied
1. **Updated `frontend/nginx.conf`** - Added proxy rules for `/auth/`, `/api/`, `/session/`, and `/ws/` to forward to `api-gateway:8090`
2. **Updated `frontend/src/environments/environment.ts`** - Disabled Google/Facebook OAuth (not configured for Docker)
3. **Updated `frontend/src/environments/environment.prod.ts`** - Changed to use relative URLs (nginx handles proxying)

## Next Steps (Docker Desktop is currently having issues)

### 1. Restart Docker Desktop
Close and reopen Docker Desktop, or restart the Docker service.

### 2. Rebuild and Restart Frontend
```bash
cd C:\Users\jbili\OneDrive\Bureau\Fakarni_App

# Rebuild frontend with new nginx config
docker compose build frontend

# Restart just the frontend
docker compose up -d frontend
```

### 3. Test the Application
Open http://localhost:4200 and try to:
- Register a new account
- Login with credentials

The frontend will now proxy all `/auth/*` and `/api/*` requests through nginx to the API Gateway.

## Alternative: Quick Test Without Rebuild
If you want to test immediately, you can:
1. Open http://localhost:8081/auth/login directly in Postman
2. Send POST request with body:
```json
{
  "email": "test@example.com",
  "password": "Test1234"
}
```

## Verification
After rebuild, check browser console - you should see:
- ✅ `POST http://localhost:4200/auth/login` → proxied to gateway
- ❌ No more "405 Not Allowed" errors

## Files Changed
- `frontend/nginx.conf` - Added API proxy rules
- `frontend/src/environments/environment.ts` - Disabled OAuth
- `frontend/src/environments/environment.prod.ts` - Changed to relative URLs
