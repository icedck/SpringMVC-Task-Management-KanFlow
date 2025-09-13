// JWT Authentication Helper
class AuthManager {
    constructor() {
        this.token = null;
        this.init();
    }

    init() {
        // Không cần kiểm tra authentication client-side
        // Spring Security sẽ xử lý redirect tự động
        this.setupAjaxInterceptor();
    }

    getTokenFromCookie() {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'jwt') {
                return value;
            }
        }
        return null;
    }

    getTokenFromStorage() {
        return localStorage.getItem('jwt_token');
    }

    setToken(token) {
        this.token = token;
        // Không cần lưu vào localStorage vì server đã tạo cookie
        // localStorage.setItem('jwt_token', token);
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem('jwt_token');
        // Xóa cookie
        document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }

    setupAjaxInterceptor() {
        const self = this;
        
        // Override jQuery's ajaxSetup để tự động thêm token vào header
        $.ajaxSetup({
            beforeSend: function(xhr, settings) {
                if (self.token && !settings.url.includes('/api/auth/')) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + self.token);
                }
            },
            error: function(xhr, status, error) {
                if (xhr.status === 401) {
                    // Token hết hạn hoặc không hợp lệ
                    self.handleAuthError();
                }
            }
        });
    }

    handleAuthError() {
        this.clearToken();
        alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        window.location.href = '/login';
    }

    async login(username, password) {
        try {
            const response = await $.ajax({
                url: '/api/login',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    username: username,
                    password: password
                })
            });

            if (response.accessToken) {
                this.setToken(response.accessToken);
                // Cập nhật token từ cookie (server đã tạo cookie)
                this.token = this.getTokenFromCookie() || response.accessToken;
                return { success: true, data: response };
            } else {
                return { success: false, message: 'Đăng nhập thất bại' };
            }
        } catch (error) {
            return { 
                success: false, 
                message: error.responseJSON || 'Có lỗi xảy ra khi đăng nhập' 
            };
        }
    }

    async logout() {
        try {
            await $.ajax({
                url: '/api/logout',
                method: 'POST'
            });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            this.clearToken();
            window.location.href = '/login';
        }
    }

    isAuthenticated() {
        // Không cần kiểm tra client-side
        // Spring Security sẽ xử lý authentication
        return true;
    }

    getAuthHeaders() {
        // JWT được gửi qua HttpOnly cookie, không cần header
        return {
            'Content-Type': 'application/json'
        };
    }
}

// Khởi tạo AuthManager global
window.authManager = new AuthManager();

// Hàm helper để gọi API với authentication
function apiCall(url, options = {}) {
    const defaultOptions = {
        headers: window.authManager.getAuthHeaders(),
        ...options
    };
    
    return $.ajax(url, defaultOptions);
}

// Form login được xử lý trong login.html

