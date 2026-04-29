# API Réactions et Commentaires

## Réactions

### Toggle Reaction (Like/Heart/Support)
**POST** `/api/posts/{postId}/reactions/toggle`

Toggle une réaction sur un post. Si l'utilisateur a déjà la même réaction, elle est supprimée. Si l'utilisateur a une réaction différente, elle est mise à jour.

**Request Body:**
```json
{
  "userId": 1,
  "type": "LIKE"  // LIKE, HEART, ou SUPPORT
}
```

**Response:**
```json
{
  "counts": {
    "LIKE": 5,
    "HEART": 3,
    "SUPPORT": 2
  },
  "userReaction": "LIKE"  // ou null si l'utilisateur n'a pas de réaction
}
```

### Get Reaction Counts
**GET** `/api/posts/{postId}/reactions?userId={userId}`

Récupère le comptage des réactions par type pour un post.

**Query Parameters:**
- `userId` (optionnel): ID de l'utilisateur pour savoir quelle réaction il a donnée

**Response:**
```json
{
  "counts": {
    "LIKE": 5,
    "HEART": 3,
    "SUPPORT": 2
  },
  "userReaction": "LIKE"
}
```

## Commentaires

### Add Comment
**POST** `/api/posts/{postId}/comments`

Ajoute un commentaire sur un post ou une réponse à un commentaire existant.

**Request Body:**
```json
{
  "userId": 1,
  "content": "Ceci est un commentaire",
  "parentCommentId": null  // ou ID du commentaire parent pour une réponse
}
```

**Contraintes:**
- Un seul niveau d'imbrication autorisé (commentaire -> réponse)
- Contenu maximum: 1000 caractères

**Response:**
```json
{
  "id": 1,
  "postId": 1,
  "userId": 1,
  "content": "Ceci est un commentaire",
  "parentCommentId": null,
  "createdAt": "2026-04-01T10:00:00",
  "updatedAt": "2026-04-01T10:00:00",
  "replies": []
}
```

### Get Comments (Paginated)
**GET** `/api/posts/{postId}/comments?page=0&size=10`

Récupère les commentaires d'un post avec pagination. Les réponses sont incluses dans chaque commentaire parent.

**Query Parameters:**
- `page` (défaut: 0): Numéro de page
- `size` (défaut: 10): Nombre de commentaires par page

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "postId": 1,
      "userId": 1,
      "content": "Commentaire parent",
      "parentCommentId": null,
      "createdAt": "2026-04-01T10:00:00",
      "updatedAt": "2026-04-01T10:00:00",
      "replies": [
        {
          "id": 2,
          "postId": 1,
          "userId": 2,
          "content": "Réponse au commentaire",
          "parentCommentId": 1,
          "createdAt": "2026-04-01T10:05:00",
          "updatedAt": "2026-04-01T10:05:00",
          "replies": []
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### Delete Comment
**DELETE** `/api/posts/{postId}/comments/{commentId}?userId={userId}`

Supprime un commentaire. Seul le propriétaire peut supprimer son commentaire.

**Query Parameters:**
- `userId` (requis): ID de l'utilisateur qui tente de supprimer le commentaire

**Response:** 204 No Content

## Règles de Gestion

### Réactions
- Un utilisateur ne peut avoir qu'une seule réaction par post
- Types de réactions disponibles: LIKE, HEART, SUPPORT
- Toggle: cliquer sur la même réaction la supprime, cliquer sur une autre la change
- Contrainte d'unicité en base: (post_id, user_id)

### Commentaires
- Un seul niveau d'imbrication autorisé
- Seul le propriétaire peut supprimer son commentaire
- Contenu limité à 1000 caractères
- Pagination pour les listes de commentaires
- Les réponses sont triées par date croissante
- Les commentaires parents sont triés par date décroissante
