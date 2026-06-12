# 💚 Mindful Wellness Platform

Welcome to **Mindful**, a premium, full-stack student mental health and wellness platform designed to facilitate secure support, self-tracking, and session booking between students and professional counsellors. 

This repository contains both the **Spring Boot backend** and the **Vite + React frontend** applications.

---

## 🚀 Technical Architecture

The platform is built as a decoupled full-stack application using a modern, scalable architecture:

```mermaid
graph TD
    Student[Browser: Student Client] -->|React Router / Tailwind| FE[Vite Frontend :5173]
    Counsellor[Browser: Counsellor Client] -->|React Router / Tailwind| FE
    FE -->|HTTP / Bearer ID Token| BE[Spring Boot Backend :8080]
    BE -->|FirebaseTokenFilter| FB[Firebase Auth Admin SDK]
    BE -->|Spring Data JPA| DB[(PostgreSQL Database :5432)]
    BE -->|Groq API Client| LLM[Groq AI LLM]
    BE -->|Session Caching| RD[(Redis :6379)]
```

### 🛠️ Technology Stack
* **Frontend**: React (v19), TypeScript, Vite, TailwindCSS (v4), Lucide Icons, React Router Dom (v7).
* **Backend**: Java 17, Spring Boot 3.1.5, Spring Security, Flyway (DB migrations), Hibernate/JPA.
* **Services**: Firebase Authentication (Identity Provider), Groq AI (Llama 3.1 8B model for MindBot), Redis (session/caching).
* **Database**: PostgreSQL 15 (Docker Containerized).

---

## 🌟 Key Features

### 👤 Student Section
* 📊 **Interactive Dashboard**: View wellness metrics, daily logs, and upcoming sessions at a glance.
* 📝 **Mood Journaling**: Track emotions, energy levels, sleep quality, and triggers. Maps 1-10 UI scores to 1-5 database entries.
* 👥 **Session Booking**: Select preferred counsellors, dates, time slots, and session types (Video, Phone, In-person) with automatic double-booking prevention.
* 💬 **MindBot Chatbot**: An AI wellness companion powered by Groq AI. Supports casual conversation and comprehensive wellness assessments (which generate downloadable mental health reports).
* 👥 **Community Forum**: Safe, category-filtered space to share thoughts, create posts (optionally anonymous), like, and comment.
* 🎮 **Wellness Tracker**: Personal goal-setting tool with gamified level-ups, points, and unlockable badges saved to browser storage.
* 🆘 **Crisis Support**: Dedicated helpline and resource guide page.

### 🩺 Counsellor Section
* 📊 **Counsellor Dashboard**: Overview of unique student cases, average rating, session metrics, and upcoming sessions.
* 📅 **Appointment Manager**: Weekly calendar view to accept, reschedule, or cancel student sessions, and mark sessions as complete with session notes.
* ⚙️ **Availability Settings**: Set standard weekly working hours and calendar leave exceptions dynamically.
* 🗂️ **My Students Directory**: Historical lists of unique student cases with total sessions, last visit date, and average mood ratings.
* 👤 **Professional Profile**: Manage public credentials, including license number, specialisations, bio, qualifications, and session parameters.

---

## 📂 Project Structure

```
Minor-2.0/
├── README.md                           # Main Workspace Documentation
└── Mindful/
    ├── backend/                        # Spring Boot Java Application
    │   ├── src/main/java/com/mindful/wellness/
    │   │   ├── config/                 # Configurations (Firebase, CORS, beans)
    │   │   ├── controller/             # REST API Controllers
    │   │   ├── dto/                    # Data Transfer Objects
    │   │   ├── entity/                 # JPA database entities
    │   │   ├── repository/             # Spring Data repositories
    │   │   ├── security/jwt/           # FirebaseTokenFilter & JwtAuthenticationFilter
    │   │   └── service/                # Business logic & schedulers
    │   ├── src/main/resources/
    │   │   ├── db/migration/           # Flyway PostgreSQL migrations
    │   │   └── application.properties  # App configurations
    │   └── pom.xml                     # Maven configuration
    │
    └── frontend/                       # Vite React Single Page App
        ├── src/
        │   ├── components/             # Reusable React components
        │   ├── context/                # Context providers (Auth, Theme)
        │   ├── pages/                  # Student & Auth Pages
        │   │   └── counsellor/         # Counsellor Pages
        │   ├── services/               # API clients (apiClient, moodService, etc.)
        │   ├── types/                  # TypeScript interfaces
        │   └── index.css               # Global styling
        ├── .env.local                  # Environment configuration
        └── package.json                # npm dependencies & scripts
```

---

## ⚡ Setup & Launch Guide

### Prerequisites
* Java 17+ & Maven 3.8+
* Node.js 18+ & npm
* Docker & Docker Compose

### Step 1: Start Database Services
First, spin up PostgreSQL and Redis in the background:
```bash
cd backend
docker-compose up -d
```
*PostgreSQL is exposed on port `5432` (database `mindful_db`, user `mindful`, password `mindful123`), and Redis is on port `6379`.*

### Step 2: Configure & Start the Backend
1. **Config Properties**: Ensure [application.properties](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/backend/src/main/resources/application.properties) database parameters match your environment:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mindful_db
   spring.datasource.username=mindful
   spring.datasource.password=mindful123
   ```
2. **Launch Application**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   *The backend starts on `http://localhost:8080` (API endpoint prefix `/api`).*

