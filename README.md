# 🏥 Fakarni App - Plateforme de Santé Connectée

## 📋 Description

Fakarni est une plateforme complète de gestion de santé basée sur une architecture microservices, offrant des services de télémédecine, suivi médical, et détection de maladies par IA.

## 🏗️ Architecture

### Microservices Backend (14 services Spring Boot)

- **User-Service** - Gestion des utilisateurs et authentification
- **Eureka-Service** - Service discovery
- **Gateway-Service** - API Gateway
- **Tracking-Service** - Suivi géolocalisation
- **Event-Service** - Gestion des événements
- **activite-educative-service** - Activités éducatives
- **Chat-Service** - Messagerie instantanée
- **Detection-Maladie-Service** - Détection de maladies
- **Dossier-Medical-service** - Dossiers médicaux
- **Notification-Service** - Notifications
- **Paiement-Service** - Gestion des paiements
- **Pharmacie-Service** - Gestion pharmacie
- **Rendez-Vous-Service** - Prise de rendez-vous
- **Video-Service** - Visioconférence

### Services Spéciaux

- **detection-alzheimer** - Service ML Python/Flask pour détection Alzheimer
- **frontend** - Application Angular

### Infrastructure

- **MongoDB** - Base de données NoSQL
- **MySQL** - Bases de données relationnelles
- **SonarQube** - Analyse qualité du code
- **Jenkins** - CI/CD

## 🚀 Démarrage Rapide

### Prérequis

- Docker & Docker Compose
- Java 21
- Node.js 20
- Python 3.x

### Lancer l'application

```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier le statut
docker-compose ps
```

### Accès aux services

- **Frontend**: http://localhost:4200
- **API Gateway**: http://localhost:8081
- **Eureka Dashboard**: http://localhost:8761
- **SonarQube**: http://localhost:9000
- **Jenkins**: http://localhost:8085

## 🔧 Configuration

### Variables d'environnement

Copiez `.env.example` vers `.env` et configurez vos variables:

```bash
cp .env.example .env
```

**Important**: Le fichier `.env` contient des secrets et ne doit JAMAIS être commité.

## 🔄 CI/CD

Le projet utilise Jenkins pour l'intégration et le déploiement continus.

### Documentation CI/CD

Consultez **[CICD_SETUP_GUIDE.md](./CICD_SETUP_GUIDE.md)** pour:
- Configuration Jenkins
- Création des pipelines CI/CD
- Gestion des credentials
- Déploiement automatique

### Flux CI/CD

```
GitHub → Jenkins CI → SonarQube → Docker Hub → Jenkins CD → Production
```

## 📊 Qualité du Code

- **JaCoCo**: Couverture de code configurée dans tous les services
- **SonarQube**: Analyse statique et quality gates
- **Tests**: Tests unitaires et d'intégration

## 🛠️ Développement

### Structure du projet

```
Fakarni_App/
├── backend/              # Microservices Spring Boot
├── frontend/             # Application Angular
├── detection-alzheimer/  # Service ML Python
├── docker-compose.yml    # Configuration Docker
├── .env.example          # Template variables d'environnement
└── CICD_SETUP_GUIDE.md  # Guide CI/CD complet
```

### Développement local

#### Backend (Spring Boot)

```bash
cd backend/User-Service
mvn spring-boot:run
```

#### Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

#### Service Python

```bash
cd detection-alzheimer/detection-alzheimer
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

## 🧪 Tests

### Tests unitaires

```bash
# Spring Boot
mvn test

# Angular
npm test

# Python
pytest
```

### Analyse SonarQube

```bash
mvn sonar:sonar \
  -Dsonar.projectKey=mediconnect-service-name \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN
```

## 📦 Build & Déploiement

### Build Docker

```bash
# Build un service spécifique
docker-compose build user-service

# Build tous les services
docker-compose build
```

### Déploiement

Le déploiement est automatisé via Jenkins. Consultez `CICD_SETUP_GUIDE.md`.

## 🔐 Sécurité

- Authentification JWT
- OAuth2 (Google, Facebook)
- Secrets gérés via Jenkins Credentials
- HTTPS en production

## 📝 Documentation API

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: Disponible sur chaque microservice

## 👥 Équipe

Projet développé par l'équipe 4SAE5 - ESPRIT 2026

## 📄 Licence

Ce projet est développé dans un cadre académique.

## 🆘 Support

Pour toute question:
- Consultez `CICD_SETUP_GUIDE.md` pour le CI/CD
- Consultez `detection-alzheimer/README.md` pour le service ML
- Ouvrez une issue sur GitHub

---

**Version**: 1.0  
**Dernière mise à jour**: Mai 2026
