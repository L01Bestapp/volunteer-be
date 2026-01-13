# 📚 Deployment Documentation Index

> Tài liệu hướng dẫn deploy Volunteer Management System lên Render.

---

## 📖 Tài Liệu Có Sẵn

### 1. 🚀 [DEPLOY_RENDER.md](./DEPLOY_RENDER.md)
**Quick Start Guide** - Hướng dẫn nhanh deploy lên Render

**Nội dung:**
- ✅ Yêu cầu trước khi deploy
- ✅ Hướng dẫn tạo JWT keys
- ✅ Tạo PostgreSQL database trên Render
- ✅ Tạo Web Service
- ✅ Quick reference env vars
- ✅ Deploy & verify
- ✅ GitHub Actions auto-deploy (optional)
- ✅ Common issues & solutions
- ✅ Deployment checklist

**Dành cho:** Người mới, muốn deploy nhanh

---

### 2. 🔧 [RENDER_ENV_SETUP.md](./RENDER_ENV_SETUP.md)
**Detailed Environment Variables Guide** - Hướng dẫn chi tiết từng environment variable

**Nội dung:**
- ✅ Tổng quan tất cả 23 env vars
- ✅ Phân loại theo độ ưu tiên (CRITICAL, HIGH, MEDIUM, LOW)
- ✅ Hướng dẫn chuẩn bị credentials (Google OAuth, Firebase, Cloudinary, etc.)
- ✅ Hướng dẫn set từng biến chi tiết
- ✅ Troubleshooting đầy đủ cho từng lỗi
- ✅ Checklist kiểm tra

**Dành cho:** Cần hiểu rõ từng biến, troubleshooting lỗi

---

### 3. 📝 [.env.example](./.env.example)
**Environment Variables Template** - Template đầy đủ với comments

**Nội dung:**
- ✅ Template tất cả env vars
- ✅ Comments giải thích từng biến
- ✅ Hướng dẫn lấy credentials
- ✅ Priority level summary
- ✅ Có thể dùng trực tiếp cho local development

**Dành cho:** Copy-paste template, setup local env

---

### 4. ⚙️ [render.yaml](./render.yaml)
**Render Blueprint** - Infrastructure as Code

**Nội dung:**
- ✅ Cấu hình PostgreSQL service
- ✅ Cấu hình Web Service
- ✅ Auto-linking database
- ✅ Environment variables mapping

**Dành cho:** Deploy tự động với Render Blueprint

---

## 🎯 Lộ Trình Deploy Được Khuyến Nghị

### Lần Đầu Deploy
```
1. Đọc DEPLOY_RENDER.md (Phần "Yêu Cầu Trước Khi Deploy")
2. Chuẩn bị credentials theo RENDER_ENV_SETUP.md (Phần 2)
3. Tạo JWT keys theo DEPLOY_RENDER.md (Bước 1)
4. Follow DEPLOY_RENDER.md từ Bước 2-7
5. Nếu gặp lỗi → Xem RENDER_ENV_SETUP.md (Phần 5 - Troubleshooting)
```

### Khi Gặp Vấn Đề
```
1. Check logs trên Render Dashboard
2. Đối chiếu env vars với .env.example
3. Xem RENDER_ENV_SETUP.md (Phần 5) cho lỗi cụ thể
4. Verify credentials (Google OAuth, Database URL, etc.)
```

### Cập Nhật Env Vars
```
1. Xem .env.example để tham khảo format
2. Đọc RENDER_ENV_SETUP.md (Phần 3) cho biến cụ thể
3. Update trên Render Dashboard → Environment tab
4. Redeploy service
```

---

## 📊 Env Vars Quick Reference

### ⚠️ CRITICAL (App không chạy nếu thiếu)
```bash
JWT_PRIVATE_KEY_CONTENT    # RSA private key
JWT_PUBLIC_KEY_CONTENT     # RSA public key
POSTGRES_HOST              # Database host
POSTGRES_PORT              # Database port
POSTGRES_DB                # Database name
POSTGRES_USER              # Database user
POSTGRES_PASSWORD          # Database password
BASE_URL_WEBSITE           # App base URL
```

