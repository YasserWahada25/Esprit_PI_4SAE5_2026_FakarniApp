# Intégration Post-Service avec User-Service

## Vue d'ensemble
Le Post-Service est maintenant intégré avec le User-Service. Chaque post créé est automatiquement associé à l'utilisateur connecté via son token JWT.

## Modifications apportées

### 1. Entité Post
- Ajout du champ `userId` (String) pour stocker l'ID de l'utilisateur créateur

### 2. Sécurité
- **JwtService**: Service pour valider et extraire les informations du token JWT
- **JwtAuthenticationFilter**: Filtre pour intercepter les requêtes et authentifier l'utilisateur
- **SecurityConfig**: Configuration de Spring Security pour protéger les endpoints

### 3. Communication inter-services
- **UserClient**: Client Feign pour communiquer avec le User-Service
- **UserDTO**: DTO pour recevoir les informations utilisateur

### 4. Nouveaux endpoints

#### POST /api/posts
Crée un post pour l'utilisateur connecté (userId extrait du token JWT)
```bash
curl -X POST http://localhost:8069/api/posts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"content": "Mon post", "imageUrl": null}'
```

#### GET /api/posts/my-posts
Récupère tous les posts de l'utilisateur connecté
```bash
curl -X GET http://localhost:8069/api/posts/my-posts \
  -H "Authorization: Bearer <token>"
```

#### GET /api/posts/user/{userId}
Récupère tous les posts d'un utilisateur spécifique
```bash
curl -X GET http://localhost:8069/api/posts/user/user-id-123 \
  -H "Authorization: Bearer <token>"
```

#### GET /api/posts
Liste tous les posts (avec informations utilisateur enrichies)

#### GET /api/posts/{id}
Récupère un post spécifique

#### PUT /api/posts/{id}
Met à jour un post

#### DELETE /api/posts/{id}
Supprime un post

## Format de réponse enrichi

```json
{
  "id": 1,
  "content": "Contenu du post",
  "imageUrl": "data:image/jpeg;base64,...",
  "userId": "675e1234567890abcdef1234",
  "user": {
    "id": "675e1234567890abcdef1234",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com"
  },
  "createdAt": "2026-04-16T10:30:00",
  "updatedAt": "2026-04-16T10:30:00"
}
```

## Configuration requise

### application.properties
```properties
# JWT Secret (doit correspondre au User-Service)
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation1234567890

# Feign Configuration
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

### Base de données
Exécutez le script de migration si vous avez des données existantes:
```sql
ALTER TABLE posts ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT 'unknown';
CREATE INDEX idx_posts_user_id ON posts(user_id);
```

## Flux d'authentification

1. L'utilisateur s'authentifie via le User-Service et reçoit un token JWT
2. Le token est envoyé dans le header `Authorization: Bearer <token>`
3. Le JwtAuthenticationFilter valide le token et extrait le userId
4. Le userId est stocké dans le SecurityContext
5. Lors de la création d'un post, le userId est automatiquement récupéré et associé au post
6. Lors de la récupération d'un post, les informations utilisateur sont enrichies via le UserClient

## Dépendances ajoutées

```xml
<!-- Security & JWT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<!-- OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

## Points importants

1. **Token JWT obligatoire**: Tous les endpoints nécessitent un token JWT valide
2. **Secret partagé**: Le `jwt.secret` doit être identique entre User-Service et Post-Service
3. **Eureka**: Les deux services doivent être enregistrés dans Eureka pour que Feign fonctionne
4. **Ordre de démarrage**: Eureka → User-Service → Post-Service

## Gestion des erreurs

- Si le token est invalide ou absent: HTTP 401 Unauthorized
- Si l'utilisateur n'est pas trouvé: Le post est créé mais le champ `user` sera null
- Si le User-Service est indisponible: Le post est créé mais le champ `user` sera null (erreur loggée)
