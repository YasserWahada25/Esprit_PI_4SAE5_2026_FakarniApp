
# 🧠 Fakarni — Alzheimer Care Platform

<div align="center">

![Angular](https://img.shields.io/badge/Angular-20-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**A comprehensive microservices platform for Alzheimer's patient care, monitoring, and family support.**

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Services](#services)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

---

## 🔍 Overview

**Fakarni** (فكرني — "Remind me" in Arabic) is a full-stack healthcare application designed to support Alzheimer's patients and their caregivers. It provides tools for medical monitoring, geofencing alerts, virtual consultations, educational content, and community communication — all in one platform.

---

## ✨ Features

| Module | Description |
|---|---|
| 🏥 **Medical Monitoring** | Track patient records, MRI analysis, cognitive follow-ups |
| 📍 **Geofencing** | Real-time GPS tracking, zone management, and exit alerts |
| 🎓 **Educational Content** | Quizzes, cognitive games, video activities, and event scheduling |
| 🎥 **Virtual Meetings** | Session scheduling, favorites, notifications, and meeting reports |
| 💬 **Communication** | Messaging, thematic groups, community feed, and moderation |
| 👥 **User Management** | Role-based access (Patient, Doctor, Caregiver, Admin) |
| 📊 **Admin Dashboard** | Engagement charts, statistics, and full administrative control |

---

## 🏗️ Architecture

Fakarni follows a **microservices architecture** with a Spring Cloud Gateway and Eureka service discovery.

```
┌─────────────────────────────────────────────────────────────┐
│                        Angular Frontend                      │
│                        (Port 4200)                           │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Gateway Service                           │
│                    (Port 8090)                               │
└──────┬───────────────┬──────────────┬────────────┬──────────┘
       │               │              │            │
  ┌────▼────┐    ┌──────▼────┐  ┌────▼─────┐ ┌───▼──────────┐
  │  User   │    │  Session  │  │  Event   │ │   Group /    │
  │ Service │    │  Service  │  │  Service │ │  Post / etc. │
  │  :8080  │    │  :8085    │  │  :8087   │ │              │
  └─────────┘    └───────────┘  └──────────┘ └──────────────┘
       │
  ┌────▼────────────────────────────────┐
  │         Eureka Discovery            │
  │         (Port 8761)                 │
  └─────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Frontend
- **Framework:** Angular 20 (Standalone Components + SSR)
- **UI:** Angular Material, Font Awesome, Chart.js, Leaflet.js
- **State:** RxJS, Angular Signals
- **Styling:** SCSS, CSS

### Backend (Microservices)
- **Framework:** Spring Boot 3.x (Java 21)
- **Service Discovery:** Spring Cloud Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Auth:** JWT (Access + Refresh tokens), BCrypt
- **ORM:** Spring Data MongoDB, Spring Data JPA (Hibernate)

### Databases
- **MongoDB** — Users, Sessions (auth)
- **MySQL** — Events, Posts, Groups, Session-service

### DevOps
- **Containerization:** Docker, Docker Compose
- **Build:** Maven (backend), Angular CLI (frontend)

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### Option 1 — Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/YasserWahada25/Fakarni_App.git
cd Fakarni_App

# Start all services
docker-compose up --build
```

Access the app at **http://localhost:4200**

### Option 2 — Manual Setup

**1. Start databases:**
```bash
docker-compose up mongodb mongo-express
```

**2. Start backend services (in order):**
```bash
# 1. Eureka Service Discovery
cd backend/Eureka-Service && mvn spring-boot:run

# 2. Gateway
cd backend/Gateway-Service && mvn spring-boot:run

# 3. User Service
cd backend/User-Service && mvn spring-boot:run

# 4. Other services (Event, Session, Group, Post...)
cd backend/Event-Service && mvn spring-boot:run
```

**3. Start frontend:**
```bash
cd frontend
npm install
ng serve
```

---

## 📦 Services

| Service | Port | Database | Description |
|---|---|---|---|
| Eureka-Service | 8761 | — | Service registry & discovery |
| Gateway-Service | 8090 | — | API Gateway (routing) |
| User-Service | 8080 | MongoDB | Auth, users, JWT sessions |
| session-service | 8085 | MySQL | Virtual sessions & participants |
| Event-Service | 8087 | MySQL | Calendar events management |
| Post-Service | 8069 | MySQL | Community posts/feed |
| Group-Service | 8097 | MySQL | Thematic groups |
| meeting-insights-service | 8086 | — | Meeting analytics & insights |
| Frontend | 4200 | — | Angular SSR application |
| MongoDB | 27017 | — | NoSQL database |
| Mongo Express | 8081 | — | MongoDB web UI |

---

## 🔌 API Endpoints

### Auth (`/auth`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Login, returns JWT tokens |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Invalidate refresh token |

### Users (`/api/users`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create user |
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

### Events (`/api/events`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/events` | Create event |
| GET | `/api/events` | List all events |
| GET | `/api/events/{id}` | Get event by ID |
| GET | `/api/events/user/{userId}` | Events by user |
| PUT | `/api/events/{id}` | Update event |
| DELETE | `/api/events/{id}` | Delete event |

### Groups (`/api/groups`) & Posts (`/api/posts`)
Standard CRUD — `POST`, `GET /{id}`, `GET`, `PUT /{id}`, `DELETE /{id}`

---

## 📁 Project Structure

```
Fakarni_App/
├── frontend/                   # Angular 20 SSR application
│   └── src/app/
│       ├── admin/              # Admin dashboard (users, sessions, geolocation...)
│       ├── auth/               # Sign in / Sign up
│       ├── medical/            # Patient records, detection, follow-up
│       ├── educational/        # Activities, events, progress tracking
│       ├── alzheimer_meeting/  # Virtual consultations, calendar, reports
│       ├── geofencing/         # Live tracking, alerts, supervision
│       └── communication/      # Messaging, groups, moderation
│
├── backend/
│   ├── Eureka-Service/         # Service discovery
│   ├── Gateway-Service/        # API Gateway (port 8090)
│   ├── User-Service/           # Auth + User management (MongoDB)
│   ├── session-service/        # Virtual sessions (MySQL)
│   ├── Event-Service/          # Events calendar (MySQL)
│   ├── Post-Service/           # Community posts (MySQL)
│   ├── group/                  # Thematic groups (MySQL)
│   └── meeting-insights-service/ # Meeting analytics
│
└── docker-compose.yml          # Full stack orchestration
```

---

## 👤 User Roles

| Role | Description |
|---|---|
| `PATIENT_PROFILE` | Alzheimer patient — access to activities, tracking, meetings |
| `DOCTOR_PROFILE` | Medical professional — records, reports, consultations |
| `CARE_OWNER` | Caregiver/family — monitoring, geofencing, communication |
| `ADMIN` | Full platform access and moderation |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'feat: add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed for academic and research purposes.

---

<div align="center">
Made with ❤️ for Alzheimer's patients and their families.
</div>
