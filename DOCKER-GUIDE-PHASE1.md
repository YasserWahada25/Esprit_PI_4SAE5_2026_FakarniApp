# 🚀 GUIDE DOCKERISATION PHASE 1 - FAKARNI

## 📋 OBJECTIF
Lancer toute l'architecture avec Docker Compose sans utiliser IntelliJ ou VS Code.

---

## ⚡ DÉMARRAGE RAPIDE

### 1️⃣ Prérequis
```bash
# Vérifier Docker
docker --version
docker compose version

# Vérifier que Docker Desktop est lancé (Windows)
```

### 2️⃣ Configuration des variables d'environnement
Éditer le fichier `.env` à la racine du projet et remplir vos vraies valeurs :

```env
# Mail Configuration (obligatoire pour User Service)
MAIL_USERNAME_USER=votre-email@gmail.com
MAIL_PASSWORD_USER=votre-mot-de-passe-app-gmail

# OAuth2 (optionnel pour tests)
GOOGLE_CLIENT_ID=votre-google-client-id
FACEBOOK_APP_ID=votre-facebook-app-id
FACEBOOK_APP_SECRET=votre-facebook-app-secret
```

### 3️⃣ Lancer toute l'architecture
```bash
# Depuis la racine du projet
docker compose up --build
```

**⏱️ Temps de build initial : 10-15 minutes**

### 4️⃣ Vérifier que tout fonctionne

#### Eureka Dashboard
```
http://localhost:8762
```
✅ Vous devez voir tous les services enregistrés

#### API Gateway
```
http://localhost:8090/actuator/health
```
✅ Doit retourner `{"status":"UP"}`

#### Frontend Angular
```
http://localhost:4200
```
✅ L'application doit s'afficher

#### PhpMyAdmin (pour MySQL)
```
http://localhost:8086
```
- Serveur : `db-tracking` (ou autre service)
- Utilisateur : `root`
- Mot de passe : `root`

---

## 🔍 COMPRENDRE L'ARCHITECTURE DOCKER

### Pourquoi localhost ne fonctionne plus ?

#### ❌ AVANT (développement local)
```
Frontend (localhost:4200) → Gateway (localhost:8090) → User-Service (localhost:8081)
```

#### ✅ MAINTENANT (Docker)
```
Frontend (container) → Gateway (container) → User-Service (container)
                ↓
         Eureka (container)
```

### Communication Docker

Chaque container a son propre réseau. Docker crée un **DNS interne** :
- `mongodb` → pointe vers le container MongoDB
- `eureka-server` → pointe vers le container Eureka
- `api-gateway` → pointe vers le container Gateway
- `db-tracking` → pointe vers le container MySQL tracking

**C'est pourquoi dans `application-docker.properties` on utilise :**
```properties
# ❌ NE MARCHE PAS dans Docker
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# ✅ MARCHE dans Docker
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka/
```

---

## 📦 SERVICES DISPONIBLES

| Service | Port Externe | Port Interne | URL |
|---------|--------------|--------------|-----|
| Frontend | 4200 | 4000 | http://localhost:4200 |
| Eureka | 8762 | 8761 | http://localhost:8762 |
| Gateway | 8090 | 8090 | http://localhost:8090 |
| User Service | 8081 | 8081 | http://localhost:8081 |
| Chat Service | 8070 | 8070 | http://localhost:8070 |
| Tracking Service | 9011 | 9011 | http://localhost:9011 |
| Geofencing Service | 9012 | 9012 | http://localhost:9012 |
| Activité Service | 8084 | 8084 | http://localhost:8084 |
| Detection Service | 8058 | 8058 | http://localhost:8058 |
| Dossier Service | 8059 | 8059 | http://localhost:8059 |
| Event Service | 8087 | 8087 | http://localhost:8087 |
| Group Service | 8097 | 8097 | http://localhost:8097 |
| Meeting Service | 8096 | 8096 | http://localhost:8096 |
| Post Service | 8069 | 8069 | http://localhost:8069 |
| Suivi Service | 8088 | 8088 | http://localhost:8088 |
| MongoDB | 27018 | 27017 | mongodb://localhost:27018 |
| MySQL Tracking | 3310 | 3306 | localhost:3310 |
| MySQL Geofencing | 3311 | 3306 | localhost:3311 |
| MySQL Activité | 3312 | 3306 | localhost:3312 |
| MySQL Detection | 3313 | 3306 | localhost:3313 |
| MySQL Dossier | 3314 | 3306 | localhost:3314 |
| MySQL Event | 3315 | 3306 | localhost:3315 |
| MySQL Group | 3316 | 3306 | localhost:3316 |
| MySQL Post | 3317 | 3306 | localhost:3317 |
| MySQL Suivi | 3319 | 3306 | localhost:3319 |
| PhpMyAdmin | 8086 | 80 | http://localhost:8086 |

---

## 🛠️ COMMANDES UTILES

### Démarrer tous les services
```bash
docker compose up
```

### Démarrer en arrière-plan
```bash
docker compose up -d
```

### Rebuild et démarrer
```bash
docker compose up --build
```

### Arrêter tous les services
```bash
docker compose down
```

### Arrêter et supprimer les volumes (⚠️ SUPPRIME LES DONNÉES)
```bash
docker compose down -v
```

### Voir les logs d'un service
```bash
docker compose logs -f user-service
docker compose logs -f api-gateway
docker compose logs -f frontend
```

### Voir tous les logs
```bash
docker compose logs -f
```

### Redémarrer un service spécifique
```bash
docker compose restart user-service
```

### Rebuild un seul service
```bash
docker compose up --build user-service
```

