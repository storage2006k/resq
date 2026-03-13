# 🚨 RESQ — Deployment Guide

> **RESQ** (formerly ARIS) — Emergency Ambulance Routing Intelligence System

---

## 📁 Project Structure

```
resq/
├── .env.template                    # Environment variables template
├── .github/workflows/deploy.yml     # GitHub Actions CI/CD
├── Dockerfile                       # Multi-stage Docker build
├── docker-compose.yml               # Full stack (Postgres+App+Nginx)
├── deploy.sh                        # EC2 Ubuntu deployment script
├── nginx/nginx.conf                 # Reverse proxy + SSL + WebSocket
├── sql/init.sql                     # Docker Postgres init
├── pom.xml                          # Maven (Flyway, Actuator, prod profile)
├── src/main/resources/
│   ├── application.yml              # Dev config (H2)
│   ├── application-prod.yml         # Prod config (PostgreSQL)
│   └── db/migration/
│       └── V1__init_schema.sql      # Flyway migration (6 tables + seed data)
```

---

## 🔐 Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/resq_db` |
| `DB_USER` | Database username | `resq_user` |
| `DB_PASS` | Database password | `your_secure_password` |
| `JWT_SECRET` | Base64 256-bit secret | `openssl rand -base64 64` |
| `PORT` | Server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` |

---

## 🐳 Option 1: Docker Compose (Recommended)

```bash
# 1. Copy and edit .env
cp .env.template .env
nano .env   # Set DB_PASS and JWT_SECRET

# 2. Create SSL directory (self-signed for testing)
mkdir -p nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/privkey.pem \
  -out nginx/ssl/fullchain.pem \
  -subj "/CN=localhost"

# 3. Build and start
docker compose up -d --build

# 4. Check status
docker compose ps
docker compose logs -f resq-app

# 5. Access
# App:    https://localhost
# Health: https://localhost/actuator/health
```

### Useful Commands
```bash
docker compose restart resq-app   # Restart app
docker compose down               # Stop all
docker compose logs -f             # Stream logs
docker exec -it resq_postgres psql -U resq_user -d resq_db  # DB shell
```

---

## ☁️ Option 2: AWS EC2 (Ubuntu 22.04)

```bash
# 1. Launch EC2 instance (t3.small+, Ubuntu 22.04, ports 80/443/8080)

# 2. SSH in and clone
ssh -i your-key.pem ubuntu@your-ec2-ip
git clone https://github.com/yourorg/resq.git
cd resq

# 3. Run deployment script
chmod +x deploy.sh
./deploy.sh

# Script handles: Docker install, Certbot SSL, build, deploy
```

### Required EC2 Security Group Rules
| Port | Protocol | Source |
|------|----------|--------|
| 22   | TCP      | Your IP |
| 80   | TCP      | 0.0.0.0/0 |
| 443  | TCP      | 0.0.0.0/0 |

---

## 🚄 Option 3: Railway (Easiest — Free Tier)

1. Push project to GitHub
2. Go to [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub**
3. Add **PostgreSQL plugin** (auto-injects `DATABASE_URL`)
4. Set environment variables:
   ```
   DB_URL=<from Railway Postgres plugin>
   DB_USER=<from Railway>
   DB_PASS=<from Railway>
   JWT_SECRET=<generate with openssl rand -base64 64>
   SPRING_PROFILES_ACTIVE=prod
   ```
5. Railway auto-detects Java/Maven and builds
6. **Custom domain**: Settings → Domains → Add domain

> ✅ Live in ~3 minutes

---

## 🎨 Option 4: Render (Free Tier)

1. Push to GitHub
2. [render.com](https://render.com) → **New Web Service** → Connect repo
3. **Runtime**: Docker
4. Add **Render PostgreSQL** database → copy connection string
5. Environment variables:
   ```
   DB_URL=jdbc:postgresql://...
   DB_USER=...
   DB_PASS=...
   JWT_SECRET=...
   SPRING_PROFILES_ACTIVE=prod
   ```
6. **Health check path**: `/actuator/health`
7. Deploy — auto-deploys on every git push

---

## 🔄 CI/CD Pipeline (GitHub Actions)

The CI/CD pipeline (`.github/workflows/deploy.yml`) triggers on push to `main`:

1. **Build & Test** → Maven build + tests
2. **Docker Build** → Build image + push to Docker Hub
3. **Deploy** → SSH to EC2, pull latest, restart containers

### Required GitHub Secrets
| Secret | Description |
|--------|-------------|
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub password/token |
| `EC2_HOST` | EC2 public IP/hostname |
| `EC2_SSH_KEY` | EC2 SSH private key |

---

## 🗄️ Database Migrations (Flyway)

- **Dev (H2)**: Flyway is **disabled** — uses `ddl-auto: update`
- **Prod (PostgreSQL)**: Flyway is **enabled** — runs `V1__init_schema.sql`

To add a new migration:
```
src/main/resources/db/migration/V2__add_new_feature.sql
```

---

## 📊 Monitoring

### Actuator Endpoints (enabled in prod)
| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application info |
| `/actuator/metrics` | JVM + app metrics |

### Recommended External Monitoring
- **UptimeRobot** (free) — ping `/actuator/health` every 5 min
- **Prometheus + Grafana** — add as docker-compose service for dashboards

---

## 👤 Default Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | COMMAND |
| dispatcher | dispatch123 | DISPATCHER |
| coordinator | coord123 | COORDINATOR |
| supervisor | super123 | SUPERVISOR |

> ⚠️ **Change all passwords in production!**

---

## 🏗️ Build for Production (without Docker)

```bash
# Build JAR
mvn clean package -DskipTests -Pprod

# Run
java -Xms256m -Xmx512m \
  -Dspring.profiles.active=prod \
  -jar target/resq-1.0.0.jar
```
