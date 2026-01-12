# Hướng dẫn đầy đủ Push Notification với Expo React Native

Tôi sẽ hướng dẫn chi tiết từng bước để triển khai hệ thống thông báo hoàn chỉnh.

## Tổng quan luồng hoạt động

```
┌─────────────────────────────────────────────────────────────────┐
│                        APP LIFECYCLE                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. APP KHỞI ĐỘNG (App.tsx)                                     │
│     ├── Cấu hình notification handler                           │
│     ├── Xin quyền notification                                  │
│     ├── Lắng nghe notification (foreground + response)          │
│     └── Kiểm tra notification đã mở app (killed state)          │
│                                                                  │
│  2. USER ĐĂNG NHẬP                                              │
│     ├── Lấy Expo Push Token                                     │
│     └── Gửi token lên backend                                   │
│                                                                  │
│  3. NHẬN NOTIFICATION                                           │
│     ├── Foreground: App đang mở → hiển thị banner               │
│     ├── Background: App ở background → hiển thị notification    │
│     └── Killed: App đã tắt → hiển thị notification              │
│                                                                  │
│  4. USER ĐĂNG XUẤT                                              │
│     └── Xóa FCM token trên backend                              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Bước 1: Cài đặt dependencies

```bash
npx expo install expo-notifications expo-device expo-constants
```

## Bước 2: Cấu hình app.json

```json
{
  "expo": {
    "name": "Your App Name",
    "slug": "your-app-slug",
    "plugins": [
      [
        "expo-notifications",
        {
          "icon": "./assets/notification-icon.png",
          "color": "#4CAF50",
          "sounds": ["./assets/sounds/notification.wav"]
        }
      ]
    ],
    "android": {
      "googleServicesFile": "./google-services.json",
      "package": "com.yourcompany.yourapp"
    },
    "ios": {
      "googleServicesFile": "./GoogleService-Info.plist",
      "bundleIdentifier": "com.yourcompany.yourapp"
    }
  }
}
```

## Bước 3: Tạo Notification Service

```typescript
// src/services/notificationService.ts
import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import Constants from 'expo-constants';
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { apiClient } from './apiClient'; // axios instance của bạn

// Keys để lưu trữ
const FCM_TOKEN_KEY = '@fcm_token';
const FCM_TOKEN_SENT_KEY = '@fcm_token_sent';

// ============================================
// 1. CẤU HÌNH NOTIFICATION HANDLER
// ============================================
// Gọi 1 lần ở ngoài component, ngay khi app load
export function configureNotificationHandler() {
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldShowAlert: true,    // Hiển thị alert khi app đang mở
      shouldPlaySound: true,    // Phát âm thanh
      shouldSetBadge: true,     // Cập nhật badge number
    }),
  });
}

// ============================================
// 2. XIN QUYỀN NOTIFICATION
// ============================================
export async function requestNotificationPermission(): Promise<boolean> {
  // Chỉ hoạt động trên thiết bị thật
  if (!Device.isDevice) {
    console.log('⚠️ Push notifications chỉ hoạt động trên thiết bị thật');
    return false;
  }

  // Kiểm tra quyền hiện tại
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  
  let finalStatus = existingStatus;

  // Nếu chưa có quyền, xin quyền
  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }

  if (finalStatus !== 'granted') {
    console.log('❌ User từ chối quyền notification');
    return false;
  }

  // Cấu hình notification channel cho Android
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: 'Thông báo mặc định',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#4CAF50',
      sound: 'default',
      enableVibrate: true,
      showBadge: true,
    });

    // Channel riêng cho reminder
    await Notifications.setNotificationChannelAsync('reminder', {
      name: 'Nhắc nhở hoạt động',
      importance: Notifications.AndroidImportance.HIGH,
      vibrationPattern: [0, 500, 250, 500],
      lightColor: '#FF9800',
      sound: 'default',
    });
  }

  console.log('✅ Notification permission granted');
  return true;
}

