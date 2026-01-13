# Hướng Dẫn Deploy Spring Boot Lên Render

> **Quick Start Guide** - Hướng dẫn nhanh deploy ứng dụng Volunteer Management System lên Render.
>
> 📖 **Chi tiết về Environment Variables:** Xem [RENDER_ENV_SETUP.md](./RENDER_ENV_SETUP.md)

---

## 📋 Yêu Cầu Trước Khi Deploy

### Tài Khoản & Services
- [x] Tài khoản GitHub (đã có repository)
- [ ] Tài khoản Render - Đăng ký tại [render.com](https://render.com)
- [ ] Push code lên GitHub repository

### Credentials Cần Chuẩn Bị

| Service | Cần Lấy | Nơi Lấy |
|---------|---------|---------|
| **JWT Keys** | Private & Public key (RSA 2048-bit) | Tạo mới với OpenSSL |
| **Google OAuth** | Client ID & Secret | [Google Cloud Console](https://console.cloud.google.com/apis/credentials) |
| **Gmail** | Email & App Password | [Google App Passwords](https://myaccount.google.com/apppasswords) |
| **Cloudinary** | Cloud Name, API Key, API Secret | [Cloudinary Console](https://cloudinary.com/console) |
| **Firebase** (Optional) | Service Account JSON | [Firebase Console](https://console.firebase.google.com/) |

📖 **Hướng dẫn chi tiết lấy credentials:** Xem [RENDER_ENV_SETUP.md - Phần 2](./RENDER_ENV_SETUP.md#2-chuẩn-bị-trước-khi-set)

---

## 🚀 Phương Pháp 1: Auto-Deploy (Khuyến Nghị)

### Bước 1: Chuẩn Bị JWT Keys

```bash
# Tạo private key (RSA 2048-bit)
openssl genrsa -out privateKey.pem 2048

# Tạo public key từ private key
openssl rsa -in privateKey.pem -pubout -out publicKey.pem

# Verify keys
openssl rsa -in privateKey.pem -check
openssl rsa -pubin -in publicKey.pem -text
```

**⚠️ Quan trọng:**
- Lưu 2 file này ở nơi an toàn (password manager)
- KHÔNG commit vào Git
- Copy toàn bộ nội dung (bao gồm `-----BEGIN...-----` và `-----END...-----`)

---

### Bước 2: Push Code Lên GitHub

```bash
git add .
git commit -m "chore: add Render deployment configuration"
git push origin main
```

---

### Bước 3: Tạo PostgreSQL Database Trên Render

1. Truy cập [Render Dashboard](https://dashboard.render.com)
2. Click **New** → **PostgreSQL**
3. Cấu hình:
   ```
   Name:     volunteer-db
   Database: volunteer_db
   User:     volunteer
   Region:   Singapore
   Plan:     Free (hoặc Starter $7/tháng)
   ```
4. Click **Create Database**
5. Đợi database khởi tạo (~2-3 phút)
6. **Lưu lại Internal Database URL:**
   ```
   postgres://volunteer:abc123@dpg-xxxxx-a.singapore-postgres.render.com/volunteer_db
   ```
   Parse thành 5 biến: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`

---

### Bước 4: Tạo Web Service

1. Click **New** → **Web Service**
2. Connect GitHub repository: `volunteer`
3. Cấu hình:
   ```
   Name:                 volunteer-api
   Region:               Singapore
   Branch:               main
   Root Directory:       (leave empty)
   Environment:          Docker
   Dockerfile Path:      ./Dockerfile
   Docker Context:       .
   Plan:                 Free (hoặc Starter)
   ```
4. Click **Advanced** để set Environment Variables

---

### Bước 5: Set Environment Variables

Click **Add Environment Variable** và thêm các biến sau:

#### 🔴 CRITICAL (Bắt buộc - App không chạy nếu thiếu)

```bash
# JWT Keys (CRITICAL SECURITY)
JWT_PRIVATE_KEY_CONTENT=-----BEGIN PRIVATE KEY-----
<copy toàn bộ content từ privateKey.pem>
-----END PRIVATE KEY-----

JWT_PUBLIC_KEY_CONTENT=-----BEGIN PUBLIC KEY-----
<copy toàn bộ content từ publicKey.pem>
-----END PUBLIC KEY-----

# Database (từ Internal Database URL)
POSTGRES_HOST=dpg-xxxxx-a.singapore-postgres.render.com
POSTGRES_PORT=5432
POSTGRES_DB=volunteer_db
POSTGRES_USER=volunteer
POSTGRES_PASSWORD=<từ Internal URL>

# Base URL
BASE_URL_WEBSITE=https://volunteer-api.onrender.com

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

#### 🟠 HIGH PRIORITY (Core features)

```bash
# Google OAuth (Login with Google)
GOOGLE_CLIENT_ID=123456789-abcdefghijk.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-xxxxxxxxxxxxxxxxxxxxxxxx

# Gmail SMTP (Password reset, notifications)
MAIL_SENDER_USERNAME=your-email@gmail.com
MAIL_SENDER_PASSWORD=abcd efgh ijkl mnop

# Cloudinary (Image upload)
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=abcdefghijklmnopqrstuvwxyz123456
```

#### 🟡 OPTIONAL (Features tùy chọn)

```bash
# Firebase (Push notifications) - OPTIONAL
FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account","project_id":"..."}

# Admin Credentials - Khuyến nghị thay đổi
ADMIN_USERNAME=admin@example.com
ADMIN_PASSWORD=YourSecurePasswordHere123!
```

📖 **Hướng dẫn chi tiết từng biến:** Xem [RENDER_ENV_SETUP.md - Phần 3](./RENDER_ENV_SETUP.md#3-hướng-dẫn-set-từng-biến)

---

### Bước 6: Deploy

1. Click **Create Web Service**
2. Render sẽ tự động:
   - ✅ Clone repository
   - ✅ Build Docker image (~5-10 phút)
   - ✅ Deploy container
   - ✅ Map domain: `https://volunteer-api.onrender.com`
3. Theo dõi **Logs** tab để kiểm tra quá trình deploy

**Logs thành công:**
```
[INFO] Started VolunteerApplication in 45.123 seconds
[INFO] HikariPool-1 - Start completed
[INFO] RSA keys loaded from environment variables
[INFO] Tomcat started on port 8080
```

---

### Bước 7: Verify Deployment

#### Test Health Check
```bash
curl https://volunteer-api.onrender.com/actuator/health
```

**Expected:**
```json
{"status":"UP"}
```

#### Test Swagger UI
```
https://volunteer-api.onrender.com/swagger-ui/index.html
```

#### Test Login Endpoint
```bash
curl -X POST https://volunteer-api.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

---

## 🔧 Phương Pháp 2: Deploy Với GitHub Actions (Advanced)

Tự động trigger deploy từ GitHub Actions khi push code.

### Bước 1: Lấy Deploy Hook từ Render

1. Render Dashboard → Service `volunteer-api`
2. **Settings** → **Deploy Hook**
3. Copy URL: `https://api.render.com/deploy/srv-xxxxx?key=yyyyy`

### Bước 2: Add GitHub Secret

1. GitHub repo → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret:**
   ```
   Name:  RENDER_DEPLOY_HOOK_URL
   Value: https://api.render.com/deploy/srv-xxxxx?key=yyyyy
   ```

### Bước 3: Push Code

```bash
git add .
git commit -m "ci: enable GitHub Actions auto-deploy to Render"
git push origin main
```

File `.github/workflows/ci-cd.yml` đã có sẵn deploy job:
```yaml
deploy:
  needs: build
  if: github.event_name == 'push' && github.ref == 'refs/heads/main'
  steps:
    - name: Trigger Render Deployment
      run: curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK_URL }}
```

**Workflow:**
1. Push code → GitHub Actions
2. Run tests → Build Maven
3. Generate coverage report
4. Trigger Render deployment

---

## 📊 Monitoring & Management

### View Logs
```
Render Dashboard → volunteer-api → Logs
```

### Health & Metrics
```
Health:  https://volunteer-api.onrender.com/actuator/health
Metrics: https://volunteer-api.onrender.com/actuator/metrics
Info:    https://volunteer-api.onrender.com/actuator/info
```

### Custom Domain (Optional)
```
Render Dashboard → volunteer-api → Settings → Custom Domain
```

---

## ⚠️ Lưu Ý Quan Trọng

### Free Plan Limitations

| Limitation | Detail | Workaround |
|------------|--------|------------|
| **Sleep after 15 min** | App ngủ sau 15 phút không dùng | Dùng [UptimeRobot](https://uptimerobot.com) ping mỗi 14 phút |
| **512MB RAM** | Đủ cho small-medium app | Đã optimize trong Dockerfile & application-prod.yml |
| **750 hours/month** | ~31 ngày | Đủ cho 1 service |
| **Slow build** | Build ~5-10 phút | Enable dependency caching |
| **Shared CPU** | Performance không ổn định | Upgrade Starter Plan $7/tháng |

### Security Best Practices

✅ **DO:**
- Sử dụng **Internal Database URL** (nhanh hơn, miễn phí bandwidth)
- Set strong password cho `ADMIN_PASSWORD`
- Rotate JWT keys định kỳ (mỗi 3-6 tháng)
- Enable HTTPS (Render tự động cung cấp SSL)
- Monitor logs thường xuyên

❌ **DON'T:**
- KHÔNG commit `.env` hoặc credentials vào Git
- KHÔNG dùng External Database URL (chậm + tính phí bandwidth)
- KHÔNG dùng default admin password `admin` trong production
- KHÔNG share JWT keys với ai

---

## 🐛 Common Issues & Solutions

### ❌ "Invalid JWT signature"

**Cause:** JWT keys không đúng.

**Fix:**
```bash
# 1. Verify keys locally
openssl rsa -in privateKey.pem -check

# 2. Kiểm tra env vars trên Render:
# - Copy TOÀN BỘ content (bao gồm BEGIN/END)
# - Không có spaces thừa ở đầu/cuối
# - Giữ nguyên line breaks

# 3. Check logs:
"RSA keys loaded from environment variables" ← Phải có dòng này
```

### ❌ "Database connection failed"

**Fix:**
1. Verify PostgreSQL service đã **running**
2. Dùng **Internal Database URL** (không phải External)
3. Parse đúng 5 biến: HOST, PORT, DB, USER, PASSWORD

### ❌ "Google OAuth callback error"

**Fix:**
1. Google Cloud Console → OAuth Client → Edit
2. **Authorized redirect URIs:**
   ```
   https://volunteer-api.onrender.com/login/oauth2/code/google
   ```
3. Verify `BASE_URL_WEBSITE` không có trailing slash

### ❌ "Out of Memory / App crashed"

**Fix:**
1. Check logs: `java.lang.OutOfMemoryError`
2. Đã optimize trong Dockerfile:
   ```
   -XX:MaxRAMPercentage=75.0
   -XX:InitialRAMPercentage=50.0
   ```
3. Nếu vẫn bị: **Upgrade Starter Plan** ($7/tháng)

📖 **Troubleshooting đầy đủ:** Xem [RENDER_ENV_SETUP.md - Phần 5](./RENDER_ENV_SETUP.md#5-troubleshooting)

---

## ✅ Deployment Checklist

### Pre-Deploy
- [ ] Tạo RSA key pair (privateKey.pem, publicKey.pem)
- [ ] Lấy Google OAuth credentials
- [ ] Setup OAuth redirect URI trên Google Cloud Console
- [ ] Lấy Gmail App Password (16 chars)
- [ ] Lấy Cloudinary credentials
- [ ] Push code lên GitHub

### On Render
- [ ] Tạo PostgreSQL database service
- [ ] Lưu Internal Database URL
- [ ] Tạo Web Service (Docker)
- [ ] Set 8 biến CRITICAL
- [ ] Set 7 biến HIGH PRIORITY
- [ ] Set ADMIN_PASSWORD (không dùng default)
- [ ] Deploy service

### Post-Deploy
- [ ] Check logs: "Started VolunteerApplication"
- [ ] Test health: `/actuator/health`
- [ ] Test Swagger UI
- [ ] Test login (username/password)
- [ ] Test Google OAuth login
- [ ] Test email sending
- [ ] Test image upload
- [ ] Setup custom domain (optional)
- [ ] Configure UptimeRobot (Free plan)

---

## 📚 Tài Liệu Liên Quan

- 📖 [RENDER_ENV_SETUP.md](./RENDER_ENV_SETUP.md) - Chi tiết environment variables
- 📖 [.env.example](./.env.example) - Template env vars
- 📖 [render.yaml](./render.yaml) - Render Blueprint (Infrastructure as Code)
- 📖 [Dockerfile](./Dockerfile) - Docker configuration
- 📖 [application-prod.yml](./src/main/resources/application-prod.yml) - Spring Boot config

### External Resources
- [Render Official Docs](https://render.com/docs)
- [Render Blueprint Spec](https://render.com/docs/blueprint-spec)
- [Spring Boot on Render](https://render.com/docs/deploy-spring-boot)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---

## 🆘 Support

- 📧 **Issues:** https://github.com/L01Bestapp/volunteer-be/issues
- 💬 **Render Community:** https://community.render.com/
- 📖 **Docs:** https://render.com/docs

---

**🎉 Chúc bạn deploy thành công!**

Nếu gặp vấn đề, kiểm tra logs hoặc xem [RENDER_ENV_SETUP.md](./RENDER_ENV_SETUP.md) để troubleshooting chi tiết.
