# Intégration Group-Service ↔ User-Service - Résumé Complet

## 🎯 Objectif
Associer automatiquement chaque groupe créé à l'utilisateur connecté via son token JWT, et enrichir les réponses avec les informations utilisateur complètes.

## ✅ Modifications effectuées

### 1. Types d'IDs changés (Long → String)

**Entités modifiées:**
- `Group.creatorId`: `Long` → `String`
- `GroupMember.userId`: `Long` → `String`
- `GroupMember.invitedBy`: `Long` → `String`

**DTOs modifiés:**
- `GroupRequest.creatorId`: `Long` → `String`
- `GroupResponse.creatorId`: `Long` → `String`
- `GroupMemberResponse.userId`: `Long` → `String`
- `GroupMemberResponse.invitedBy`: `Long` → `String`
- `AddMemberRequest.userId`: `Long` → `String`
- `AddMemberRequest.invitedBy`: `Long` → `String`

**Repositories modifiés:**
- Toutes les méthodes de `GroupMemberRepository` utilisant `userId`

### 2. Sécurité JWT ajoutée

**Nouvelles classes créées:**

**JwtService** (`config/JwtService.java`)
```java
- validateToken(String token): boolean
- extractAllClaims(String token): Claims
- extractUserId(String token): String
- getSigningKey(): SecretKey (décode Base64)
```

**JwtAuthenticationFilter** (`config/JwtAuthenticationFilter.java`)
```java
- Intercepte les requêtes HTTP
- Valide le token JWT
- Extrait userId et role
- Stocke dans SecurityContext
```

**SecurityConfig** (`config/SecurityConfig.java`)
```java
- Configuration Spring Security
- CORS configuré pour localhost:4200
- Tous les endpoints protégés (sauf /actuator)
- Mode stateless
```

### 3. Communication inter-services (Feign)

**UserClient** (`client/UserClient.java`)
```java
@FeignClient(name = "user-service", path = "/api/users")
public interface UserClient {
    @GetMapping("/{id}")
    UserDTO getUserById(@PathVariable("id") String id);
}
```

**UserDTO** (`dto/UserDTO.java`)
```java
public class UserDTO {
    private String id;
    private String nom;
    private String prenom;
    private String email;
}
```

### 4. DTOs enrichis

**GroupResponse**
- Ajout du champ `creator` (UserDTO)
- Contient les informations complètes du créateur

**GroupMemberResponse**
- Ajout du champ `user` (UserDTO)
- Contient les informations complètes du membre

### 5. Service mis à jour

**GroupService**

Nouvelle méthode:
```java
private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new RuntimeException("User not authenticated");
    }
    return authentication.getName();
}
```

Méthode `createGroup()` modifiée:
```java
public GroupResponse createGroup(GroupRequest request) {
    String userId = getCurrentUserId(); // ✅ Récupère automatiquement
    
    Group group = new Group();
    group.setName(request.getName());
    group.setDescription(request.getDescription());
    group.setCreatorId(userId); // ✅ Associe au user connecté
    // ...
}
```

Méthode `toResponse()` enrichie:
```java
private GroupResponse toResponse(Group group, boolean includeMembers) {
    // ...
    
    // ✅ Fetch creator details from User-Service
    try {
        UserDTO creator = userClient.getUserById(group.getCreatorId());
        response.setCreator(creator);
    } catch (Exception e) {
        System.err.println("Failed to fetch creator details: " + e.getMessage());
    }
    
    // ...
}
```

Méthode `toMemberResponse()` enrichie:
```java
private GroupMemberResponse toMemberResponse(GroupMember member) {
    // ...
    
    // ✅ Fetch user details from User-Service
    try {
        UserDTO user = userClient.getUserById(member.getUserId());
        response.setUser(user);
    } catch (Exception e) {
        System.err.println("Failed to fetch user details: " + e.getMessage());
    }
    
    return response;
}
```

### 6. Configuration

**pom.xml**
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

**application.properties**
```properties
# JWT Configuration (must match User-Service secret - Base64 encoded)
jwt.secret=${JWT_SECRET:ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=}

# Feign Configuration
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

**GroupServiceApplication.java**
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // ✅ Ajouté
public class GroupServiceApplication {
    // ...
}
```

### 7. Base de données

**Script de migration** (`src/main/resources/db/migration.sql`)
```sql
-- Modifier les types de colonnes
ALTER TABLE groups MODIFY COLUMN creator_id VARCHAR(255) NOT NULL;
ALTER TABLE group_members MODIFY COLUMN user_id VARCHAR(255) NOT NULL;
ALTER TABLE group_members MODIFY COLUMN invited_by VARCHAR(255);

-- Créer des index
CREATE INDEX IF NOT EXISTS idx_groups_creator_id ON groups(creator_id);
CREATE INDEX IF NOT EXISTS idx_group_members_user_id ON group_members(user_id);
CREATE INDEX IF NOT EXISTS idx_group_members_group_user ON group_members(group_id, user_id);
```

## 📋 Endpoints

