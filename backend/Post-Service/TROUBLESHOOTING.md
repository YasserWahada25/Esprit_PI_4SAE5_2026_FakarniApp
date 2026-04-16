# Dépannage - Post-Service

## Erreur 403 Forbidden

### Symptôme
```
Failed to load resource: the server responded with a status of 403 (Forbidden)
```

### Causes possibles et solutions

#### 1. Secret JWT différent entre User-Service et Post-Service

**Vérification:**
```bash
# User-Service
grep "jwt.secret" backend/User-Service/src/main/resources/application.properties

# Post-Service
grep "jwt.secret" backend/Post-Service/src/main/resources/application.properties
```

**Solution:**
Les deux services doivent utiliser le même secret JWT:
```properties
jwt.secret=${JWT_SECRET:ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=}
```

#### 2. Token JWT invalide ou expiré

**Vérification:**
Décodez votre token sur https://jwt.io et vérifiez:
- La date d'expiration (`exp`)
- Le format du payload
- La présence du champ `role`

**Solution:**
Reconnectez-vous pour obtenir un nouveau token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Password123!"}'
```

#### 3. CORS mal configuré

**Vérification:**
Vérifiez les logs du navigateur pour des erreurs CORS.

**Solution:**
La configuration CORS dans `SecurityConfig.java` doit inclure votre origine:
```java
configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:3000"));
```

#### 4. Service non démarré ou non enregistré dans Eureka

**Vérification:**
```bash
# Vérifier Eureka
curl http://localhost:8761/eureka/apps

# Vérifier que Post-Service est enregistré
curl http://localhost:8761/eureka/apps/POST-SERVICE
```

**Solution:**
Redémarrez les services dans l'ordre:
1. Eureka-Service
2. User-Service
3. Post-Service

#### 5. Header Authorization mal formaté

**Vérification:**
Le header doit être exactement:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Solution:**
Vérifiez votre intercepteur Angular:
```typescript
const authReq = req.clone({
  setHeaders: {
    Authorization: `Bearer ${token}`
  }
});
```

## Logs de débogage

### Activer les logs détaillés

Ajoutez dans `application.properties`:
```properties
# Logs Spring Security
logging.level.org.springframework.security=DEBUG

# Logs JWT
logging.level.com.alzheimer.post_service.config=DEBUG

# Logs Feign
logging.level.feign=DEBUG
```

### Vérifier les logs du Post-Service

```bash
# Rechercher les erreurs JWT
grep "JWT" logs/post-service.log

# Rechercher les erreurs d'authentification
grep "authentication" logs/post-service.log
```

## Tests manuels

### 1. Test sans authentification (doit échouer avec 401)
```bash
curl -v http://localhost:8069/api/posts
```

### 2. Test avec token invalide (doit échouer avec 401)
```bash
curl -v http://localhost:8069/api/posts \
  -H "Authorization: Bearer invalid-token"
```

### 3. Test avec token valide (doit réussir)
```bash
# Obtenir un token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Password123!"}' \
  | jq -r '.token')

# Tester
curl -v http://localhost:8069/api/posts \
  -H "Authorization: Bearer $TOKEN"
```

## Checklist de vérification

- [ ] Eureka-Service est démarré (port 8761)
- [ ] User-Service est démarré et enregistré dans Eureka
- [ ] Post-Service est démarré et enregistré dans Eureka
- [ ] Les secrets JWT sont identiques dans les deux services
- [ ] Le token JWT est valide et non expiré
- [ ] Le header Authorization est correctement formaté
- [ ] CORS est configuré pour votre origine
- [ ] MySQL est démarré et accessible
- [ ] La table `posts` a la colonne `user_id`

## Commandes utiles

### Redémarrer le Post-Service
```bash
cd backend/Post-Service
mvn clean install
mvn spring-boot:run
```

### Vérifier la connexion MySQL
```bash
mysql -u root -p
USE post_service_db;
SHOW TABLES;
DESCRIBE posts;
```

### Vérifier Eureka
```bash
curl http://localhost:8761/eureka/apps | grep -i post-service
```

## Contact

Si le problème persiste après avoir vérifié tous ces points, vérifiez:
1. Les logs du Post-Service
2. Les logs du User-Service
3. La console du navigateur (Network tab)
