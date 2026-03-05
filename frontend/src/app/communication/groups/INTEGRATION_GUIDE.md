# Guide d'Intégration - Module Groups

## 📋 Vue d'ensemble

Ce module fournit une interface complète pour gérer les groupes et leurs membres, intégré avec le backend Spring Boot.

## 🎯 Fonctionnalités

### Pour tous les utilisateurs
- ✅ Voir tous les groupes publics
- ✅ Voir mes groupes
- ✅ Créer un nouveau groupe
- ✅ Rejoindre un groupe public
- ✅ Quitter un groupe
- ✅ Voir les détails d'un groupe
- ✅ Voir la liste des membres

### Pour les Modérateurs
- ✅ Retirer des membres
- ✅ Promouvoir des membres

### Pour les Administrateurs
- ✅ Toutes les permissions des modérateurs
- ✅ Promouvoir en admin/modérateur
- ✅ Rétrograder des admins/modérateurs
- ✅ Supprimer le groupe

## 📁 Structure des fichiers

```
communication/groups/
├── models/
│   └── group.model.ts          # Interfaces et enums
├── services/
│   └── group.service.ts        # Service HTTP
├── group-details/
│   ├── group-details.component.ts
│   ├── group-details.component.html
│   └── group-details.component.css
├── groups.component.ts
├── groups.component.html
├── groups.component.css
└── INTEGRATION_GUIDE.md
```

## 🔧 Configuration

### 1. URL de l'API

Dans `group.service.ts`, l'URL par défaut est:
```typescript
private apiUrl = 'http://localhost:8080/api/groups';
```

Pour la production, utilisez une variable d'environnement:
```typescript
private apiUrl = environment.apiUrl + '/api/groups';
```

### 2. Authentification

Le `currentUserId` est actuellement codé en dur. Pour l'intégrer avec votre système d'authentification:

```typescript
// Dans groups.component.ts et group-details.component.ts
export class GroupsComponent implements OnInit {
  currentUserId: number;

  constructor(
    private groupService: GroupService,
    private authService: AuthService  // Votre service d'auth
  ) {
    this.currentUserId = this.authService.getCurrentUserId();
  }
}
```

### 3. HttpClient

Assurez-vous que `provideHttpClient()` est configuré dans `app.config.ts`:

```typescript
import { provideHttpClient } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    // ... autres providers
  ]
};
```

## 🚀 Utilisation

### Créer un groupe

```typescript
const newGroup: CreateGroupRequest = {
  name: 'Mon Groupe',
  description: 'Description du groupe',
  creatorId: currentUserId,
  groupType: GroupType.PUBLIC,
  maxMembers: 50,
  isJoinable: true
};

this.groupService.createGroup(newGroup).subscribe({
  next: (group) => console.log('Groupe créé:', group),
  error: (err) => console.error('Erreur:', err)
});
```

### Ajouter un membre

```typescript
const request: AddMemberRequest = {
  userId: 123,
  role: MemberRole.MEMBER,
  invitedBy: currentUserId
};

this.groupService.addMember(groupId, request).subscribe({
  next: (member) => console.log('Membre ajouté:', member),
  error: (err) => console.error('Erreur:', err)
});
```

### Modifier le rôle d'un membre

```typescript
const request: UpdateMemberRoleRequest = {
  role: MemberRole.MODERATOR
};

this.groupService.updateMemberRole(groupId, userId, request).subscribe({
  next: (member) => console.log('Rôle mis à jour:', member),
  error: (err) => console.error('Erreur:', err)
});
```

## 🎨 Personnalisation

### Couleurs

Les couleurs principales sont définies dans les fichiers CSS:
- Primaire: `#04d9c4` (boutons d'action)
- Secondaire: `#3498db` (liens et infos)
- Danger: `#e74c3c` (suppressions)

### Badges de rôle

```css
.badge-admin { background: #fff9c4; color: #f57f17; }
.badge-moderator { background: #e1bee7; color: #6a1b9a; }
.badge-member { background: #e0e0e0; color: #424242; }
```

## 🔐 Gestion des permissions

Le système de permissions est basé sur les rôles:

```typescript
// Vérifier si l'utilisateur est admin
isAdmin(): boolean {
  const member = this.members.find(m => m.userId === this.currentUserId);
  return member?.role === MemberRole.ADMIN;
}

// Vérifier si l'utilisateur est modérateur ou admin
isModerator(): boolean {
  const member = this.members.find(m => m.userId === this.currentUserId);
  return member?.role === MemberRole.MODERATOR || this.isAdmin();
}
```

## 📱 Responsive Design

Le module est entièrement responsive avec des breakpoints à:
- Mobile: < 768px
- Tablet: 768px - 1024px
- Desktop: > 1024px

## 🧪 Tests

Pour tester l'intégration:

1. Démarrer le backend:
```bash
cd backend/group
./mvnw spring-boot:run
```

2. Démarrer le frontend:
```bash
cd frontend
npm start
```

3. Naviguer vers: `http://localhost:4200/communication/groups`

## 🐛 Dépannage

### Erreur CORS

Si vous rencontrez des erreurs CORS, ajoutez dans votre backend:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH");
            }
        };
    }
}
```

### Erreur 404 sur les routes

Vérifiez que les routes sont bien configurées dans `communication.routes.ts`.

### Les membres ne s'affichent pas

Assurez-vous que le paramètre `includeMembers=true` est passé:
```typescript
this.groupService.getGroupById(groupId, true)
```

## 🔄 Prochaines améliorations

- [ ] Upload d'image de couverture
- [ ] Recherche et filtres de groupes
- [ ] Notifications en temps réel
- [ ] Chat de groupe intégré
- [ ] Système d'invitations
- [ ] Statistiques d'activité
- [ ] Export de données

## 📞 Support

Pour toute question ou problème, consultez:
- Documentation API: `backend/group/API_DOCUMENTATION.md`
- Exemples: `backend/group/EXAMPLES.md`
- Changelog: `backend/group/CHANGELOG.md`
