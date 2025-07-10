package com.codegym.kanflow.controller;

import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class AuthController {
    @Autowired
    private IUserService userService;

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
    // Thêm @Valid và BindingResult
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model) {

        // Kiểm tra xem username đã tồn tại chưa
        if (userService.findByUsername(user.getUsername()) != null) {
            // Thêm lỗi vào BindingResult
            bindingResult.rejectValue("username", "error.user", "Username already exists!");
        }
        // Kiểm tra xem email đã tồn tại chưa
        if (userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.user", "Email already exists!");
        }
        // Nếu có lỗi validation (từ annotation hoặc từ code)
        if (bindingResult.hasErrors()) {
            // Không cần thêm attribute lỗi vào model nữa, Thymeleaf sẽ tự lấy từ BindingResult
            return "auth/register"; // Quay lại trang đăng ký
        }
        // Nếu không có lỗi, lưu người dùng mới
        userService.save(user);
        return "redirect:/login?register_success";
    }
}