### Voir les containers en cours
```bash
docker compose ps
```

### Entrer dans un container
```bash
docker exec -it fakarni_user_service sh
docker exec -it fakarni_mongo mongosh
```

---

## 🔧 ORDRE DE DÉMARRAGE

Docker Compose gère automatiquement l'ordre avec `depends_on`, mais voici la séquence :

1. **Bases de données** (MongoDB, MySQL)
2. **Eureka Server** (Discovery)
3. **API Gateway** (attend Eureka)
4. **Microservices** (attendent Eureka + leurs DB)
5. **Frontend** (attend Gateway)

---

## ❗ PROBLÈMES FRÉQUENTS

### 1. Service ne démarre pas
```bash
# Voir les logs
docker compose logs -f nom-du-service

# Vérifier l'état
docker compose ps
```

### 2. Erreur de connexion à la base de données
**Symptôme :** `Communications link failure`

**Solution :**
```bash
# Attendre que la DB soit prête (healthcheck)
# Ou redémarrer le service
docker compose restart user-service
```

### 3. Eureka ne voit pas les services
**Symptôme :** Dashboard Eureka vide

**Causes possibles :**
- Services pas encore démarrés (attendre 1-2 minutes)
- Erreur dans `application-docker.properties`
- Problème réseau Docker

**Vérification :**
```bash
# Vérifier que le profil Docker est actif
docker compose logs user-service | grep "docker"
```

### 4. Frontend ne peut pas joindre le Gateway
**Symptôme :** Erreur CORS ou 404

**Solution :**
- Vérifier que Gateway est UP : http://localhost:8090/actuator/health
- Vérifier les logs du Gateway : `docker compose logs -f api-gateway`

### 5. Port déjà utilisé
**Symptôme :** `Bind for 0.0.0.0:8090 failed: port is already allocated`

**Solution :**
```bash
# Windows - Trouver le processus
netstat -ano | findstr :8090

# Tuer le processus
taskkill /PID <PID> /F

# Ou changer le port dans docker-compose.yml
ports:
  - "8091:8090"  # Utiliser 8091 au lieu de 8090
```

### 6. Build Maven échoue
**Symptôme :** Erreur pendant `mvn clean package`

**Solution :**
```bash
# Nettoyer les images Docker
docker compose down
docker system prune -a

# Rebuild
docker compose up --build
```

### 7. Mémoire insuffisante
**Symptôme :** Container killed / OOMKilled

**Solution :**
- Augmenter la mémoire Docker Desktop (Settings → Resources → Memory)
- Minimum recommandé : **8 GB**

---

## ✅ CHECKLIST DE VÉRIFICATION

### Après `docker compose up --build`

- [ ] Tous les containers sont UP : `docker compose ps`
- [ ] Eureka accessible : http://localhost:8762
- [ ] Tous les services enregistrés dans Eureka (attendre 2 min)
- [ ] Gateway accessible : http://localhost:8090/actuator/health
- [ ] Frontend accessible : http://localhost:4200
- [ ] PhpMyAdmin accessible : http://localhost:8086
- [ ] Pas d'erreurs dans les logs : `docker compose logs`

### Test fonctionnel

1. Ouvrir http://localhost:4200
2. Aller sur la page d'inscription
3. Créer un compte
4. Vérifier que l'API répond (pas d'erreur 500)

---

## 📊 MONITORING (PHASE 2)

SonarQube est disponible mais désactivé par défaut pour économiser les ressources.

### Activer SonarQube
```bash
docker compose --profile monitoring up -d
```

### Accéder à SonarQube
```
http://localhost:9000
```
- Login : `admin`
- Password : `admin` (changez-le au premier login)

---

## 🎯 PROCHAINES ÉTAPES (PHASE 2)

Une fois que tout fonctionne :

1. **JaCoCo** : Ajouter la couverture de code
2. **Prometheus + Grafana** : Monitoring avancé
3. **ELK Stack** : Logging centralisé
4. **Nginx** : Reverse proxy
5. **CI/CD Jenkins** : Pipeline automatisé
6. **HTTPS** : Certificats SSL
7. **Kubernetes** : Orchestration avancée

---

## 📝 NOTES IMPORTANTES

### Données persistantes
Les volumes Docker conservent les données même après `docker compose down` :
- `fakarni_mongo_data` : Données MongoDB
- `fakarni_db_*_data` : Données MySQL

Pour tout supprimer :
```bash
docker compose down -v
```

### Profils Spring Boot
Chaque service utilise automatiquement le profil `docker` grâce à :
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
```

### Variables d'environnement
Le fichier `.env` est automatiquement chargé par Docker Compose.

---

## 🆘 SUPPORT

### Logs détaillés
```bash
# Tous les services
docker compose logs -f

# Service spécifique
docker compose logs -f user-service

# Dernières 100 lignes
docker compose logs --tail=100 user-service
```

### Redémarrage complet
```bash
# Arrêter tout
docker compose down

# Nettoyer (optionnel)
docker system prune -f

# Redémarrer
docker compose up --build
```

---

## 🎉 SUCCÈS !

Si vous voyez :
- ✅ Eureka Dashboard avec tous les services
- ✅ Frontend Angular qui s'affiche
- ✅ Pas d'erreurs dans les logs

**Félicitations ! Votre architecture microservices est dockerisée et fonctionnelle !**

Vous pouvez maintenant :
- Démo le projet sans ouvrir IntelliJ/VS Code
- Partager l'architecture avec `docker compose up`
- Déployer facilement sur un serveur

---

## 📚 RESSOURCES

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Eureka Server](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
