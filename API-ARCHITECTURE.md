# Comment accéder à l’API (Fakarni)

## À ne pas faire

- **Ne jamais** ouvrir `http://localhost:8761/api/users` dans le navigateur.  
  **8761** = Eureka (registre de services). Il n’y a pas d’endpoint `/api/users` sur Eureka, donc vous aurez toujours **404**.  
  Eureka sert uniquement à lister les services (dashboard) ; ce n’est pas une passerelle API.

## Comment ça fonctionne

```
Frontend Angular (4200)  →  Gateway (8090)  →  User-Service (8081)
                                ↑
                         Eureka (8761) : la Gateway trouve User-Service ici
```

- **Frontend** : l’app Angular sur `http://localhost:4200` appelle **automatiquement** la Gateway (`http://localhost:8090`). Vous n’avez rien à taper avec 8761.
- **Gateway (8090)** : point d’entrée de l’API. C’est elle qui reçoit `/api/users`, `/auth/login`, etc. et qui redirige vers User-Service (ou d’autres microservices).
- **User-Service (8081)** : contient le `UserController` avec `@RequestMapping("/api/users")`. On y accède **via la Gateway** (8090) ou en direct (8081) pour des tests.

## Comment tester

1. Démarrer dans l’ordre : **Eureka** (8761) → **User-Service** (8081) → **Gateway** (8090) → **Angular** (4200).
2. Pour le **frontend** : ouvrir `http://localhost:4200`, aller sur Inscription, remplir le formulaire. L’app envoie les requêtes vers **8090** (déjà configuré dans le code).
3. Pour un **test direct** de l’API (sans Angular) :
   - Via la Gateway : `http://localhost:8090/api/users` (GET/POST selon besoin).
   - En direct sur User-Service : `http://localhost:8081/api/users`.

En résumé : **utilisez l’app Angular sur 4200** pour tester l’inscription ; l’URL 8761 ne sert qu’au dashboard Eureka, pas à appeler l’API.
