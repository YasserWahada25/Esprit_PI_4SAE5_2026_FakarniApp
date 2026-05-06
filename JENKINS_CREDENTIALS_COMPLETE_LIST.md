# 🔐 Complete Jenkins Credentials List

## Instructions
Add these credentials in Jenkins:
**Jenkins → Manage Jenkins → Credentials → System → Global credentials (unrestricted) → Add Credentials**

---

## ✅ Already Created (3 credentials)
1. ✅ `github-credentials` - Username with password (GitHub)
2. ✅ `dockerhub-credentials` - Username with password (Docker Hub)
3. ✅ `sonarqube-token` - Secret text (SonarQube)

---

## 📋 TO ADD: 18 New Credentials

### 1️⃣ MAIL CREDENTIALS (6 credentials)

#### 1. User Service - Gmail
- **Type**: Username with password
- **ID**: `mail-user-service`
- **Username**: `mohamadrayen.jbili@esprit.tn`
- **Password**: `ueivocwsiczztvem`
- **Description**: Gmail SMTP for User Service

#### 2. User Service - Gmail (Alternative ID for compatibility)
- **Type**: Username with password
- **ID**: `MAIL_USERNAME_USER`
- **Username**: `mohamadrayen.jbili@esprit.tn`
- **Password**: `ueivocwsiczztvem`
- **Description**: Gmail SMTP for User Service (env var name)

#### 3. Geofencing Service - Gmail
- **Type**: Username with password
- **ID**: `mail-geofencing-service`
- **Username**: `bahri.rania098@gmail.com`
- **Password**: `cfbyuwbpasuwnxvj`
- **Description**: Gmail SMTP for Geofencing Service

#### 4. Geofencing Service - Gmail (Alternative ID)
- **Type**: Username with password
- **ID**: `MAIL_USERNAME`
- **Username**: `bahri.rania098@gmail.com`
- **Password**: `cfbyuwbpasuwnxvj`
- **Description**: Gmail SMTP for Geofencing (env var name)

#### 5. Event Service - Mailtrap
- **Type**: Username with password
- **ID**: `mailtrap-credentials`
- **Username**: `votre-mailtrap-username`
- **Password**: `votre-mailtrap-password`
- **Description**: Mailtrap SMTP for Event Service

#### 6. Event Service - Mailtrap (Alternative ID)
- **Type**: Username with password
- **ID**: `MAILTRAP_USERNAME`
- **Username**: `votre-mailtrap-username`
- **Password**: `votre-mailtrap-password`
- **Description**: Mailtrap credentials (env var name)

---

### 2️⃣ OAUTH2 CREDENTIALS (4 credentials)

#### 7. Google OAuth2 - Client ID
- **Type**: Secret text
- **ID**: `google-client-id`
- **Secret**: `968599520946-llp69cv61a73f9457lpedn7m4tflrr2t.apps.googleusercontent.com`
- **Description**: Google OAuth2 Client ID

#### 8. Google OAuth2 - Client ID (Alternative ID)
- **Type**: Secret text
- **ID**: `GOOGLE_CLIENT_ID`
- **Secret**: `968599520946-llp69cv61a73f9457lpedn7m4tflrr2t.apps.googleusercontent.com`
- **Description**: Google OAuth2 Client ID (env var name)

#### 9. Facebook OAuth2 - App ID
- **Type**: Secret text
- **ID**: `facebook-app-id`
- **Secret**: `1270980888473415`
- **Description**: Facebook OAuth2 App ID

#### 10. Facebook OAuth2 - App Secret
- **Type**: Secret text
- **ID**: `facebook-app-secret`
- **Secret**: `your-facebook-app-secret-here`
- **Description**: Facebook OAuth2 App Secret

---

### 3️⃣ TWILIO CREDENTIALS (3 credentials)

#### 11. Twilio Account SID
- **Type**: Secret text
- **ID**: `twilio-account-sid`
- **Secret**: `AC67b937e03fdc6d358fe90c94866ca636`
- **Description**: Twilio Account SID for SMS

