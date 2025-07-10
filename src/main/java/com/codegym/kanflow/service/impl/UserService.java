package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Board;
import com.codegym.kanflow.model.Card;
import com.codegym.kanflow.model.Role;
import com.codegym.kanflow.model.User;
import com.codegym.kanflow.repository.RoleRepository;
import com.codegym.kanflow.repository.UserRepository;
import com.codegym.kanflow.service.ICardService;
import com.codegym.kanflow.service.IUserService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    //Chúng ta đã tiêm PasswordEncoder vào UserService và sử dụng nó để mã hóa
// mật khẩu của người dùng ngay trước khi lưu vào CSDL.
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ICardService cardService;


    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void save(User user) { // <-- THÊM PHƯƠNG THỨC NÀY
        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Tự động gán vai trò ROLE_USER cho người dùng mới
        Role userRole = roleRepository.findByName("ROLE_USER");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    public User findByIdWithRoles(Long id) {
        return userRepository.findByIdWithRoles(id).orElse(null);
    }

    @Override
    @Transactional // Quan trọng
    public void updateUser(User userFromForm) {
        // Lấy user gốc từ DB
        User existingUser = findByIdWithRoles(userFromForm.getId());
        if (existingUser != null) {
            // Cập nhật các trường cho phép
            existingUser.setEmail(userFromForm.getEmail());
            // Cập nhật danh sách vai trò
            existingUser.setRoles(userFromForm.getRoles());
            userRepository.save(existingUser);
        }
    }

    @Override
    @Transactional // Transaction là bắt buộc!
    public void deleteById(Long id) {
        User userToDelete = this.findById(id); // findById là đủ ở đây

        if (userToDelete != null) {
            // === BƯỚC 1: Dọn dẹp các mối quan hệ phụ thuộc ===

            // 1.1 Dọn dẹp mối quan hệ Assignee trong các Card
            List<Card> assignedCards = cardService.findAllByAssignee(userToDelete);
            for (Card card : assignedCards) {
                // Xóa user này khỏi danh sách assignees của card
                card.getAssignees().remove(userToDelete);
                // Lưu lại card đã cập nhật
                cardService.save(card);
            }

            // 1.2 Dọn dẹp mối quan hệ Member trong các Board
            // Cần tải lại user với sharedBoards để dọn dẹp
            User userWithSharedBoards = userRepository.findById(id).orElse(null); // Tải lại để có transaction
            if(userWithSharedBoards != null) {
                Hibernate.initialize(userWithSharedBoards.getSharedBoards());
                for (Board sharedBoard : new ArrayList<>(userWithSharedBoards.getSharedBoards())) {
                    sharedBoard.getMembers().remove(userToDelete);
                }
            }

            // 1.3 Dọn dẹp mối quan hệ Roles
            // Tải lại user với roles để dọn dẹp
            User userWithRoles = userRepository.findById(id).orElse(null);
            if(userWithRoles != null) {
                Hibernate.initialize(userWithRoles.getRoles());
                userWithRoles.getRoles().clear();
            }

            // === BƯỚC 2: Kiểm tra các mối quan hệ sở hữu ===
            // Tải lại user với boards để kiểm tra
            User userWithOwnerBoards = userRepository.findById(id).orElse(null);
            if(userWithOwnerBoards != null) {
                Hibernate.initialize(userWithOwnerBoards.getBoards());
                if (!userWithOwnerBoards.getBoards().isEmpty()) {
                    throw new IllegalStateException("Cannot delete user. This user still owns " +
                            userWithOwnerBoards.getBoards().size() +
                            " boards. Please reassign ownership or delete those boards first.");
                }
            }

            // === BƯỚC 3: Xóa User ===
            // Sau khi tất cả các mối quan hệ đã được dọn dẹp, tiến hành xóa User
            userRepository.delete(userToDelete);
        }
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
