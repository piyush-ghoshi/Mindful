# Mindful Wellness Platform — Production Deployment Guide

This guide details the step-by-step process for deploying the **Mindful Wellness Platform** to production. It covers two distinct deployment strategies:

1. **Path A: Managed Cloud Platforms (Recommended & Easiest)**
   * Frontend: **Vercel** / **Netlify**
   * Backend: **Render** / **Railway**
   * Database: **Neon PostgreSQL** / **Supabase**
2. **Path B: Self-Hosted Single VPS (Cost-Effective & Private)**
   * Frontend & Backend: Bundled via **Docker Compose**
   * Database: Self-hosted PostgreSQL on container volumes
   * SSL & Proxy: **Nginx** + **Let's Encrypt (Certbot)**

---

## 📋 Common Prerequisites

Regardless of the deployment path you choose, you must set up and gather the following environment configurations:

### 1. Firebase Authentication
Since the application uses Firebase Auth as the primary identity provider, you need:
1. **Firebase Project**: Your Firebase Console ID (e.g., `mindful-54fd2`).
2. **Frontend Config**: Copy the web credentials from Firebase Console (Settings -> Project settings -> General -> Web apps):
   * `API Key`
   * `Auth Domain`
   * `Project ID`
   * `Storage Bucket`
   * `Messaging Sender ID`
   * `App ID`
3. **Backend Service Account JSON**:
   * Generate a new private key from Firebase Console -> Project settings -> Service accounts.
   * Save this JSON file inside your backend resources folder: `backend/src/main/resources/mindful-54fd2-firebase-adminsdk-fbsvc-2e613fd09a.json` (or set the path dynamically).
4. **Authorized Domains**:
   * Go to **Firebase Console -> Authentication -> Settings -> Authorized domains**.
   * Add your production frontend domain (e.g., `your-app.vercel.app` or `yourdomain.com`). **If you skip this step, Firebase logins will fail on production!**

