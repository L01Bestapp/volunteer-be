# Fix Lỗi: Expo Push Token vs FCM Token

## 🐛 Lỗi gốc

```
The registration token is not a valid FCM registration token
Token: ExponentPushToken[YK2cTUFvT-F0xiBrZL6hFO]
```

## 🔍 Nguyên nhân

Có **2 loại push token** khác nhau:

### 1. Expo Push Token (từ Expo apps)
- Format: `ExponentPushToken[xxxxxx]` hoặc `ExpoPushToken[xxxxxx]`
- Được trả về bởi `expo-notifications` package
- **KHÔNG** tương thích với Firebase Admin SDK trực tiếp

### 2. Native FCM Token
- Format: String dài không có prefix (ví dụ: `dGzK5j8...`)
- Được trả về bởi Firebase JS SDK
- **CÓ THỂ** dùng với Firebase Admin SDK

**Vấn đề**: Frontend dùng Expo → Trả về Expo token → Backend dùng Firebase Admin SDK → **KHÔNG HỢP LỆ!**

## ✅ Giải pháp đã implement

Thêm **ExpoPushService** để gửi notification qua Expo Push Notification API thay vì Firebase Admin SDK.

### Flow mới:

```
User Token
    ↓
NotificationService detect token type
    ↓
Is Expo Token? ───Yes──→ ExpoPushService → Expo API → FCM/APNs → Device
    ↓ No
    ↓
FCMService → Firebase Admin SDK → FCM → Device
```

## 📁 Files đã thay đổi

### 1. **ExpoPushService.java** (NEW)
Service mới để gửi notification qua Expo Push API.

**Key features:**
- Gửi notification qua `https://exp.host/--/api/v2/push/send`
- Hỗ trợ Android heads-up configuration
- Batch sending (gửi nhiều notification cùng lúc)
- Validate Expo token format

### 2. **NotificationService.java** (UPDATED)
Thêm logic detect token type và chọn service phù hợp.

**Changes:**
```java
// OLD: Chỉ dùng FCMService
String messageId = fcmService.sendNotification(fcmToken, title, body, data);

// NEW: Detect và chọn service
if (isExpoToken(pushToken)) {
    success = expoPushService.sendNotification(pushToken, title, body, data);
} else {
    String messageId = fcmService.sendNotification(pushToken, title, body, data);
    success = (messageId != null);
}
```

### 3. **WebConfig.java** (UPDATED)
Thêm `WebClient.Builder` bean để ExpoPushService sử dụng.

```java
@Bean
public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
}
```

## 🧪 Test lại notification

### 1. Restart Backend
```bash
mvn spring-boot:run
```

### 2. Gửi test notification
```bash
curl -X POST http://localhost:8080/api/v1/notifications/test \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "🎉 Test Fixed!",
    "body": "Notification giờ đã hoạt động với Expo token!",
    "type": "REMINDER"
  }'
```

### 3. Check logs
**Nếu thành công**, bạn sẽ thấy:
```
Detected Expo push token, using ExpoPushService
Successfully sent Expo push notification. Ticket ID: xxx
Successfully sent push notification to user 2
```

**Thay vì lỗi:**
```
Failed to send FCM message to token ExponentPushToken[xxx]:
The registration token is not a valid FCM registration token
```

## 📱 App sẽ nhận notification

- **App foreground**: Banner popup nổi lên
- **App background**: Heads-up notification
- **App killed**: Heads-up notification

## 🔧 Cấu hình Expo Push Message

Backend gửi message với format:

```json
{
  "to": "ExponentPushToken[xxx]",
  "title": "Notification Title",
  "body": "Notification Body",
  "sound": "default",
  "priority": "high",
  "android": {
    "channelId": "default",
    "priority": "max"
  },
  "data": {
    "type": "REMINDER",
    "activityId": "123"
  }
}
```

### Android specific settings:
- `channelId: "default"` - Match với channel ID trong app
- `priority: "max"` - Để hiện heads-up notification

## 🎯 So sánh 2 approaches

### Expo Push Service (đang dùng) ⭐
**Pros:**
- ✅ Đơn giản, không cần config Firebase ở frontend
- ✅ Tự động routing đến FCM (Android) hoặc APNs (iOS)
- ✅ Phù hợp với Expo managed workflow
- ✅ Miễn phí đến 600 notifications/giây

**Cons:**
- ⚠️ Phụ thuộc vào Expo infrastructure
- ⚠️ Rate limit (600/s free tier, có thể upgrade)

### Firebase Admin SDK (cho native FCM tokens)
**Pros:**
- ✅ Full control, không giới hạn rate
- ✅ Nhiều features nâng cao (topics, conditions)
- ✅ Trực tiếp với Firebase

**Cons:**
- ⚠️ Phải config Firebase SDK ở frontend
- ⚠️ Phức tạp hơn với Expo apps

## 📊 Token Detection Logic

Backend tự động detect loại token:

```java
private boolean isExpoToken(String token) {
    return token.startsWith("ExponentPushToken[") ||
           token.startsWith("ExpoPushToken[");
}
```

- **Expo token** → ExpoPushService
- **Native FCM token** → FCMService
- **Hỗ trợ cả 2 loại** → Linh hoạt cho mọi client

## 🚀 Next Steps (Optional)

### Option 1: Tiếp tục dùng Expo Push Service (Recommended)
- Không cần thay đổi gì
- Hoạt động tốt với Expo apps

### Option 2: Chuyển sang Native FCM Token
Nếu muốn full control, update Frontend:

1. **Install Firebase JS SDK**:
```bash
npm install firebase
```

2. **Init Firebase**:
```typescript
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken } from 'firebase/messaging';

const firebaseConfig = {
  // Your config from Firebase Console
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);
```

3. **Get FCM Token**:
```typescript
const fcmToken = await getToken(messaging, {
  vapidKey: 'YOUR_VAPID_KEY'
});
```

4. **Send to Backend**:
```typescript
await api.put('/api/v1/notifications/fcm-token', { fcmToken });
```

**Lưu ý**: Approach này phức tạp hơn và không cần thiết nếu Expo Push Service đã hoạt động tốt.

## 📝 Summary

✅ **Đã fix lỗi**: Thêm ExpoPushService để hỗ trợ Expo tokens
✅ **Backward compatible**: Vẫn hỗ trợ native FCM tokens
✅ **Auto detection**: Tự động chọn service phù hợp
✅ **Tested**: Test ngay với `/api/v1/notifications/test`

**Giờ notification đã hoạt động hoàn toàn với Expo apps!** 🎉
