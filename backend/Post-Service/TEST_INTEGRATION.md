# Test de l'intégration Post-Service ↔ User-Service

## Prérequis
1. MySQL en cours d'exécution (port 3306)
2. MongoDB en cours d'exécution (port 27017)
3. Eureka-Service démarré (port 8761)
4. User-Service démarré (port 8080)
5. Post-Service démarré (port 8069)

## Étape 1: Créer un utilisateur

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@test.com",
    "password": "Password123!",
    "role": "PATIENT",
    "numTel": "0612345678",
    "adresse": "123 Rue Test"
  }'
```

## Étape 2: S'authentifier

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jean.dupont@test.com",
    "password": "Password123!"
  }'
```

Réponse attendue:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "675e1234567890abcdef1234",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@test.com",
    "role": "PATIENT"
  }
}
```

**Copiez le token pour les étapes suivantes!**

## Étape 3: Créer un post

```bash
curl -X POST http://localhost:8069/api/posts \
  -H "Authorization: Bearer <VOTRE_TOKEN_ICI>" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Ceci est mon premier post authentifié!",
    "imageUrl": null
  }'
```

Réponse attendue:
```json
{
  "id": 1,
  "content": "Ceci est mon premier post authentifié!",
  "imageUrl": null,
  "userId": "675e1234567890abcdef1234",
  "user": {
    "id": "675e1234567890abcdef1234",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@test.com"
  },
  "createdAt": "2026-04-16T10:30:00",
  "updatedAt": "2026-04-16T10:30:00"
}
```

## Étape 4: Récupérer mes posts

```bash
curl -X GET http://localhost:8069/api/posts/my-posts \
  -H "Authorization: Bearer <VOTRE_TOKEN_ICI>"
```

## Étape 5: Récupérer tous les posts

```bash
curl -X GET http://localhost:8069/api/posts \
  -H "Authorization: Bearer <VOTRE_TOKEN_ICI>"
```

## Étape 6: Récupérer les posts d'un utilisateur spécifique

```bash
curl -X GET http://localhost:8069/api/posts/user/675e1234567890abcdef1234 \
  -H "Authorization: Bearer <VOTRE_TOKEN_ICI>"
```

## Tests d'erreur

### Test sans token (doit échouer avec 401)
```bash
curl -X POST http://localhost:8069/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Ce post devrait échouer",
    "imageUrl": null
  }'
```

### Test avec token invalide (doit échouer avec 401)
```bash
curl -X POST http://localhost:8069/api/posts \
  -H "Authorization: Bearer invalid-token-here" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Ce post devrait échouer",
    "imageUrl": null
  }'
```

## Vérification dans la base de données

### MySQL (Post-Service)
```sql
USE post_service_db;
SELECT id, content, user_id, created_at FROM posts;
```

Vous devriez voir le `user_id` correspondant à l'ID de l'utilisateur MongoDB.

### MongoDB (User-Service)
```javascript
use user_service_db
db.users.find({email: "jean.dupont@test.com"})
```

L'ID retourné doit correspondre au `userId` dans la table posts.

## Résultat attendu

✅ Le post est créé avec le `userId` de l'utilisateur connecté
✅ Les informations utilisateur sont enrichies dans la réponse
✅ Les requêtes sans token sont rejetées
✅ Les posts peuvent être filtrés par utilisateur
