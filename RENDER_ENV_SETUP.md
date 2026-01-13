# Hướng Dẫn Chi Tiết: Set Environment Variables Trên Render

> **Mục đích:** Hướng dẫn từng bước cách set đúng và đủ tất cả environment variables cần thiết để deploy ứng dụng Spring Boot lên Render.

---

## 📋 Table of Contents

1. [Tổng Quan Environment Variables](#1-tổng-quan-environment-variables)
2. [Chuẩn Bị Trước Khi Set](#2-chuẩn-bị-trước-khi-set)
3. [Hướng Dẫn Set Từng Biến](#3-hướng-dẫn-set-từng-biến)
4. [Checklist Kiểm Tra](#4-checklist-kiểm-tra)
5. [Troubleshooting](#5-troubleshooting)

---

## 1. Tổng Quan Environment Variables

Ứng dụng này cần **23 environment variables**, được phân loại theo độ ưu tiên:

### CRITICAL (Bắt buộc - App không chạy nếu thiếu)
- `JWT_PRIVATE_KEY_CONTENT` và `JWT_PUBLIC_KEY_CONTENT`
- `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` (5 biến)
- `BASE_URL_WEBSITE`

**Tổng: 8 biến CRITICAL**

### HIGH PRIORITY (Core features không hoạt động)
- `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` - Login with Google
- `MAIL_SENDER_USERNAME` và `MAIL_SENDER_PASSWORD` - Password reset, email
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` - Upload images

**Tổng: 7 biến HIGH PRIORITY**

### MEDIUM PRIORITY (Optional features)
- `FIREBASE_SERVICE_ACCOUNT_JSON` - Push notifications
- `ADMIN_USERNAME` và `ADMIN_PASSWORD` - Admin account

**Tổng: 3 biến MEDIUM PRIORITY**

### LOW PRIORITY (Có default values)
- `SPRING_PROFILES_ACTIVE` (default: dev)
- `SERVER_PORT` (default: 8080)
- `JWT_KEY_ID` (default: ctxh-key)
- `EXP_TOKEN` (default: 604800)
- `EXP_REFRESH_TOKEN` (default: 2592000)
- `GOOGLE_CLIENT_ID_ANDROID`, `GOOGLE_CLIENT_ID_IOS` (default: empty)

**Tổng: 5 biến LOW PRIORITY (có thể skip)**

---

## 2. Chuẩn Bị Trước Khi Set

### Bước 1: Tạo JWT RSA Keys

```bash
# Tạo private key (2048-bit)
openssl genrsa -out privateKey.pem 2048

# Tạo public key từ private key
openssl rsa -in privateKey.pem -pubout -out publicKey.pem

# Kiểm tra keys đã tạo
ls -lh *.pem
```

**Output:**
```
-rw-r--r--  1 user  staff   1.7K Jan 13 10:00 privateKey.pem
-rw-r--r--  1 user  staff   451B Jan 13 10:00 publicKey.pem
```

**Lưu ý:**
- Lưu 2 file này ở nơi an toàn (password manager, vault)
- KHÔNG commit vào Git
- KHÔNG share cho ai

### Bước 2: Lấy Google OAuth Credentials

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Tạo hoặc chọn một Project
3. **APIs & Services** → **Credentials** → **Create Credentials** → **OAuth 2.0 Client ID**
4. Application type: **Web application**
5. **Authorized redirect URIs:**
   ```
   https://volunteer-api.onrender.com/login/oauth2/code/google
   ```
   (Thay `volunteer-api` bằng service name của bạn)
6. Click **Create** → Copy **Client ID** và **Client secret**

### Bước 3: Lấy Firebase Service Account JSON (Optional)

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. **Project Settings** (⚙️) → **Service Accounts**
4. Click **Generate new private key**
5. Download file JSON → Copy toàn bộ content

**Minify JSON (optional):**
```bash
cat firebase-service-account.json | jq -c
```

### Bước 4: Tạo Gmail App Password

1. Truy cập [Google Account Settings](https://myaccount.google.com/)
2. **Security** → Bật **2-Step Verification** (nếu chưa bật)
3. Vào [App Passwords](https://myaccount.google.com/apppasswords)
4. **Select app**: Mail
5. **Select device**: Other (Custom name) → Nhập "Volunteer App"
6. Click **Generate** → Copy password (16 ký tự, có thể có spaces)

### Bước 5: Lấy Cloudinary Credentials

1. Truy cập [Cloudinary Console](https://cloudinary.com/console)
2. **Dashboard** → Xem **Account Details**
3. Copy:
   - Cloud name
   - API Key
   - API Secret

---

## 3. Hướng Dẫn Set Từng Biến

### A. Trên Render Dashboard

1. Vào [Render Dashboard](https://dashboard.render.com/)
2. Chọn service **volunteer-api** (hoặc tên service của bạn)
3. Vào **Environment** tab (sidebar bên trái)
4. Click **Add Environment Variable**

### B. Set CRITICAL Variables

#### 3.1. JWT_PRIVATE_KEY_CONTENT

**Cách 1: Paste trực tiếp (Multiline)**

1. Click **Add Environment Variable**
2. **Key:** `JWT_PRIVATE_KEY_CONTENT`
3. **Value:**
   - Mở file `privateKey.pem`
   - Copy **TOÀN BỘ** nội dung (bao gồm `-----BEGIN PRIVATE KEY-----` và `-----END PRIVATE KEY-----`)
   - Paste vào ô Value
   - Render hỗ trợ multiline, giữ nguyên line breaks

```
-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDfRdwWjXwivJ09
8UYoVvjZu87aXAxQldWdkLEdunsn9I6pZPZ/BI6YJ5e6hs9kmKy6AE4wCRwlwfVD
... (full content) ...
fhUGXE14eUdqgjp2RWUwB2bm
-----END PRIVATE KEY-----
```

**Cách 2: Encode base64 (nếu Render không hỗ trợ multiline tốt)**

```bash
# Encode private key
cat privateKey.pem | base64

# Decode trong code (cần modify code)
```

**⚠️ Lưu ý:**
- **KHÔNG** thêm quotes (' hoặc ")
- **KHÔNG** thêm khoảng trắng thừa ở đầu/cuối
- **GIỮ NGUYÊN** line breaks

#### 3.2. JWT_PUBLIC_KEY_CONTENT

Tương tự như private key:

1. **Key:** `JWT_PUBLIC_KEY_CONTENT`
2. **Value:** Copy toàn bộ content của `publicKey.pem`

```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA30XcFo18IrydPfFGKFb4
... (full content) ...
OQIDAQAB
-----END PUBLIC KEY-----
```

#### 3.3. POSTGRES_* (Database Variables)

**Cách 1: Sử dụng Render Internal Database URL (Khuyến nghị)**

Nếu bạn đã tạo PostgreSQL service trên Render:

1. Vào PostgreSQL service → **Info** tab
2. Copy **Internal Database URL**
   ```
   postgres://volunteer:abc123@dpg-xxxxx-a.singapore-postgres.render.com/volunteer_db
   ```

3. Parse URL và set 5 biến:
   ```
   POSTGRES_USER     = volunteer
   POSTGRES_PASSWORD = abc123
   POSTGRES_HOST     = dpg-xxxxx-a.singapore-postgres.render.com
   POSTGRES_PORT     = 5432
   POSTGRES_DB       = volunteer_db
   ```

**Cách 2: Link Database từ render.yaml (Tự động)**

Nếu bạn dùng `render.yaml`, các biến này được link tự động:

```yaml
- key: POSTGRES_HOST
  fromDatabase:
    name: volunteer-db
    property: host
```

**⚠️ Lưu ý:**
- Dùng **Internal URL** (nhanh hơn, không tính bandwidth)
- **KHÔNG** dùng External URL trừ khi connect từ bên ngoài Render

#### 3.4. BASE_URL_WEBSITE

1. **Key:** `BASE_URL_WEBSITE`
2. **Value:** `https://volunteer-api.onrender.com`
   - Thay `volunteer-api` bằng service name của bạn
   - **KHÔNG** có trailing slash (/)
   - **BẮT BUỘC** dùng HTTPS

**Sử dụng trong:**
- Google OAuth redirect URI: `${BASE_URL_WEBSITE}/login/oauth2/code/google`
- Email links (password reset, verification)
- CORS configuration

---

### C. Set HIGH PRIORITY Variables

#### 3.5. GOOGLE_CLIENT_ID

1. **Key:** `GOOGLE_CLIENT_ID`
2. **Value:** `123456789-abcdefghijk.apps.googleusercontent.com`
   - Copy từ Google Cloud Console (Bước 2.2)

#### 3.6. GOOGLE_CLIENT_SECRET

1. **Key:** `GOOGLE_CLIENT_SECRET`
2. **Value:** `GOCSPX-xxxxxxxxxxxxxxxxxxxxxxxx`
   - Copy từ Google Cloud Console

**Test OAuth flow:**
```
https://volunteer-api.onrender.com/oauth2/authorization/google
```

#### 3.7. MAIL_SENDER_USERNAME

1. **Key:** `MAIL_SENDER_USERNAME`
2. **Value:** `your-email@gmail.com`
   - Gmail account có bật 2FA

#### 3.8. MAIL_SENDER_PASSWORD

1. **Key:** `MAIL_SENDER_PASSWORD`
2. **Value:** `abcd efgh ijkl mnop`
   - 16-character App Password từ Google (Bước 2.4)
   - Có thể có spaces (không cần xóa)

**Test email:**
```bash
curl -X POST https://volunteer-api.onrender.com/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'
```

#### 3.9. CLOUDINARY_CLOUD_NAME

1. **Key:** `CLOUDINARY_CLOUD_NAME`
2. **Value:** `your-cloud-name`
   - Copy từ Cloudinary Dashboard (Bước 2.5)

#### 3.10. CLOUDINARY_API_KEY

1. **Key:** `CLOUDINARY_API_KEY`
2. **Value:** `123456789012345`

#### 3.11. CLOUDINARY_API_SECRET

1. **Key:** `CLOUDINARY_API_SECRET`
2. **Value:** `abcdefghijklmnopqrstuvwxyz123456`

**Test upload:**
```bash
curl -X POST https://volunteer-api.onrender.com/api/v1/users/avatar \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@image.jpg"
```

---

### D. Set MEDIUM PRIORITY Variables (Optional)

#### 3.12. FIREBASE_SERVICE_ACCOUNT_JSON

**⚠️ Optional:** Chỉ cần nếu dùng FCM push notifications. Nếu không set, app vẫn chạy bình thường.

1. **Key:** `FIREBASE_SERVICE_ACCOUNT_JSON`
2. **Value:**
   ```json
   {"type":"service_account","project_id":"your-project","private_key_id":"...","private_key":"-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n","client_email":"firebase-adminsdk-xxx@your-project.iam.gserviceaccount.com","client_id":"...","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_x509_cert_url":"..."}
   ```
   - Copy toàn bộ content từ `firebase-service-account.json`
   - Có thể minify thành 1 dòng hoặc giữ nguyên multiline

**Kiểm tra trong logs:**
- ✅ Nếu có: `Firebase Admin SDK initialized successfully`
- ⚠️ Nếu không: `Firebase credentials not found in environment variables` (warning, không phải error)

#### 3.13. ADMIN_USERNAME

1. **Key:** `ADMIN_USERNAME`
2. **Value:** `admin@example.com`
   - Default nếu không set: `admin`
   - Được tạo tự động bởi `DataInitializer` khi app khởi động lần đầu

#### 3.14. ADMIN_PASSWORD

1. **Key:** `ADMIN_PASSWORD`
2. **Value:** `YourSecurePasswordHere123!`
   - Default nếu không set: `admin` (KHÔNG AN TOÀN cho production)
   - Password sẽ được hash bằng BCrypt trước khi lưu vào database

**Test admin login:**
```bash
curl -X POST https://volunteer-api.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@example.com","password":"YourSecurePasswordHere123!"}'
```

---

### E. Set LOW PRIORITY Variables (Có Default Values)

#### 3.15. SPRING_PROFILES_ACTIVE

1. **Key:** `SPRING_PROFILES_ACTIVE`
2. **Value:** `prod`
   - **QUAN TRỌNG:** Phải set `prod` cho production
   - Default: `dev` (sẽ load `application-dev.yml`)

#### 3.16. Các Biến Khác (Optional)

Các biến này có default values và thường **KHÔNG CẦN** set:

| Key | Default | Mô Tả |
|-----|---------|-------|
| `SERVER_PORT` | 8080 | Render tự động map port |
| `JWT_KEY_ID` | ctxh-key | Key ID cho JWT |
| `EXP_TOKEN` | 604800 | Token expiration (7 days) |
| `EXP_REFRESH_TOKEN` | 2592000 | Refresh token expiration (30 days) |
| `GOOGLE_CLIENT_ID_ANDROID` | (empty) | Android OAuth Client ID |
| `GOOGLE_CLIENT_ID_IOS` | (empty) | iOS OAuth Client ID |

---

## 4. Checklist Kiểm Tra

### ✅ Before Deploy

- [ ] Đã tạo RSA key pair (privateKey.pem, publicKey.pem)
- [ ] Đã có Google OAuth credentials (Client ID, Client Secret)
- [ ] Đã setup Google OAuth redirect URI: `https://your-app.onrender.com/login/oauth2/code/google`
- [ ] Đã có Gmail App Password (16 characters)
- [ ] Đã có Cloudinary credentials (Cloud Name, API Key, API Secret)
- [ ] Đã tạo PostgreSQL service trên Render

### ✅ Environment Variables Set (Minimum Required)

**CRITICAL (8 biến):**
- [ ] `JWT_PRIVATE_KEY_CONTENT`
- [ ] `JWT_PUBLIC_KEY_CONTENT`
- [ ] `POSTGRES_HOST`
- [ ] `POSTGRES_PORT`
- [ ] `POSTGRES_DB`
- [ ] `POSTGRES_USER`
- [ ] `POSTGRES_PASSWORD`
- [ ] `BASE_URL_WEBSITE`

**HIGH PRIORITY (7 biến):**
- [ ] `GOOGLE_CLIENT_ID`
- [ ] `GOOGLE_CLIENT_SECRET`
- [ ] `MAIL_SENDER_USERNAME`
- [ ] `MAIL_SENDER_PASSWORD`
- [ ] `CLOUDINARY_CLOUD_NAME`
- [ ] `CLOUDINARY_API_KEY`
- [ ] `CLOUDINARY_API_SECRET`

**RECOMMENDED (2 biến):**
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `ADMIN_PASSWORD` (thay đổi từ default "admin")

### ✅ After Deploy

- [ ] Service đã deploy thành công (check logs)
- [ ] Health check endpoint hoạt động: `https://your-app.onrender.com/actuator/health`
- [ ] Database connection thành công (check logs: "HikariPool-1 - Start completed")
- [ ] JWT keys loaded successfully (check logs: "RSA keys loaded")
- [ ] Test login flow (username/password)
- [ ] Test Google OAuth login
- [ ] Test email sending (password reset)
- [ ] Test image upload

---

## 5. Troubleshooting

### Lỗi: "Invalid JWT signature"

**Nguyên nhân:** JWT keys không đúng hoặc không được set.

**Giải pháp:**
1. Kiểm tra `JWT_PRIVATE_KEY_CONTENT` và `JWT_PUBLIC_KEY_CONTENT` đã set chưa
2. Đảm bảo copy **TOÀN BỘ** content (bao gồm BEGIN/END)
3. Không có khoảng trắng thừa ở đầu/cuối
4. Kiểm tra logs: `RSA keys loaded from environment variables`

```bash
# Verify keys locally
openssl rsa -in privateKey.pem -check
openssl rsa -pubin -in publicKey.pem -text
```

### Lỗi: "Database connection failed"

**Nguyên nhân:** Database credentials không đúng hoặc database chưa ready.

**Giải pháp:**
1. Kiểm tra PostgreSQL service đã **running** chưa
2. Verify 5 biến `POSTGRES_*` đã set đúng
3. Dùng **Internal Database URL** (không phải External)
4. Check logs: `HikariPool-1 - Starting...`

```bash
# Test connection (từ Render shell)
psql $POSTGRES_URL
```

### Lỗi: "Google OAuth callback error"

**Nguyên nhân:** Redirect URI không khớp.

**Giải pháp:**
1. Kiểm tra `BASE_URL_WEBSITE` đúng chưa (không có trailing slash)
2. Vào Google Cloud Console → Credentials → Edit OAuth Client
3. **Authorized redirect URIs** phải có:
   ```
   https://volunteer-api.onrender.com/login/oauth2/code/google
   ```
4. Đợi vài phút để Google cập nhật

### Lỗi: "Failed to send email"

**Nguyên nhân:** Gmail credentials không đúng hoặc 2FA chưa bật.

**Giải pháp:**
1. Kiểm tra `MAIL_SENDER_USERNAME` và `MAIL_SENDER_PASSWORD`
2. Đảm bảo đã bật 2-Factor Authentication trên Google Account
3. Tạo lại App Password: https://myaccount.google.com/apppasswords
4. Check logs: `MailService` errors

### Lỗi: "Cloudinary upload failed"

**Nguyên nhân:** Cloudinary credentials không đúng.

**Giải pháp:**
1. Verify 3 biến `CLOUDINARY_*` đã set đúng
2. Check Cloudinary Dashboard: Account Settings → API Keys
3. Test upload từ Cloudinary Console
4. Check logs: `CloudinaryConfig` initialization

### Warning: "Firebase credentials not found"

**Không phải lỗi!** Đây chỉ là warning nếu không dùng FCM push notifications.

**Giải pháp:**
- Nếu cần FCM: Set `FIREBASE_SERVICE_ACCOUNT_JSON`
- Nếu không cần: Ignore warning này, app vẫn chạy bình thường

### Lỗi: "Out of Memory" / "Application crashed"

**Nguyên nhân:** Free plan chỉ có 512MB RAM.

**Giải pháp:**
1. Đã optimize trong Dockerfile:
   ```
   -XX:MaxRAMPercentage=75.0
   -XX:InitialRAMPercentage=50.0
   ```
2. Đã optimize trong `application-prod.yml`:
   - `spring.main.lazy-initialization: true`
   - `hikari.maximum-pool-size: 5`
   - Disabled second-level cache
3. Nếu vẫn bị: Nâng cấp lên **Starter Plan** ($7/tháng)

### Check Logs

```bash
# Trên Render Dashboard
Dashboard → volunteer-api → Logs

# Hoặc dùng Render CLI
render logs volunteer-api --tail
```

**Logs quan trọng:**
- ✅ `Started VolunteerApplication in X.XXX seconds`
- ✅ `HikariPool-1 - Start completed`
- ✅ `RSA keys loaded from environment variables`
- ✅ `Firebase Admin SDK initialized successfully` (nếu có FCM)
- ⚠️ `Firebase credentials not found` (OK nếu không dùng FCM)

---

## 📚 References

- [Render Environment Variables Docs](https://render.com/docs/environment-variables)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Google OAuth 2.0 Setup](https://developers.google.com/identity/protocols/oauth2)
- [Firebase Admin SDK Setup](https://firebase.google.com/docs/admin/setup)
- [Cloudinary Upload API](https://cloudinary.com/documentation/image_upload_api_reference)

---

**Cần trợ giúp?** Kiểm tra logs hoặc contact support tại: https://github.com/L01Bestapp/volunteer-be/issues