// ============================================
// 3. LẤY EXPO PUSH TOKEN
// ============================================
export async function getExpoPushToken(): Promise<string | null> {
  try {
    // Lấy project ID từ Constants
    const projectId = Constants.expoConfig?.extra?.eas?.projectId 
      ?? Constants.easConfig?.projectId;

    if (!projectId) {
      console.error('❌ Không tìm thấy projectId');
      return null;
    }

    const tokenData = await Notifications.getExpoPushTokenAsync({
      projectId,
    });

    const token = tokenData.data;
    console.log('📱 Expo Push Token:', token);

    // Lưu token vào AsyncStorage
    await AsyncStorage.setItem(FCM_TOKEN_KEY, token);

    return token;
  } catch (error) {
    console.error('❌ Lỗi khi lấy Expo Push Token:', error);
    return null;
  }
}

// ============================================
// 4. GỬI TOKEN LÊN BACKEND
// ============================================
export async function sendTokenToBackend(authToken: string): Promise<boolean> {
  try {
    // Lấy token đã lưu
    const fcmToken = await AsyncStorage.getItem(FCM_TOKEN_KEY);
    
    if (!fcmToken) {
      console.log('⚠️ Không có FCM token để gửi');
      return false;
    }

    // Kiểm tra xem token này đã được gửi chưa
    const sentToken = await AsyncStorage.getItem(FCM_TOKEN_SENT_KEY);
    if (sentToken === fcmToken) {
      console.log('ℹ️ Token đã được gửi trước đó');
      return true;
    }

    // Gửi lên backend
    await apiClient.put(
      '/api/v1/notifications/fcm-token',
      { fcmToken },
      {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      }
    );

    // Đánh dấu đã gửi thành công
    await AsyncStorage.setItem(FCM_TOKEN_SENT_KEY, fcmToken);
    console.log('✅ Đã gửi FCM token lên backend');
    return true;
  } catch (error) {
    console.error('❌ Lỗi khi gửi FCM token:', error);
    return false;
  }
}

// ============================================
// 5. XÓA TOKEN KHI ĐĂNG XUẤT
// ============================================
export async function removeTokenFromBackend(authToken: string): Promise<void> {
  try {
    await apiClient.delete('/api/v1/notifications/fcm-token', {
      headers: {
        Authorization: `Bearer ${authToken}`,
      },
    });

    // Xóa trạng thái đã gửi
    await AsyncStorage.removeItem(FCM_TOKEN_SENT_KEY);
    console.log('✅ Đã xóa FCM token khỏi backend');
  } catch (error) {
    console.error('❌ Lỗi khi xóa FCM token:', error);
  }
}

// ============================================
// 6. LẤY NOTIFICATION ĐÃ MỞ APP (KILLED STATE)
// ============================================
export async function getLastNotificationResponse(): Promise<Notifications.NotificationResponse | null> {
  return await Notifications.getLastNotificationResponseAsync();
}

// ============================================
// 7. LẤY BADGE COUNT
// ============================================
export async function getBadgeCount(): Promise<number> {
  return await Notifications.getBadgeCountAsync();
}

export async function setBadgeCount(count: number): Promise<void> {
  await Notifications.setBadgeCountAsync(count);
}

// ============================================
// 8. HỦY TẤT CẢ NOTIFICATIONS
// ============================================
export async function dismissAllNotifications(): Promise<void> {
  await Notifications.dismissAllNotificationsAsync();
}
```

## Bước 4: Tạo Hook quản lý Notifications

```typescript
// src/hooks/useNotifications.ts
import { useEffect, useRef, useCallback } from 'react';
import * as Notifications from 'expo-notifications';
import { useNavigation } from '@react-navigation/native';
import {
  configureNotificationHandler,
  requestNotificationPermission,
  getExpoPushToken,
  sendTokenToBackend,
  getLastNotificationResponse,
  setBadgeCount,
} from '../services/notificationService';
import { useAuth } from '../contexts/AuthContext'; // Context auth của bạn

// Types cho notification data từ backend
interface NotificationData {
  type: 'REMINDER' | 'ATTENDANCE_COMPLETED' | 'ENROLLMENT_APPROVED' | 'GENERAL';
  activityId?: string;
  enrollmentId?: string;
  [key: string]: any;
}

