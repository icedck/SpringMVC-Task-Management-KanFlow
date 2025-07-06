package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.User;
import com.codegym.kanflow.repository.UserRepository;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    //Chúng ta đã tiêm PasswordEncoder vào UserService và sử dụng nó để mã hóa
// mật khẩu của người dùng ngay trước khi lưu vào CSDL.
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void save(User user) { // <-- THÊM PHƯƠNG THỨC NÀY
        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}
