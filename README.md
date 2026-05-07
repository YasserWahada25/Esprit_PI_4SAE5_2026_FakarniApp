# 🏥 Fakarni App - Plateforme de Santé Connectée

## 📋 Description

Fakarni est une plateforme complète de gestion de santé basée sur une architecture microservices, offrant des services de télémédecine, suivi médical, et détection de maladies par IA.

## 🏗️ Architecture

### Microservices Backend (15 services Spring Boot)

#### Infrastructure Services
- **Eureka-Service** (Port 8762) - Service Discovery
- **Gateway-Service** (Port 8090) - API Gateway & Routing

#### Core Services
- **User-Service** (Port 8081) - Gestion utilisateurs & authentification
- **Chat-Service** (Port 8070) - Messagerie instantanée
- **Dossier-Medical-service** (Port 8059) - Dossiers médicaux électroniques

#### Business Services
- **Tracking-Service** (Port 9011) - Suivi géolocalisation temps réel
- **Geofencing-Service** (Port 9012) - Zones géographiques & alertes
- **Event-Service** (Port 8087) - Gestion événements médicaux
- **Post-Service** (Port 8069) - Publications & actualités santé
- **Detection-Maladie-Service** (Port 8058) - Détection maladies par IA
- **activite-educative-service** (Port 8084) - Activités éducatives
- **session-service** (Port 8071) - Gestion sessions thérapeutiques
- **meeting-insights-service** (Port 8096) - Analyse réunions médicales
- **suivi-engagement-service** (Port 8088) - Suivi engagement patients
- **group-service** (Port 8097) - Gestion groupes de support

### Services Additionnels

- **detection-alzheimer** - Service ML Python/Flask pour détection Alzheimer
- **frontend** (Port 4200) - Application Angular

### Infrastructure & DevOps

- **MongoDB** (Port 27018) - Base NoSQL (Chat, Users)
- **MySQL** (Ports 3310-3319) - 10 bases relationnelles dédiées
- **SonarQube** (Port 9000) - Analyse qualité du code
- **Jenkins** (Port 8085) - CI/CD Automation
- **Kubernetes** - Orchestration conteneurs (Docker Desktop)
- **Docker Hub** - Registry images Docker

## 🚀 Démarrage Rapide

### Prérequis

- **Docker Desktop** avec Kubernetes activé
- **Java 21** (JDK)
- **Maven 3.9+**
- **Node.js 20+**
- **Python 3.13+**
- **Git**

### Installation

```bash
# 1. Cloner le repository
git clone https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
cd Esprit_PI_4SAE5_2026_FakarniApp

# 2. Configurer les variables d'environnement
cp .env.example .env
# Éditer .env avec vos credentials

# 3. Démarrer l'infrastructure (bases de données + Jenkins)
docker-compose --profile cicd up -d

# 4. Vérifier le statut
docker-compose ps
```

### Accès aux Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:4200 | - |
| API Gateway | http://localhost:8090 | - |
| Eureka Dashboard | http://localhost:8762 | - |
| Jenkins | http://localhost:8085 | admin / (voir logs) |
| SonarQube | http://localhost:9000 | admin / admin |
| PhpMyAdmin | http://localhost:8086 | root / root |
| Mongo Express | http://localhost:8091 | admin / admin |
| Adminer | http://localhost:8092 | root / root |

## 🔄 DevOps & CI/CD

### Architecture CI/CD

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌──────────────┐
│   GitHub    │────▶│  Jenkins CI  │────▶│  Docker Hub │────▶│  Jenkins CD  │
│  (Source)   │     │   (Build)    │     │  (Registry) │     │   (Deploy)   │
└─────────────┘     └──────────────┘     └─────────────┘     └──────────────┘
                            │                                         │
                            ▼                                         ▼
                    ┌──────────────┐                         ┌──────────────┐
                    │  SonarQube   │                         │  Kubernetes  │
                    │  (Quality)   │                         │  (Runtime)   │
                    └──────────────┘                         └──────────────┘
