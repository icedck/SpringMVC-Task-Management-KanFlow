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
    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
    @Transactional
    public void updateUser(User userFromForm) {
        User existingUser = findByIdWithRoles(userFromForm.getId());
        if (existingUser != null) {
            existingUser.setEmail(userFromForm.getEmail());
            existingUser.setRoles(userFromForm.getRoles());
            userRepository.save(existingUser);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        User userToDelete = this.findById(id);

        if (userToDelete != null) {
            List<Card> assignedCards = cardService.findAllByAssignee(userToDelete);
            for (Card card : assignedCards) {
                card.getAssignees().remove(userToDelete);
                cardService.save(card);
            }

            User userWithSharedBoards = userRepository.findById(id).orElse(null); // Tải lại để có transaction
            if(userWithSharedBoards != null) {
                Hibernate.initialize(userWithSharedBoards.getSharedBoards());
                for (Board sharedBoard : new ArrayList<>(userWithSharedBoards.getSharedBoards())) {
                    sharedBoard.getMembers().remove(userToDelete);
                }
            }

            User userWithRoles = userRepository.findById(id).orElse(null);
            if(userWithRoles != null) {
                Hibernate.initialize(userWithRoles.getRoles());
                userWithRoles.getRoles().clear();
            }

            User userWithOwnerBoards = userRepository.findById(id).orElse(null);
            if(userWithOwnerBoards != null) {
                Hibernate.initialize(userWithOwnerBoards.getBoards());
                if (!userWithOwnerBoards.getBoards().isEmpty()) {
                    throw new IllegalStateException("Cannot delete user. This user still owns " +
                            userWithOwnerBoards.getBoards().size() +
                            " boards. Please reassign ownership or delete those boards first.");
                }
            }

            userRepository.delete(userToDelete);
        }
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
