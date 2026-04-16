# Résolution de l'erreur 403 Forbidden

## Problème
Lors de la création d'un post depuis le frontend, vous obtenez une erreur 403 Forbidden.

## Cause
Le Post-Service exige maintenant un token JWT valide pour toutes les requêtes. Si l'utilisateur n'est pas connecté ou si le token n'est pas envoyé, la requête est rejetée.

## Solutions

### Solution 1: Se connecter avant de créer un post (RECOMMANDÉ)

1. Assurez-vous que l'utilisateur est connecté dans le frontend
2. Le token JWT sera automatiquement ajouté par l'intercepteur Angular
3. La création de post fonctionnera

**Test rapide:**
```bash
# 1. Connectez-vous via le frontend ou via curl
curl -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Password123!"}'

# 2. Copiez le token retourné
# 3. Utilisez-le pour créer un post
curl -X POST http://localhost:8090/api/posts \
  -H "Authorization: Bearer <VOTRE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"content":"Mon post authentifié","imageUrl":null}'
```

### Solution 2: Permettre l'accès public à certains endpoints (DÉVELOPPEMENT UNIQUEMENT)

Si vous voulez tester sans authentification pendant le développement, modifiez `SecurityConfig.java`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll() // Lecture publique
            .anyRequest().authenticated() // Création/modification nécessite auth
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

⚠️ **ATTENTION**: Cette solution est pour le développement uniquement. En production, tous les endpoints doivent être protégés.

### Solution 3: Vérifier que l'intercepteur fonctionne

Ouvrez la console du navigateur (F12) et vérifiez les logs de l'intercepteur:

```
🔐 Auth Interceptor: { url: '/api/posts', hasToken: true, token: 'eyJhbGciOiJIUzI1...' }
✅ Token added to request: Bearer eyJhbGciOiJIUzI1...
```

Si vous voyez `hasToken: false`, cela signifie que l'utilisateur n'est pas connecté.

## Vérifications

### 1. Vérifier que l'utilisateur est connecté
```typescript
// Dans la console du navigateur
sessionStorage.getItem('fakarni_token')
```

Si cela retourne `null`, l'utilisateur n'est pas connecté.

### 2. Vérifier que le token est valide
```bash
# Décoder le token JWT (sans vérifier la signature)
echo "VOTRE_TOKEN" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq
```

### 3. Vérifier que le secret JWT est identique
Le `jwt.secret` doit être identique dans:
- `backend/User-Service/src/main/resources/application.properties`
- `backend/Post-Service/src/main/resources/application.properties`

Actuellement configuré à:
```
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation1234567890
```

### 4. Vérifier les logs du Post-Service
```bash
# Démarrez le Post-Service et observez les logs
# Vous devriez voir:
# - "JWT authentication failed" si le token est invalide
# - Aucune erreur si le token est valide
```

## Flux correct

1. **Connexion**: User se connecte via `/auth/login`
2. **Token stocké**: Le token est stocké dans `sessionStorage`
3. **Intercepteur**: L'intercepteur ajoute automatiquement le token aux requêtes
4. **Post-Service**: Valide le token et extrait le userId
5. **Création**: Le post est créé avec le userId

## Test complet

```bash
# 1. Créer un utilisateur (si nécessaire)
curl -X POST http://localhost:8090/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Test",
    "prenom": "User",
    "email": "test@example.com",
    "password": "Password123!",
    "role": "PATIENT",
    "numTel": "0612345678",
    "adresse": "123 Test St"
  }'

# 2. Se connecter
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

echo "Token: $TOKEN"

# 3. Créer un post
curl -X POST http://localhost:8090/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Mon premier post authentifié!","imageUrl":null}'

# 4. Récupérer mes posts
curl -X GET http://localhost:8090/api/posts/my-posts \
  -H "Authorization: Bearer $TOKEN"
```

## Erreurs courantes

### "JWT authentication failed"
- Le token est invalide ou expiré
- Le secret JWT ne correspond pas entre User-Service et Post-Service

### "User not authenticated"
- Aucun token n'a été envoyé
- L'intercepteur n'a pas ajouté le token

### "403 Forbidden"
- Le token n'est pas présent dans la requête
- L'utilisateur n'est pas connecté dans le frontend

## Solution rapide pour le développement

Si vous voulez désactiver temporairement l'authentification pour tester:

1. Commentez `@EnableWebSecurity` dans `SecurityConfig.java`
2. Ou modifiez pour permettre toutes les requêtes:
```java
.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
```

⚠️ N'oubliez pas de réactiver la sécurité avant de déployer en production!