### 🔴 HIGH PRIORITY (Core features)
```bash
GOOGLE_CLIENT_ID           # OAuth login
GOOGLE_CLIENT_SECRET       # OAuth login
MAIL_SENDER_USERNAME       # Email sending
MAIL_SENDER_PASSWORD       # Email sending
CLOUDINARY_CLOUD_NAME      # Image upload
CLOUDINARY_API_KEY         # Image upload
CLOUDINARY_API_SECRET      # Image upload
```

### 🟡 OPTIONAL
```bash
FIREBASE_SERVICE_ACCOUNT_JSON  # Push notifications
ADMIN_USERNAME                 # Admin account
ADMIN_PASSWORD                 # Admin account
```

📖 **Chi tiết:** Xem [RENDER_ENV_SETUP.md](./RENDER_ENV_SETUP.md#1-tổng-quan-environment-variables)

---

## 🔗 Quick Links

### Render Services
- [Render Dashboard](https://dashboard.render.com/)
- [Render Docs](https://render.com/docs)
- [Render Blueprint Spec](https://render.com/docs/blueprint-spec)

### Get Credentials
- [Google Cloud Console](https://console.cloud.google.com/apis/credentials) - OAuth
- [Google App Passwords](https://myaccount.google.com/apppasswords) - Gmail
- [Firebase Console](https://console.firebase.google.com/) - FCM
- [Cloudinary Console](https://cloudinary.com/console) - Image upload

### Testing
```bash
# Health check
curl https://volunteer-api.onrender.com/actuator/health

# Swagger UI
https://volunteer-api.onrender.com/swagger-ui/index.html
```

### Monitoring
```bash
# UptimeRobot (prevent free plan sleep)
https://uptimerobot.com
```

---

## 🆘 Getting Help

### Troubleshooting Order
1. Check Render Dashboard → Logs
2. Verify env vars với `.env.example`
3. Read [RENDER_ENV_SETUP.md - Troubleshooting](./RENDER_ENV_SETUP.md#5-troubleshooting)
4. Check GitHub Issues: https://github.com/L01Bestapp/volunteer-be/issues

### Common Issues
- ❌ "Invalid JWT signature" → [Solution](./RENDER_ENV_SETUP.md#lỗi-invalid-jwt-signature)
- ❌ "Database connection failed" → [Solution](./RENDER_ENV_SETUP.md#lỗi-database-connection-failed)
- ❌ "Google OAuth callback error" → [Solution](./RENDER_ENV_SETUP.md#lỗi-google-oauth-callback-error)
- ❌ "Out of Memory" → [Solution](./RENDER_ENV_SETUP.md#lỗi-out-of-memory--application-crashed)

---

## 📁 File Structure

```
volunteer/
├── DEPLOYMENT_GUIDE_INDEX.md    ← Bạn đang ở đây
├── DEPLOY_RENDER.md             ← Quick Start Guide
├── RENDER_ENV_SETUP.md          ← Chi tiết Env Vars
├── .env.example                 ← Template env vars
├── render.yaml                  ← Render Blueprint
├── Dockerfile                   ← Docker config
├── docker-compose.yml           ← Local development
├── .github/workflows/ci-cd.yml  ← GitHub Actions
└── src/main/resources/
    ├── application.yml          ← Base config
    ├── application-dev.yml      ← Dev config
    ├── application-prod.yml     ← Production config
    └── application-test.yml     ← Test config
```

---

## ✅ Pre-Flight Checklist

Trước khi deploy, đảm bảo:

- [ ] Đã đọc DEPLOY_RENDER.md
- [ ] Đã tạo RSA key pair (privateKey.pem, publicKey.pem)
- [ ] Đã có Google OAuth credentials
- [ ] Đã setup OAuth redirect URI
- [ ] Đã có Gmail App Password
- [ ] Đã có Cloudinary credentials
- [ ] Đã push code lên GitHub
- [ ] Đã có tài khoản Render

---

**Happy Deploying! 🚀**

Nếu còn thắc mắc, tham khảo:
- 📖 [DEPLOY_RENDER.md](./DEPLOY_RENDER.md) - Quick start
- 🔧 [RENDER_ENV_SETUP.md](./RENDER_ENV_SETUP.md) - Deep dive
- 📝 [.env.example](./.env.example) - Template
