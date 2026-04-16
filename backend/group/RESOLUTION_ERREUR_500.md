# Résolution de l'erreur 500 - User-Service

## Problème
```
Failed to fetch creator details: [500] during [GET] to [http://user-service/api/users/1]
```

## Cause
Le Group-Service essaie de récupérer un utilisateur avec l'ID "1" (ancien format Long), mais le User-Service attend un ID MongoDB (format String comme "675e1234567890abcdef1234").

Cela se produit parce que vous avez des données existantes dans la base de données créées avant la migration.

## Solutions

### Solution 1: Nettoyer la base de données (RECOMMANDÉ pour le développement)

Cette solution supprime toutes les données existantes et vous permet de repartir à zéro.

#### Étape 1: Exécuter le script de nettoyage
```bash
mysql -u root -p < backend/group/CLEAN_DATABASE.sql
```

Ou manuellement:
```sql
USE group_service_db;

-- Supprimer toutes les données
DELETE FROM group_members;
DELETE FROM groups;

-- Réinitialiser les auto-increment
ALTER TABLE groups AUTO_INCREMENT = 1;
ALTER TABLE group_members AUTO_INCREMENT = 1;
```

#### Étape 2: Redémarrer le Group-Service
```bash
cd backend/group
mvn spring-boot:run
```

#### Étape 3: Créer un nouveau groupe
```bash
# 1. Se connecter
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

# 2. Créer un groupe
curl -X POST http://localhost:8090/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mon nouveau groupe",
    "description": "Description du groupe",
    "groupType": "PUBLIC",
    "maxMembers": 50,
    "isJoinable": true
  }' | jq
```

### Solution 2: Migrer les données existantes

Si vous voulez conserver vos données, vous devez les migrer vers des IDs MongoDB valides.

#### Étape 1: Récupérer les IDs MongoDB des utilisateurs

Connectez-vous à MongoDB:
```bash
mongosh
use user_service_db
db.users.find({}, {_id: 1, email: 1, nom: 1, prenom: 1})
```

Exemple de résultat:
```json
{
  "_id": "675e1234567890abcdef1234",
  "email": "test@example.com",
  "nom": "Dupont",
  "prenom": "Jean"
}
```

#### Étape 2: Mettre à jour la base de données MySQL

```sql
USE group_service_db;

-- Voir les données actuelles
SELECT id, name, creator_id FROM groups;
SELECT id, group_id, user_id, role FROM group_members;

-- Mettre à jour avec les vrais IDs MongoDB
UPDATE groups 
SET creator_id = '675e1234567890abcdef1234' 
WHERE creator_id = '1';

UPDATE group_members 
SET user_id = '675e1234567890abcdef1234' 
WHERE user_id = '1';

-- Vérifier les modifications
SELECT id, name, creator_id FROM groups;
SELECT id, group_id, user_id, role FROM group_members;
```

#### Étape 3: Redémarrer le Group-Service

```bash
cd backend/group
mvn spring-boot:run
```

### Solution 3: Désactiver temporairement l'enrichissement utilisateur

Si vous voulez tester sans enrichir les données utilisateur, vous pouvez commenter temporairement le code dans `GroupService.java`:

```java
private GroupResponse toResponse(Group group, boolean includeMembers) {
    GroupResponse response = new GroupResponse();
    // ... autres champs ...
    
    // ❌ Commentez temporairement cette partie
    /*
    try {
        UserDTO creator = userClient.getUserById(group.getCreatorId());
        response.setCreator(creator);
    } catch (Exception e) {
        System.err.println("Failed to fetch creator details: " + e.getMessage());
    }
    */
    
    return response;
}
```

⚠️ **ATTENTION**: Cette solution est temporaire et ne doit être utilisée que pour le développement.

## Vérification

Après avoir appliqué une solution, vérifiez que tout fonctionne:

### 1. Vérifier la base de données
```sql
USE group_service_db;

-- Les creator_id doivent être des IDs MongoDB (24 caractères hexadécimaux)
SELECT id, name, creator_id, LENGTH(creator_id) as id_length FROM groups;

-- Les user_id doivent être des IDs MongoDB
SELECT id, group_id, user_id, LENGTH(user_id) as id_length FROM group_members;
```

Résultat attendu:
- `id_length` doit être 24 pour les IDs MongoDB
- Les IDs doivent ressembler à: `675e1234567890abcdef1234`

### 2. Tester la création d'un groupe
```bash
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

curl -X POST http://localhost:8090/api/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test groupe",
    "description": "Test",
    "groupType": "PUBLIC"
  }' | jq
```

Résultat attendu:
```json
{
  "id": 1,
  "name": "Test groupe",
  "description": "Test",
  "creatorId": "675e1234567890abcdef1234",
  "creator": {
    "id": "675e1234567890abcdef1234",
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "test@example.com"
  },
  "groupType": "PUBLIC",
  "status": "ACTIVE",
  ...
}
```

### 3. Vérifier les logs

Les logs ne doivent plus afficher:
```
Failed to fetch creator details: [500]
```

Mais plutôt:
```
JWT validated successfully for user: 675e1234567890abcdef1234
```

## Prévention

Pour éviter ce problème à l'avenir:

1. **Toujours exécuter les scripts de migration** avant de démarrer un service modifié
2. **Nettoyer les données de test** entre les modifications majeures
3. **Utiliser des IDs cohérents** entre les services (String pour MongoDB)
4. **Tester avec des données fraîches** après une migration

## Checklist

- [ ] Base de données nettoyée OU données migrées
- [ ] Group-Service redémarré
- [ ] Utilisateur créé dans User-Service
- [ ] Token JWT obtenu
- [ ] Groupe créé avec succès
- [ ] Champ `creator` rempli avec les informations utilisateur
- [ ] Aucune erreur 500 dans les logs
