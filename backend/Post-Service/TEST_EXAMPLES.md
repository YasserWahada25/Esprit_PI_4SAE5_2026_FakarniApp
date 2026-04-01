# Exemples de Tests pour Réactions et Commentaires

## Tests avec cURL

### 1. Créer un post (prérequis)
```bash
curl -X POST http://localhost:8069/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Ceci est un post de test pour les réactions et commentaires"
  }'
```

### 2. Ajouter une réaction LIKE
```bash
curl -X POST http://localhost:8069/api/posts/1/reactions/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "type": "LIKE"
  }'
```

### 3. Changer la réaction en HEART
```bash
curl -X POST http://localhost:8069/api/posts/1/reactions/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "type": "HEART"
  }'
```

### 4. Supprimer la réaction (toggle sur la même)
```bash
curl -X POST http://localhost:8069/api/posts/1/reactions/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "type": "HEART"
  }'
```

### 5. Obtenir les comptages de réactions
```bash
curl -X GET "http://localhost:8069/api/posts/1/reactions?userId=1"
```

### 6. Ajouter un commentaire
```bash
curl -X POST http://localhost:8069/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "content": "Ceci est un commentaire de test"
  }'
```

### 7. Ajouter une réponse à un commentaire
```bash
curl -X POST http://localhost:8069/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "content": "Ceci est une réponse au commentaire",
    "parentCommentId": 1
  }'
```

### 8. Obtenir les commentaires (paginés)
```bash
curl -X GET "http://localhost:8069/api/posts/1/comments?page=0&size=10"
```

### 9. Supprimer un commentaire
```bash
curl -X DELETE "http://localhost:8069/api/posts/1/comments/1?userId=1"
```

## Tests avec Postman

### Collection Postman

Créez une collection avec les requêtes suivantes:

1. **Toggle Reaction**
   - Method: POST
   - URL: `http://localhost:8069/api/posts/{{postId}}/reactions/toggle`
   - Body (JSON):
     ```json
     {
       "userId": 1,
       "type": "LIKE"
     }
     ```

2. **Get Reactions**
   - Method: GET
   - URL: `http://localhost:8069/api/posts/{{postId}}/reactions?userId=1`

3. **Add Comment**
   - Method: POST
   - URL: `http://localhost:8069/api/posts/{{postId}}/comments`
   - Body (JSON):
     ```json
     {
       "userId": 1,
       "content": "Mon commentaire"
     }
     ```

4. **Get Comments**
   - Method: GET
   - URL: `http://localhost:8069/api/posts/{{postId}}/comments?page=0&size=10`

5. **Delete Comment**
   - Method: DELETE
   - URL: `http://localhost:8069/api/posts/{{postId}}/comments/{{commentId}}?userId=1`

## Scénarios de Test

### Scénario 1: Cycle complet de réactions
1. User 1 ajoute un LIKE → counts: {LIKE: 1, HEART: 0, SUPPORT: 0}, userReaction: "LIKE"
2. User 2 ajoute un HEART → counts: {LIKE: 1, HEART: 1, SUPPORT: 0}
3. User 1 change en SUPPORT → counts: {LIKE: 0, HEART: 1, SUPPORT: 1}, userReaction: "SUPPORT"
4. User 1 supprime sa réaction → counts: {LIKE: 0, HEART: 1, SUPPORT: 0}, userReaction: null

### Scénario 2: Commentaires avec réponses
1. User 1 crée un commentaire parent
2. User 2 répond au commentaire
3. User 3 répond aussi au même commentaire
4. Récupérer les commentaires → le parent contient 2 réponses
5. User 2 tente de supprimer le commentaire de User 1 → Erreur 403
6. User 1 supprime son commentaire → Succès 204

### Scénario 3: Validation des contraintes
1. Tenter d'ajouter un commentaire vide → Erreur 400
2. Tenter d'ajouter un commentaire > 1000 caractères → Erreur 400
3. Tenter de répondre à une réponse (2 niveaux) → Erreur 400
4. Tenter de supprimer un commentaire sans userId → Erreur 400