export function useNotifications() {
  const navigation = useNavigation<any>();
  const { authToken, isAuthenticated } = useAuth();
  
  // Refs để lưu subscription
  const notificationListener = useRef<Notifications.Subscription>();
  const responseListener = useRef<Notifications.Subscription>();

  // ============================================
  // XỬ LÝ KHI NHẬN NOTIFICATION (APP ĐANG MỞ)
  // ============================================
  const handleNotificationReceived = useCallback(
    (notification: Notifications.Notification) => {
      console.log('📬 Nhận notification (foreground):', notification);
      
      const data = notification.request.content.data as NotificationData;
      
      // Có thể hiển thị in-app notification hoặc update UI
      // Ví dụ: update badge count, refresh danh sách thông báo
    },
    []
  );

  // ============================================
  // XỬ LÝ KHI USER BẤM VÀO NOTIFICATION
  // ============================================
  const handleNotificationResponse = useCallback(
    (response: Notifications.NotificationResponse) => {
      console.log('👆 User bấm notification:', response);
      
      const data = response.notification.request.content.data as NotificationData;
      
      // Navigate dựa vào loại notification
      navigateFromNotification(data);
    },
    [navigation]
  );

  // ============================================
  // NAVIGATE DỰA VÀO NOTIFICATION DATA
  // ============================================
  const navigateFromNotification = useCallback(
    (data: NotificationData) => {
      if (!data) return;

      switch (data.type) {
        case 'REMINDER':
        case 'ATTENDANCE_COMPLETED':
          if (data.activityId) {
            navigation.navigate('ActivityDetail', { 
              activityId: data.activityId 
            });
          }
          break;
          
        case 'ENROLLMENT_APPROVED':
          if (data.enrollmentId) {
            navigation.navigate('EnrollmentDetail', { 
              enrollmentId: data.enrollmentId 
            });
          }
          break;
          
        case 'GENERAL':
        default:
          navigation.navigate('Notifications');
          break;
      }
    },
    [navigation]
  );

  // ============================================
  // KHỞI TẠO KHI APP MOUNT
  // ============================================
  useEffect(() => {
    // Cấu hình handler (chỉ cần 1 lần)
    configureNotificationHandler();

    // Xin quyền notification
    requestNotificationPermission();

    // Lắng nghe notification khi app đang mở (foreground)
    notificationListener.current = Notifications.addNotificationReceivedListener(
      handleNotificationReceived
    );

    // Lắng nghe khi user bấm vào notification
    responseListener.current = Notifications.addNotificationResponseReceivedListener(
      handleNotificationResponse
    );

    // Kiểm tra xem app có được mở từ notification không (killed state)
    getLastNotificationResponse().then((response) => {
      if (response) {
        console.log('🚀 App được mở từ notification:', response);
        const data = response.notification.request.content.data as NotificationData;
        
        // Delay một chút để navigation sẵn sàng
        setTimeout(() => {
          navigateFromNotification(data);
        }, 1000);
      }
    });

    // Cleanup
    return () => {
      if (notificationListener.current) {
        Notifications.removeNotificationSubscription(notificationListener.current);
      }
      if (responseListener.current) {
        Notifications.removeNotificationSubscription(responseListener.current);
      }
    };
  }, []);

  // ============================================
  // ĐĂNG KÝ TOKEN KHI ĐÃ ĐĂNG NHẬP
  // ============================================
  useEffect(() => {
    if (isAuthenticated && authToken) {
      registerPushToken();
    }
  }, [isAuthenticated, authToken]);

  const registerPushToken = async () => {
    // Lấy token
    const token = await getExpoPushToken();
    
    if (token && authToken) {
      // Gửi lên backend
      await sendTokenToBackend(authToken);
    }
  };

  return {
    registerPushToken,
    setBadgeCount,
  };
}
```

## Bước 5: Tích hợp vào App.tsx

```typescript
// App.tsx
import React, { useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { AuthProvider } from './src/contexts/AuthContext';
import { configureNotificationHandler } from './src/services/notificationService';
import { useNotifications } from './src/hooks/useNotifications';
import RootNavigator from './src/navigation/RootNavigator';

// ⚠️ QUAN TRỌNG: Gọi NGOÀI component, chạy ngay khi app load
configureNotificationHandler();

function AppContent() {
  // Hook này sẽ xử lý mọi thứ về notification
  useNotifications();

  return <RootNavigator />;
}

export default function App() {
  return (
    <AuthProvider>
      <NavigationContainer>
        <AppContent />
      </NavigationContainer>
    </AuthProvider>
  );
}
```

## Bước 6: Xử lý trong màn hình Login

```typescript
// src/screens/LoginScreen.tsx
import React, { useState } from 'react';
import { View, TextInput, Button, Alert } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { 
  getExpoPushToken, 
  sendTokenToBackend 
} from '../services/notificationService';

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();

  const handleLogin = async () => {
    try {
      // 1. Đăng nhập
      const authToken = await login(email, password);
      
      // 2. Lấy và gửi FCM token lên backend
      const fcmToken = await getExpoPushToken();
      if (fcmToken) {
        await sendTokenToBackend(authToken);
      }
      
      // Navigate đến Home
    } catch (error) {
      Alert.alert('Lỗi', 'Đăng nhập thất bại');
    }
  };

  return (
    <View>
      <TextInput
        placeholder="Email"
        value={email}
        onChangeText={setEmail}
      />
      <TextInput
        placeholder="Mật khẩu"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />
      <Button title="Đăng nhập" onPress={handleLogin} />
    </View>
  );
}
```

## Bước 7: Xử lý khi đăng xuất

```typescript
// src/screens/ProfileScreen.tsx hoặc trong AuthContext
import { removeTokenFromBackend, setBadgeCount } from '../services/notificationService';

