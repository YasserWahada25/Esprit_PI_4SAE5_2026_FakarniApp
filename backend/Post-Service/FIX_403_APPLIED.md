# Correction appliquée pour l'erreur 403

## Problème identifié
Le secret JWT était encodé en Base64 dans les fichiers de configuration, mais le Post-Service le traitait comme du texte brut, alors que le User-Service le décodait correctement.

## Solution appliquée
Modification du `JwtService.java` pour décoder le secret Base64 comme le fait le User-Service:

```java
private SecretKey getSigningKey() {
    // Decode Base64 secret like User-Service does
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
}
```

Au lieu de:
```java
private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
}
```

## Étapes pour appliquer la correction

### 1. Redémarrer le Post-Service

**Option A: Via IDE (IntelliJ/Eclipse)**
1. Arrêtez le Post-Service
2. Redémarrez-le

**Option B: Via Maven**
```bash
cd backend/Post-Service
mvn clean install
mvn spring-boot:run
```

**Option C: Via JAR**
```bash
cd backend/Post-Service
mvn clean package
java -jar target/Post-Service-0.0.1-SNAPSHOT.jar
```

### 2. Vérifier que le service démarre correctement

Vous devriez voir dans les logs:
```
Started PostServiceApplication in X.XXX seconds
```

### 3. Tester la création d'un post

**Via le frontend:**
1. Connectez-vous avec un utilisateur existant
2. Allez sur "Create Post"
3. Créez un post
4. ✅ Le post devrait être créé avec succès

**Via curl:**
```bash
# 1. Se connecter
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

# 2. Créer un post
curl -X POST http://localhost:8090/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Test post après correction","imageUrl":null}'
```

### 4. Vérifier les logs

Dans la console du navigateur, vous devriez voir:
```
🔐 Auth Interceptor: {url: '/api/posts', hasToken: true, token: 'eyJhbGciOiJIUzI1NiJ9...'}
✅ Token added to request: Bearer eyJhbGciOiJIUzI1...
Post created successfully: {id: 1, content: "...", userId: "...", ...}
```

## Vérification du secret JWT

Les deux services utilisent maintenant le même secret:
- **User-Service**: `security.jwt.secret=ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=`
- **Post-Service**: `jwt.secret=ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=`

Les deux décodent maintenant le Base64 de la même manière.

## Décoder le secret (pour information)

```bash
echo "ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=" | base64 -d
# Résultat: dummy-secret-secret-secret-secret-secret-secret
```

## Fichiers modifiés

- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/JwtService.java`

## Prochaines étapes

Une fois le Post-Service redémarré, l'intégration devrait fonctionner correctement:
1. ✅ Les tokens JWT sont validés correctement
2. ✅ Le userId est extrait du token
3. ✅ Les posts sont associés à l'utilisateur connecté
4. ✅ Les informations utilisateur sont enrichies via Feign

## En cas de problème persistant

Si vous obtenez toujours une erreur 403:

1. **Vérifiez que le Post-Service a bien redémarré**
   ```bash
   curl http://localhost:8069/actuator/health
   ```

2. **Vérifiez les logs du Post-Service**
   Recherchez "JWT validation failed" ou "JWT authentication failed"

3. **Vérifiez que l'utilisateur est connecté**
   Dans la console du navigateur:
   ```javascript
   sessionStorage.getItem('fakarni_token')
   ```

4. **Testez avec un nouveau token**
   Déconnectez-vous et reconnectez-vous pour obtenir un nouveau token

5. **Vérifiez que tous les services sont démarrés**
   - Eureka (8761)
   - User-Service (8080)
   - Gateway (8090)
   - Post-Service (8069)
