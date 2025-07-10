package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.Role;
import com.codegym.kanflow.repository.RoleRepository;
import com.codegym.kanflow.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService implements IRoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role findById(Long id) { // <-- THÊM PHƯƠNG THỨC NÀY
        return roleRepository.findById(id).orElse(null);
    }
}