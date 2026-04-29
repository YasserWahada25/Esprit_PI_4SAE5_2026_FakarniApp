# Affichage du nom de l'utilisateur dans les posts

## Modifications effectuées

### 1. Modèle Post mis à jour
**Fichier:** `frontend/src/app/posts/models/post.model.ts`

Ajout de l'interface User et des champs userId et user:
```typescript
export interface User {
    id: string;
    nom: string;
    prenom: string;
    email: string;
}

export interface Post {
    id: number;
    content: string;
    imageUrl?: string;
    userId?: string;      // ✅ Nouveau
    user?: User;          // ✅ Nouveau
    createdAt: string;
    updatedAt: string;
}
```

### 2. Template HTML mis à jour
**Fichier:** `frontend/src/app/posts/list/posts-list.component.html`

Changé de:
```html
<span class="user-name">Community Member</span>
```

À:
```html
<span class="user-name">
    {{ post.user ? (post.user.prenom + ' ' + post.user.nom) : 'Community Member' }}
</span>
```

### 3. Service mis à jour
**Fichier:** `frontend/src/app/posts/services/posts.service.ts`

La méthode `formatPost()` inclut maintenant les données utilisateur:
```typescript
private formatPost(response: PostResponse): Post {
    return {
        id: response.id,
        content: response.content,
        imageUrl: response.imageUrl,
        userId: response.userId,    // ✅ Nouveau
        user: response.user,        // ✅ Nouveau
        createdAt: response.createdAt,
        updatedAt: response.updatedAt
    };
}
```

### 4. Composant posts-list mis à jour
**Fichier:** `frontend/src/app/posts/list/posts-list.component.ts`

Le mapping des posts inclut maintenant les données utilisateur:
```typescript
const postsData = response.map((p, index) => ({
    id: p.id,
    content: p.content,
    imageUrl: p.imageUrl,
    userId: p.userId,    // ✅ Nouveau
    user: p.user,        // ✅ Nouveau
    createdAt: p.createdAt,
    updatedAt: p.updatedAt
}));
```

## Résultat

Maintenant, au lieu d'afficher "Community Member", l'interface affichera:
- **"Prénom Nom"** si les informations utilisateur sont disponibles
- **"Community Member"** comme fallback si les données ne sont pas disponibles

## Exemple

Si un post est créé par un utilisateur avec:
- Prénom: "Jean"
- Nom: "Dupont"

L'interface affichera: **"Jean Dupont"**

## Format de réponse du backend

Le backend (Post-Service) retourne maintenant:
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
    "email": "jean.dupont@example.com"
  },
  "createdAt": "2026-04-16T10:30:00",
  "updatedAt": "2026-04-16T10:30:00"
}
```

## Test

1. Assurez-vous que le Post-Service est redémarré avec la nouvelle configuration JWT
2. Connectez-vous dans le frontend
3. Créez un post
4. Le post devrait afficher votre nom (Prénom Nom) au lieu de "Community Member"
5. Tous les posts existants afficheront également le nom de leur créateur

## Notes

- Si le backend ne peut pas récupérer les informations utilisateur (User-Service indisponible), le champ `user` sera `null` et "Community Member" sera affiché
- Les posts créés avant l'intégration n'auront pas de `userId` et afficheront "Community Member"
