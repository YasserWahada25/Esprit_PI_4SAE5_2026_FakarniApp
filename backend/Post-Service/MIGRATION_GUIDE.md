# Guide de Migration - Intégration User-Service

## Changements apportés

### 1. Base de données
Un nouveau champ `userId` a été ajouté à la table `posts`. Pour les posts existants, vous devrez exécuter cette requête SQL:

```sql
-- Ajouter la colonne userId si elle n'existe pas
ALTER TABLE posts ADD COLUMN user_id VARCHAR(255) NOT NULL DEFAULT 'unknown';

-- Optionnel: Mettre à jour les posts existants avec un userId par défaut
-- UPDATE posts SET user_id = 'default-user-id' WHERE user_id = 'unknown';
```

### 2. Dépendances Maven
Les dépendances suivantes ont été ajoutées au `pom.xml`:
- Spring Security
- Spring OAuth2 Resource Server
- JWT (jjwt)
- OpenFeign

### 3. Configuration
Ajoutez ces propriétés dans `application.properties`:
```properties
jwt.secret=${JWT_SECRET:ZHVtbXktc2VjcmV0LXNlY3JldC1zZWNyZXQtc2VjcmV0LXNlY3JldC1zZWNyZXQ=}
feign.client.config.default.connectTimeout=5000
feign.client.config.default.readTimeout=5000
```

**Important**: 
- Le `jwt.secret` doit être identique à celui du User-Service!
- Le secret est encodé en Base64 et sera décodé par le JwtService

### 4. Authentification requise
Tous les endpoints du Post-Service nécessitent maintenant un token JWT valide dans le header:
```
Authorization: Bearer <votre-token-jwt>
```

### 5. Réponse enrichie
Les réponses POST incluent maintenant les informations de l'utilisateur:
```json
{
  "id": 1,
  "content": "Mon post",
  "imageUrl": "...",
  "userId": "user-id-123",
  "user": {
    "id": "user-id-123",
    "nom": "Doe",
    "prenom": "John",
    "email": "john@example.com"
  },
  "createdAt": "2026-04-16T10:30:00",
  "updatedAt": "2026-04-16T10:30:00"
}
```

## Test de l'intégration

1. Démarrez les services dans cet ordre:
   - Eureka-Service (port 8761)
   - User-Service (port 8080)
   - Post-Service (port 8069)

2. Authentifiez-vous via le User-Service pour obtenir un token JWT

3. Créez un post avec le token:
```bash
curl -X POST http://localhost:8069/api/posts \
  -H "Authorization: Bearer <votre-token>" \
  -H "Content-Type: application/json" \
  -d '{"content": "Mon premier post authentifié", "imageUrl": null}'
```

4. Vérifiez que le post est associé à votre utilisateur
