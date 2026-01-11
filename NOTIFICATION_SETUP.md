# Hướng dẫn cấu hình hệ thống thông báo

## Tổng quan

Hệ thống thông báo đã được triển khai với các tính năng sau:
1. Thông báo sau khi check-in/check-out thành công
2. Nhắc nhở tự động trước 1 giờ khi hoạt động bắt đầu
3. Lưu lịch sử thông báo vào database
4. Gửi push notification qua Firebase Cloud Messaging (FCM)

## Kiến trúc

### Backend (Spring Boot)
- **Spring Events**: Xử lý bất đồng bộ khi có sự kiện (check-in/out)
- **Spring Scheduler**: Quét database định kỳ để gửi nhắc nhở
- **Firebase Admin SDK**: Gửi push notification đến mobile app
- **PostgreSQL**: Lưu trữ thông báo và FCM tokens

### Mobile App (Expo/React Native)
- **expo-notifications**: Nhận và hiển thị thông báo
- Gửi FCM token lên backend khi đăng nhập

## Cấu hình Backend

### 1. Tạo Firebase Project

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Vào **Project Settings** > **Service Accounts**
4. Click **Generate New Private Key**
5. Lưu file JSON với tên `firebase-service-account.json`

### 2. Thêm Firebase Service Account vào Project

Đặt file `firebase-service-account.json` vào thư mục `src/main/resources/`

```
src/
  main/
    resources/
      firebase-service-account.json  <-- Đặt file ở đây
```

### 3. Cấu hình Database

Database sẽ tự động tạo các bảng sau khi chạy ứng dụng (nếu bạn dùng `spring.jpa.hibernate.ddl-auto=update`):

- **users**: Thêm cột `fcm_token` để lưu token từ mobile app
- **notifications**: Lưu lịch sử thông báo

Nếu bạn muốn tạo migration thủ công, sử dụng script SQL sau:

```sql
-- Thêm cột fcm_token vào bảng users
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(500);

-- Tạo bảng notifications
CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT,
    type VARCHAR(50) NOT NULL,
    data JSONB,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_sent BOOLEAN NOT NULL DEFAULT FALSE,
    create_by BIGINT,
    update_by BIGINT,
    create_at TIMESTAMP,
    update_at TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Tạo indexes
CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_type ON notifications(type);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
```

### 4. Chạy Backend

```bash
mvn spring-boot:run
```

Kiểm tra log xem Firebase đã khởi tạo thành công:
```
Firebase Admin SDK initialized successfully
```

## Cấu hình Mobile App (Expo)

### 1. Cài đặt thư viện

```bash
npx expo install expo-notifications expo-device expo-constants
```

### 2. Cấu hình app.json

Thêm plugin notifications vào `app.json`:

```json
{
  "expo": {
    "plugins": [
      [
        "expo-notifications",
        {
          "icon": "./assets/notification-icon.png",
          "color": "#ffffff"
        }
      ]
    ]
  }
}
```

### 3. Lấy và gửi FCM Token

Tạo file `src/services/notificationService.js`:

```javascript
import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import { Platform } from 'react-native';
import axios from 'axios';

// Cấu hình cách hiển thị notification khi app đang mở
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

// Đăng ký nhận notification và gửi token lên backend
export async function registerForPushNotifications(authToken) {
  if (!Device.isDevice) {
    console.log('Push notifications only work on physical devices');
    return null;
  }

  // Xin quyền
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;

  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }

  if (finalStatus !== 'granted') {
    console.log('Permission not granted for push notifications');
    return null;
  }

  // Lấy token
  const tokenData = await Notifications.getExpoPushTokenAsync({
    projectId: 'your-expo-project-id', // Thay bằng project ID của bạn
  });

  const fcmToken = tokenData.data;

  // Gửi token lên backend
  try {
    await axios.put(
      'http://your-backend-url/api/v1/notifications/fcm-token',
      { fcmToken },
      {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      }
    );
    console.log('FCM token sent to backend:', fcmToken);
  } catch (error) {
    console.error('Failed to send FCM token:', error);
  }

  // Cấu hình notification channel cho Android
  if (Platform.OS === 'android') {
    Notifications.setNotificationChannelAsync('default', {
      name: 'default',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#FF231F7C',
    });
  }

  return fcmToken;
}

// Lắng nghe notification khi app đang mở
export function addNotificationListener(callback) {
  return Notifications.addNotificationReceivedListener(callback);
}

// Lắng nghe khi user bấm vào notification
export function addNotificationResponseListener(callback) {
  return Notifications.addNotificationResponseReceivedListener(callback);
}
```

### 4. Sử dụng trong App

Trong component đăng nhập hoặc App.js:

```javascript
import { useEffect } from 'react';
import { registerForPushNotifications, addNotificationListener, addNotificationResponseListener } from './services/notificationService';

function App() {
  useEffect(() => {
    // Đăng ký notification khi user đăng nhập
    const authToken = getAuthToken(); // Lấy token từ storage
    if (authToken) {
      registerForPushNotifications(authToken);
    }

    // Lắng nghe notification khi app đang mở
    const notificationListener = addNotificationListener((notification) => {
      console.log('Received notification:', notification);
      // Hiển thị in-app notification hoặc update UI
    });

    // Lắng nghe khi user bấm vào notification
    const responseListener = addNotificationResponseListener((response) => {
      console.log('User tapped notification:', response);
      const data = response.notification.request.content.data;

      // Navigate đến màn hình tương ứng
      if (data.activityId) {
        navigation.navigate('ActivityDetail', { id: data.activityId });
      }
    });

    // Cleanup
    return () => {
      notificationListener.remove();
      responseListener.remove();
    };
  }, []);

  return (
    // Your app components
  );
}
```

