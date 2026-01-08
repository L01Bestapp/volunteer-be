# CI/CD Pipeline Guide

## 📋 Pipeline Overview

Pipeline CI/CD được thiết kế với 6 jobs chính:

```
┌─────────────────┐
│  Push to main   │
│  or Pull Request│
└────────┬────────┘
         │
         ▼
┌────────────────────────────────────────┐
│     1. BUILD & TEST (CI)               │
│  - Compile code                        │
│  - Run unit tests                      │
│  - Generate coverage (must be >70%)    │
│  - Upload artifacts                    │
└────────┬───────────────────────────────┘
         │
         ├──────────────┬────────────────┐
         ▼              ▼                ▼
┌───────────────┐ ┌─────────┐  ┌────────────┐
│2. CODE QUALITY│ │3.SECURITY│  │ PR Comment │
│  - SpotBugs   │ │  - Trivy │  │ w/ Coverage│
│  - Checkstyle │ │  Scan    │  │            │
└───────────────┘ └─────────┘  └────────────┘
         │              │
         ▼              ▼
    (Only on main branch push)
         │
         ▼
┌─────────────────────────────┐
│  4. BUILD DOCKER IMAGE      │
│  - Multi-stage build        │
│  - Push to Docker Hub       │
│  - Tag: latest, sha, branch │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│  5. DEPLOY TO RAILWAY       │
│  - Railway CLI deploy       │
│  - Health check validation  │
└────────┬────────────────────┘
         │
         ▼
┌─────────────────────────────┐
│  6. NOTIFICATION            │
│  - Slack (optional)         │
│  - Status summary           │
└─────────────────────────────┘
```

---

## 🚀 Pipeline Details

### 1️⃣ Build & Test (CI)

**Triggers**:
- Push to `main` or `develop`
- Pull Request to `main` or `develop`

**Steps**:
```yaml
✅ Checkout code
✅ Setup JDK 21 với cache Maven
✅ Build: ./mvnw clean compile
✅ Test: ./mvnw test
✅ Coverage check: ./mvnw jacoco:check (threshold: 70%)
✅ Generate coverage report
✅ Upload to Codecov
✅ Archive artifacts
✅ Comment PR với coverage details
```

**Artifacts**:
- `test-results/`: Surefire test reports
- `coverage-report/`: JaCoCo HTML reports

**Coverage Threshold**: Fail nếu < 70%

---

### 2️⃣ Code Quality Analysis

**Triggers**: Pull Request only

**Tools**:
- **SpotBugs**: Tìm bugs tiềm ẩn trong code
- **Checkstyle**: Kiểm tra code style conventions

**Note**: Jobs này chạy song song với Build & Test, không block deployment nếu warning.

---

### 3️⃣ Security Scan

**Triggers**: Mọi push/PR

**Tool**: Trivy - vulnerability scanner

**Checks**:
- Dependencies với known CVEs
- Security issues trong code
- Severity: CRITICAL và HIGH

**Output**:
- SARIF file uploaded to GitHub Security tab
- Visible trong **Security** → **Code scanning alerts**

---

### 4️⃣ Build Docker Image

**Triggers**: Push to `main` branch only

**Process**:
```dockerfile
1. Multi-stage build:
   - Stage 1: Maven build (cached)
   - Stage 2: Runtime với JRE minimal

2. Tags generated:
   - latest (for main branch)
   - main-<sha> (commit SHA)
   - main (branch name)

3. Security:
   - Non-root user
   - Health check built-in
   - Optimized JVM settings

4. Cache strategy:
   - Layer caching với BuildKit
   - Registry cache: buildcache tag
```

**Image size**: ~300MB (optimized với Alpine base)

---

### 5️⃣ Deploy to Railway

**Triggers**: Successful Docker build on `main`

**Process**:
```bash
1. Install Railway CLI
2. Deploy với: railway up --service volunteer-backend
3. Wait 30s for deployment
4. Health check verification (10 retries × 10s)
5. Report deployment status
```

**Environment**: Production (requires manual approval option)

**Rollback**: Railway keeps previous deployments, rollback từ Railway dashboard nếu cần.

---

### 6️⃣ Notification

**Triggers**: After deployment (always run)

**Channels**:
- **Slack** (optional): Deployment status với commit info
- **GitHub**: Check run summary

---

## 🔧 Local Development với Docker

### Quick Start

```bash
# 1. Clone repository
git clone <repo-url>
cd volunteer

# 2. Start all services
docker-compose up -d

# 3. Check logs
docker-compose logs -f app

# 4. Stop services
docker-compose down
```

### Available Services

| Service | Port | Description | Profile |
|---------|------|-------------|---------|
| postgres | 5432 | PostgreSQL database | default |
| app | 8080 | Spring Boot app | default |
| pgadmin | 5050 | DB management UI | tools |
| redis | 6379 | Cache (optional) | cache |

