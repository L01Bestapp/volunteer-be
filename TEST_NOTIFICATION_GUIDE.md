# Hướng dẫn Test Notification

Hướng dẫn này giúp bạn gửi notification giả để test app trong quá trình phát triển.

## 🎯 API Endpoint

```
POST /api/v1/notifications/test
Authorization: Bearer {access_token}
Content-Type: application/json
```

## 📝 Request Body

```json
{
  "title": "Test Notification",
  "body": "Đây là nội dung test notification",
  "type": "REMINDER",
  "activityId": "123",
  "customData": "any custom string"
}
```

### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `title` | String | ✅ Yes | Tiêu đề notification |
| `body` | String | ✅ Yes | Nội dung chi tiết |
| `type` | String | ❌ No | Loại notification: `REMINDER`, `ATTENDANCE_COMPLETED`, `ENROLLMENT_APPROVED`, `GENERAL`, etc. Default: `GENERAL` |
| `activityId` | String | ❌ No | Activity ID để test navigation |
| `customData` | String | ❌ No | Dữ liệu tùy chỉnh |

## 🔧 Cách sử dụng

### Option 1: Dùng cURL

```bash
# 1. Login để lấy access token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@hcmut.edu.vn",
    "password": "your_password"
  }'

# Response sẽ có accessToken
# {
#   "data": {
#     "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
#     "refreshToken": "...",
#     "role": "STUDENT"
#   }
# }

# 2. Gửi test notification
curl -X POST http://localhost:8080/api/v1/notifications/test \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "🔔 Test Notification",
    "body": "Đây là notification test. Bạn sẽ thấy popup nổi lên!",
    "type": "REMINDER",
    "activityId": "3"
  }'
```

### Option 2: Dùng Postman

1. **Tạo request mới**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/notifications/test`

2. **Thêm Authorization**
   - Tab: **Authorization**
   - Type: **Bearer Token**
   - Token: Paste access token của bạn

3. **Thêm Body**
   - Tab: **Body**
   - Type: **raw** → **JSON**
   - Content:
   ```json
   {
     "title": "Test Notification",
     "body": "Hello from Postman!",
     "type": "REMINDER"
   }
   ```

4. **Send** → Kiểm tra app trên điện thoại

### Option 3: Dùng Thunder Client (VS Code Extension)

1. Install Thunder Client extension
2. New Request → POST
3. URL: `http://localhost:8080/api/v1/notifications/test`
4. Headers:
   ```
   Authorization: Bearer YOUR_TOKEN
   Content-Type: application/json
   ```
5. Body:
   ```json
   {
     "title": "⚡ Thunder Test",
     "body": "Testing from VS Code"
   }
   ```

### Option 4: Dùng Swagger UI

1. Mở trình duyệt: `http://localhost:8080/swagger-ui.html`
2. Tìm endpoint **POST /api/v1/notifications/test**
3. Click **Try it out**
4. Click **Authorize** → Nhập Bearer token
5. Điền request body → **Execute**

## 📱 Các Test Case Nên Thử

### Test 1: Basic Notification
```json
{
  "title": "Test Basic",
  "body": "Notification đơn giản không có data"
}
```

### Test 2: Reminder Notification
```json
{
  "title": "Nhắc nhở: Hoạt động sắp bắt đầu",
  "body": "Hoạt động 'Dọn rác công viên' sẽ bắt đầu sau 1 giờ nữa",
  "type": "REMINDER",
  "activityId": "123"
}
```

### Test 3: Attendance Completed
```json
{
  "title": "Hoàn thành hoạt động",
  "body": "Bạn đã hoàn thành hoạt động 'Dọn rác'. Được cộng 1.5 ngày CTXH",
  "type": "ATTENDANCE_COMPLETED",
  "activityId": "456",
  "customData": "ctxhDays: 1.5"
}
```

### Test 4: Enrollment Approved
```json
{
  "title": "Đơn đăng ký được duyệt",
  "body": "Đơn đăng ký của bạn cho hoạt động 'Hiến máu' đã được chấp thuận",
  "type": "ENROLLMENT_APPROVED",
  "activityId": "789"
}
```

### Test 5: Long Text Notification
```json
{
  "title": "Thông báo quan trọng",
  "body": "Đây là một thông báo rất dài để test việc hiển thị nội dung. Chúng ta cần đảm bảo rằng notification có thể hiển thị được nhiều dòng text và không bị cắt nửa chừng. Hãy kiểm tra xem notification có hiển thị đầy đủ không nhé!",
  "type": "GENERAL"
}
```

