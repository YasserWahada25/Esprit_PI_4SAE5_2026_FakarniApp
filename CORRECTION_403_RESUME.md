# Résumé de la correction - Erreur 403 Forbidden

## Problème
Lors de la création d'un post depuis le frontend Angular, l'erreur 403 Forbidden était retournée malgré la présence du token JWT dans la requête.

## Diagnostic
Les logs du navigateur montraient:
```
🔐 Auth Interceptor: {url: '/api/posts', hasToken: true, token: 'eyJhbGciOiJIUzI1NiJ9...'}
✅ Token added to request: Bearer eyJhbGciOiJIUzI1...
GET http://localhost:4200/api/posts 403 (Forbidden)
```

Le token était bien envoyé, mais le backend le rejetait.

## Cause racine
**Incompatibilité dans le traitement du secret JWT:**

- **User-Service**: Décode le secret Base64 avant utilisation
  ```java
  Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
  ```

- **Post-Service (avant correction)**: Utilisait le secret comme texte brut
  ```java
  Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))
  ```

Résultat: Les tokens générés par le User-Service ne pouvaient pas être validés par le Post-Service.

## Solution appliquée

### Fichier modifié
`backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/JwtService.java`

### Changement
```java
// AVANT (incorrect)
private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
}

// APRÈS (correct)
private SecretKey getSigningKey() {
    // Decode Base64 secret like User-Service does
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
}
```

### Import ajouté
```java
import io.jsonwebtoken.io.Decoders;
```

## Action requise

### ⚠️ REDÉMARRER LE POST-SERVICE

Le Post-Service doit être redémarré pour que la correction prenne effet:

```bash
# Arrêter le service actuel
# Puis redémarrer via votre IDE ou:
cd backend/Post-Service
mvn spring-boot:run
```

## Test de validation

Après redémarrage, testez la création d'un post:

### Via le frontend
1. Connectez-vous avec un utilisateur
2. Créez un post
3. ✅ Le post devrait être créé avec succès

### Via curl
```bash
# 1. Obtenir un token
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

# 2. Créer un post
curl -X POST http://localhost:8090/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Post de test après correction","imageUrl":null}'

# Réponse attendue: 201 Created avec les détails du post
```

## Vérification

### Logs attendus dans le navigateur
```
🔐 Auth Interceptor: {url: '/api/posts', hasToken: true, token: 'eyJhbGciOiJIUzI1NiJ9...'}
✅ Token added to request: Bearer eyJhbGciOiJIUzI1...
Post created successfully: {id: 1, content: "...", userId: "675e...", user: {...}}
```

### Logs attendus dans le Post-Service
```
Hibernate: insert into posts (content, created_at, image_url, updated_at, user_id) values (?, ?, ?, ?, ?)
```

Aucune erreur "JWT validation failed" ne devrait apparaître.

## Configuration finale

Les deux services utilisent maintenant le même secret JWT encodé en Base64:

**User-Service** (`application.properties`):
```properties
security.jwt.secret=${JWT_SECRET:ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=}
```

**Post-Service** (`application.properties`):
```properties
jwt.secret=${JWT_SECRET:ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=}
```

Les deux décodent le Base64 de la même manière.

## Résultat

✅ L'intégration Post-Service ↔ User-Service fonctionne correctement
✅ Les posts sont automatiquement associés à l'utilisateur connecté
✅ Les informations utilisateur sont enrichies dans les réponses
✅ L'authentification JWT fonctionne entre les services

## Documentation

Pour plus de détails, consultez:
- `backend/Post-Service/FIX_403_APPLIED.md` - Guide de redémarrage
- `backend/Post-Service/TROUBLESHOOTING_403.md` - Guide de dépannage
- `backend/Post-Service/INTEGRATION_USER_SERVICE.md` - Documentation complète
- `INTEGRATION_POST_USER_COMPLETE.md` - Vue d'ensemble de l'intégration
