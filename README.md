# 🧠 Fakarni App - Alzheimer Care Platform

[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-green)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20.3-red)](https://angular.io/)
[![Python](https://img.shields.io/badge/Python-3.11-yellow)](https://www.python.org/)

## 📋 Overview

Fakarni is a comprehensive microservices-based platform designed to support Alzheimer's patients and their caregivers. The platform combines modern web technologies with AI-powered disease detection to provide a complete care management solution.

---

## ✨ Key Features

### 👥 User Management
- User registration and authentication
- Role-based access control (Patients, Caregivers, Medical Staff)
- Profile management with medical history
- OAuth integration (Google, Facebook)

### 📍 Location Tracking & Geofencing
- Real-time GPS tracking for patients
- Customizable geofence zones
- Instant alerts when patients leave safe zones
- Location history and analytics

### 🏥 Medical Records Management
- Digital medical records storage
- Medication tracking and reminders
- Appointment scheduling
- Medical document uploads

### 🧠 AI-Powered Disease Detection
- MRI scan analysis using deep learning
- Alzheimer's disease detection and classification
- Risk assessment and progression tracking
- TensorFlow-based neural network model

### 📚 Educational Activities
- Cognitive training exercises
- Memory games and puzzles
- Progress tracking and analytics
- Personalized activity recommendations

### 💬 Real-Time Communication
- WebSocket-based chat system
- Group messaging for families and caregivers
- Emergency broadcast notifications
- Message history and attachments

### 📅 Event & Meeting Management
- Calendar integration
- Appointment scheduling
- Reminder notifications
- Group meeting coordination

### 👨‍👩‍👧‍👦 Family & Caregiver Groups
- Create and manage care groups
- Share updates and information
- Coordinate care activities
- Role assignment and permissions

### 📝 Social Features
- News feed and posts
- Photo and video sharing
- Comments and reactions
- Activity engagement tracking

### 📊 Analytics & Reporting
- Patient engagement metrics
- Activity completion rates
- Health trend analysis
- Customizable reports

---

## 🏗️ Architecture

### Microservices Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (Angular + Nginx)               │
│                   http://localhost:4200                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              API GATEWAY (Spring Cloud Gateway)             │
│                   http://localhost:8090                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           SERVICE DISCOVERY (Eureka Server)                 │
│                   http://localhost:8762                     │
└────────────────────────┬────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┬──────────────┐
         ▼               ▼               ▼              ▼
    ┌────────┐     ┌─────────┐    ┌─────────┐   ┌──────────┐
    │ Spring │     │ Python  │    │ MongoDB │   │  MySQL   │
    │Services│     │   AI    │    │         │   │          │
    │  (14)  │     │ Service │    └─────────┘   └──────────┘
    └────────┘     └─────────┘
```

### Services

| Service | Technology | Port | Database |
|---------|-----------|------|----------|
| **User Service** | Spring Boot | 8081 | MySQL |
| **Tracking Service** | Spring Boot | 9011 | MySQL |
| **Geofencing Service** | Spring Boot | 9012 | MySQL |
| **Activity Service** | Spring Boot | 8084 | MySQL |
| **Chat Service** | Spring Boot | 8070 | MongoDB |
| **Detection Service** | Python/Flask | 8058 | - |
| **Medical Records** | Spring Boot | 8059 | MySQL |
| **Event Service** | Spring Boot | 8087 | MySQL |
| **Group Service** | Spring Boot | 8097 | MySQL |
| **Meeting Service** | Spring Boot | 8096 | MySQL |
| **Post Service** | Spring Boot | 8069 | MySQL |
| **Engagement Service** | Spring Boot | 8088 | MySQL |

---

## 🛠️ Technology Stack

### Backend
- **Spring Boot 4.0.2** - Core framework
- **Spring Cloud 2025.1.0** - Microservices infrastructure
- **Spring Cloud Gateway** - API routing and filtering
- **Eureka** - Service discovery and registration
- **Spring Data JPA** - Database abstraction
- **Spring Security** - Authentication and authorization
- **WebSocket** - Real-time communication
- **Java 21** - Programming language

### AI/ML
- **Python 3.11** - ML service runtime
- **Flask** - REST API framework
- **TensorFlow 2.x** - Deep learning framework
- **Keras** - Neural network API
- **NumPy & Pandas** - Data processing
- **OpenCV** - Image processing

### Frontend
- **Angular 20.3** - SPA framework
- **TypeScript** - Type-safe JavaScript
- **RxJS** - Reactive programming
- **Angular Material** - UI components
- **Nginx** - Web server and reverse proxy

### Databases
- **MySQL 8.0** - Relational data storage
- **MongoDB** - Document storage for chat

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Git LFS** - Large file storage for ML models

---

## 🚀 Quick Start

### Prerequisites

- **Docker Desktop** (20.10+)
- **8 GB RAM minimum**
- **20 GB free disk space**

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/YasserWahada25/Esprit_PI_4SAE5_2026_FakarniApp.git
cd Esprit_PI_4SAE5_2026_FakarniApp
```

2. **Configure environment variables**
```bash
cp .env.example .env
# Edit .env with your credentials
```

3. **Start the application**
```bash
docker compose up -d
```

4. **Wait for services to start** (2-3 minutes)

5. **Access the application**
- Frontend: http://localhost:4200
- API Gateway: http://localhost:8090
- Eureka Dashboard: http://localhost:8762

### Stopping the Application

```bash
docker compose down
```

---

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory:

```env
# Email Configuration
MAIL_USERNAME_USER=your-email@gmail.com
MAIL_PASSWORD_USER=your-app-password

# OAuth - Google
GOOGLE_CLIENT_ID=your-google-client-id

# OAuth - Facebook
FACEBOOK_APP_ID=your-facebook-app-id
FACEBOOK_APP_SECRET=your-facebook-app-secret

# Twilio SMS
TWILIO_ACCOUNT_SID=your-twilio-sid
TWILIO_AUTH_TOKEN=your-twilio-token
TWILIO_FROM_NUMBER=your-twilio-number

# Mailtrap (Development)
MAILTRAP_USERNAME=your-mailtrap-username
MAILTRAP_PASSWORD=your-mailtrap-password
```

---

## 📊 API Documentation

### API Gateway Endpoints

All requests go through the API Gateway at `http://localhost:8090`

#### User Service
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User authentication
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update profile

#### Tracking Service
- `POST /api/tracking/location` - Update location
- `GET /api/tracking/history/{userId}` - Get location history
- `GET /api/tracking/current/{userId}` - Get current location

#### Geofencing Service
- `POST /api/geofence/zones` - Create geofence zone
- `GET /api/geofence/zones/{userId}` - Get user zones
- `POST /api/geofence/check` - Check if location is in zone

#### Detection Service
- `POST /api/detection/analyze` - Analyze MRI scan
- `GET /api/detection/results/{id}` - Get analysis results

#### Activity Service
- `GET /api/activities` - List activities
- `POST /api/activities/{id}/complete` - Mark activity complete
- `GET /api/activities/progress/{userId}` - Get user progress

---

## 🧪 Testing

### Health Checks

```bash
# Check all services
curl http://localhost:8090/actuator/health

# Check specific service
curl http://localhost:8081/actuator/health
```

### AI Detection Service Test

```bash
cd detection-alzheimer/detection-alzheimer
python test_api.py
```

---

## 📱 Mobile Support

The Angular frontend is responsive and works on:
- Desktop browsers (Chrome, Firefox, Safari, Edge)
- Tablets (iPad, Android tablets)
- Mobile devices (iOS, Android)

---

## 🔐 Security Features

- JWT-based authentication
- Role-based access control (RBAC)
- OAuth 2.0 integration
- CORS configuration
- SQL injection prevention
- XSS protection
- HTTPS ready (production)

---

## 📈 Monitoring

### Service Health

Access Eureka Dashboard: http://localhost:8762

### Database Management

- **MySQL**: http://localhost:8086 (PhpMyAdmin)
  - Username: `root`
  - Password: `root`

- **MongoDB**: `mongodb://localhost:27018`

### Logs

```bash
# View all logs
docker compose logs -f

# View specific service
docker compose logs -f user-service

# View last 100 lines
docker compose logs --tail=100 frontend
```

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 Team

Developed by the Esprit PI 4SAE5 2026 team

---

## 📞 Support

For issues and questions:
- Open an issue on GitHub
- Contact: support@fakarni.com

---

## 🙏 Acknowledgments

- Spring Boot and Spring Cloud teams
- Angular team
- TensorFlow and Keras communities
- Docker team
- Open source community

---

**Made with ❤️ for Alzheimer's care**

**Version:** 1.0.0  
**Last Updated:** May 2026