### Test 6: Emoji Test
```json
{
  "title": "🎉 Chúc mừng! 🎊",
  "body": "Bạn đã tích lũy đủ 10 ngày CTXH! 🏆 Tiếp tục phát huy nhé! 💪",
  "type": "GENERAL"
}
```

## 🧪 Test Scenarios

### Scenario 1: App Foreground (Đang mở)
1. Mở app trên điện thoại
2. Gửi test notification từ Postman/curl
3. **Kỳ vọng**: Banner popup nổi lên ở đầu màn hình

### Scenario 2: App Background (Minimize)
1. Minimize app (nhấn Home button)
2. Gửi test notification
3. **Kỳ vọng**: Popup heads-up hiện lên trên màn hình

### Scenario 3: App Killed (Tắt hẳn)
1. Force quit app (swipe ra khỏi recent apps)
2. Gửi test notification
3. **Kỳ vọng**: Popup heads-up hiện lên
4. Nhấn vào notification → App mở và navigate đến màn hình tương ứng

### Scenario 4: Multiple Notifications
1. Gửi 3-5 notifications liên tiếp
2. **Kỳ vọng**: Mỗi notification hiện lên riêng biệt
3. Badge count tăng lên

### Scenario 5: Navigation Test
1. Gửi notification với `activityId: "123"`
2. Nhấn vào notification
3. **Kỳ vọng**: App navigate đến ActivityDetail screen với ID = 123

## 🔍 Debugging

### Nếu không nhận được notification:

1. **Kiểm tra FCM Token đã được gửi lên backend chưa**
   ```sql
   SELECT user_id, email, fcm_token
   FROM users
   WHERE user_id = YOUR_USER_ID;
   ```
   → fcm_token phải khác NULL

2. **Kiểm tra log Backend**
   ```
   Tìm log: "Successfully sent FCM message with Android config. Message ID: xxx"
   ```

3. **Kiểm tra notification đã lưu vào DB chưa**
   ```sql
   SELECT * FROM notifications
   WHERE user_id = YOUR_USER_ID
   ORDER BY create_at DESC
   LIMIT 5;
   ```
   → Kiểm tra field `is_sent` = true

4. **Kiểm tra app có xin quyền notification chưa**
   - Android: Settings → Apps → Your App → Notifications → Enabled

5. **Kiểm tra Firebase Config**
   - File `firebase-service-account.json` phải nằm trong `src/main/resources/`
   - Backend log phải có: "Firebase Admin SDK initialized successfully"

6. **Kiểm tra device**
   - Phải là thiết bị thật (không phải emulator)
   - Có kết nối internet

## 📊 Response Examples

### Success Response
```json
{
  "timestamp": "2026-01-12T10:30:00",
  "status": 200,
  "message": "Test notification sent successfully",
  "data": null
}
```

### Error Response (Not authenticated)
```json
{
  "timestamp": "2026-01-12T10:30:00",
  "status": 401,
  "message": "Unauthorized",
  "error": "Full authentication is required to access this resource"
}
```

### Error Response (Invalid request)
```json
{
  "timestamp": "2026-01-12T10:30:00",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "body": "Body is required"
  }
}
```

## 💡 Tips

1. **Để test nhanh**, lưu các request vào Postman Collection hoặc tạo script bash
2. **Test trên nhiều thiết bị** để đảm bảo notification hoạt động đồng nhất
3. **Test với các loại notification khác nhau** để đảm bảo navigation đúng
4. **Test trong các điều kiện mạng khác nhau** (WiFi, 4G, mạng yếu)
5. **Monitor Backend logs** để debug nếu có vấn đề

## 🚀 Quick Test Script

Tạo file `test-notification.sh`:

```bash
#!/bin/bash

# Cấu hình
BASE_URL="http://localhost:8080"
EMAIL="student@hcmut.edu.vn"
PASSWORD="your_password"

# Login
echo "Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')

if [ "$ACCESS_TOKEN" == "null" ]; then
  echo "Login failed!"
  exit 1
fi

echo "Login successful!"
echo "Sending test notification..."

# Gửi notification
curl -X POST "$BASE_URL/api/v1/notifications/test" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "🚀 Auto Test Notification",
    "body": "This notification was sent by automated script",
    "type": "REMINDER",
    "activityId": "123"
  }'

echo "\nDone! Check your phone."
```

Chạy script:
```bash
chmod +x test-notification.sh
./test-notification.sh
```

---

**Lưu ý**: API này chỉ nên dùng cho **development/testing**. Trên production, nên thêm role check hoặc disable endpoint này.
