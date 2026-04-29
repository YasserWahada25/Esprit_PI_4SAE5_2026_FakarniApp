# Résolution de l'erreur 403 Forbidden - Étapes à suivre

## Problème
Le frontend Angular reçoit une erreur 403 Forbidden lors de la création ou récupération de posts, même avec un token JWT valide.

## Cause identifiée
Le secret JWT du Post-Service ne correspondait pas à celui du User-Service.

## ✅ Modifications effectuées

### 1. Configuration JWT synchronisée
**Fichier:** `backend/Post-Service/src/main/resources/application.properties`

Changé de:
```properties
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation1234567890
```

À:
```properties
jwt.secret=${JWT_SECRET:ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=}
```

### 2. Configuration CORS ajoutée
**Fichier:** `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/SecurityConfig.java`

Ajout d'une configuration CORS appropriée pour permettre les requêtes depuis `http://localhost:4200`.

### 3. Logs de débogage améliorés
**Fichier:** `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/JwtAuthenticationFilter.java`

Ajout de logs pour faciliter le débogage de l'authentification JWT.

## 🔧 Étapes à suivre MAINTENANT

### Étape 1: Redémarrer le Post-Service

**IMPORTANT:** Les modifications ne prendront effet qu'après redémarrage!

#### Option A: Via IDE
1. Arrêtez le Post-Service dans votre IDE
2. Nettoyez le projet: `Run > Clean`
3. Recompilez: `Build > Rebuild Project`
4. Redémarrez le Post-Service

#### Option B: Via ligne de commande (Windows)
```powershell
# Ouvrez PowerShell dans le dossier du projet
cd backend\Post-Service

# Arrêtez le service (Ctrl+C si il tourne)

# Nettoyez et recompilez
mvn clean install -DskipTests

# Redémarrez
mvn spring-boot:run
```

### Étape 2: Vérifier le démarrage

Attendez que vous voyiez dans les logs:
```
Started PostServiceApplication in X.XXX seconds
Tomcat started on port 8069
Registered instance POST-SERVICE with eureka
```

### Étape 3: Tester l'authentification

#### Option A: Script automatique (Windows)
```powershell
.\test-post-service.ps1
```

#### Option B: Test manuel

1. **Connectez-vous dans le frontend Angular**
   - Allez sur http://localhost:4200
   - Connectez-vous avec vos identifiants
   - Ouvrez la console du navigateur (F12)

2. **Vérifiez le token**
   ```javascript
   // Dans la console du navigateur
   sessionStorage.getItem('fakarni_token')
   ```
   Vous devriez voir un token JWT.

3. **Essayez de créer un post**
   - Allez sur "Create Post"
   - Écrivez du contenu (minimum 10 caractères)
   - Cliquez sur "Submit"

4. **Vérifiez les logs dans la console**
   Vous devriez voir:
   ```
   🔐 Auth Interceptor: { url: '/api/posts', hasToken: true, token: 'eyJ...' }
   ✅ Token added to request: Bearer eyJ...
   ```

### Étape 4: Vérifier le résultat

#### ✅ Succès
- Le post est créé sans erreur
- Vous êtes redirigé vers la liste des posts
- Le post apparaît dans la liste avec vos informations utilisateur

#### ❌ Toujours 403?

Si vous obtenez toujours une erreur 403, vérifiez:

1. **Le Post-Service a-t-il bien redémarré?**
   ```bash
   # Vérifiez les logs du Post-Service
   # Recherchez "JWT validated successfully"
   ```

2. **Le token est-il valide?**
   - Décodez-le sur https://jwt.io
   - Vérifiez qu'il n'est pas expiré
   - Vérifiez qu'il contient les champs `sub` et `role`

3. **Les services sont-ils tous démarrés?**
   ```bash
   curl http://localhost:8761/eureka/apps
   ```
   Vous devriez voir: USER-SERVICE, POST-SERVICE, GATEWAY-SERVICE

4. **Reconnectez-vous**
   - Déconnectez-vous du frontend
   - Reconnectez-vous pour obtenir un nouveau token
   - Réessayez

## 📋 Checklist complète

- [ ] Modifications du code effectuées (déjà fait ✅)
- [ ] Post-Service redémarré
- [ ] Aucune erreur dans les logs du Post-Service
- [ ] Post-Service enregistré dans Eureka
- [ ] User-Service fonctionne (test de login OK)
- [ ] Token JWT obtenu après connexion
- [ ] Token visible dans sessionStorage
- [ ] Intercepteur ajoute le token aux requêtes
- [ ] Test de création de post réussi

## 🔍 Débogage avancé

### Voir les logs JWT du Post-Service

Ajoutez dans `backend/Post-Service/src/main/resources/application.properties`:
```properties
logging.level.com.alzheimer.post_service.config=DEBUG
logging.level.org.springframework.security=DEBUG
```

Redémarrez et observez les logs lors d'une requête.

### Tester directement le Post-Service (bypass Gateway)

```bash
# 1. Obtenez un token
TOKEN="<votre-token-ici>"

# 2. Testez directement le Post-Service
curl -v http://localhost:8069/api/posts \
  -H "Authorization: Bearer $TOKEN"
```

Si cela fonctionne mais pas via la Gateway, le problème vient de la Gateway.

### Vérifier la Gateway

```bash
# Vérifiez que la Gateway route bien vers le Post-Service
curl http://localhost:8090/actuator/gateway/routes
```

## 📞 Support

Si après toutes ces étapes le problème persiste:

1. **Partagez les logs du Post-Service**
   - Recherchez les lignes contenant "JWT" ou "authentication"
   - Partagez les erreurs complètes

2. **Partagez le token JWT décodé**
   - Allez sur https://jwt.io
   - Collez votre token
   - Partagez le payload (sans le token complet)

3. **Vérifiez la configuration**
   ```bash
   # Post-Service
   grep "jwt.secret" backend/Post-Service/src/main/resources/application.properties
   
   # User-Service
   grep "jwt.secret" backend/User-Service/src/main/resources/application.properties
   ```
   Les deux doivent être identiques!

## 🎯 Résultat attendu

Après avoir suivi toutes ces étapes:
- ✅ Connexion au frontend fonctionne
- ✅ Token JWT stocké dans sessionStorage
- ✅ Création de post fonctionne
- ✅ Liste des posts s'affiche avec informations utilisateur
- ✅ Aucune erreur 403 dans la console

## 📚 Documents de référence

- `backend/Post-Service/RESTART_GUIDE.md` - Guide de redémarrage détaillé
- `backend/Post-Service/TROUBLESHOOTING.md` - Dépannage complet
- `backend/Post-Service/INTEGRATION_USER_SERVICE.md` - Documentation de l'intégration
- `backend/Post-Service/TEST_INTEGRATION.md` - Tests d'intégration
- `test-post-service.ps1` - Script de test automatique (Windows)
- `test-post-service.sh` - Script de test automatique (Linux/Mac)
