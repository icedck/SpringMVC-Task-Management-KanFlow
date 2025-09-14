package com.codegym.kanflow.controller;

import com.codegym.kanflow.model.Role;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.service.IRoleService;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
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
    public ModelAndView listUsers() {
        ModelAndView modelAndView = new ModelAndView("admin/user-list");
        List<User> users = userService.findAll();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        Map<Long, Boolean> userCanEdit = new HashMap<>();

        for (User user : users) {
            boolean canEdit = currentUsername.equals(user.getUsername()) || !isUserAdmin(user);
            userCanEdit.put(user.getId(), canEdit);
        }

        modelAndView.addObject("users", users);
        modelAndView.addObject("currentUsername", currentUsername);
        modelAndView.addObject("userCanEdit", userCanEdit);

        return modelAndView;
    }

    @GetMapping("/users/edit/{id}")
    public ModelAndView showUserEditForm(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        User userToEdit = userService.findByIdWithRoles(id);
        User currentUser = userService.findByUsername(currentUsername);

        if (isUserAdmin(userToEdit)) {
            if (!userToEdit.getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Admins cannot edit other admins.");
            }
        }

        ModelAndView modelAndView = new ModelAndView("admin/user-edit");
        List<Role> allRoles = roleService.findAll();

        modelAndView.addObject("user", userToEdit);
        modelAndView.addObject("allRoles", allRoles);
        return modelAndView;
    }

    @PostMapping("/users/edit")
    public String updateUser(@Valid @ModelAttribute("user") User userFromForm,
                             BindingResult bindingResult,
                             Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User userToEdit = userService.findByIdWithRoles(userFromForm.getId());
        User currentUser = userService.findByUsername(currentUsername);

        if (isUserAdmin(userToEdit) && !userToEdit.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Admins cannot edit other admins.");
        }

        User existingUserByEmail = userService.findByEmail(userFromForm.getEmail());
        if (existingUserByEmail != null && !existingUserByEmail.getId().equals(userFromForm.getId())) {
            bindingResult.rejectValue("email", "error.user", "Email already exists for another user.");
        }

        if (userFromForm.getRoles() == null || userFromForm.getRoles().isEmpty()) {
            bindingResult.rejectValue("roles", "error.user", "User must have at least one role.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.findAll());
            return "admin/user-edit";
        }

        userService.updateUser(userFromForm);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        User userToDelete = userService.findByIdWithRoles(id);
        User currentUser = userService.findByUsername(currentUsername);

        if (userToDelete.getId().equals(currentUser.getId())) {
            return "redirect:/admin/users?error=self_delete";
        }

        if (isUserAdmin(userToDelete)) {
            throw new AccessDeniedException("Admins cannot delete other admins.");
        }

        userService.deleteById(id);
        return "redirect:/admin/users";
    }
}