package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.User;
import com.codegym.kanflow.repository.UserRepository;
import com.codegym.kanflow.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
