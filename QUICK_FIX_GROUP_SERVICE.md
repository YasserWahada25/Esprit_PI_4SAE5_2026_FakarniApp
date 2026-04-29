# Fix rapide - Group-Service erreur 500

## Problème
Le Group-Service retourne une erreur 500 car il a des données avec des IDs numériques (Long) alors qu'il attend maintenant des IDs MongoDB (String).

## Solution rapide

### Étape 1: Nettoyer la base de données

Ouvrez MySQL et exécutez:

```sql
USE group_service_db;

-- Supprimer toutes les données
DELETE FROM group_members;
DELETE FROM groups;

-- Réinitialiser les auto-increment
ALTER TABLE groups AUTO_INCREMENT = 1;
ALTER TABLE group_members AUTO_INCREMENT = 1;

-- Vérifier que c'est vide
SELECT COUNT(*) FROM groups;
SELECT COUNT(*) FROM group_members;
```

### Étape 2: Redémarrer le Group-Service

```bash
cd backend/group
mvn clean install -DskipTests
mvn spring-boot:run
```

### Étape 3: Tester

1. Connectez-vous dans le frontend
2. Allez sur la page Groups
3. Créez un nouveau groupe
4. Le groupe devrait être créé avec votre ID MongoDB

## Vérification

Dans les logs du Group-Service, vous devriez voir:
```
JWT validated successfully for user: 675e1234567890abcdef1234 with role: PATIENT
```

Et NON:
```
Failed to fetch creator details: [500] during [GET] to [http://user-service/api/users/1]
```

## Si le problème persiste

Vérifiez que:
1. Le Group-Service est bien redémarré
2. La base de données est bien nettoyée
3. L'utilisateur est connecté dans le frontend
4. Le token JWT est valide

## Commande complète (Windows PowerShell)

```powershell
# 1. Nettoyer la base de données
mysql -u root -p -e "USE group_service_db; DELETE FROM group_members; DELETE FROM groups; ALTER TABLE groups AUTO_INCREMENT = 1; ALTER TABLE group_members AUTO_INCREMENT = 1;"

# 2. Redémarrer le service
cd backend\group
mvn clean install -DskipTests
mvn spring-boot:run
```