### 2. Groq AI API Key
The AI chatbot (`MindBot`) requires a Groq API Key:
* Get an API key from the [Groq Console](https://console.groq.com/).
* Keep this key secret. You will pass it to the backend as an environment variable (`GROQ_API_KEY`).

---

## 🚀 Path A: Managed Cloud Platforms (Vercel + Render + Neon)

This is the best choice for fast deployments, automated continuous integration (CI/CD) on GitHub push, and zero server maintenance.

```
┌─────────────────────────────────┐
│       Vercel / Netlify          │  ◄── Frontend (Static HTML/React)
└────────────────┬────────────────┘
                 │ HTTPS
                 ▼ (REST / WebSockets)
┌─────────────────────────────────┐
│        Render Web Service       │  ◄── Backend API (Docker Container)
└────────────────┬────────────────┘
                 │ Secure JDBC
                 ▼
┌─────────────────────────────────┐
│         Neon PostgreSQL         │  ◄── Serverless Database
└─────────────────────────────────┘
```

### Step 1: Set Up Neon Serverless PostgreSQL
1. Sign up on [Neon.tech](https://neon.tech/).
2. Create a new project named `mindful-wellness`.
3. Select your region (closest to your users or backend server region).
4. Copy the connection string. It will look like this:
   ```
   postgresql://mindful_owner:password@ep-cool-snowflake-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```
5. Convert this into a Spring-compatible JDBC connection URL:
   * Neon string: `postgresql://username:password@hostname/database?sslmode=require`
   * Spring JDBC: `jdbc:postgresql://hostname/database?user=username&password=password&sslmode=require`
   * *Example*: `jdbc:postgresql://ep-cool-snowflake-123456.us-east-2.aws.neon.tech/neondb?user=mindful_owner&password=yourpassword&sslmode=require`

### Step 2: Deploy Backend API on Render
Render can build and run applications directly from a GitHub repository using your existing Dockerfile.

1. Commit your backend changes and push them to a **GitHub repository** (either public or private).
2. Sign up/log in on [Render](https://render.com/).
3. Click **New +** and select **Web Service**.
4. Connect your GitHub repository.
5. Configure the service settings:
   * **Name**: `mindful-backend`
   * **Environment**: `Docker`
   * **Branch**: `main` (or your deployment branch)
   * **Region**: Choose the same region as your Neon database.
   * **Root Directory**: `Mindful/backend` (This is critical since the backend sits in a subfolder!)
   * **Plan**: Free or Starter
6. Add the following **Environment Variables** under the Advanced section:
   * `SPRING_DATASOURCE_URL` = (Your Neon Spring JDBC connection URL from Step 1)
   * `SPRING_DATASOURCE_USERNAME` = (Your Neon DB username)
   * `SPRING_DATASOURCE_PASSWORD` = (Your Neon DB password)
   * `GROQ_API_KEY` = (Your secret Groq API Key)
   * `JWT_SECRET` = (A secure random string of at least 256 bits for backend token fallback)
   * `PORT` = `8080`
7. Click **Deploy Web Service**. Render will read the `Dockerfile` inside `Mindful/backend/`, run the Maven build stage, package the JAR, run it, and expose it via a URL like `https://mindful-backend.onrender.com`.

> [!NOTE]
> Render's Free tier spins down web services after 15 minutes of inactivity. When a new request arrives, it can take 50+ seconds to spin back up ("cold start"). To avoid this, consider upgrading to Render's starter plan ($7/month).

### Step 3: Deploy Frontend on Vercel
Vercel is optimized for building and hosting static applications like Vite + React.

1. Sign up/log in on [Vercel](https://vercel.com/).
2. Click **Add New** -> **Project**.
3. Import your GitHub repository.
4. Configure Project settings:
   * **Framework Preset**: `Vite`
   * **Root Directory**: `Mindful/frontend` (Critical!)
   * **Build Command**: `npm run build`
   * **Output Directory**: `dist`
5. Under **Environment Variables**, add:
   * `VITE_API_BASE_URL` = `https://mindful-backend.onrender.com/api` (Replace with your backend URL)
   * `VITE_WEBSOCKET_URL` = `wss://mindful-backend.onrender.com/ws` (Replace with backend URL, using `wss` instead of `ws`)
   * `VITE_FIREBASE_API_KEY` = (Your Firebase API Key)
   * `VITE_FIREBASE_AUTH_DOMAIN` = `your-project.firebaseapp.com`
   * `VITE_FIREBASE_PROJECT_ID` = `your-project`
   * `VITE_FIREBASE_STORAGE_BUCKET` = `your-project.appspot.com`
   * `VITE_FIREBASE_MESSAGING_SENDER_ID` = (Your Firebase Sender ID)
   * `VITE_FIREBASE_APP_ID` = (Your Firebase App ID)
   * `VITE_ENVIRONMENT` = `production`
6. Click **Deploy**. Vercel will build your static files and deploy them globally to a CDN, giving you a domain like `https://mindful-wellness.vercel.app`.

### Step 4: Finalize CORS and Firebase White-listings
1. **CORS Configuration**: Open `SecurityConfig.java` in the backend and ensure the backend accepts requests from your Vercel frontend URL. 
   *(In your current codebase, `https://mindful-umber.vercel.app` is already allowed in `SecurityConfig.java:74`. If you use this address, you are good to go. If not, add your new URL to `SecurityConfig.java` and redeploy).*
2. **Firebase Auth Domain**: In the Firebase console (Authentication -> Settings -> Authorized domains), add your Vercel domain.

---

## 🐳 Path B: Self-Hosted Single VPS (Docker Compose + Nginx + SSL)

If you want absolute control over your hosting, want to avoid cold starts, and want to keep your database private inside your own server, hosting on a single Virtual Private Server (VPS) is the best approach.

### Step 1: Prepare the Production Docker Compose Files
We will create a root-level production configuration that bundles the React frontend (compiled and served via Nginx), the Spring Boot backend, and a PostgreSQL database.

#### 1. Create Frontend Dockerfile
Create `Mindful/frontend/Dockerfile` to compile the frontend and serve it using Nginx:

```dockerfile
# Stage 1: Build
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# We inject production env variables during build time
ARG VITE_API_BASE_URL
ARG VITE_WEBSOCKET_URL
ARG VITE_FIREBASE_API_KEY
ARG VITE_FIREBASE_AUTH_DOMAIN
ARG VITE_FIREBASE_PROJECT_ID
ARG VITE_FIREBASE_STORAGE_BUCKET
ARG VITE_FIREBASE_MESSAGING_SENDER_ID
ARG VITE_FIREBASE_APP_ID

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_WEBSOCKET_URL=$VITE_WEBSOCKET_URL
ENV VITE_FIREBASE_API_KEY=$VITE_FIREBASE_API_KEY
ENV VITE_FIREBASE_AUTH_DOMAIN=$VITE_FIREBASE_AUTH_DOMAIN
ENV VITE_FIREBASE_PROJECT_ID=$VITE_FIREBASE_PROJECT_ID
ENV VITE_FIREBASE_STORAGE_BUCKET=$VITE_FIREBASE_STORAGE_BUCKET
ENV VITE_FIREBASE_MESSAGING_SENDER_ID=$VITE_FIREBASE_MESSAGING_SENDER_ID
ENV VITE_FIREBASE_APP_ID=$VITE_FIREBASE_APP_ID
ENV VITE_ENVIRONMENT=production

RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 2. Create Frontend Nginx Configuration
Create `Mindful/frontend/nginx.conf` to direct React routes to `index.html` and proxy API calls to the backend:

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # Proxy backend API requests
    location /api {
        proxy_pass http://backend:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Proxy WebSockets
    location /ws {
        proxy_pass http://backend:8080/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
    }
}
```

#### 3. Create Root Production docker-compose.yml
Create `Mindful/docker-compose.prod.yml` to stitch the components together:

```yaml
version: '3.8'

services:
  postgres-prod:
    image: postgres:15-alpine
    container_name: mindful_postgres_prod
    environment:
      POSTGRES_DB: mindful_wellness_db
      POSTGRES_USER: mindful_user
      POSTGRES_PASSWORD: secure_prod_db_password_here
    ports:
      - "127.0.0.1:5432:5432" # Bind to local loopback to avoid exposing database directly to public
    volumes:
      - postgres_prod_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U mindful_user -d mindful_wellness_db"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: always

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: mindful_backend_prod
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-prod:5432/mindful_wellness_db
      SPRING_DATASOURCE_USERNAME: mindful_user
      SPRING_DATASOURCE_PASSWORD: secure_prod_db_password_here
      GROQ_API_KEY: ${GROQ_API_KEY}
      JWT_SECRET: ${JWT_SECRET}
      PORT: 8080
    depends_on:
      postgres-prod:
        condition: service_healthy
    restart: always

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        VITE_API_BASE_URL: https://${DOMAIN_NAME}/api
        VITE_WEBSOCKET_URL: wss://${DOMAIN_NAME}/ws
        VITE_FIREBASE_API_KEY: ${VITE_FIREBASE_API_KEY}
        VITE_FIREBASE_AUTH_DOMAIN: ${VITE_FIREBASE_AUTH_DOMAIN}
        VITE_FIREBASE_PROJECT_ID: ${VITE_FIREBASE_PROJECT_ID}
        VITE_FIREBASE_STORAGE_BUCKET: ${VITE_FIREBASE_STORAGE_BUCKET}
        VITE_FIREBASE_MESSAGING_SENDER_ID: ${VITE_FIREBASE_MESSAGING_SENDER_ID}
        VITE_FIREBASE_APP_ID: ${VITE_FIREBASE_APP_ID}
    container_name: mindful_frontend_prod
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: always

volumes:
  postgres_prod_data:
```

### Step 2: Configure VPS & Reverse Proxy on Server
1. Rent a VPS from a provider (DigitalOcean, AWS, Linode, Hetzner, etc.) with Ubuntu Server installed (minimum 2GB RAM recommended for compiling/running Java).
2. Point your domain name (e.g., `mindful.yourdomain.com`) to the IP address of your VPS.
3. SSH into your VPS:
   ```bash
   ssh root@your_vps_ip
   ```
4. Install Docker and Docker Compose on the VPS:
   ```bash
   sudo apt-get update
   sudo apt-get install -y docker.io docker-compose git
   sudo systemctl enable docker
   sudo systemctl start docker
   ```
5. Clone your project onto the VPS:
   ```bash
   git clone https://github.com/yourusername/your-repo.git
   cd your-repo/Mindful
   ```

### Step 3: Configure Environment Variables
Create a file named `.env` in the same directory as `docker-compose.prod.yml` on the VPS, filling in all production values:

```env
DOMAIN_NAME=mindful.yourdomain.com
GROQ_API_KEY=gsk_your_actual_groq_api_key
JWT_SECRET=super_long_custom_random_jwt_secret_key_make_it_extremely_long_and_secure
VITE_FIREBASE_API_KEY=AIzaSy...
VITE_FIREBASE_AUTH_DOMAIN=mindful-54fd2.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=mindful-54fd2
VITE_FIREBASE_STORAGE_BUCKET=mindful-54fd2.firebasestorage.app
VITE_FIREBASE_MESSAGING_SENDER_ID=259988372381
VITE_FIREBASE_APP_ID=1:259988372381:web:eaae125adb2be6bbd78d74
```

### Step 4: Build and Launch the Stack
Run the following command to build the Docker images and start the containers in detached mode:

```bash
docker-compose -f docker-compose.prod.yml --env-file .env up -d --build
```

Flyway will automatically detect the fresh PostgreSQL database and run the migrations to seed the initial schema and wellness resources.

### Step 5: Install SSL Certificates (HTTPS) using Certbot & Nginx
To secure logins and enable HTTPS:
1. Install Certbot on the host:
   ```bash
   sudo apt-get install -y certbot python3-certbot-nginx nginx
   ```
2. Create an Nginx config on the VPS host (`/etc/nginx/sites-available/mindful`) to route traffic to the Docker container:
   ```nginx
   server {
       listen 80;
       server_name mindful.yourdomain.com;

       location / {
           proxy_pass http://localhost:80;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```
3. Enable the host site and reload Nginx:
   ```bash
   sudo ln -s /etc/nginx/sites-available/mindful /etc/nginx/sites-enabled/
   sudo systemctl restart nginx
   ```
4. Obtain and install the SSL certificate:
   ```bash
   sudo certbot --nginx -d mindful.yourdomain.com
   ```
   Follow the prompts to enable redirecting HTTP traffic to HTTPS. Nginx on the host will handle SSL termination and route requests securely to the frontend Docker container.

---

## 🛠️ Flyway Migrations in Production

The Spring Boot backend is configured with:
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```
This means when the backend container starts up (either on Render or via Docker Compose on your VPS), Flyway automatically checks the database, runs any pending migrations in `db/migration/`, and records them in the `flyway_schema_history` table. You **do not need to execute database scripts manually in production**.

---

## 🛡️ Best Practices for Production Deployment

1. **Do Not Store Credentials in Git**: Never hardcode database passwords, Groq API keys, or JWT secrets inside `application.properties` or `.env` files committed to Git. Always use environment variables as shown above.
2. **Setup DB Backups**: If using Path B, make sure to set up a daily cron job to run `pg_dump` on the PostgreSQL container and back it up off-site. (If using Neon on Path A, backups are handled automatically).
3. **Set Roles Securely**:
   * All accounts register as `STUDENT` by default.
   * To create a `COUNSELLOR` or `ADMIN`, log into the database and modify the `role` enum in the `users` table:
     ```sql
     UPDATE users SET role = 'COUNSELLOR' WHERE email = 'counsellor@yourdomain.com';
     UPDATE users SET role = 'ADMIN' WHERE email = 'admin@yourdomain.com';
     ```
4. **WebSocket Support**: Ensure your host and proxy configurations support HTTP `Upgrade` headers. The Nginx configs in this document are fully configured to allow WebSockets for real-time messaging.
