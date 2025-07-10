package com.codegym.kanflow.controller;

import com.codegym.kanflow.model.Role;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IRoleService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin") // Tất cả các URL trong đây sẽ bắt đầu bằng /admin
public class AdminController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;

    @GetMapping("/users")
    public ModelAndView listUsers() {
        ModelAndView modelAndView = new ModelAndView("admin/user-list");
        List<User> users = userService.findAll(); // Cần thêm phương thức này vào service
        modelAndView.addObject("users", users);
        return modelAndView;
    }

    // HIỂN THỊ FORM SỬA USER
    @GetMapping("/users/edit/{id}")
    public ModelAndView showUserEditForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("admin/user-edit");
        User user = userService.findByIdWithRoles(id); // Cần phương thức mới để tránh LAZY
        List<Role> allRoles = roleService.findAll(); // Cần phương thức này

        modelAndView.addObject("user", user);
        modelAndView.addObject("allRoles", allRoles);
        return modelAndView;
    }

    // XỬ LÝ VIỆC SỬA USER
    @PostMapping("/users/edit")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             Model model) {

        // --- Logic kiểm tra email trùng lặp ---
        User existingUserByEmail = userService.findByEmail(user.getEmail());
        if (existingUserByEmail != null && !existingUserByEmail.getId().equals(user.getId())) {
            // Tìm thấy một user khác có cùng email
            // Gắn lỗi vào trường 'email' của đối tượng binding
            bindingResult.rejectValue("email", "error.user", "Email already exists for another user.");
        }

        // --- Kiểm tra kết quả validation ---
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi, quay trở lại form edit
            // Cần thêm lại danh sách allRoles vào model để trang edit có thể render lại các checkbox
            model.addAttribute("allRoles", roleService.findAll());
            return "admin/user-edit";
        }

        // Nếu không có lỗi, tiến hành cập nhật
        userService.updateUser(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, Principal principal) {
        // Không cho admin tự xóa mình
        User userToDelete = userService.findById(id);
        if (userToDelete != null && !userToDelete.getUsername().equals(principal.getName())) {
            userService.deleteById(id);
        }
        return "redirect:/admin/users";
    }
}