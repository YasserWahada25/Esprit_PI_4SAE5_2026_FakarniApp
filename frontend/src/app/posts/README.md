# Module Posts - Documentation

## Overview
Le module **Posts** a été créé pour permettre aux utilisateurs de partager et de consulter des publications au sein de la communauté Fakarni. Il suit la même structure et les mêmes conventions que les autres modules de l'application.

## Structure du Module

### Composants Principaux

#### 1. **PostsLayoutComponent** (`shared/posts-layout.component.*`)
- **Rôle**: Composant de mise en page principal du module
- **Fonctionnalités**:
  - Sidebar collapsible avec navigation
  - Menu de navigation vers les sous-sections
  - Gestion de l'état du sidebar (agrandi/réduit)

#### 2. **PostsListComponent** (`list/posts-list.component.*`)
- **Rôle**: Affiche la liste de tous les posts de la communauté
- **Fonctionnalités**:
  - Affichage en grille responsive des posts
  - Barre de recherche avec filtre en temps réel
  - Filtrage par catégorie
  - Tri par date récente ou popularité
  - Système de like/unlike fonctionnel
  - Affichage des informations de l'auteur
  - Badges de catégorie colorés
  - État vide lorsqu'aucun post ne correspond aux filtres

#### 3. **CreatePostComponent** (`create/create-post.component.*`)
- **Rôle**: Permet aux utilisateurs de créer un nouveau post
- **Fonctionnalités**:
  - Formulaire complet avec validation
  - Champ titre (limite 200 caractères)
  - Sélection de catégorie
  - Zone de contenu (limite 5000 caractères)
  - Upload d'image avec aperçu
  - Compteur de caractères en temps réel
  - Bouton de sauvegarde en brouillon
  - Bouton de publication
  - Section de conseils pour créer un bon post
  - Aperçu en direct du post

#### 4. **MyPostsComponent** (`my-posts/my-posts.component.*`)
- **Rôle**: Affiche les posts et brouillons de l'utilisateur
- **Fonctionnalités**:
  - Tableau des posts publiés
  - Statistiques (likes, commentaires, vues)
  - Section des brouillons
  - Boutons d'édition, visualisation et suppression
  - Affichage responsive (tableau sur desktop, cartes sur mobile)

### Routes

```
/posts
├── /list (par défaut) - Affiche tous les posts
├── /create - Créer un nouveau post
└── /my-posts - Gérer ses propres posts
```

### Intégration dans l'Application

Le module est intégré dans les routes principales:
```typescript
{
    path: 'posts',
    loadChildren: () => import('./posts/posts.routes').then(m => m.POSTS_ROUTES)
}
```

Et accessible via le lien de navigation dans la navbar.

## Design et Styling

### Thème de Couleur
- **Couleur primaire**: Bleu (#2196F3)
- **Couleur d'accent**: Rose (#e91e63) pour les likes
- **Couleurs des catégories**:
  - Lifestyle: Orange
  - Health & Wellness: Vert
  - Family: Rose
  - Hobbies: Violet

### Responsive Design
- Desktop: Grille multi-colonnes, tableau complet
- Tablet: Grille 2-3 colonnes, tableau adapté
- Mobile: Affichage en colonne unique, cartes empilées

## Fonctionnalités Clés

### Posts List
- ✅ Recherche multi-champs (titre, contenu, auteur)
- ✅ Filtrage par catégorie
- ✅ Tri par récence/popularité
- ✅ System de like avec compteur
- ✅ Affichage des avatars utilisateur
- ✅ Badges de catégorie
- ✅ Aperçu des images

### Create Post
- ✅ Validation du formulaire
- ✅ Upload d'image avec prévisualisation
- ✅ Comptage de caractères en temps réel
- ✅ Sauvegarde en brouillon
- ✅ Aperçu en direct
- ✅ Conseils pour rédiger un bon post

### My Posts
- ✅ Tableau avec statistiques d'engagement
- ✅ Gestion des brouillons
- ✅ Actions rapides (éditer, voir, supprimer)
- ✅ Statistiques globales (likes, commentaires, vues)

## Utilisation

### Accéder au Module Posts
1. Se connecter à l'application
2. Cliquer sur "POSTS" dans la barre de navigation

### Publier un Post
1. Aller à `/posts/create`
2. Remplir le titre et le contenu
3. Sélectionner une catégorie
4. (Optionnel) Ajouter une image
5. Cliquer sur "Publish Post"

### Consulter les Posts
1. Aller à `/posts/list` (par défaut)
2. Utiliser la barre de recherche pour trouver des posts spécifiques
3. Filtrer par catégorie
4. Trier par récence ou popularité
5. Liker les posts intéressants

### Gérer ses Posts
1. Aller à `/posts/my-posts`
2. Voir les statistiques d'engagement
3. Éditer, visualiser ou supprimer les posts
4. Gérer les brouillons

## API Integration (À Implémenter)

Les composants contiennent actuellement des données de démonstration (mock data). Pour intégrer avec le backend:

1. Créer un service `PostsService`
2. Implémenter les méthodes:
   - `getAllPosts()` - Récupérer tous les posts
   - `getUserPosts()` - Récupérer les posts de l'utilisateur
   - `createPost(post)` - Créer un post
   - `updatePost(id, post)` - Modifier un post
   - `deletePost(id)` - Supprimer un post
   - `likePost(id)` - Liker un post

3. Remplacer les appels directs aux données par les appels au service

## Structure des Données

### Post Object
```typescript
{
    id: number;
    author: string;
    avatar: string;
    title: string;
    content: string;
    category: 'lifestyle' | 'health' | 'family' | 'hobby';
    date: string; // ISO format
    likes: number;
    comments: number;
    views?: number;
    image?: string; // URL de l'image
    liked?: boolean; // État du like pour l'utilisateur actuel
    status?: 'published' | 'draft';
}
```

## Prochaines Étapes

1. **Intégration API**: Connecter les composants au backend Post-Service
2. **Commentaires**: Ajouter la fonctionnalité de commentaires
3. **Système d'authentification**: Valider les actions basées sur l'utilisateur connecté
4. **Upload de fichiers**: Intégrer CloudStorage pour les images
5. **Notifications**: Ajouter les notifications pour les likes/commentaires
6. **Modération**: Implémenter la modération des contenus
7. **Partage social**: Ajouter la fonctionnalité de partage
8. **Analytics**: Tracker les statistiques des posts

## Technologies Utilisées

- **Angular 15+**: Framework frontend
- **TypeScript**: Langage de programmation
- **RxJS**: Gestion des observables (à implémenter)
- **Bootstrap/CSS**: Styling responsive
- **Font Awesome**: Icônes
- **ngModel**: Two-way binding pour les formulaires