| Méthode | Endpoint | Description | Auth | CreatorId |
|---------|----------|-------------|------|-----------|
| POST | /api/groups | Créer un groupe | ✅ | Auto |
| GET | /api/groups | Liste tous les groupes | ✅ | - |
| GET | /api/groups/{id} | Récupérer un groupe | ✅ | - |
| PUT | /api/groups/{id} | Modifier un groupe | ✅ | - |
| DELETE | /api/groups/{id} | Supprimer un groupe | ✅ | - |
| POST | /api/groups/{groupId}/members | Ajouter un membre | ✅ | - |
| DELETE | /api/groups/{groupId}/members/{userId} | Retirer un membre | ✅ | - |
| PATCH | /api/groups/{groupId}/members/{userId}/role | Modifier le rôle | ✅ | - |
| GET | /api/groups/{groupId}/members | Liste des membres | ✅ | - |
| GET | /api/groups/user/{userId} | Groupes d'un utilisateur | ✅ | - |

## 📦 Format de réponse enrichi

### Création de groupe
**Requête:**
```json
{
  "name": "Groupe de soutien",
  "description": "Un groupe pour s'entraider",
  "groupType": "PUBLIC",
  "maxMembers": 50,
  "isJoinable": true
}
```

**Réponse:**
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
  "memberCount": 1,
  "members": [
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
  ],
  "createdAt": "2026-04-16T10:30:00",
  "updatedAt": "2026-04-16T10:30:00"
}
```

## 🔐 Flux d'authentification

1. User s'authentifie via User-Service → Reçoit JWT
2. User envoie requête avec `Authorization: Bearer <token>`
3. JwtAuthenticationFilter valide le token
4. userId extrait du token et stocké dans SecurityContext
5. GroupService.createGroup() récupère userId automatiquement
6. Groupe créé avec creatorId = userId
7. Créateur ajouté automatiquement comme ADMIN
8. Réponse enrichie avec infos user via UserClient (Feign)

## 🚀 Démarrage

### Ordre de démarrage
1. MySQL (port 3306)
2. MongoDB (port 27017)
3. Eureka-Service (port 8761)
4. User-Service (port 8080)
5. Group-Service (port 8097)
6. Gateway-Service (port 8090)

### Commandes
```bash
# 1. Exécuter la migration SQL (si données existantes)
mysql -u root -p group_service_db < backend/group/src/main/resources/db/migration.sql

# 2. Compiler le Group-Service
cd backend/group
mvn clean install -DskipTests

# 3. Démarrer le Group-Service
mvn spring-boot:run
```

## 🧪 Test de l'intégration

```bash
# 1. Se connecter
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

echo "Token: $TOKEN"

# 2. Créer un groupe
curl -X POST http://localhost:8090/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mon groupe de test",
    "description": "Description du groupe",
    "groupType": "PUBLIC",
    "maxMembers": 50,
    "isJoinable": true
  }' | jq

# 3. Récupérer tous les groupes
curl -X GET "http://localhost:8090/api/groups?includeMembers=true" \
  -H "Authorization: Bearer $TOKEN" | jq

# 4. Récupérer mes groupes
USER_ID=$(echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq -r '.sub')
curl -X GET "http://localhost:8090/api/groups/user/$USER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

## ⚠️ Points importants

1. **Secret JWT**: Doit être identique entre User-Service et Group-Service
   ```
   ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=
   ```

2. **Migration SQL**: Obligatoire si vous avez des données existantes

3. **Eureka**: Les services doivent être enregistrés pour que Feign fonctionne

4. **Token JWT**: Obligatoire pour tous les endpoints

5. **CreatorId**: N'est plus fourni dans la requête, il est automatiquement récupéré du token

## 📚 Fichiers créés/modifiés

### Nouveaux fichiers
- `backend/group/src/main/java/com/alzheimer/group_service/config/JwtService.java`
- `backend/group/src/main/java/com/alzheimer/group_service/config/JwtAuthenticationFilter.java`
- `backend/group/src/main/java/com/alzheimer/group_service/config/SecurityConfig.java`
- `backend/group/src/main/java/com/alzheimer/group_service/client/UserClient.java`
- `backend/group/src/main/java/com/alzheimer/group_service/dto/UserDTO.java`
- `backend/group/src/main/resources/db/migration.sql`
- `backend/group/INTEGRATION_USER_SERVICE.md`

### Fichiers modifiés
- `backend/group/pom.xml`
- `backend/group/src/main/java/com/alzheimer/group_service/entities/Group.java`
- `backend/group/src/main/java/com/alzheimer/group_service/entities/GroupMember.java`
- `backend/group/src/main/java/com/alzheimer/group_service/dto/GroupResponse.java`
- `backend/group/src/main/java/com/alzheimer/group_service/dto/GroupMemberResponse.java`
- `backend/group/src/main/java/com/alzheimer/group_service/dto/GroupRequest.java`
- `backend/group/src/main/java/com/alzheimer/group_service/dto/AddMemberRequest.java`
- `backend/group/src/main/java/com/alzheimer/group_service/services/GroupService.java`
- `backend/group/src/main/java/com/alzheimer/group_service/repositories/GroupMemberRepository.java`
- `backend/group/src/main/java/com/alzheimer/group_service/controllers/GroupController.java`
- `backend/group/src/main/java/com/alzheimer/group_service/GroupServiceApplication.java`
- `backend/group/src/main/resources/application.properties`

## ✨ Résultat

✅ L'intégration Group-Service ↔ User-Service est complète
✅ Les groupes sont automatiquement associés à l'utilisateur connecté
✅ Les informations utilisateur sont enrichies dans les réponses
✅ L'authentification JWT fonctionne entre les services
✅ Le créateur est automatiquement ajouté comme ADMIN du groupe
✅ Les membres du groupe ont leurs informations utilisateur enrichies
