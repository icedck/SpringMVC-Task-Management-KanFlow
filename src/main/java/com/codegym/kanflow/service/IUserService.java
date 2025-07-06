package com.codegym.kanflow.service;

import com.codegym.kanflow.model.User;

public interface IUserService {
    User findById(Long id);

    User findByUsername(String username);

    void save(User user);
}
