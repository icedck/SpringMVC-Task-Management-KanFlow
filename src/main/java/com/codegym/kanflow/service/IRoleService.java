package com.codegym.kanflow.service;

import com.codegym.kanflow.model.Role;

import java.util.List;

public interface IRoleService {
    List<Role> findAll();

    Role findById(Long id);
}
