# Addon — Generated / Large Files

This folder documents files that are excluded from the repository due to size constraints
but are part of the project. They are regenerated automatically by running the project.

## Excluded folders (auto-generated, not committed)

| Folder | How to regenerate |
|--------|------------------|
| `frontend/node_modules/` | `cd frontend && npm install` |
| `frontend/dist/` | `cd frontend && npm run build` |
| `backend/target/` | `cd backend && mvn package` |
| `backend/.mvn/` | Maven wrapper — optional |

## How to run the project

### Prerequisites
- Node.js 20+
- Java 21
- Docker Desktop
- Maven 3.9+

### Frontend
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5174
```

### Backend
```bash
cd backend
docker compose up -d        # Start PostgreSQL + Redis
mvn spring-boot:run         # Start Spring Boot on port 8080
```

### Environment variables
Copy `frontend/.env.example` to `frontend/.env.local` and fill in values.
The Firebase credentials are already set in `frontend/.env.local` (not committed for security in real projects).
