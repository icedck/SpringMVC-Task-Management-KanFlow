package com.codegym.kanflow.service.impl;

import com.codegym.kanflow.model.User;
import com.codegym.kanflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service // Đánh dấu đây là một Spring Bean
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Spring Security sẽ gọi phương thức này khi một người dùng cố gắng đăng nhập.
     * Nhiệm vụ của nó là tìm người dùng trong CSDL bằng username
     * và trả về một đối tượng UserDetails mà Spring Security có thể hiểu được.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm người dùng trong CSDL
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Tạm thời, chúng ta sẽ gán cho tất cả người dùng vai trò "ROLE_USER"
        // Sau này có thể mở rộng để lấy vai trò từ CSDL
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Trả về một đối tượng User của Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                grantedAuthorities
        );
    }
}
