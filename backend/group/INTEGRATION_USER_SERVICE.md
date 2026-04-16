# Intégration Group-Service avec User-Service

## Vue d'ensemble
Le Group-Service est maintenant intégré avec le User-Service. Chaque groupe créé est automatiquement associé à l'utilisateur connecté via son token JWT, et les informations des membres sont enrichies avec les données utilisateur.

## Modifications apportées

### 1. Entités mises à jour
**Group.java**
- `creatorId`: Changé de `Long` à `String` pour correspondre aux IDs MongoDB du User-Service

**GroupMember.java**
- `userId`: Changé de `Long` à `String`
- `invitedBy`: Changé de `Long` à `String`

### 2. Sécurité JWT
Création de 3 nouvelles classes de configuration:

**JwtService** (`config/JwtService.java`)
- Validation des tokens JWT
- Extraction du userId et des claims
- Décodage Base64 du secret

**JwtAuthenticationFilter** (`config/JwtAuthenticationFilter.java`)
- Intercepte les requêtes HTTP
- Valide le token JWT dans le header Authorization
- Stocke l'authentification dans le SecurityContext

**SecurityConfig** (`config/SecurityConfig.java`)
- Configure Spring Security
- Protège tous les endpoints (sauf /actuator)
- Mode stateless (pas de session)
- Configuration CORS

### 3. Communication inter-services
**UserClient** (`client/UserClient.java`)
- Client Feign pour appeler le User-Service
- Récupère les informations utilisateur par ID

**UserDTO** (`dto/UserDTO.java`)
- DTO pour recevoir les données utilisateur

### 4. DTOs enrichis
**GroupResponse**
- Ajout du champ `creator` (UserDTO) avec les informations du créateur
- `creatorId` changé de `Long` à `String`

**GroupMemberResponse**
- Ajout du champ `user` (UserDTO) avec les informations du membre
- `userId` changé de `Long` à `String`
- `invitedBy` changé de `Long` à `String`

**GroupRequest**
- `creatorId` changé de `Long` à `String` (mais sera automatiquement récupéré du token)

**AddMemberRequest**
- `userId` changé de `Long` à `String`
- `invitedBy` changé de `Long` à `String`

### 5. Service mis à jour
**GroupService**
- Méthode `getCurrentUserId()`: Récupère l'ID de l'utilisateur depuis le SecurityContext
- Méthode `createGroup()`: Associe automatiquement le creatorId au user connecté
- Méthode `toResponse()`: Enrichit la réponse avec les infos du créateur via Feign
- Méthode `toMemberResponse()`: Enrichit la réponse avec les infos du membre via Feign

### 6. Repositories mis à jour
**GroupMemberRepository**
- Toutes les méthodes utilisant `userId` acceptent maintenant `String` au lieu de `Long`

### 7. Controller mis à jour
**GroupController**
- Les endpoints utilisant `userId` acceptent maintenant `String` au lieu de `Long`

## Nouveaux endpoints

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | /api/groups | Créer un groupe (creatorId auto) | ✅ |
| GET | /api/groups | Liste tous les groupes | ✅ |
| GET | /api/groups/{id} | Récupérer un groupe | ✅ |
| PUT | /api/groups/{id} | Modifier un groupe | ✅ |
| DELETE | /api/groups/{id} | Supprimer un groupe | ✅ |
| POST | /api/groups/{groupId}/members | Ajouter un membre | ✅ |
| DELETE | /api/groups/{groupId}/members/{userId} | Retirer un membre | ✅ |
| PATCH | /api/groups/{groupId}/members/{userId}/role | Modifier le rôle | ✅ |
| GET | /api/groups/{groupId}/members | Liste des membres | ✅ |
| GET | /api/groups/user/{userId} | Groupes d'un utilisateur | ✅ |

## Format de réponse enrichi

### GroupResponse
```json
{
  "id": 1,
  "name": "Groupe de soutien",
  "description": "Un groupe pour s'entraider",
  "creatorId": "675e1234567890abcdef1234",
  "creator": {
    "id": "675e1234567890abcdef1234",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com"
  },
  "groupType": "PUBLIC",
  "status": "ACTIVE",
  "coverImageUrl": null,
  "maxMembers": 50,
  "isJoinable": true,
  "memberCount": 5,
  "members": [...],
  "createdAt": "2026-04-16T10:30:00",
  "updatedAt": "2026-04-16T10:30:00"
}
```

### GroupMemberResponse
```json
{
  "id": 1,
  "userId": "675e1234567890abcdef1234",
  "user": {
    "id": "675e1234567890abcdef1234",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@example.com"
  },
  "role": "ADMIN",
  "joinedAt": "2026-04-16T10:30:00",
  "invitedBy": null
}
```

## Configuration requise

### application.properties
```properties
# JWT Secret (doit correspondre au User-Service)
jwt.secret=${JWT_SECRET:ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=}

# Feign Configuration
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

### Base de données
Exécutez le script de migration si vous avez des données existantes:
```sql
ALTER TABLE groups MODIFY COLUMN creator_id VARCHAR(255) NOT NULL;
ALTER TABLE group_members MODIFY COLUMN user_id VARCHAR(255) NOT NULL;
ALTER TABLE group_members MODIFY COLUMN invited_by VARCHAR(255);
```

## Flux d'authentification

1. User s'authentifie → User-Service retourne un JWT
2. User envoie requête avec `Authorization: Bearer <token>`
3. JwtAuthenticationFilter valide le token
4. userId extrait et stocké dans SecurityContext
5. GroupService récupère userId automatiquement
6. Groupe créé avec creatorId = userId
7. Réponse enrichie avec infos user via Feign

## Dépendances ajoutées

```xml
<!-- Security & JWT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
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
2. **Secret partagé**: Le `jwt.secret` doit être identique entre User-Service et Group-Service
3. **Eureka**: Les deux services doivent être enregistrés dans Eureka pour que Feign fonctionne
4. **Ordre de démarrage**: Eureka → User-Service → Group-Service
5. **Migration**: Exécutez le script SQL si vous avez des données existantes

## Gestion des erreurs

- Si le token est invalide ou absent: HTTP 401 Unauthorized
- Si l'utilisateur n'est pas trouvé: Le groupe est créé mais le champ `creator` sera null
- Si le User-Service est indisponible: Le groupe est créé mais le champ `creator` sera null (erreur loggée)

## Test de l'intégration

```bash
# 1. Se connecter
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

# 2. Créer un groupe
curl -X POST http://localhost:8090/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mon groupe",
    "description": "Description du groupe",
    "groupType": "PUBLIC",
    "maxMembers": 50,
    "isJoinable": true
  }'

# 3. Récupérer mes groupes
curl -X GET "http://localhost:8090/api/groups/user/$(echo $TOKEN | cut -d'.' -f2 | base64 -d | jq -r '.sub')" \
  -H "Authorization: Bearer $TOKEN"
```

## Résultat

✅ L'intégration Group-Service ↔ User-Service fonctionne correctement
✅ Les groupes sont automatiquement associés à l'utilisateur connecté
✅ Les informations utilisateur sont enrichies dans les réponses
✅ L'authentification JWT fonctionne entre les services
