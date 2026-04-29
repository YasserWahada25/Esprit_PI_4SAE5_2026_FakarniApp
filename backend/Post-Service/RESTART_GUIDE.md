# Guide de Redémarrage - Post-Service

## ⚠️ IMPORTANT
Après avoir modifié la configuration JWT, vous DEVEZ redémarrer le Post-Service pour que les changements prennent effet.

## Étapes de redémarrage

### Option 1: Via IDE (IntelliJ IDEA / Eclipse)
1. Arrêtez l'application Post-Service
2. Nettoyez le projet: `mvn clean`
3. Recompilez: `mvn install`
4. Redémarrez l'application

### Option 2: Via ligne de commande

#### Windows (PowerShell)
```powershell
cd backend\Post-Service

# Arrêter le service s'il tourne (Ctrl+C dans le terminal)

# Nettoyer et recompiler
mvn clean install -DskipTests

# Redémarrer
mvn spring-boot:run
```

#### Linux/Mac (Bash)
```bash
cd backend/Post-Service

# Arrêter le service s'il tourne (Ctrl+C dans le terminal)

# Nettoyer et recompiler
mvn clean install -DskipTests

# Redémarrer
mvn spring-boot:run
```

## Vérification du démarrage

Le service devrait démarrer sur le port **8069**. Vérifiez les logs pour:

### ✅ Signes de succès
```
Started PostServiceApplication in X.XXX seconds
Tomcat started on port 8069
Registered instance POST-SERVICE with eureka
```

### ❌ Erreurs courantes

#### Erreur: Port déjà utilisé
```
Port 8069 is already in use
```
**Solution:** Tuez le processus qui utilise le port 8069
```powershell
# Windows
netstat -ano | findstr :8069
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8069 | xargs kill -9
```

#### Erreur: MySQL non accessible
```
Communications link failure
```
**Solution:** Démarrez MySQL
```bash
# Windows (XAMPP)
# Démarrez XAMPP et lancez MySQL

# Linux
sudo systemctl start mysql

# Mac
brew services start mysql
```

#### Erreur: Eureka non accessible
```
Cannot execute request on any known server
```
**Solution:** Démarrez Eureka-Service d'abord
```bash
cd backend/Discovery-Service
mvn spring-boot:run
```

## Test après redémarrage

### 1. Vérifier qu'Eureka voit le service
```bash
curl http://localhost:8761/eureka/apps/POST-SERVICE
```

### 2. Tester l'authentification

#### Windows PowerShell
```powershell
.\test-post-service.ps1
```

#### Linux/Mac Bash
```bash
chmod +x test-post-service.sh
./test-post-service.sh
```

### 3. Test manuel avec curl

```bash
# 1. Obtenir un token
TOKEN=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')

# 2. Tester le Post-Service
curl -v http://localhost:8069/api/posts \
  -H "Authorization: Bearer $TOKEN"
```

**Résultat attendu:** HTTP 200 avec la liste des posts (ou tableau vide)

**Si vous obtenez 403:** Le secret JWT ne correspond toujours pas. Vérifiez:
1. Que vous avez bien modifié `application.properties`
2. Que vous avez redémarré le service
3. Que le token est valide (pas expiré)

## Ordre de démarrage recommandé

Pour éviter les problèmes, démarrez les services dans cet ordre:

1. **MySQL** (base de données)
2. **MongoDB** (base de données)
3. **Eureka-Service** (port 8761) - Registre de services
4. **User-Service** (port 8080) - Authentification
5. **Post-Service** (port 8069) - Posts
6. **Gateway-Service** (port 8090) - API Gateway
7. **Frontend Angular** (port 4200)

## Vérification complète

```bash
# Vérifier tous les services
curl http://localhost:8761/eureka/apps | grep -i "post-service\|user-service\|gateway"

# Vérifier les ports
netstat -ano | findstr "8761 8080 8069 8090 4200"
```

## Logs utiles

### Activer les logs de débogage JWT

Ajoutez dans `application.properties`:
```properties
logging.level.com.alzheimer.post_service.config=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Voir les logs en temps réel
```bash
# Dans le terminal où tourne le Post-Service
# Les logs s'affichent automatiquement

# Ou si vous utilisez un fichier de log
tail -f logs/post-service.log
```

## Checklist de vérification

- [ ] MySQL est démarré
- [ ] Eureka-Service est démarré (8761)
- [ ] User-Service est démarré (8080)
- [ ] Post-Service est redémarré après modification
- [ ] Le secret JWT est identique dans User-Service et Post-Service
- [ ] Post-Service est enregistré dans Eureka
- [ ] Aucune erreur dans les logs du Post-Service
- [ ] Le test d'authentification fonctionne

## En cas de problème persistant

1. Supprimez le dossier `target/` du Post-Service
2. Recompilez complètement: `mvn clean install`
3. Vérifiez que `application.properties` contient bien le bon secret
4. Redémarrez tous les services dans l'ordre
5. Testez avec le script de test fourni

## Support

Si le problème persiste:
1. Vérifiez les logs du Post-Service
2. Vérifiez les logs du User-Service
3. Testez l'authentification directement sur le User-Service
4. Vérifiez que le token JWT contient bien les champs `sub` et `role`
