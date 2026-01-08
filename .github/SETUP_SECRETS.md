# GitHub Actions Secrets Setup Guide

Để CI/CD pipeline hoạt động, bạn cần cấu hình các secrets sau trong GitHub repository.

## Cách thêm secrets

1. Vào repository trên GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**

---

## 📋 Required Secrets

### 1. Docker Hub Credentials

#### `DOCKER_USERNAME`
- **Mô tả**: Username Docker Hub của bạn
- **Cách lấy**: Đăng ký tại [hub.docker.com](https://hub.docker.com)
- **Ví dụ**: `yourusername`

#### `DOCKER_PASSWORD`
- **Mô tả**: Access Token hoặc Password Docker Hub
- **Cách lấy**:
  1. Login vào Docker Hub
  2. Vào **Account Settings** → **Security** → **Access Tokens**
  3. Click **New Access Token**
  4. Đặt tên token (vd: `github-actions`)
  5. Copy token (chỉ hiển thị 1 lần!)
- **Lưu ý**: ⚠️ Dùng Access Token thay vì password trực tiếp (bảo mật hơn)

---

### 2. Railway Credentials

#### `RAILWAY_TOKEN`
- **Mô tả**: API Token để deploy lên Railway
- **Cách lấy**:
  1. Đăng ký tại [railway.app](https://railway.app)
  2. Vào **Account Settings** → **Tokens**
  3. Click **Create Token**
  4. Copy token
- **Tài liệu**: [Railway Tokens](https://docs.railway.app/develop/tokens)

#### `RAILWAY_APP_URL` (Environment Variable)
- **Mô tả**: URL của app trên Railway để verify deployment
- **Cách lấy**:
  1. Deploy app lên Railway lần đầu
  2. Railway sẽ tự generate URL
  3. Thường có dạng: `https://your-app.up.railway.app`
- **Cách set**: Thêm vào **Settings** → **Environments** → **production**

---

### 3. Code Coverage (Optional)

#### `CODECOV_TOKEN`
- **Mô tả**: Token để upload coverage report lên Codecov
- **Cách lấy**:
  1. Đăng ký tại [codecov.io](https://codecov.io)
  2. Kết nối với GitHub repository
  3. Copy token từ repository settings
- **Lưu ý**: Không bắt buộc, nhưng giúp track coverage qua thời gian

---

### 4. Slack Notification (Optional)

#### `SLACK_WEBHOOK_URL`
- **Mô tả**: Webhook URL để gửi thông báo deployment
- **Cách lấy**:
  1. Vào Slack workspace
  2. Vào **Apps** → tìm **Incoming Webhooks**
  3. Add to channel
  4. Copy Webhook URL
- **Tài liệu**: [Slack Incoming Webhooks](https://api.slack.com/messaging/webhooks)

---

## 🔒 Railway Environment Variables

Sau khi có `RAILWAY_TOKEN`, bạn cần set environment variables trên Railway:

### Cách set trên Railway:
1. Login vào [railway.app](https://railway.app)
2. Chọn project **volunteer-backend**
3. Vào tab **Variables**
4. Thêm các biến sau:

```bash
# Database
DATABASE_URL=postgresql://user:password@host:5432/volunteer
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT
JWT_PRIVATE_KEY=your_rsa_private_key
JWT_PUBLIC_KEY=your_rsa_public_key
JWT_EXPIRATION_TIME=604800
JWT_REFRESH_EXP_TIME=2592000

# Mail (Gmail SMTP)
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# App Config
SPRING_PROFILES_ACTIVE=prod
BASE_URL=https://your-app.up.railway.app
```

---

## ✅ Verification Checklist

- [ ] `DOCKER_USERNAME` - Docker Hub username
- [ ] `DOCKER_PASSWORD` - Docker Hub access token
- [ ] `RAILWAY_TOKEN` - Railway API token
- [ ] `RAILWAY_APP_URL` - Railway app URL (trong Environments → production)
- [ ] `CODECOV_TOKEN` (optional) - Codecov upload token
- [ ] `SLACK_WEBHOOK_URL` (optional) - Slack webhook URL
- [ ] Railway environment variables đã được set

---

## 🧪 Test Pipeline

Sau khi setup xong:

1. **Test CI**: Push code lên branch `develop` hoặc tạo Pull Request
   ```bash
   git checkout -b test-ci
   git commit --allow-empty -m "Test CI pipeline"
   git push origin test-ci
   ```

2. **Test CD**: Merge vào branch `main`
   ```bash
   git checkout main
   git merge test-ci
   git push origin main
   ```

3. **Xem kết quả**: Vào tab **Actions** trên GitHub repository

---

## 🔧 Troubleshooting

### Pipeline fails với "Error: Docker login failed"
- Kiểm tra `DOCKER_USERNAME` và `DOCKER_PASSWORD`
- Đảm bảo dùng Access Token thay vì password

### Pipeline fails với "Railway deployment failed"
- Kiểm tra `RAILWAY_TOKEN` còn valid không
- Verify Railway project đã được tạo
- Check Railway logs: `railway logs`

### Coverage badge không hiển thị
- Kiểm tra `CODECOV_TOKEN`
- Đảm bảo repository là public hoặc có Codecov Pro

### Deployment success nhưng app không chạy
- Check Railway logs để xem lỗi startup
- Verify environment variables trên Railway
- Check database connection string

---

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Hub](https://hub.docker.com)
- [Railway Documentation](https://docs.railway.app)
- [Codecov Documentation](https://docs.codecov.com)
