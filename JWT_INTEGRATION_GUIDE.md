# Hướng dẫn tích hợp JWT vào KanFlow

## Tổng quan
Dự án KanFlow đã được tích hợp JWT (JSON Web Token) authentication để cải thiện bảo mật và trải nghiệm người dùng.

## Các thay đổi chính

### 1. Backend Changes

#### Dependencies mới (pom.xml)
```xml
<!-- JWT Dependencies -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

#### Classes mới
- `JwtTokenProvider`: Xử lý tạo và validate JWT tokens
- `JwtAuthenticationFilter`: Filter để xác thực JWT trong mỗi request
- `JwtAuthenticationResponse`: DTO cho response đăng nhập
- `LoginRequest`: DTO cho request đăng nhập
- `AuthApiController`: REST API cho authentication

#### Cấu hình Security
- Cập nhật `WebSecurityConfig` để hỗ trợ JWT filter
- Thêm endpoint `/api/auth/**` cho phép truy cập không cần xác thực
- JWT filter được thêm vào security filter chain

### 2. Frontend Changes

#### JavaScript mới
- `auth.js`: Quản lý JWT authentication trên frontend
- `AuthManager` class: Xử lý login, logout, token management
- Tự động thêm Authorization header vào tất cả AJAX requests
- Hỗ trợ cả cookie và localStorage để lưu token

#### Cập nhật UI
- Form đăng nhập sử dụng AJAX thay vì form submit
- Thêm nút đăng xuất
- Kiểm tra authentication trước khi load trang

## Cách sử dụng

### 1. Đăng nhập
```javascript
// Sử dụng AuthManager
const result = await window.authManager.login(username, password);
if (result.success) {
    // Đăng nhập thành công
    console.log('Token:', result.data.accessToken);
} else {
    // Đăng nhập thất bại
    console.error('Error:', result.message);
}
```

### 2. Gọi API với authentication
```javascript
// Sử dụng helper function
apiCall('/api/boards', {
    type: 'GET',
    success: function(data) {
        console.log(data);
    }
});

// Hoặc sử dụng jQuery thông thường (tự động thêm header)
$.ajax({
    url: '/api/boards',
    type: 'GET',
    success: function(data) {
        console.log(data);
    }
});
```

### 3. Đăng xuất
```javascript
window.authManager.logout();
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Đăng nhập và nhận JWT token
- `POST /api/auth/logout` - Đăng xuất và xóa token
- `POST /api/auth/validate` - Kiểm tra tính hợp lệ của token

### Response Format
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
}
```

## Cấu hình

### JWT Properties (jwt.properties)
```properties
jwt.secret=mySecretKeyForKanflowApplication2024
jwt.expiration=86400000
```

### Security Configuration
- JWT filter được thêm vào security filter chain
- Hỗ trợ cả Authorization header và cookie
- Tự động xử lý token hết hạn

## Bảo mật

### Token Storage
- Token được lưu trong cookie (HttpOnly) và localStorage
- Cookie được sử dụng cho server-side requests
- localStorage được sử dụng cho client-side requests

### Token Validation
- Mỗi request đều được validate token
- Token hết hạn sẽ tự động redirect về trang login
- Hỗ trợ refresh token (có thể mở rộng)

## Troubleshooting

### Lỗi thường gặp
1. **Token không hợp lệ**: Kiểm tra secret key và token format
2. **CORS issues**: Đảm bảo cấu hình CORS đúng
3. **Cookie không được gửi**: Kiểm tra domain và path settings

### Debug
```javascript
// Kiểm tra token hiện tại
console.log('Current token:', window.authManager.token);

// Kiểm tra authentication status
console.log('Is authenticated:', window.authManager.isAuthenticated());
```

## Mở rộng

### Refresh Token
Có thể thêm refresh token mechanism để tự động gia hạn token:

```javascript
// Trong AuthManager
async refreshToken() {
    // Implementation cho refresh token
}
```

### Role-based Access
JWT token chứa thông tin roles, có thể sử dụng để kiểm tra quyền:

```javascript
// Lấy thông tin user từ token
const userInfo = window.authManager.getUserInfo();
console.log('User roles:', userInfo.roles);
```

## Kết luận

JWT integration đã được hoàn thành với:
- ✅ Backend JWT implementation
- ✅ Frontend authentication management
- ✅ Security configuration
- ✅ API endpoints
- ✅ Error handling
- ✅ Token management

Dự án giờ đây hỗ trợ cả form-based authentication (cho web UI) và JWT authentication (cho API và SPA).