### Step 3: Configure & Start the Frontend
1. **Environment Variables**: Verify your local environment variables in [frontend/.env.local](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/frontend/.env.local):
   ```env
   VITE_API_BASE_URL=http://localhost:8080/api
   VITE_WEBSOCKET_URL=ws://localhost:8080/ws
   VITE_FIREBASE_API_KEY=AIzaSyC0ZNbKQFXPVgKA8vYx2mzIKH7fcv7qncM
   ```
2. **Launch Server**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *The client starts on `http://localhost:5173`.*

---

## 🔧 Authentication & Role-Based Access Control

1. **Identity Provider**: Authentication is handled by **Firebase Authentication** on the client side.
2. **Auto-Provisioning**: On requests, the frontend sends a Firebase ID token. The [FirebaseTokenFilter](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/backend/src/main/java/com/mindful/wellness/security/jwt/FirebaseTokenFilter.java) validates this with the Firebase Admin SDK. If it is the user's first login, it automatically registers a matching record in the local database.
3. **Counsellor Accounts**: All sign-ups default to the `STUDENT` role. To promote an account to `COUNSELLOR`, update the database record:
   ```sql
   UPDATE users SET role = 'COUNSELLOR' WHERE email = 'counsellor@example.com';
   ```

---

## 📊 Core API Endpoints

### Authentication
* `POST /api/auth/register` — Register a new user
* `POST /api/auth/login` — Log in and retrieve tokens
* `GET /api/auth/me` — Retrieve current authenticated user info
* `POST /api/auth/refresh` — Refresh expired JWT token
* `POST /api/auth/logout` — Logout user

### User & Profiles
* `GET /api/users/{id}` — Get user profile info
* `PUT /api/users/{id}` — Update user profile info
* `GET /api/users/{id}/counsellor-profile` — Fetch counsellor specific profile
* `PUT /api/users/{id}/counsellor-profile` — Update counsellor specific profile
* `GET /api/users/counsellors` — Retrieve list of available counsellors

### Appointments
* `GET /api/appointments` — List appointments for current user (student or counsellor)
* `POST /api/appointments` — Book a new appointment
* `GET /api/appointments/upcoming` — List upcoming appointments
* `PUT /api/appointments/{id}` — Reschedule an appointment
* `POST /api/appointments/{id}/cancel` — Cancel an appointment
* `POST /api/appointments/{id}/confirm` — Counsellor accepts a booking request
* `POST /api/appointments/{id}/complete` — Mark appointment complete with notes

### Mood Tracking
* `POST /api/mood/entries` — Record a daily mood check-in
* `GET /api/mood/history` — Get paginated student mood history
* `GET /api/mood/stats` — Retrieve student mood statistics and recommendations

### Community Forum
* `GET /api/forum/posts` — List approved forum posts
* `POST /api/forum/posts` — Create a new post (optionally anonymous)
* `GET /api/forum/posts/{id}` — View post details and comments
* `POST /api/forum/posts/{id}/comments` — Add a comment to a post
* `DELETE /api/forum/posts/{id}` — Delete a forum post (author or admin)

---

## 🛠️ Recent Fixes & Improvements

1. **Counsellor Availability Settings**: Fixed a JSON structure mismatch where the frontend sent a standard array list of weekday objects, but the backend [AvailabilityScheduleRepository](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/backend/src/main/java/com/mindful/wellness/repository/AvailabilityScheduleRepository.java) and [AvailabilityService.java](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/backend/src/main/java/com/mindful/wellness/service/AvailabilityService.java#L97) expected individual weekday lists (e.g. `monday`, `tuesday`). The handler payload was redesigned in [CounsellorAvailabilityPage.tsx](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/frontend/src/pages/counsellor/CounsellorAvailabilityPage.tsx#L65).
2. **Student Name Visibility in Appointments**: Re-designed the table cell layout in [CounsellorAppointmentsPage.tsx](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/frontend/src/pages/counsellor/CounsellorAppointmentsPage.tsx#L174) to fully render the student's name next to their avatar rather than displaying only the ID suffix.

---

## 🔍 Troubleshooting

### Backend Issues
* **"firebase-key.json not found"**: Ensure the Firebase credential configuration file is placed under [backend/src/main/resources/](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/backend/src/main/resources/) and matches the property path `firebase.credentials.path` inside [application.properties](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/backend/src/main/resources/application.properties#L58).
* **"Relation does not exist" or Database Errors**: Flyway database migrations might not have run. Execute `mvn flyway:info` to verify the database migrations status. Ensure the local Postgres container is healthy and running.
* **Port 8080 in use**: Stop any existing processes using port 8080, or override the port by setting `server.port=8081` in `application.properties`.

### Frontend Issues
* **401 Unauthorized Errors**: Check if the Firebase session has expired. Log out of the frontend and log back in to refresh the `firebase_id_token` in local storage.
* **CORS Errors**: Ensure the origin of the running Vite client is listed in the CORS configuration settings inside [application.properties](file:///c:/Users/piyus/Desktop/Minor-2.0/Mindful/backend/src/main/resources/application.properties#L38) (`security.cors.allowed-origins`).
* **Vite Dev Server Fails**: Delete cache folders and run install fresh:
  ```bash
  rm -rf node_modules dist
  npm install
  npm run dev
  ```
