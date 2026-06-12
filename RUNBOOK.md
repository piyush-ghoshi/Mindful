# Mindful Wellness Platform — Fullstack Runbook

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Node.js | 18+ | `node -version` |
| Docker Desktop | any | `docker -version` |
| PostgreSQL | via Docker | (handled below) |

---

## 1. Start the Database (Docker)

```bash
cd Mindful/backend
docker-compose up -d postgres
```

This starts PostgreSQL on **localhost:5432** with:
- Database: `mindful_wellness_db`
- User: `postgres`
- Password: `postgres`

Wait ~10 seconds for it to be healthy, then verify:
```bash
docker ps   # should show mindful_postgres as "healthy"
```

---

## 2. Start the Backend

```bash
cd Mindful/backend
mvn spring-boot:run
```

Spring Boot will:
1. Connect to PostgreSQL
2. Run Flyway migrations (V1 → V3) — creates all tables and seeds resources
3. Initialize Firebase Admin SDK from the service account JSON
4. Start on **http://localhost:8080**

**Verify it's up:**
```
GET http://localhost:8080/api/health
→ 200 OK
```

### Backend environment variables (optional overrides)

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/mindful_wellness_db` | Database URL |
| `DB_USER` | `postgres` | DB username |
| `DB_PASS` | `postgres` | DB password |
| `JWT_SECRET` | (long default) | HS512 signing key — change in production |
| `MAIL_ENABLED` | `false` | Set `true` + configure SMTP to enable email |

---

## 3. Start the Frontend

```bash
cd Mindful/frontend
npm install        # first time only
npm run dev
```

Frontend runs on **http://localhost:5173**

The `.env.local` is already configured:
```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_FIREBASE_*=<already set>
```

---

## 4. How Authentication Works

The app uses **Firebase Authentication** as the identity provider:

```
User logs in via Firebase (email/password or Google)
  → Firebase returns an ID token
  → Frontend stores it as firebase_id_token in localStorage
  → Every API call sends: Authorization: Bearer <firebase-id-token>
  → Backend FirebaseTokenFilter verifies it with Firebase Admin SDK
  → Auto-creates a local User row in PostgreSQL if first login
  → Spring Security context is set → @PreAuthorize works
```

**No manual user creation needed** — the first login auto-provisions the account.

---

## 5. Creating a Counsellor Account

By default all registrations create STUDENT accounts. To create a counsellor:

1. Register normally via the UI
2. Connect to the database and update the role:

```sql
UPDATE users SET role = 'COUNSELLOR' WHERE email = 'counsellor@example.com';
```

Or use the Admin API (requires an ADMIN user):
```
PUT http://localhost:8080/api/admin/users/{id}/activate
```

---

## 6. Full Stack at a Glance

```
Browser :5173 (Vite + React)
    │
    │  HTTP + Firebase ID Token (Bearer)
    ▼
Spring Boot :8080
    ├── FirebaseTokenFilter  ← verifies Firebase ID tokens
    ├── JwtAuthenticationFilter  ← verifies local HS512 JWTs
    ├── Controllers (REST)
    │   ├── /api/auth/*          Public auth endpoints
    │   ├── /api/mood/*          Mood tracking
    │   ├── /api/appointments/*  Appointment booking
    │   ├── /api/messages/*      Messaging
    │   ├── /api/forum/*         Community forum
    │   ├── /api/resources       Resource library
    │   ├── /api/crisis/*        Crisis support (public)
    │   ├── /api/users/*         User/counsellor profiles
    │   ├── /api/notifications/* Notifications
    │   └── /api/admin/*         Admin dashboard (ADMIN role)
    └── PostgreSQL :5432
            ├── users
            ├── appointments
            ├── mood_entries
            ├── conversations + messages
            ├── forum_posts + forum_comments
            ├── resources  (seeded with 8 entries)
            ├── notifications
            └── ... (15 tables total)
```

---

## 7. Common Issues

### Backend won't start — "firebase-key.json not found"
The Firebase credentials file is already in `src/main/resources/` with its full name.
`FirebaseConfig` now auto-detects it. If you see this error, check the file exists:
```
Mindful/backend/src/main/resources/mindful-54fd2-firebase-adminsdk-fbsvc-2e613fd09a.json
```

### Backend won't start — "relation does not exist"
Flyway hasn't run yet or failed. Check:
```bash
docker ps   # is postgres healthy?
mvn flyway:info -pl backend   # check migration status
```

### 401 on all API calls
- Make sure you're logged in (Firebase session active)
- Open browser DevTools → Application → Local Storage → check `firebase_id_token` exists
- The token expires after 1 hour; logging out and back in refreshes it

### CORS errors
The backend allows `localhost:3000`, `localhost:5173`, `localhost:5174`.
If you're running on a different port, add it to `SecurityConfig.java` CORS config.

### Google Sign-In popup blocked
Allow popups for `localhost:5173` in your browser settings.

---

## 8. Stopping Everything

```bash
# Stop frontend: Ctrl+C in the terminal running npm run dev
# Stop backend: Ctrl+C in the terminal running mvn spring-boot:run
# Stop database:
docker-compose -f Mindful/backend/docker-compose.yml down
```

To also delete the database volume (fresh start):
```bash
docker-compose -f Mindful/backend/docker-compose.yml down -v
```
