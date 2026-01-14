# Volunteer Management System

![CI/CD Pipeline](https://github.com/L01Bestapp/volunteer-be/actions/workflows/ci-cd.yml/badge.svg)
![Coverage](https://codecov.io/gh/L01Bestapp/volunteer-be/branch/main/graph/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

Hệ thống quản lý hoạt động tình nguyện cho sinh viên HCMUT (Đại học Bách khoa TP.HCM), hỗ trợ đăng ký hoạt động, điểm danh QR code, và cấp chứng nhận CTXH (Công tác xã hội).

## 📋 Mục lục

- [Tính năng nổi bật](#-tính-năng-nổi-bật)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Thuật toán & Security](#-thuật-toán--security)
- [Best Practices](#-best-practices)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Deployment](#-deployment)
- [Cài đặt & Chạy local](#-cài-đặt--chạy-local)

---

## 🚀 Tính năng nổi bật

### 1. Quản lý người dùng & Xác thực
- **OAuth2 Google Login** - Đăng nhập bằng email @hcmut.edu.vn
- **JWT Authentication** với RSA-256 asymmetric key
- **Role-based Access Control** (STUDENT, ORGANIZATION, ADMIN)
- **OTP Verification** cho reset password
- **Email Verification** cho tài khoản mới

### 2. Quản lý hoạt động tình nguyện
- CRUD hoạt động với đầy đủ thông tin (thời gian, địa điểm, số giờ CTXH)
- Phân loại theo category (EDUCATION, ENVIRONMENT, HEALTH, COMMUNITY...)
- Trạng thái hoạt động (DRAFT, OPEN, CLOSED, COMPLETED, CANCELLED)
- Specification Pattern cho filter & search nâng cao

### 3. Đăng ký & Điểm danh
- Sinh viên đăng ký tham gia hoạt động
- **QR Code Check-in/Check-out** - Điểm danh bằng mã QR
- Theo dõi trạng thái điểm danh (CHECKED_IN, CHECKED_OUT, ABSENT)
- Tự động cập nhật số giờ CTXH cho sinh viên

### 4. Chứng nhận điện tử
- Tự động sinh chứng nhận khi hoàn thành hoạt động
- Mã chứng nhận unique (CERT-XXXXXXXX)
- Lưu trữ đầy đủ thông tin (sinh viên, hoạt động, tổ chức)
- Hỗ trợ thu hồi chứng nhận với lý do

### 5. Thông báo đa nền tảng
- **Firebase Cloud Messaging (FCM)** - Push notification
- **Expo Push Service** - Cho ứng dụng React Native
- Event-driven notifications (AttendanceCompletedEvent)

### 6. Upload & Media
- **Cloudinary Integration** - Upload ảnh avatar
- Image validation (size, format)

---

## 🏗 Kiến trúc hệ thống

```
src/main/java/com/ctxh/volunteer/
├── common/                     # Shared components
│   ├── config/                 # App configurations
│   ├── dto/                    # Common DTOs (ApiResponse, PaginatedResponse)
│   ├── entity/                 # BaseEntity với audit fields
│   ├── exception/              # Global exception handling
│   ├── init/                   # Data initializer
│   └── util/                   # Utilities (AuthUtil, ImageValidator)
│
├── config/                     # JPA Auditing config
│
└── module/                     # Feature modules
    ├── auth/                   # Authentication & Authorization
    │   ├── config/             # Security, JWT, OAuth2 configs
    │   ├── dto/                # Login, Token, OTP requests/responses
    │   ├── entity/             # User, Role, CustomUserDetails
    │   ├── enums/              # PurposeToken, EmailTemplates
    │   ├── repository/         # UserRepository, RoleRepository
    │   └── service/            # AuthService, MailService
    │
    ├── student/                # Student management
    ├── organization/           # Organization management
    ├── activity/               # Activity CRUD & search
    ├── enrollment/             # Activity registration
    ├── attendance/             # QR check-in/out
    ├── certificate/            # Certificate generation
    ├── notification/           # Push notifications
    └── task/                   # Task management
```

---

## 🔐 Thuật toán & Security

### JWT Token Signing - RS256 (RSA + SHA-256)

```java
// Sử dụng RSA asymmetric key pair
JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
    .keyID(rsaKeyRecord.keyId())
    .type(JOSEObjectType.JWT)
    .build();

// Sign với private key
JWSSigner signer = new RSASSASigner(rsaKeyRecord.rsaPrivateKey());
jwsObject.sign(signer);

// Verify với public key
JWSVerifier verifier = new RSASSAVerifier(publicKey);
jwsObject.verify(verifier);
```

**Đặc điểm:**
- **Asymmetric Encryption** - Private key để sign, Public key để verify
- **Key Rotation Support** - Hỗ trợ key ID để rotate keys
- **Multiple Token Types** - ACCESS, REFRESH, VERIFY_EMAIL, RESET_PASSWORD

### Password Hashing - BCrypt

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // Cost factor = 10 (default)
}
```

### OTP Generation - SecureRandom

```java
private static final SecureRandom secureRandom = new SecureRandom();
int randomInt = secureRandom.nextInt(999999);
String otpCode = String.format("%06d", randomInt);  // 6-digit OTP
```

---

## ✅ Best Practices

### 1. Security Best Practices
- **Stateless JWT** - Không lưu session server-side
- **BCrypt Password** - Hash password với salt tự động
- **CORS Configuration** - Whitelist origins
- **Method-level Security** - `@EnableMethodSecurity`
- **Custom Error Responses** - Không leak internal errors

### 2. API Design
- **RESTful Conventions** - Standard HTTP methods & status codes
- **Global Exception Handler** - Centralized error handling
- **Validation** - Bean Validation (Jakarta)
- **Pagination** - Spring Data Pageable
- **Specification Pattern** - Dynamic query building

### 3. Database & JPA
- **JPA Auditing** - Tự động createdAt, updatedAt
- **TSID** - Time-sorted unique ID (Hypersistence Utils)
- **Index Optimization** - Index trên các columns thường query
- **Batch Operations** - Hibernate batch insert/update

### 4. Docker Best Practices
```dockerfile
# Multi-stage build
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
# ... build stage

FROM eclipse-temurin:21-jre-alpine
# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
```

### 5. Code Quality
- **JaCoCo Coverage** - Minimum 70% line coverage, 70% branch coverage
- **Lombok** - Reduce boilerplate
- **SLF4J Logging** - Structured logging

---

## 🔄 CI/CD Pipeline

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Push to main   │────▶│  Build & Test   │────▶│  Docker Build   │
│  or PR          │     │  + JaCoCo       │     │  & Push Hub     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │  Deploy to      │
                                               │  Railway        │
                                               └─────────────────┘
```

### GitHub Actions Workflow

| Stage | Actions |
|-------|---------|
| **Build & Test** | Checkout → Setup JDK 21 → Maven verify → JaCoCo report |
| **Coverage** | Upload to Codecov |
| **Docker** | Login Docker Hub → Buildx → Build & Push (latest + SHA tag) |
| **Deploy** | Railway CLI → Redeploy service |

**Triggers:**
- `push` to `main` branch - Full pipeline (test + build + deploy)
- `pull_request` to `main` - Test only

---

## 🌐 Deployment

### Production Environment

| Service                | Platform | URL                                                                                                   |
|------------------------|----------|-------------------------------------------------------------------------------------------------------|
| **Backend API**        | Railway | [Railway Dashboard](https://railway.com/invite/lJLIbSOOzbn)                                           |
| **Swagger for backend** | Railway | [Swagger](https://volunteer-api-production.up.railway.app/swagger-ui/index.html?urls.primaryName=all) |
| **Database**           | Railway PostgreSQL | Internal connection                                                                                   |
| **Docker Image**       | Docker Hub | `quangthangk4/volunteer-api:latest`                                                                   |

### Environment Variables (Production)

```bash
# Database
POSTGRES_HOST, POSTGRES_PORT, POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD

# JWT RSA Keys (PEM content)
JWT_PRIVATE_KEY_CONTENT, JWT_PUBLIC_KEY_CONTENT, JWT_KEY_ID

# OAuth2 Google
GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET

# Email (Gmail SMTP)
MAIL_SENDER_USERNAME, MAIL_SENDER_PASSWORD

# Cloudinary
CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET

# App
BASE_URL_WEBSITE, SERVER_PORT
```

---

## 💻 Cài đặt & Chạy local

### Yêu cầu
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (hoặc dùng Docker)

### 1. Clone repository

```bash
git clone https://github.com/L01Bestapp/volunteer-be.git
cd volunteer-be
```

### 2. Chạy PostgreSQL với Docker

```bash
docker-compose up -d postgres
```

### 3. Cấu hình environment

Tạo file `.env` hoặc set environment variables:

```bash
# Tạo RSA key pair cho JWT
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
```

### 4. Chạy ứng dụng

```bash
# Development mode
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Hoặc build & run
./mvnw clean package -DskipTests
java -jar target/volunteer-0.0.1-SNAPSHOT.jar
```

### 5. Chạy với Docker Compose (Production-like)

```bash
# Build và chạy full stack
docker-compose -f docker-compose.prod.yml up -d
```

### API Documentation

Sau khi chạy, truy cập Swagger UI:
- Local: http://localhost:8080/swagger-ui.html
- Production: https://volunteer-api-production.up.railway.app/swagger-ui/index.html?urls.primaryName=all

---

## 📊 Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.7 |
| **Security** | Spring Security, OAuth2 Resource Server |
| **Database** | PostgreSQL 16, Spring Data JPA |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **Build** | Maven, Docker |
| **CI/CD** | GitHub Actions |
| **Cloud** | Railway, Docker Hub, Cloudinary |
| **Notifications** | Firebase Admin SDK |

---

## 📄 License

MIT License - Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

---

## **Link deploy Backend + Database:** https://railway.com/invite/lJLIbSOOzbn