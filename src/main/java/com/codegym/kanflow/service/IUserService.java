package com.codegym.kanflow.service;

import com.codegym.kanflow.model.User;

import java.util.List;

public interface IUserService {
    User findById(Long id);

    User findByUsername(String username);

    void save(User user);

    List<User> findAll();

    User findByIdWithRoles(Long id);

    void updateUser(User user);

    void deleteById(Long id);

    User findByEmail(String email);
}