```

### Pipelines Configurés

#### CI Pipelines (15 services)
Chaque service dispose d'un pipeline CI automatisé :

**Stages CI** :
1. 📥 **Checkout** - Clone du code depuis GitHub (shallow clone)
2. 🔨 **Build** - Compilation Maven (`mvn clean compile`)
3. 🧪 **Test** - Tests unitaires + JaCoCo coverage
4. 📊 **SonarQube** - Analyse qualité (désactivé temporairement)
5. 📦 **Package** - Création JAR (`mvn package`)
6. 🐳 **Docker Build** - Build image Docker
7. 📤 **Docker Push** - Push vers Docker Hub
8. 🚀 **Trigger CD** - Déclenchement automatique du CD

#### CD Pipelines (15 services)
Déploiement automatique sur Kubernetes :

**Stages CD** :
1. 📥 **Checkout** - Récupération manifests K8s
2. 🔄 **Update Image Tag** - Mise à jour tag Docker
3. 🚀 **Deploy to Kubernetes** - Déploiement via kubectl
4. ✅ **Verify** - Vérification pods Running

### Ordre d'Exécution des Pipelines

#### Phase 1 : Infrastructure (Séquentiel)
```bash
1. eureka-service-CI     # Service Discovery - DOIT être premier
2. gateway-service-CI    # API Gateway - Attend Eureka
```

#### Phase 2 : Core Services (Parallèle possible)
```bash
3. user-service-CI
4. dossier-medical-service-CI
5. chat-service-CI
```

#### Phase 3 : Business Services (Parallèle)
```bash
6-15. Tous les autres services peuvent être lancés en parallèle
```

### Commandes de Vérification

```bash
# Vérifier les pods Kubernetes
kubectl get pods -n fakarni

# Vérifier les services
kubectl get svc -n fakarni

# Logs d'un service
kubectl logs -n fakarni -l app=eureka-server --tail=50

# État du cluster
kubectl cluster-info
```

## 🔧 Configuration

### Variables d'Environnement (.env)

```env
# Mail Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_USERNAME_USER=your-email@gmail.com
MAIL_PASSWORD_USER=your-app-password

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
FACEBOOK_APP_ID=your-facebook-app-id
FACEBOOK_APP_SECRET=your-facebook-app-secret

# Twilio (SMS)
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_FROM_NUMBER=your-twilio-number

# Mailtrap
MAILTRAP_USERNAME=your-mailtrap-username
MAILTRAP_PASSWORD=your-mailtrap-password
```

**⚠️ Important** : Ne jamais commiter le fichier `.env` !

### Jenkins Credentials

Les credentials suivants doivent être configurés dans Jenkins :

| ID | Type | Description |
|----|------|-------------|
| `github-credentials` | Username/Password | Token GitHub |
| `dockerhub-credentials` | Username/Password | Docker Hub |
| `kubeconfig` | Secret File | Kubeconfig Kubernetes |

## 📊 Qualité du Code

### Métriques

- **JaCoCo Coverage** : Configuré sur tous les services
- **SonarQube** : Analyse statique du code
- **Tests Unitaires** : JUnit 5 + Mockito
- **Tests d'Intégration** : Spring Boot Test

### Exécuter les Tests

```bash
# Tests unitaires
cd backend/User-Service
mvn test

# Tests avec coverage
mvn clean test jacoco:report

# Analyse SonarQube (si activé)
mvn sonar:sonar \
  -Dsonar.projectKey=user-service \
  -Dsonar.host.url=http://localhost:9000
```

## 🛠️ Développement Local

### Backend (Spring Boot)

```bash
# Démarrer un service
cd backend/User-Service
mvn spring-boot:run

# Avec profil Docker
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Frontend (Angular)

```bash
cd frontend
npm install
npm start
# Accès: http://localhost:4200
```

### Service Python (ML)

```bash
cd detection-alzheimer/detection-alzheimer
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

## 🐳 Docker

### Build Images

```bash
# Build un service spécifique
docker-compose build user-service

# Build tous les services
docker-compose build