#### 12. Twilio Auth Token
- **Type**: Secret text
- **ID**: `twilio-auth-token`
- **Secret**: `5129a79de1a2e8c4e4a917dbc4f25f0a`
- **Description**: Twilio Auth Token for SMS

#### 13. Twilio Phone Number
- **Type**: Secret text
- **ID**: `twilio-from-number`
- **Secret**: `+16812708324`
- **Description**: Twilio From Phone Number

---

### 4️⃣ DATABASE CREDENTIALS (3 credentials)

#### 14. MySQL Root Password
- **Type**: Secret text
- **ID**: `mysql-root-password`
- **Secret**: `root`
- **Description**: MySQL Root Password (all databases)

#### 15. MongoDB Admin Username
- **Type**: Secret text
- **ID**: `mongo-root-username`
- **Secret**: `admin`
- **Description**: MongoDB Root Username

#### 16. MongoDB Admin Password
- **Type**: Secret text
- **ID**: `mongo-root-password`
- **Secret**: `admin`
- **Description**: MongoDB Root Password

---

### 5️⃣ JWT SECRET (2 credentials)

#### 17. JWT Secret
- **Type**: Secret text
- **ID**: `jwt-secret`
- **Secret**: `ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=`
- **Description**: JWT Secret (shared across Gateway and all services)

#### 18. JWT Secret (Alternative ID)
- **Type**: Secret text
- **ID**: `JWT_SECRET`
- **Secret**: `ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=`
- **Description**: JWT Secret (env var name)

---

## 📊 Summary

| Category | Count | IDs |
|----------|-------|-----|
| **Already Created** | 3 | github-credentials, dockerhub-credentials, sonarqube-token |
| **Mail** | 6 | mail-user-service, MAIL_USERNAME_USER, mail-geofencing-service, MAIL_USERNAME, mailtrap-credentials, MAILTRAP_USERNAME |
| **OAuth2** | 4 | google-client-id, GOOGLE_CLIENT_ID, facebook-app-id, facebook-app-secret |
| **Twilio** | 3 | twilio-account-sid, twilio-auth-token, twilio-from-number |
| **Database** | 3 | mysql-root-password, mongo-root-username, mongo-root-password |
| **JWT** | 2 | jwt-secret, JWT_SECRET |
| **TOTAL TO ADD** | **18** | |
| **GRAND TOTAL** | **21** | |

---

## 🎯 Quick Add Script (Copy-Paste Values)

### For Jenkins UI:
1. Go to: http://localhost:8085
2. Navigate: Manage Jenkins → Credentials → System → Global credentials → Add Credentials
3. For each credential above:
   - Select the Type
   - Enter the ID (exactly as shown)
   - Enter Username/Password or Secret
   - Enter Description
   - Click "Create"

---

## ⚠️ Important Notes

1. **Duplicate IDs**: Some credentials have 2 versions (e.g., `mail-user-service` and `MAIL_USERNAME_USER`)
   - This is intentional for compatibility with different pipeline scripts
   - Some pipelines use descriptive IDs, others use env var names

2. **Mailtrap**: Currently has placeholder values (`votre-mailtrap-username`)
   - Update with real Mailtrap credentials if you want email testing
   - Or leave as-is if not using Event Service emails

3. **Facebook Secret**: Currently has placeholder (`your-facebook-app-secret-here`)
   - Update with real Facebook App Secret if using Facebook OAuth
   - Or leave as-is if not using Facebook login

4. **Security**: These credentials are stored securely in Jenkins
   - Never commit them to Git
   - `.env` file is already in `.gitignore`

---

## ✅ Verification

After adding all credentials, verify in Jenkins:
```
Manage Jenkins → Credentials → System → Global credentials
```

You should see **21 total credentials**:
- 3 already created
- 18 newly added

---

**Ready to add these credentials? Let's do it! 🚀**
