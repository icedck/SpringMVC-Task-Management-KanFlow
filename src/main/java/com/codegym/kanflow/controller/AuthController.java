package com.codegym.kanflow.controller;

import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    /**
     * Phương thức này xử lý các request GET đến "/login".
     * Nó chỉ đơn giản là trả về tên của file view để hiển thị.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        // Trả về đường dẫn đến file view: "/WEB-INF/views/auth/login.html"
        return "auth/login";
    }

    @GetMapping("/register")
    public ModelAndView showRegisterPage() {
        ModelAndView modelAndView = new ModelAndView("auth/register");
        modelAndView.addObject("user", new User());
        return modelAndView;
    }

    // PHƯƠNG THỨC XỬ LÝ VIỆC ĐĂNG KÝ (MỚI)
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        // Kiểm tra xem username đã tồn tại chưa
        if (userService.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists!");
            return "auth/register"; // Quay lại trang đăng ký với thông báo lỗi
        }

        // Nếu username chưa tồn tại, lưu người dùng mới
        userService.save(user);

        // Chuyển hướng đến trang đăng nhập với thông báo thành công
        return "redirect:/login?register_success";
    }
}