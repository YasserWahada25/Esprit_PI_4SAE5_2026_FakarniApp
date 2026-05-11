# Fakarni – Connected Health Platform for Seniors

## Overview

This project was developed as part of the **PIDEV – 4th Year Engineering Program** at **Esprit School of Engineering** (Academic Year 2025–2026).

Fakarni is a comprehensive connected health platform designed specifically for elderly people and Alzheimer's patients. Built on a microservices architecture, it provides telemedicine services, medical monitoring, AI-powered disease detection, and family connectivity features to ensure safety, health, and social engagement for seniors.

## Features

- **Real-time GPS Tracking & Geofencing**: Monitor patient location with customizable safety zones and automatic alerts
- **AI-Powered Alzheimer Detection**: Early detection through behavioral analysis using machine learning
- **Electronic Medical Records**: Centralized and secure patient data management
- **Telemedicine Integration**: Video consultations with healthcare providers
- **Family Communication Hub**: Simplified chat interface and shared calendar for family coordination
- **Cognitive Stimulation Activities**: Educational games and therapeutic exercises with progress tracking
- **Support Groups**: Community forums for patients and caregivers
- **Smart Medication Management**: Automated reminders and prescription tracking
- **Emergency Alerts**: Instant SMS/Email notifications to family members

## Tech Stack

### Frontend
- Angular 18
- TypeScript
- Angular Material
- Leaflet (Interactive Maps)
- RxJS

### Backend
- Spring Boot 4.0 (15 Microservices)
- Java 21
- Spring Cloud (Eureka, Gateway)
- MongoDB (Chat, Users)
- MySQL 8.0 (Business Data)
- Python 3.13 + Flask (ML Service)

### DevOps
- Docker & Docker Compose
- Kubernetes
- Jenkins (CI/CD)
- SonarQube
- JaCoCo
- Prometheus & Grafana

## Architecture

### Microservices Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Angular)                    │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│              API Gateway (Port 8090)                     │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│          Eureka Service Discovery (Port 8762)            │
└─────────────┬──────────────────────────┬─────────────────┘
              │                          │
    ┌─────────▼─────────┐      ┌────────▼──────────┐
    │  Core Services    │      │ Business Services │
    ├───────────────────┤      ├───────────────────┤
    │ • User Service    │      │ • Tracking        │
    │ • Chat Service    │      │ • Geofencing      │
    │ • Medical Records │      │ • Activities      │
    │ • Detection AI    │      │ • Events          │
    └───────────────────┘      └───────────────────┘
```

### CI/CD Pipeline

```
GitHub → Jenkins CI → Docker Hub → Jenkins CD → Kubernetes
           ↓
       SonarQube
```

**Pipeline Stages:**
1. Checkout from GitHub
2. Build with Maven
3. Run Tests + JaCoCo Coverage
4. Code Quality Analysis (SonarQube)
5. Package JAR
6. Build Docker Image
7. Push to Docker Hub
8. Deploy to Kubernetes
9. Verify Deployment

## Contributors

**Development Team - Esprit School of Engineering**

- Project Lead & Backend Developer
- Frontend Developer
- DevOps Engineer
- ML Engineer
- Medical Advisor

## Academic Context

**Institution**: Esprit School of Engineering – Tunisia  
**Program**: PIDEV – 4th Year Engineering (4SAE5)  
**Academic Year**: 2025–2026  
**Project Type**: Integrated Development Project  
**Domain**: Healthcare Technology & Connected Health

This project represents the culmination of our engineering studies, combining software architecture, artificial intelligence, DevOps practices, and healthcare domain knowledge to create a meaningful solution for elderly care.

## Getting Started

### Prerequisites

- Docker Desktop with Kubernetes enabled
- Java 21 (JDK)
- Maven 3.9+
- Node.js 20+
- Python 3.13+
- Git

### Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
cd Esprit_PI_4SAE5_2026_FakarniApp

# 2. Configure environment variables
cp .env.example .env
# Edit .env with your credentials

# 3. Start the application
docker-compose up -d

# 4. Access the application
# Frontend: http://localhost:4200
# API Gateway: http://localhost:8090
# Eureka Dashboard: http://localhost:8762
```

### Development Setup

**Backend Service:**
```bash
cd backend/User-Service
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm start
```

**ML Service:**
```bash
cd detection-alzheimer/detection-alzheimer
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

### Running Tests

```bash
# Unit tests
mvn test

# Tests with coverage
mvn clean test jacoco:report

# View coverage report
# Open: backend/User-Service/target/site/jacoco/index.html
```

### Kubernetes Deployment

```bash
# Deploy to Kubernetes
kubectl apply -f k8s/ --recursive

# Check deployment status
kubectl get pods -n fakarni

# Access services
# Frontend: http://localhost:30080
# API Gateway: http://localhost:30090
```

## Acknowledgments

We would like to express our gratitude to:

- **Esprit School of Engineering** for providing the academic framework and resources
- Our **project supervisors** for their guidance and expertise
- **Healthcare professionals** who provided domain knowledge and feedback
- The **open-source community** for the tools and frameworks used in this project
- Our **families** for their continuous support throughout this journey

Special thanks to the elderly care facilities that participated in our user testing and provided valuable insights into the real-world needs of seniors and Alzheimer's patients.

---

**Project Status**: ✅ Production Ready  
**Version**: 2.0  
**Last Updated**: May 2026  
**License**: Academic Project - Educational Use

**Developed with ❤️ at Esprit School of Engineering**
