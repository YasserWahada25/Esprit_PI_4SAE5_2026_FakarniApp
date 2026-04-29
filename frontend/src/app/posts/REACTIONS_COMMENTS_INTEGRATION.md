# Intégration Réactions et Commentaires - Documentation

## Vue d'ensemble

Cette documentation décrit l'intégration complète des fonctionnalités de réactions et commentaires dans le module Posts.

## Architecture

### Services

#### ReactionService (`services/reaction.service.ts`)
- `toggleReaction(postId, userId, type)`: Toggle une réaction (LIKE, HEART, SUPPORT)
- `getReactionCounts(postId, userId?)`: Récupère les comptages de réactions

#### CommentService (`services/comment.service.ts`)
- `addComment(postId, userId, content, parentCommentId?)`: Ajoute un commentaire ou une réponse
- `getComments(postId, page, size)`: Récupère les commentaires paginés
- `deleteComment(postId, commentId, userId)`: Supprime un commentaire

### Composants

#### PostReactionsComponent (`shared/post-reactions/`)
Affiche les boutons de réaction avec compteurs.

**Inputs:**
- `postId`: ID du post
- `userId`: ID de l'utilisateur (par défaut: 1)

**Fonctionnalités:**
- Toggle réaction au clic
- Affichage du compteur par type
- Indication visuelle de la réaction active
- Animation au toggle

#### PostCommentsComponent (`shared/post-comments/`)
Affiche et gère les commentaires d'un post.

**Inputs:**
- `postId`: ID du post
- `userId`: ID de l'utilisateur (par défaut: 1)

**Fonctionnalités:**
- Ajout de commentaires
- Réponses aux commentaires (1 niveau)
- Suppression (propriétaire uniquement)
- Pagination
- Formatage des dates relatives

## Utilisation

### Dans un composant

```typescript
import { PostReactionsComponent } from '../shared/post-reactions/post-reactions.component';
import { PostCommentsComponent } from '../shared/post-comments/post-comments.component';

@Component({
    imports: [PostReactionsComponent, PostCommentsComponent]
})
export class MyComponent {
    // ...
}
```

### Dans le template

```html
<!-- Réactions -->
<app-post-reactions [postId]="post.id" [userId]="currentUserId"></app-post-reactions>

<!-- Commentaires -->
<app-post-comments [postId]="post.id" [userId]="currentUserId"></app-post-comments>
```

## Modèles de données

### Reaction
```typescript
type ReactionType = 'LIKE' | 'HEART' | 'SUPPORT';

interface ReactionCounts {
    counts: {
        LIKE: number;
        HEART: number;
        SUPPORT: number;
    };
    userReaction: ReactionType | null;
}
```

### Comment
```typescript
interface Comment {
    id: number;
    postId: number;
    userId: number;
    content: string;
    parentCommentId: number | null;
    createdAt: string;
    updatedAt: string;
    replies: Comment[];
}
```

## API Endpoints

### Réactions
- `POST /api/posts/{postId}/reactions/toggle` - Toggle réaction
- `GET /api/posts/{postId}/reactions?userId={userId}` - Obtenir comptages

### Commentaires
- `POST /api/posts/{postId}/comments` - Ajouter commentaire
- `GET /api/posts/{postId}/comments?page=0&size=10` - Liste paginée
- `DELETE /api/posts/{postId}/comments/{commentId}?userId={userId}` - Supprimer

## Configuration Proxy

Le proxy Angular (`proxy.conf.json`) redirige `/api` vers `http://localhost:8090` (Gateway).

## Styles

Les composants utilisent les variables CSS définies dans `styles.css`:
- `--primary-color`
- `--primary-hover`
- `--primary-light`
- `--error-color`

## TODO / Améliorations futures

1. **Authentification**: Remplacer `userId: number = 1` par un service d'authentification
2. **Temps réel**: Ajouter WebSocket pour les mises à jour en temps réel
3. **Notifications**: Notifier l'utilisateur des nouvelles réponses
4. **Édition**: Permettre l'édition des commentaires
5. **Mentions**: Ajouter @mentions dans les commentaires
6. **Emojis**: Picker d'emojis pour les commentaires
7. **Images**: Support d'images dans les commentaires
8. **Modération**: Signalement et modération des commentaires

## Tests

Pour tester l'intégration:

1. Démarrer les services backend:
   ```bash
   # Terminal 1 - Eureka
   cd backend/Eureka-Service
   ./mvnw spring-boot:run

   # Terminal 2 - Gateway
   cd backend/Gateway-Service
   ./mvnw spring-boot:run

   # Terminal 3 - Post-Service
   cd backend/Post-Service
   ./mvnw spring-boot:run
   ```

2. Démarrer le frontend:
   ```bash
   cd frontend
   npm start
   ```

3. Accéder à `http://localhost:4200/posts`

4. Tester:
   - Cliquer sur les boutons de réaction
   - Ajouter des commentaires
   - Répondre aux commentaires
   - Supprimer vos commentaires
   - Naviguer dans la pagination

## Dépannage

### Les réactions ne s'affichent pas
- Vérifier que le Post-Service est démarré
- Vérifier la console pour les erreurs HTTP
- Vérifier que le proxy fonctionne

### Les commentaires ne se chargent pas
- Vérifier la pagination (page=0, size=10)
- Vérifier les logs du backend
- Vérifier la base de données MySQL

### Erreur 403 lors de la suppression
- Vérifier que `userId` correspond au propriétaire
- Vérifier les logs du backend

## Support

Pour toute question ou problème, consulter:
- Documentation API: `backend/Post-Service/API_REACTIONS_COMMENTS.md`
- Tests backend: `backend/Post-Service/TEST_EXAMPLES.md`
- Guide d'intégration frontend: `backend/Post-Service/FRONTEND_INTEGRATION.md`
