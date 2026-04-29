# Intégration Post-Service ↔ User-Service - Résumé Complet

## 🎯 Objectif
Associer automatiquement chaque post créé à l'utilisateur connecté via son token JWT.

## ✅ Modifications effectuées

### 1. Post-Service - Entité Post
- Ajout du champ `userId` (String) pour stocker l'ID de l'utilisateur MongoDB
- Ajout des getters/setters correspondants

### 2. Post-Service - Sécurité JWT
Création de 3 nouvelles classes de configuration:

**JwtService** (`config/JwtService.java`)
- Validation des tokens JWT
- Extraction du userId et des claims

**JwtAuthenticationFilter** (`config/JwtAuthenticationFilter.java`)
- Intercepte les requêtes HTTP
- Valide le token JWT dans le header Authorization
- Stocke l'authentification dans le SecurityContext

**SecurityConfig** (`config/SecurityConfig.java`)
- Configure Spring Security
- Protège tous les endpoints (sauf /actuator)
- Mode stateless (pas de session)

### 3. Post-Service - Communication inter-services
**UserClient** (`client/UserClient.java`)
- Client Feign pour appeler le User-Service
- Récupère les informations utilisateur par ID

**UserDTO** (`dto/UserDTO.java`)
- DTO pour recevoir les données utilisateur

### 4. Post-Service - Service et Controller
**PostService**
- Méthode `getCurrentUserId()`: Récupère l'ID de l'utilisateur depuis le SecurityContext
- Méthode `createPost()`: Associe automatiquement le userId au post
- Méthode `getPostsByUserId()`: Récupère les posts d'un utilisateur
- Méthode `getCurrentUserPosts()`: Récupère les posts de l'utilisateur connecté
- Méthode `toResponse()`: Enrichit la réponse avec les infos utilisateur via Feign

**PostController**
- Nouveau endpoint: `GET /api/posts/my-posts` - Posts de l'utilisateur connecté
- Nouveau endpoint: `GET /api/posts/user/{userId}` - Posts d'un utilisateur spécifique

**PostRepository**
- Nouvelle méthode: `findByUserId(String userId)`
- Nouvelle méthode: `findByUserIdOrderByCreatedAtDesc(String userId)`

### 5. Post-Service - Configuration
**pom.xml**
- Spring Security
- Spring OAuth2 Resource Server
- JWT (jjwt 0.12.6)
- OpenFeign

**application.properties**
```properties
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation1234567890
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

**PostServiceApplication.java**
- Ajout de `@EnableFeignClients`

### 6. Base de données
**Migration SQL** (`src/main/resources/db/migration.sql`)
```sql
ALTER TABLE posts ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT 'unknown';
CREATE INDEX idx_posts_user_id ON posts(user_id);
```

## 📋 Nouveaux endpoints

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | /api/posts | Créer un post (userId auto) | ✅ |
| GET | /api/posts | Liste tous les posts | ✅ |
| GET | /api/posts/{id} | Récupérer un post | ✅ |
| GET | /api/posts/my-posts | Mes posts | ✅ |
| GET | /api/posts/user/{userId} | Posts d'un user | ✅ |
| PUT | /api/posts/{id} | Modifier un post | ✅ |
| DELETE | /api/posts/{id} | Supprimer un post | ✅ |

## 🔐 Flux d'authentification

1. User s'authentifie → User-Service retourne un JWT
2. User envoie requête avec `Authorization: Bearer <token>`
3. JwtAuthenticationFilter valide le token
4. userId extrait et stocké dans SecurityContext
5. PostService récupère userId automatiquement
6. Post créé avec userId
7. Réponse enrichie avec infos user via Feign

## 📦 Format de réponse

```json
{
  "id": 1,
  "content": "Mon post",
  "imageUrl": null,
  "userId": "675e1234567890abcdef1234",
  "user": {
    "id": "675e1234567890abcdef1234",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean@example.com"
  },
  "createdAt": "2026-04-16T10:30:00",
  "updatedAt": "2026-04-16T10:30:00"
}
```

## 🚀 Démarrage

1. Démarrer Eureka-Service (port 8761)
2. Démarrer User-Service (port 8080)
3. Démarrer Post-Service (port 8069)
4. Exécuter le script de migration SQL si nécessaire

## 📝 Fichiers créés/modifiés

### Nouveaux fichiers
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/JwtService.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/JwtAuthenticationFilter.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/config/SecurityConfig.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/client/UserClient.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/dto/UserDTO.java`
- `backend/Post-Service/src/main/resources/db/migration.sql`
- `backend/Post-Service/MIGRATION_GUIDE.md`
- `backend/Post-Service/INTEGRATION_USER_SERVICE.md`
- `backend/Post-Service/TEST_INTEGRATION.md`

### Fichiers modifiés
- `backend/Post-Service/pom.xml`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/entities/Post.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/services/PostService.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/controllers/PostController.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/repositories/PostRepository.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/dto/PostResponse.java`
- `backend/Post-Service/src/main/java/com/alzheimer/Post_Service/PostServiceApplication.java`
- `backend/Post-Service/src/main/resources/application.properties`

## ⚠️ Points importants

1. **Secret JWT**: Doit être identique entre User-Service et Post-Service
2. **Eureka**: Les deux services doivent être enregistrés pour que Feign fonctionne
3. **Migration**: Exécuter le script SQL si vous avez des posts existants
4. **Token**: Tous les endpoints nécessitent un token JWT valide

## 🧪 Test rapide

```bash
# 1. S'authentifier
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Password123!"}' \
  | jq -r '.token')

# 2. Créer un post
curl -X POST http://localhost:8069/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Mon post authentifié!","imageUrl":null}'

# 3. Récupérer mes posts
curl -X GET http://localhost:8069/api/posts/my-posts \
  -H "Authorization: Bearer $TOKEN"
```

## ✨ Résultat

L'intégration est complète! Chaque post créé est maintenant automatiquement associé à l'utilisateur connecté, et les informations utilisateur sont enrichies dans les réponses.
