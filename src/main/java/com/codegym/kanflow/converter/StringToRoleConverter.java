package com.codegym.kanflow.converter;

import com.codegym.kanflow.model.Role;
import com.codegym.kanflow.service.IRoleService; // Giả sử bạn có RoleService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToRoleConverter implements Converter<String, Role> {

    @Autowired
    private IRoleService roleService;

    @Override
    public Role convert(String source) {
        try {
            Long roleId = Long.parseLong(source);
            return roleService.findById(roleId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}