# Build avec cache désactivé
docker-compose build --no-cache
```

### Gestion des Conteneurs

```bash
# Démarrer tous les services
docker-compose up -d

# Démarrer avec profil CI/CD
docker-compose --profile cicd up -d

# Arrêter tous les services
docker-compose down

# Voir les logs
docker-compose logs -f user-service

# Nettoyer les volumes
docker-compose down -v
```

## ☸️ Kubernetes

### Déploiement Manuel

```bash
# Créer le namespace
kubectl create namespace fakarni

# Appliquer les ConfigMaps et Secrets
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

# Déployer un service
kubectl apply -f k8s/user-service/deployment.yaml

# Vérifier le déploiement
kubectl get deployments -n fakarni
kubectl get pods -n fakarni
```

### Monitoring

```bash
# Dashboard Kubernetes (si installé)
kubectl proxy
# Accès: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/

# Métriques des pods
kubectl top pods -n fakarni

# Événements
kubectl get events -n fakarni --sort-by='.lastTimestamp'
```

## 🔐 Sécurité

### Authentification
- **JWT** : Tokens pour API REST
- **OAuth2** : Google & Facebook login
- **Spring Security** : Protection endpoints

### Secrets Management
- **Jenkins Credentials** : Stockage sécurisé
- **Kubernetes Secrets** : Secrets runtime
- **Environment Variables** : Configuration sensible

### Best Practices
- Pas de secrets dans le code
- HTTPS en production
- Rate limiting sur Gateway
- Input validation
- CORS configuré

## 📝 Documentation API

### Swagger UI

Chaque microservice expose sa documentation Swagger :

- User Service: http://localhost:8081/swagger-ui.html
- Gateway: http://localhost:8090/swagger-ui.html
- Etc.

### Endpoints Principaux

#### User Service
```
POST   /api/users/register
POST   /api/users/login
GET    /api/users/profile
PUT    /api/users/profile
```

#### Chat Service
```
GET    /api/chat/conversations
POST   /api/chat/messages
WS     /ws/chat
```

## 🧪 Tests

### Structure des Tests

```
src/
├── main/java/
└── test/java/
    ├── unit/          # Tests unitaires
    ├── integration/   # Tests d'intégration
    └── e2e/          # Tests end-to-end
```

### Exécution

```bash
# Tests unitaires uniquement
mvn test

# Tests d'intégration
mvn verify

# Avec coverage
mvn clean test jacoco:report

# Rapport dans: target/site/jacoco/index.html
```

## 📦 Structure du Projet

```
Fakarni_App/
├── backend/                    # Microservices Spring Boot
│   ├── Eureka-Service/        # Service Discovery
│   ├── Gateway-Service/       # API Gateway
│   ├── User-Service/          # Gestion utilisateurs
│   └── .../                   # Autres services
├── frontend/                   # Application Angular
├── detection-alzheimer/        # Service ML Python
├── k8s/                       # Manifests Kubernetes
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── eureka/
│   ├── gateway/
│   └── .../
├── docker-compose.yml         # Orchestration Docker
├── .env.example              # Template environnement
├── .gitignore
└── README.md
```

## 👥 Équipe

**Projet Académique - ESPRIT 4SAE5**  
Année Universitaire : 2025-2026

## 📄 Licence

Ce projet est développé dans un cadre académique à des fins éducatives.

## 🆘 Support & Contribution

### Problèmes Courants

**Jenkins ne démarre pas** :
```bash
docker-compose logs jenkins
docker-compose restart jenkins
```

**Pods en CrashLoopBackOff** :
```bash
kubectl describe pod <pod-name> -n fakarni
kubectl logs <pod-name> -n fakarni
```

**Build Maven échoue** :
```bash
mvn clean install -U  # Force update dependencies
```

### Contact

Pour toute question ou problème :
- Ouvrir une issue sur GitHub
- Consulter la documentation technique
- Contacter l'équipe de développement

---

**Version** : 2.0  
**Dernière mise à jour** : Mai 2026  
**Status** : ✅ Production Ready
