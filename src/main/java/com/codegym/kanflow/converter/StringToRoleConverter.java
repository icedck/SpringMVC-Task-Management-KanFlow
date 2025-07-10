package com.codegym.kanflow.converter;

import com.codegym.kanflow.model.Role;
import com.codegym.kanflow.service.IRoleService; // Giả sử bạn có RoleService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component // Rất quan trọng! Để Spring có thể tìm thấy và sử dụng nó
public class StringToRoleConverter implements Converter<String, Role> {

    @Autowired
    private IRoleService roleService; // Dùng service để tìm Role từ DB

    @Override
    public Role convert(String source) {
        // source chính là chuỗi ID được gửi từ form, ví dụ "1", "2"
        try {
            Long roleId = Long.parseLong(source);
            // Gọi service để tìm đối tượng Role tương ứng với ID
            return roleService.findById(roleId); // Bạn cần thêm phương thức này vào service
        } catch (NumberFormatException e) {
            // Xử lý nếu source không phải là một số
            return null;
        }
    }
}