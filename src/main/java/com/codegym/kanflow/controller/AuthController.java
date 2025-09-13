package com.codegym.kanflow.controller;

import com.codegym.kanflow.dto.JwtAuthenticationResponse;
import com.codegym.kanflow.dto.LoginRequest;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.security.JwtTokenProvider;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class AuthController {
    @Autowired
    private IUserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    @GetMapping("/login")
    public String showLoginPage() {
        // Kiểm tra nếu user đã đăng nhập thì redirect về boards
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/boards";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public ModelAndView showRegisterPage() {
        ModelAndView modelAndView = new ModelAndView("auth/register");
        modelAndView.addObject("user", new User());
        return modelAndView;
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model) {

        if (userService.findByUsername(user.getUsername()) != null) {
            bindingResult.rejectValue("username", "error.user", "Username already exists!");
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.user", "Email already exists!");
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        userService.save(user);
        return "redirect:/login?register_success";
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> loginApi(@Valid @RequestBody LoginRequest loginRequest, 
                                      HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            // Tạo cookie để lưu JWT token
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Đặt true nếu sử dụng HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge((int) (jwtExpirationInMs / 1000)); // Chuyển từ ms sang seconds
            response.addCookie(jwtCookie);

            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, jwtExpirationInMs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu không đúng");
        }
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<?> logoutApi(HttpServletResponse response) {
        // Xóa JWT cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}