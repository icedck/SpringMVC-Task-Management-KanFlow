package com.codegym.kanflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    /**
     * Phương thức này xử lý các request GET đến "/login".
     * Nó chỉ đơn giản là trả về tên của file view để hiển thị.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        // Trả về đường dẫn đến file view: "/WEB-INF/views/auth/login.html"
        return "auth/login";
    }

    // Chúng ta sẽ thêm trang đăng ký "/register" vào đây ở bước sau
}