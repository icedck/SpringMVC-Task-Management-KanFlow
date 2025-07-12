package com.codegym.kanflow.controller;

import com.codegym.kanflow.model.Role;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IRoleService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;

    private boolean isUserAdmin(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }

        for (Role role : user.getRoles()) {
            if ("ROLE_ADMIN".equals(role.getName())) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/users")
    public ModelAndView listUsers(Principal principal) {
        ModelAndView modelAndView = new ModelAndView("admin/user-list");
        List<User> users = userService.findAll();
        String currentUsername = principal.getName();

        // === THÊM LOGIC HELPER ĐỂ KIỂM TRA QUYỀN EDIT/DELETE ===
        Map<Long, Boolean> userCanEdit = new HashMap<>();

        for (User user : users) {
            // Logic: Có thể edit nếu:
            // 1. Là chính user đó (tự edit mình)
            // 2. HOẶC user đó không phải là ADMIN
            boolean canEdit = currentUsername.equals(user.getUsername()) || !isUserAdmin(user);
            userCanEdit.put(user.getId(), canEdit);
        }

        modelAndView.addObject("users", users);
        modelAndView.addObject("currentUsername", currentUsername);
        modelAndView.addObject("userCanEdit", userCanEdit); // Thêm map này

        return modelAndView;
    }

    // HIỂN THỊ FORM SỬA USER
    @GetMapping("/users/edit/{id}")
    public ModelAndView showUserEditForm(@PathVariable Long id, Principal principal) {
        User userToEdit = userService.findByIdWithRoles(id);
        User currentUser = userService.findByUsername(principal.getName());

        // --- LOGIC KIỂM TRA QUYỀN MỚI ---
        // 1. Nếu người cần sửa là một ADMIN
        if (isUserAdmin(userToEdit)) {
            // 2. Và người đang đăng nhập không phải là chính người đó
            if (!userToEdit.getId().equals(currentUser.getId())) {
                // 3. -> Cấm truy cập
                throw new AccessDeniedException("Admins cannot edit other admins.");
            }
        }
        // Nếu không vi phạm, tiếp tục bình thường

        ModelAndView modelAndView = new ModelAndView("admin/user-edit");
        List<Role> allRoles = roleService.findAll();

        modelAndView.addObject("user", userToEdit);
        modelAndView.addObject("allRoles", allRoles);
        return modelAndView;
    }

    // XỬ LÝ VIỆC SỬA USER
    @PostMapping("/users/edit")
    public String updateUser(@Valid @ModelAttribute("user") User userFromForm,
                             BindingResult bindingResult,
                             Model model, Principal principal) {

        User userToEdit = userService.findByIdWithRoles(userFromForm.getId());
        User currentUser = userService.findByUsername(principal.getName());

        // --- LOGIC KIỂM TRA QUYỀN MỚI ---
        if (isUserAdmin(userToEdit) && !userToEdit.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Admins cannot edit other admins.");
        }

        // --- Logic kiểm tra email trùng lặp ---
        User existingUserByEmail = userService.findByEmail(userFromForm.getEmail());
        if (existingUserByEmail != null && !existingUserByEmail.getId().equals(userFromForm.getId())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists for another user.");
        }

        if (userFromForm.getRoles() == null || userFromForm.getRoles().isEmpty()) {
            bindingResult.rejectValue("roles", "error.user", "User must have at least one role.");
        }

        // --- Kiểm tra kết quả validation ---
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.findAll());
            return "admin/user-edit";
        }

        userService.updateUser(userFromForm);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, Principal principal) {
        User userToDelete = userService.findByIdWithRoles(id);
        User currentUser = userService.findByUsername(principal.getName());

        // Không cho admin tự xóa mình
        if (userToDelete.getId().equals(currentUser.getId())) {
            // Có thể thêm một message ở đây, ví dụ dùng RedirectAttributes
            return "redirect:/admin/users?error=self_delete";
        }

        // --- LOGIC KIỂM TRA QUYỀN MỚI ---
        // Không cho admin xóa một admin khác
        if (isUserAdmin(userToDelete)) {
            throw new AccessDeniedException("Admins cannot delete other admins.");
        }

        userService.deleteById(id);
        return "redirect:/admin/users";
    }
}