const handleLogout = async () => {
  try {
    // 1. Xóa FCM token trên backend (quan trọng!)
    await removeTokenFromBackend(authToken);
    
    // 2. Reset badge count
    await setBadgeCount(0);
    
    // 3. Đăng xuất
    await logout();
  } catch (error) {
    console.error('Lỗi khi đăng xuất:', error);
  }
};
```

## Bước 8: Màn hình Notifications

```typescript
// src/screens/NotificationsScreen.tsx
import React, { useEffect, useState, useCallback } from 'react';
import { 
  View, 
  FlatList, 
  Text, 
  TouchableOpacity, 
  RefreshControl,
  StyleSheet 
} from 'react-native';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { useAuth } from '../contexts/AuthContext';
import { setBadgeCount } from '../services/notificationService';
import { apiClient } from '../services/apiClient';

interface Notification {
  notificationId: number;
  title: string;
  body: string;
  type: string;
  data: any;
  isRead: boolean;
  createAt: string;
}

export default function NotificationsScreen() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [refreshing, setRefreshing] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const { authToken } = useAuth();
  const navigation = useNavigation<any>();

  // Fetch notifications khi màn hình focus
  useFocusEffect(
    useCallback(() => {
      fetchNotifications();
      fetchUnreadCount();
    }, [])
  );

  const fetchNotifications = async () => {
    try {
      const response = await apiClient.get('/api/v1/notifications', {
        headers: { Authorization: `Bearer ${authToken}` },
      });
      setNotifications(response.data);
    } catch (error) {
      console.error('Lỗi khi lấy notifications:', error);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const response = await apiClient.get('/api/v1/notifications/unread-count', {
        headers: { Authorization: `Bearer ${authToken}` },
      });
      const count = response.data.count;
      setUnreadCount(count);
      
      // Cập nhật badge trên app icon
      await setBadgeCount(count);
    } catch (error) {
      console.error('Lỗi khi lấy unread count:', error);
    }
  };

  const markAsRead = async (notificationId: number) => {
    try {
      await apiClient.put(
        `/api/v1/notifications/${notificationId}/read`,
        {},
        { headers: { Authorization: `Bearer ${authToken}` } }
      );
      
      // Cập nhật local state
      setNotifications(prev =>
        prev.map(n =>
          n.notificationId === notificationId ? { ...n, isRead: true } : n
        )
      );
      
      // Cập nhật unread count
      fetchUnreadCount();
    } catch (error) {
      console.error('Lỗi khi đánh dấu đã đọc:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      await apiClient.put(
        '/api/v1/notifications/read-all',
        {},
        { headers: { Authorization: `Bearer ${authToken}` } }
      );
      
      // Cập nhật local state
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
      await setBadgeCount(0);
    } catch (error) {
      console.error('Lỗi khi đánh dấu tất cả đã đọc:', error);
    }
  };

  const handleNotificationPress = (notification: Notification) => {
    // Đánh dấu đã đọc
    if (!notification.isRead) {
      markAsRead(notification.notificationId);
    }

    // Navigate dựa vào loại notification
    const data = notification.data;
    if (data?.activityId) {
      navigation.navigate('ActivityDetail', { activityId: data.activityId });
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await fetchNotifications();
    await fetchUnreadCount();
    setRefreshing(false);
  };

  const renderNotification = ({ item }: { item: Notification }) => (
    <TouchableOpacity
      style={[
        styles.notificationItem,
        !item.isRead && styles.unread,
      ]}
      onPress={() => handleNotificationPress(item)}
    >
      <View style={styles.notificationContent}>
        <Text style={styles.title}>{item.title}</Text>
        <Text style={styles.body}>{item.body}</Text>
        <Text style={styles.time}>
          {new Date(item.createAt).toLocaleString('vi-VN')}
        </Text>
      </View>
      {!item.isRead && <View style={styles.unreadDot} />}
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {unreadCount > 0 && (
        <TouchableOpacity style={styles.markAllButton} onPress={markAllAsRead}>
          <Text style={styles.markAllText}>
            Đánh dấu tất cả đã đọc ({unreadCount})
          </Text>
        </TouchableOpacity>
      )}
      
      <FlatList
        data={notifications}
        keyExtractor={(item) => item.notificationId.toString()}
        renderItem={renderNotification}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        ListEmptyComponent={
          <Text style={styles.emptyText}>Không có thông báo</Text>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  markAllButton: {
    padding: 12,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  markAllText: {
    color: '#4CAF50',
    textAlign: 'center',
    fontWeight: '600',
  },
  notificationItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  unread: {
    backgroundColor: '#E8F5E9',
  },
  notificationContent: {
    flex: 1,
  },
  title: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 4,
  },
  body: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  time: {
    fontSize: 12,
    color: '#999',
  },
  unreadDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#4CAF50',
    marginLeft: 8,
  },
  emptyText: {
    textAlign: 'center',
    padding: 32,
    color: '#999',
  },
});
```

## Tóm tắt: Khi nào làm gì?

| Thời điểm | Hành động |
|-----------|-----------|
| **App khởi động** | `configureNotificationHandler()` + xin quyền + setup listeners |
| **User đăng nhập** | Lấy token → Gửi lên backend |
| **App đang mở (foreground)** | `addNotificationReceivedListener` xử lý |
| **App ở background** | OS hiển thị notification tự động |
| **App đã tắt (killed)** | OS hiển thị notification + `getLastNotificationResponseAsync()` khi mở lại |
| **User bấm notification** | `addNotificationResponseReceivedListener` xử lý |
| **User đăng xuất** | Xóa token trên backend |

## Lưu ý quan trọng

1. **Physical device**: Push notification chỉ hoạt động trên thiết bị thật, không hoạt động trên emulator/simulator.

2. **EAS Build**: Để test push notification, bạn cần build app bằng EAS:
   ```bash
   npx eas build --profile development --platform android
   ```

3. **Backend cần gửi đúng format**:
   ```json
   {
     "to": "ExponentPushToken[xxx]",
     "title": "Nhắc nhở",
     "body": "Hoạt động sẽ bắt đầu sau 1 giờ",
     "data": {
       "type": "REMINDER",
       "activityId": "123"
     }
   }
   ```

4. **Background notifications**: Khi app ở background hoặc đã tắt, OS sẽ tự động hiển thị notification. Bạn không cần làm gì thêm. Chỉ cần xử lý khi user bấm vào notification.

Bạn cần tôi giải thích thêm phần nào không?