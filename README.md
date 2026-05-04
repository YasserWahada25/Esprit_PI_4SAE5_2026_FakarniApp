# 🚀 Fakarni App - Plateforme Microservices

[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-green)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20.3-red)](https://angular.io/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)

## 📋 Description

Fakarni est une plateforme complète de microservices développée avec Spring Boot et Angular, offrant des fonctionnalités de :
- 👥 Gestion des utilisateurs
- 📍 Suivi géolocalisation et geofencing
- 💬 Chat en temps réel
- 📚 Activités éducatives
- 🏥 Dossiers médicaux
- 📊 Détection de maladies
- 📅 Gestion d'événements
- 👨‍👩‍👧‍👦 Gestion de groupes
- 📝 Publications et posts
- 📈 Suivi d'engagement

---

## ⚡ Démarrage Ultra-Rapide

### En 1 Commande

```bash
fix-all-issues.bat
```

**C'est tout !** Attendez 10-15 minutes et ouvrez http://localhost:4200

---

## 📚 Documentation

### 🎯 Pour Commencer

| Document | Description | Temps |
|----------|-------------|-------|
| **[GUIDE-VISUEL.txt](GUIDE-VISUEL.txt)** | Vue d'ensemble visuelle | 2 min |
| **[COMMENCER-ICI.md](COMMENCER-ICI.md)** | Guide de démarrage rapide | 5 min |
| **[INDEX.md](INDEX.md)** | Index complet de la documentation | 3 min |

### 📖 Documentation Complète

- **[README-DOCKER.md](README-DOCKER.md)** - Guide complet Docker
- **[PROBLEMES-RESOLUS.md](PROBLEMES-RESOLUS.md)** - Solutions aux problèmes
- **[RESUME-FINAL.md](RESUME-FINAL.md)** - Résumé des corrections
- **[DOCKER-GUIDE-PHASE1.md](DOCKER-GUIDE-PHASE1.md)** - Guide détaillé Phase 1
- **[JENKINS-CICD-GUIDE.md](JENKINS-CICD-GUIDE.md)** - Pipeline CI/CD
- **[PHASE2-MONITORING-GUIDE.md](PHASE2-MONITORING-GUIDE.md)** - Monitoring

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (Nginx)                         │
│                   http://localhost:4200                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   API GATEWAY                               │
│                   http://localhost:8090                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  EUREKA SERVER                              │
│                   http://localhost:8762                     │
└────────────────────────┬────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
    ┌────────┐     ┌────────┐     ┌────────┐
    │14 Micro│     │MongoDB │     │MySQL   │
    │services│     │        │     │        │
    └────────┘     └────────┘     └────────┘
```

---

## 🛠️ Technologies

### Backend
- **Spring Boot 4.0.2** - Framework principal
- **Spring Cloud 2025.1.0** - Microservices
- **Eureka** - Service Discovery
- **Spring Cloud Gateway** - API Gateway
- **MySQL 8.0** - Base de données relationnelle
- **MongoDB** - Base de données NoSQL
- **Java 21** - Langage de programmation

### Frontend
- **Angular 20.3** - Framework frontend
- **Nginx** - Serveur web
- **TypeScript** - Langage de programmation
- **RxJS** - Programmation réactive

### DevOps
- **Docker** - Conteneurisation
- **Docker Compose** - Orchestration
- **Jenkins** - CI/CD (Phase 2)
- **Prometheus** - Monitoring (Phase 2)
- **Grafana** - Visualisation (Phase 2)

---

## 🌐 Services et Ports

| Service | Port | URL |
|---------|------|-----|
| Frontend | 4200 | http://localhost:4200 |
| API Gateway | 8090 | http://localhost:8090 |
| Eureka | 8762 | http://localhost:8762 |
| User Service | 8081 | http://localhost:8081 |
| Tracking Service | 9011 | http://localhost:9011 |
| Geofencing Service | 9012 | http://localhost:9012 |
| Activité Service | 8084 | http://localhost:8084 |
| Chat Service | 8070 | http://localhost:8070 |
| Detection Service | 8058 | http://localhost:8058 |
| Dossier Medical | 8059 | http://localhost:8059 |
| Event Service | 8087 | http://localhost:8087 |
| Group Service | 8097 | http://localhost:8097 |
| Meeting Service | 8096 | http://localhost:8096 |
| Post Service | 8069 | http://localhost:8069 |
| Suivi Engagement | 8088 | http://localhost:8088 |
| PhpMyAdmin | 8086 | http://localhost:8086 |
| MongoDB | 27018 | mongodb://localhost:27018 |

---

## 📦 Prérequis

- **Docker Desktop** (version 20.10+)
- **Java 21**
- **Node.js 20+**
- **Maven 3.8+**
- **8 GB RAM minimum** pour Docker

### Vérification

```bash
docker --version
java -version
node --version
mvn --version
```

---

## 🚀 Installation

### Méthode 1 : Automatique (Recommandée)

```bash
# Clone le projet
git clone <repository-url>
cd Fakarni_App

# Lancer l'installation complète
fix-all-issues.bat
```

### Méthode 2 : Manuelle

```bash
# 1. Arrêter Docker
docker compose down -v

# 2. Build Maven
cd backend/Detection_Maladie-Service
mvn clean install -DskipTests
cd ../..

cd backend/Dossier_Medical-service
mvn clean install -DskipTests
cd ../..

# 3. Build Frontend
cd frontend
npm install
npm run build -- --configuration=production
cd ..

# 4. Build et Démarrer Docker
docker compose build --no-cache
docker compose up -d
```

---

## 🎮 Utilisation

### Démarrage Quotidien

```bash
quick-start.bat
```

### Vérifier l'État

```bash
check-status.bat
```

### Voir les Logs

```bash
# Tous les services
docker compose logs -f

# Service spécifique
docker compose logs -f frontend
```

### Arrêter

```bash
docker compose down
```

### Nettoyage Complet

```bash
clean-all.bat
```

---

## 🔧 Scripts Disponibles

| Script | Description | Durée |
|--------|-------------|-------|
| `fix-all-issues.bat` | Correction et build complet | 10-15 min |
| `quick-start.bat` | Démarrage rapide | 1-2 min |
| `check-status.bat` | Vérification de l'état | 10 sec |
| `clean-all.bat` | Nettoyage complet | 2-3 min |

---

## 🐛 Dépannage

### Frontend ne démarre pas

```bash
docker compose logs -f frontend
docker compose build --no-cache frontend
docker compose up -d frontend
```

### Services ne se connectent pas

```bash
# Vérifier Eureka
curl http://localhost:8762

# Redémarrer
docker compose restart

# Attendre 2-3 minutes
```

### Base de données non accessible

```bash
# Vérifier l'état
docker compose ps

# Redémarrer les DB
docker compose restart db-tracking db-geofencing
```

**Plus de solutions :** Voir [PROBLEMES-RESOLUS.md](PROBLEMES-RESOLUS.md)

---

## 📊 Monitoring

### Logs en Temps Réel

```bash
docker compose logs -f
```

### Statistiques

```bash
docker stats
```

### Eureka Dashboard

Ouvrez http://localhost:8762 pour voir tous les services enregistrés.

---

## 🔐 Configuration

### Variables d'Environnement

Créez un fichier `.env` à la racine :

```env
# Email
MAIL_USERNAME_USER=your-email@gmail.com
MAIL_PASSWORD_USER=your-app-password

# Google OAuth
GOOGLE_CLIENT_ID=your-client-id

# Facebook OAuth
FACEBOOK_APP_ID=your-app-id
FACEBOOK_APP_SECRET=your-app-secret

# Twilio
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_FROM_NUMBER=your-phone-number

# Mailtrap
MAILTRAP_USERNAME=your-username
MAILTRAP_PASSWORD=your-password
```

**⚠️ Important :** Ne jamais commiter le fichier `.env` !

---

## 🧪 Tests

### Tests Unitaires

```bash
# Backend
cd backend/Detection_Maladie-Service
mvn test

# Frontend
cd frontend
npm test
```

### Tests d'Intégration

```bash
# Démarrer l'environnement de test
docker compose -f docker-compose.test.yml up -d

# Exécuter les tests
npm run test:e2e
```

---

## 📈 Roadmap

### Phase 1 : Dockerisation ✅
- [x] Corriger les erreurs
- [x] Dockeriser tous les services
- [x] Créer les scripts d'automatisation
- [x] Documenter le projet

### Phase 2 : CI/CD ⏳
- [ ] Installer Jenkins
- [ ] Créer les pipelines
- [ ] Automatiser les tests
- [ ] Automatiser les déploiements

### Phase 3 : Monitoring ⏳
- [ ] Ajouter Prometheus
- [ ] Configurer Grafana
- [ ] Créer les dashboards
- [ ] Mettre en place les alertes

### Phase 4 : Production ⏳
- [ ] Sécuriser avec HTTPS
- [ ] Configurer les secrets
- [ ] Optimiser les performances
- [ ] Migrer vers Kubernetes

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Veuillez suivre ces étapes :

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## 📝 License

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

---

## 👥 Équipe

- **Développement Backend** - Spring Boot Microservices
- **Développement Frontend** - Angular
- **DevOps** - Docker, CI/CD

---

## 📞 Support

- **Documentation :** [INDEX.md](INDEX.md)
- **Issues :** [GitHub Issues](https://github.com/your-repo/issues)
- **Email :** support@fakarni.com

---

## 🙏 Remerciements

- Spring Boot Team
- Angular Team
- Docker Team
- Communauté Open Source

---

## 📚 Ressources

- [Documentation Complète](INDEX.md)
- [Guide de Démarrage](COMMENCER-ICI.md)
- [Guide Docker](README-DOCKER.md)
- [Résolution de Problèmes](PROBLEMES-RESOLUS.md)

---

**Fait avec ❤️ par l'équipe Fakarni**

**Version :** 1.0.0  
**Dernière mise à jour :** Mai 2026