### With Optional Tools

```bash
# Start with pgAdmin
docker-compose --profile tools up -d

# Start with Redis cache
docker-compose --profile cache up -d

# Start with all profiles
docker-compose --profile tools --profile cache up -d
```

### Environment Variables

Tạo file `.env` trong root directory:

```env
# Database
POSTGRES_DB=volunteer
POSTGRES_USER=volunteer_user
POSTGRES_PASSWORD=volunteer_pass

# JWT Keys (generate your own)
JWT_PRIVATE_KEY=...
JWT_PUBLIC_KEY=...

# Mail
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password

# Cloudinary
CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...

# Google OAuth
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
```

---

## 📊 Monitoring & Badges

### Add Badges to README

```markdown
[![CI/CD Pipeline](https://github.com/YOUR_USERNAME/volunteer/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/YOUR_USERNAME/volunteer/actions/workflows/ci-cd.yml)
[![codecov](https://codecov.io/gh/YOUR_USERNAME/volunteer/branch/main/graph/badge.svg)](https://codecov.io/gh/YOUR_USERNAME/volunteer)
[![Docker Image](https://img.shields.io/docker/v/YOUR_DOCKERHUB_USERNAME/volunteer-management?label=docker&sort=semver)](https://hub.docker.com/r/YOUR_DOCKERHUB_USERNAME/volunteer-management)
```

### Coverage Report

- **Local**: `target/site/jacoco/index.html`
- **Codecov**: https://codecov.io/gh/YOUR_USERNAME/volunteer
- **GitHub Actions**: Download từ Artifacts

---

## 🐛 Troubleshooting

### Build fails với "Tests failed"

```bash
# Run tests locally
./mvnw test

# Check specific test
./mvnw test -Dtest=AuthServiceImplTest

# See detailed error
./mvnw test -X
```

### Build fails với "Coverage threshold not met"

```bash
# Check current coverage
./mvnw jacoco:report
open target/site/jacoco/index.html

# Coverage requirement: 70% (config in pom.xml)
```

### Docker build fails

```bash
# Build locally
docker build -t volunteer-test .

# Check Docker logs
docker logs volunteer-app

# Shell into container
docker exec -it volunteer-app sh
```

### Railway deployment fails

```bash
# Check Railway logs
railway logs

# Verify environment variables
railway variables

# Manual deploy
railway up

# Rollback to previous version
railway rollback
```

### Pipeline stuck/slow

**Common causes**:
1. **Maven download**: First run downloads dependencies (~2-3 mins)
   - Solution: Cache will speed up subsequent runs
2. **Docker build**: Building from scratch (~3-4 mins)
   - Solution: Registry cache reduces to ~1-2 mins
3. **Test execution**: All 178 tests (~40s)
   - Solution: Normal, can't optimize much

**Total pipeline time**:
- **CI only**: ~3-4 minutes
- **Full CI/CD**: ~8-10 minutes

---

## 🔐 Security Best Practices

### Secrets Management

✅ **DO**:
- Use GitHub Secrets for sensitive data
- Use Railway Environment Variables for production config
- Rotate tokens regularly
- Use Access Tokens instead of passwords

❌ **DON'T**:
- Commit `.env` files
- Hardcode secrets in code
- Share secrets in plain text
- Use same secrets for dev/prod

### Docker Security

✅ Implemented:
- Non-root user in container
- Minimal base image (Alpine)
- Security scanning with Trivy
- Health checks
- Read-only filesystem where possible

---

## 📈 Performance Optimization

### Maven Build

```xml
<!-- Already configured in pom.xml -->
<properties>
  <maven.compiler.release>21</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

### Docker Build

**Layer caching**:
```dockerfile
# Dependencies layer (rarely changes)
COPY pom.xml .
RUN mvn dependency:go-offline

# Source code layer (changes frequently)
COPY src ./src
RUN mvn clean package -DskipTests
```

### GitHub Actions Cache

```yaml
# Maven cache
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}

# Docker layer cache
cache-from: type=registry,ref=${{ env.DOCKER_IMAGE }}:buildcache
cache-to: type=registry,ref=${{ env.DOCKER_IMAGE }}:buildcache,mode=max
```

---

## 📚 Additional Resources

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Railway Deployment Guide](https://docs.railway.app/deploy/deployments)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)

---

## 🎯 Next Steps

1. ✅ Setup GitHub Secrets (see SETUP_SECRETS.md)
2. ✅ Create Railway project
3. ✅ Configure Railway environment variables
4. ✅ Test pipeline với dummy commit
5. ✅ Monitor first deployment
6. ✅ Add badges to README
7. ⚙️ Setup Codecov (optional)
8. ⚙️ Setup Slack notifications (optional)
