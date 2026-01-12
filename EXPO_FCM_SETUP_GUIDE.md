# Hướng dẫn Fix Lỗi: Expo Push Notification cần FCM Server Key

## 🐛 Lỗi hiện tại

```
Failed to send Expo push notification. Error:
Unable to retrieve the FCM server key for the recipient's app.
Make sure you have provided a server key as directed by the Expo FCM documentation.
```

## 🔍 Nguyên nhân

Expo Push Notification Service cần **FCM Server Key** để gửi notification đến Android devices. App của bạn chưa config FCM credentials trên Expo servers.

## ✅ Giải pháp 1: Config FCM cho Expo (Production-ready) - KHUYẾN NGHỊ

### Bước 1: Lấy FCM Server Key từ Firebase Console

1. **Truy cập Firebase Console**: https://console.firebase.google.com/
2. Chọn project của bạn (hoặc tạo mới nếu chưa có)
3. Vào **Project Settings** (biểu tượng ⚙️)
4. Chọn tab **Cloud Messaging**
5. Trong phần **Cloud Messaging API (Legacy)**, tìm **Server key**
   - Nếu chưa enable: Click **Enable** Cloud Messaging API (Legacy)
   - Copy **Server key** (dạng: `AAAAxxxxxxxx:xxxxxxxxxxxxxxxxxxxx`)

**Lưu ý**: Nếu không thấy Server key:
- Click vào link **Manage API in Google Cloud Console**
- Enable **Cloud Messaging API** (Legacy)
- Quay lại Firebase Console, refresh page

### Bước 2: Upload FCM Server Key lên Expo

#### Option A: Sử dụng Expo CLI (Dễ nhất)

```bash
# 1. Install Expo CLI (nếu chưa có)
npm install -g expo-cli

# 2. Login vào Expo
expo login

# 3. Navigate đến project directory
cd /path/to/your/expo/project

# 4. Upload FCM Server Key
expo push:android:upload --api-key YOUR_FCM_SERVER_KEY
```

**Ví dụ**:
```bash
expo push:android:upload --api-key AAAAxxxxxxxx:APA91bHxxxxxxxxxxxxxxxxxxxxxx
```

#### Option B: Sử dụng Expo Website (Thủ công)

1. Đăng nhập vào: https://expo.dev/
2. Vào project của bạn
3. Chọn **Credentials** (hoặc **Project Settings**)
4. Chọn **Android**
5. Tìm section **FCM Server Key**
6. Paste FCM Server Key vào và **Save**

### Bước 3: Rebuild App (QUAN TRỌNG)

**Lưu ý**: Sau khi upload FCM credentials, bạn **PHẢI rebuild app** để credentials có hiệu lực.

#### Nếu dùng EAS Build:

```bash
# Build development
eas build --platform android --profile development

# Hoặc build production
eas build --platform android --profile production
```

#### Nếu dùng Expo build (Legacy):

```bash
expo build:android
```

### Bước 4: Install app mới và test

1. Download file APK/AAB từ Expo
2. Install lên điện thoại
3. Đăng nhập vào app
4. Gửi test notification từ Backend

```bash
curl -X POST http://localhost:8080/api/v1/notifications/test \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "✅ Test với FCM Config",
    "body": "Giờ nên hoạt động rồi!",
    "type": "REMINDER"
  }'
```

---

## ✅ Giải pháp 2: Dùng Expo's Push Notification Credentials (Development Only)

**Chỉ dùng cho development/testing**. Không dùng cho production!

### Điều kiện:
- App đang chạy trên **Expo Go** (app màu tím với logo Expo)
- HOẶC development build với Expo's development credentials

### Cách làm:

**Không cần làm gì!** Expo Go tự động dùng Expo's shared FCM credentials.

**Hạn chế:**
- ⚠️ Không đảm bảo delivery rate cao
- ⚠️ Có rate limit thấp hơn
- ⚠️ Không thể dùng cho production app

---

## 🧪 Kiểm tra App đang dùng gì

### Cách 1: Check trong code

Trong file `app.json` hoặc `app.config.js`:

```json
{
  "expo": {
    "android": {
      "googleServicesFile": "./google-services.json"  // ← Nếu có dòng này = standalone app
    }
  }
}
```

- **Có** `googleServicesFile` → Standalone app → Cần config FCM (Solution 1)
- **Không có** → Expo Go hoặc bare workflow → Có thể dùng Solution 2

### Cách 2: Check token format

Nếu token có dạng `ExponentPushToken[xxx]` → Có thể là Expo Go

---

## 🎯 Khuyến nghị

### Cho Development:
- Dùng **Expo Go** với Expo's credentials (Solution 2)
- Nhanh, không cần config

### Cho Production:
- **BẮT BUỘC** dùng Solution 1 (Config FCM)
- Đảm bảo delivery rate cao
- Không bị rate limit
- Professional

---

## 🔧 Troubleshooting

### Lỗi: "Cloud Messaging API (Legacy) is disabled"

**Fix:**
1. Vào Google Cloud Console: https://console.cloud.google.com/
2. Chọn project Firebase của bạn
3. Vào **APIs & Services** → **Library**
4. Tìm "Cloud Messaging"
5. Click **Firebase Cloud Messaging API**
6. Click **Enable**

### Lỗi: "Invalid FCM Server Key"

**Fix:**
- Đảm bảo copy đúng Server Key (dạng `AAAAxxxxxxxx:...`)
- Không phải là Sender ID
- Không phải là Web API Key

### Notification vẫn không nhận được sau khi config

**Checklist:**
1. ✅ Đã upload FCM Server Key lên Expo
2. ✅ Đã rebuild app
3. ✅ Đã install app mới (không phải update từ store)
4. ✅ Backend log hiện "Successfully sent Expo push notification"
5. ✅ Device có internet
6. ✅ App đã xin quyền notification

---

## 📚 Tài liệu tham khảo

- [Expo Push Notifications Setup](https://docs.expo.dev/push-notifications/push-notifications-setup/)
- [Using FCM for Push Notifications](https://docs.expo.dev/push-notifications/using-fcm/)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

---

## 🚨 Lưu ý quan trọng

1. **FCM Server Key ≠ google-services.json**
   - Server Key: Để backend gửi notification
   - google-services.json: Để app nhận notification

2. **Phải rebuild app sau khi upload credentials**
   - Credentials được embed vào app lúc build time
   - Update OTA không đủ

3. **Legacy FCM API**
   - Expo hiện tại vẫn dùng FCM Legacy API
   - Google đang chuyển sang FCM HTTP v1 API
   - Expo sẽ support trong tương lai

4. **Backend không cần thay đổi**
   - Backend chỉ cần gửi đến Expo API
   - Expo lo việc forward đến FCM/APNs

---

## 🎊 Tóm tắt

**Development (Testing nhanh):**
```
Dùng Expo Go → Không cần config gì → Test ngay
```

**Production (Proper way):**
```
1. Lấy FCM Server Key từ Firebase Console
2. Upload lên Expo: expo push:android:upload --api-key YOUR_KEY
3. Rebuild app: eas build --platform android
4. Install app mới và test
```

**Backend đã đúng rồi, không cần sửa gì!** 👍
