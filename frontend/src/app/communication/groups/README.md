# Module Groups - Guide Complet

## 🚀 Démarrage Rapide

### 1. Démarrer le Backend

```bash
cd backend/group
./mvnw spring-boot:run
```

Le service démarre sur `http://localhost:8080`

### 2. Configurer le Proxy (si nécessaire)

Si votre backend tourne sur un port différent, modifiez `frontend/proxy.conf.json`:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

### 3. Démarrer le Frontend

```bash
cd frontend
npm install
npm start
```

L'application démarre sur `http://localhost:4200`

### 4. Accéder au Module

Naviguez vers: `http://localhost:4200/communication/groups`

## 📸 Captures d'écran des fonctionnalités

### Page principale des groupes
- Liste de "Mes Groupes" avec badge créateur
- Liste de tous les groupes disponibles
- Bouton "Créer un Groupe"
- Badges PUBLIC/PRIVATE
- Compteur de membres avec limite

### Modal de création
- Nom du groupe (requis)
- Description (requis)
- Type: Public ou Privé
- Limite de membres (optionnel)
- Autoriser l'adhésion libre (checkbox)

### Page de détails du groupe
- En-tête avec image de couverture
- Informations du groupe
- Liste complète des membres avec rôles
- Actions selon les permissions:
  - **Membre**: Quitter le groupe
  - **Modérateur**: Retirer des membres
  - **Admin**: Gérer les rôles, supprimer le groupe

## 🎯 Cas d'usage

### Scénario 1: Créer un groupe de soutien

1. Cliquer sur "Créer un Groupe"
2. Remplir le formulaire:
   - Nom: "Groupe Aidants Paris"
   - Description: "Groupe de soutien pour les aidants"
   - Type: Public
   - Limite: 50 membres
3. Soumettre
4. Vous êtes automatiquement ADMIN du groupe

### Scénario 2: Rejoindre un groupe

1. Parcourir la liste des groupes
2. Cliquer sur "Rejoindre" sur un groupe public
3. Vous êtes ajouté comme MEMBER
4. Accéder aux détails via "Voir Détails"

### Scénario 3: Gérer les membres (Admin)

1. Ouvrir les détails d'un groupe dont vous êtes admin
2. Dans la liste des membres:
   - Cliquer sur ↑ pour promouvoir en modérateur
   - Cliquer sur 👑 pour promouvoir en admin
   - Cliquer sur ↓ pour rétrograder
   - Cliquer sur 👤- pour retirer du groupe

### Scénario 4: Groupe privé

1. Créer un groupe avec Type: Privé
2. Décocher "Autoriser l'adhésion libre"
3. Les utilisateurs ne peuvent pas rejoindre directement
4. Seuls les admins peuvent ajouter des membres

## 🔑 Rôles et Permissions

| Action | MEMBER | MODERATOR | ADMIN |
|--------|--------|-----------|-------|
| Voir le groupe | ✅ | ✅ | ✅ |
| Quitter le groupe | ✅ | ✅ | ❌ |
| Retirer des membres | ❌ | ✅ | ✅ |
| Promouvoir en modérateur | ❌ | ❌ | ✅ |
| Promouvoir en admin | ❌ | ❌ | ✅ |
| Rétrograder | ❌ | ❌ | ✅ |
| Supprimer le groupe | ❌ | ❌ | ✅ |

## 🎨 Personnalisation

### Modifier les couleurs

Dans `groups.component.css`:

```css
/* Couleur primaire */
.action-btn {
  background-color: #04d9c4; /* Votre couleur */
}

/* Badges */
.badge-public {
  background-color: #e8f5e9;
  color: #2e7d32;
}
```

### Ajouter des champs personnalisés

1. Ajouter dans le modèle (`group.model.ts`):
```typescript
export interface Group {
  // ... champs existants
  category?: string;
  tags?: string[];
}
```

2. Ajouter dans le formulaire (`groups.component.html`):
```html
<div class="form-group">
  <label for="category">Catégorie</label>
  <select id="category" [(ngModel)]="newGroup.category" name="category">
    <option value="support">Soutien</option>
    <option value="activity">Activité</option>
  </select>
</div>
```

3. Mettre à jour le backend en conséquence

## 🔧 Configuration Avancée

### Utiliser des variables d'environnement

Créer `frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

Mettre à jour `group.service.ts`:

```typescript
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GroupService {
  private apiUrl = `${environment.apiUrl}/api/groups`;
  // ...
}
```

### Ajouter un intercepteur HTTP

Pour gérer l'authentification automatiquement:

```typescript
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('auth_token');
  
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  
  return next(req);
};
```

Ajouter dans `app.config.ts`:

```typescript
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),
    // ...
  ]
};
```

## 📊 Gestion des erreurs

Le module gère automatiquement les erreurs courantes:

- **400 Bad Request**: Affiche un message d'erreur
- **404 Not Found**: Redirige vers la liste des groupes
- **500 Server Error**: Affiche un message d'erreur générique

Pour personnaliser:

```typescript
this.groupService.createGroup(request).subscribe({
  next: (group) => {
    // Succès
  },
  error: (err) => {
    if (err.status === 400) {
      this.error = 'Données invalides';
    } else if (err.status === 403) {
      this.error = 'Accès refusé';
    } else {
      this.error = 'Erreur serveur';
    }
  }
});
```

## 🧪 Tests

### Test manuel

1. Créer un groupe
2. Vérifier qu'il apparaît dans "Mes Groupes"
3. Rejoindre un autre groupe
4. Vérifier les permissions selon le rôle
5. Tester la suppression

### Test avec plusieurs utilisateurs

1. Ouvrir deux navigateurs (ou mode incognito)
2. Se connecter avec deux comptes différents
3. Créer un groupe avec le premier
4. Rejoindre avec le deuxième
5. Tester les interactions

## 🐛 Problèmes courants

### Le groupe ne se crée pas

- Vérifier que le backend est démarré
- Vérifier la console du navigateur pour les erreurs
- Vérifier que tous les champs requis sont remplis

### Les membres ne s'affichent pas

- Vérifier que `includeMembers=true` dans l'appel API
- Vérifier la console réseau (F12 > Network)

### Erreur CORS

Ajouter dans le backend (`GroupController.java`):

```java
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/groups")
public class GroupController {
  // ...
}
```

## 📚 Ressources

- [Documentation Angular](https://angular.io/docs)
- [Documentation Spring Boot](https://spring.io/projects/spring-boot)
- [API REST Best Practices](https://restfulapi.net/)

## 🤝 Contribution

Pour contribuer:

1. Créer une branche: `git checkout -b feature/ma-fonctionnalite`
2. Faire vos modifications
3. Tester localement
4. Créer une pull request

## 📝 Changelog

### Version 1.0.0 (2026-02-25)

- ✅ Création et gestion des groupes
- ✅ Système de rôles (Admin, Moderator, Member)
- ✅ Gestion des membres
- ✅ Interface responsive
- ✅ Intégration complète avec le backend