## API Endpoints

### Notification APIs

```
GET    /api/v1/notifications              - Lấy tất cả thông báo
GET    /api/v1/notifications/unread       - Lấy thông báo chưa đọc
GET    /api/v1/notifications/unread-count - Đếm số thông báo chưa đọc
PUT    /api/v1/notifications/{id}/read    - Đánh dấu 1 thông báo đã đọc
PUT    /api/v1/notifications/read-all     - Đánh dấu tất cả đã đọc
PUT    /api/v1/notifications/fcm-token    - Cập nhật FCM token
```

### Request Example

Cập nhật FCM token:
```bash
curl -X PUT http://localhost:8080/api/v1/notifications/fcm-token \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]"
  }'
```

## Luồng hoạt động

### 1. Thông báo Check-out thành công

```
Student check-out
  → AttendanceServiceImpl.checkOut()
  → Lưu attendance vào DB
  → enrollment.complete() (cập nhật CTXH days)
  → Publish AttendanceCompletedEvent
  → NotificationEventListener nhận event
  → NotificationService.sendAndSaveNotification()
  → Lưu notification vào DB
  → FCMService.sendNotification()
  → Mobile app nhận notification
```

### 2. Nhắc nhở trước 1 giờ

```
Scheduler chạy mỗi 5 phút
  → ActivityReminderScheduler.sendActivityReminders()
  → Tìm activities bắt đầu trong vòng 1h
  → Lấy danh sách students đã đăng ký (APPROVED)
  → Kiểm tra xem đã gửi reminder chưa
  → NotificationService.sendAndSaveNotification()
  → Lưu notification vào DB
  → FCMService.sendNotification()
  → Mobile app nhận notification
```

## Loại thông báo

Hệ thống hỗ trợ các loại thông báo sau (NotificationType):

- `REMINDER`: Nhắc nhở trước 1 giờ
- `ATTENDANCE_COMPLETED`: Hoàn thành check-in/out
- `ENROLLMENT_APPROVED`: Đăng ký được duyệt (có thể mở rộng)
- `ENROLLMENT_REJECTED`: Đăng ký bị từ chối (có thể mở rộng)
- `ACTIVITY_UPDATED`: Hoạt động được cập nhật (có thể mở rộng)
- `ACTIVITY_CANCELLED`: Hoạt động bị hủy (có thể mở rộng)
- `GENERAL`: Thông báo chung

## Mở rộng

### Thêm loại thông báo mới

1. Thêm enum vào `NotificationType.java`
2. Tạo Event mới (ví dụ: `EnrollmentApprovedEvent`)
3. Thêm handler trong `NotificationEventListener`
4. Publish event ở nơi cần thiết

### Tùy chỉnh thời gian nhắc nhở

Sửa trong `ActivityReminderScheduler.java`:

```java
// Nhắc nhở trước 2 giờ thay vì 1 giờ
LocalDateTime twoHoursLater = now.plusHours(2);

List<Activity> upcomingActivities = activityRepository.findActivitiesStartingBetween(
    now,
    twoHoursLater.plusMinutes(5)
);
```

### Thay đổi tần suất quét

Sửa cron expression trong `@Scheduled`:

```java
// Chạy mỗi 10 phút
@Scheduled(cron = "0 */10 * * * *")

// Chạy mỗi giờ
@Scheduled(cron = "0 0 * * * *")
```

## Troubleshooting

### Backend không gửi được notification

1. Kiểm tra file `firebase-service-account.json` đã đúng vị trí chưa
2. Kiểm tra log có lỗi khi khởi tạo Firebase không
3. Kiểm tra user có FCM token chưa (query DB: `SELECT fcm_token FROM users WHERE user_id = ?`)

### Mobile app không nhận được notification

1. Kiểm tra device có phải physical device không (emulator không nhận push)
2. Kiểm tra đã xin quyền notification chưa
3. Kiểm tra FCM token đã được gửi lên backend chưa
4. Test bằng cách gửi test notification từ Firebase Console

### Nhận duplicate notifications

1. Kiểm tra query `hasReminderBeenSent()` đang hoạt động đúng chưa
2. Kiểm tra scheduler có chạy multiple instances không (nếu deploy nhiều server)

## Tài liệu tham khảo

- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Expo Notifications](https://docs.expo.dev/push-notifications/overview/)
- [Spring Events](https://spring.io/guides/gs/event-driven)
- [Spring Scheduling](https://spring.io/guides/gs/scheduling-tasks)

---

**Lưu ý**: File `firebase-service-account.json` chứa thông tin nhạy cảm. Đừng commit lên Git!
Thêm vào `.gitignore`:
```
src/main/resources/firebase-service-account.json
```
