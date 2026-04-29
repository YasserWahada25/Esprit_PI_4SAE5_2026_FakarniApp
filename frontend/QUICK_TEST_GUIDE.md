# Guide de Test Rapide - Réactions et Commentaires

## Prérequis

1. MySQL en cours d'exécution
2. Base de données `post_service_db` créée
3. Java 21 installé
4. Node.js et npm installés

## Démarrage des Services

### 1. Démarrer Eureka (Service Discovery)
```bash
cd backend/Eureka-Service
./mvnw spring-boot:run
```
Attendre que le service soit prêt (http://localhost:8761)

### 2. Démarrer Gateway
```bash
cd backend/Gateway-Service
./mvnw spring-boot:run
```
Attendre que le service soit enregistré dans Eureka (http://localhost:8090)

### 3. Démarrer Post-Service
```bash
cd backend/Post-Service
./mvnw spring-boot:run
```
Attendre que le service soit enregistré dans Eureka (http://localhost:8069)

### 4. Démarrer le Frontend
```bash
cd frontend
npm install  # Si première fois
npm start
```
Accéder à http://localhost:4200

## Tests Manuels

### Test 1: Créer un Post
1. Aller sur http://localhost:4200/posts
2. Cliquer sur "Add Post"
3. Entrer du contenu (min 10 caractères)
4. Cliquer sur "Create Post"
5. Vérifier que le post apparaît dans la liste

### Test 2: Réactions
1. Sur un post, cliquer sur le bouton 👍 (LIKE)
2. Vérifier que le compteur passe à 1
3. Vérifier que le bouton devient actif (bleu)
4. Cliquer à nouveau sur 👍
5. Vérifier que le compteur revient à 0
6. Cliquer sur ❤️ (HEART)
7. Vérifier que le compteur HEART passe à 1
8. Cliquer sur 🤝 (SUPPORT)
9. Vérifier que HEART revient à 0 et SUPPORT passe à 1

### Test 3: Commentaires
1. Sur un post, cliquer sur "Show Comments"
2. Entrer un commentaire dans le champ texte
3. Cliquer sur "Post Comment"
4. Vérifier que le commentaire apparaît
5. Cliquer sur "Reply" sur le commentaire
6. Entrer une réponse
7. Cliquer sur "Reply"
8. Vérifier que la réponse apparaît sous le commentaire parent

### Test 4: Suppression de Commentaire
1. Sur votre commentaire, cliquer sur "Delete"
2. Confirmer la suppression
3. Vérifier que le commentaire disparaît

### Test 5: Pagination des Commentaires
1. Ajouter plus de 10 commentaires sur un post
2. Vérifier que la pagination apparaît
3. Cliquer sur "Next"
4. Vérifier que la page suivante se charge

## Tests avec cURL

### Créer un Post
```bash
curl -X POST http://localhost:8090/api/posts \
  -H "Content-Type: application/json" \
  -d '{"content": "Test post pour réactions et commentaires"}'
```

### Toggle Réaction LIKE
```bash
curl -X POST http://localhost:8090/api/posts/1/reactions/toggle \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "type": "LIKE"}'
```

### Obtenir Comptages de Réactions
```bash
curl http://localhost:8090/api/posts/1/reactions?userId=1
```

### Ajouter un Commentaire
```bash
curl -X POST http://localhost:8090/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "content": "Ceci est un commentaire de test"}'
```

### Obtenir les Commentaires
```bash
curl "http://localhost:8090/api/posts/1/comments?page=0&size=10"
```

### Supprimer un Commentaire
```bash
curl -X DELETE "http://localhost:8090/api/posts/1/comments/1?userId=1"
```

## Vérification de la Base de Données

```sql
-- Voir les posts
SELECT * FROM posts;

-- Voir les réactions
SELECT * FROM reactions;

-- Voir les commentaires
SELECT * FROM comments;

-- Comptage des réactions par type pour un post
SELECT type, COUNT(*) as count 
FROM reactions 
WHERE post_id = 1 
GROUP BY type;

-- Commentaires avec leurs réponses
SELECT 
    c1.id as comment_id,
    c1.content as comment,
    c2.id as reply_id,
    c2.content as reply
FROM comments c1
LEFT JOIN comments c2 ON c2.parent_comment_id = c1.id
WHERE c1.post_id = 1 AND c1.parent_comment_id IS NULL;
```

## Problèmes Courants

### Erreur: Cannot connect to server
**Solution**: Vérifier que tous les services backend sont démarrés

### Erreur 404: Post not found
**Solution**: Créer un post d'abord

### Erreur 403: You can only delete your own comments
**Solution**: Utiliser le même userId que celui qui a créé le commentaire

### Les réactions ne se mettent pas à jour
**Solution**: 
1. Vérifier la console du navigateur
2. Vérifier les logs du Post-Service
3. Vérifier que MySQL est accessible

### Erreur: Only one level of comment nesting is allowed
**Solution**: Ne pas essayer de répondre à une réponse (max 1 niveau)

## Logs à Surveiller

### Backend (Post-Service)
```
Hibernate: insert into reactions ...
Hibernate: select ... from reactions where post_id=? and user_id=?
Hibernate: delete from reactions where id=?
```

### Frontend (Console du navigateur)
```
POST http://localhost:8090/api/posts/1/reactions/toggle 200
GET http://localhost:8090/api/posts/1/comments?page=0&size=10 200
```

## Checklist de Validation

- [ ] Les posts s'affichent correctement
- [ ] Les réactions LIKE fonctionnent
- [ ] Les réactions HEART fonctionnent
- [ ] Les réactions SUPPORT fonctionnent
- [ ] Un seul type de réaction par utilisateur
- [ ] Les compteurs sont corrects
- [ ] Les commentaires s'ajoutent
- [ ] Les réponses s'ajoutent
- [ ] La suppression fonctionne (propriétaire uniquement)
- [ ] La pagination fonctionne
- [ ] Les dates sont formatées correctement
- [ ] Les animations fonctionnent
- [ ] Pas d'erreurs dans la console

## Performance

Pour tester avec beaucoup de données:

```bash
# Script pour créer 100 posts
for i in {1..100}; do
  curl -X POST http://localhost:8090/api/posts \
    -H "Content-Type: application/json" \
    -d "{\"content\": \"Post de test numéro $i\"}"
done

# Script pour ajouter des réactions
for i in {1..50}; do
  curl -X POST http://localhost:8090/api/posts/1/reactions/toggle \
    -H "Content-Type: application/json" \
    -d "{\"userId\": $i, \"type\": \"LIKE\"}"
done

# Script pour ajouter des commentaires
for i in {1..20}; do
  curl -X POST http://localhost:8090/api/posts/1/comments \
    -H "Content-Type: application/json" \
    -d "{\"userId\": $i, \"content\": \"Commentaire numéro $i\"}"
done
```

## Support

En cas de problème:
1. Vérifier les logs des services
2. Vérifier la console du navigateur
3. Vérifier la base de données
4. Consulter la documentation API
5. Redémarrer les services si nécessaire
