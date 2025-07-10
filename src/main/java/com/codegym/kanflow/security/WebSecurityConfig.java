package com.codegym.kanflow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Kích hoạt Spring Security
public class WebSecurityConfig { // Lưu ý: Không cần "extends WebSecurityConfigurerAdapter"

    // Inject UserDetailsService của chúng ta
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean này cấu hình cách Spring Security xác thực người dùng.
     * Nó nói rằng: "Hãy dùng UserDetailsService mà tôi cung cấp để tìm người dùng,
     * và dùng PasswordEncoder này để kiểm tra mật khẩu".
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    /**
     * Đây là nơi định nghĩa toàn bộ chuỗi bộ lọc bảo mật (security filter chain).
     * Tất cả các request HTTP sẽ đi qua đây để được kiểm tra.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .antMatchers("/", "/login", "/register", "/static/**", "/error/**").permitAll()

                                // ĐỊNH NGHĨA QUYỀN MỚI
                                // Ví dụ: tạo một trang quản lý user chỉ cho ADMIN
                                .antMatchers("/admin/**").hasRole("ADMIN")

                                // Các URL của board yêu cầu vai trò USER hoặc ADMIN
                                .antMatchers("/boards/**").hasAnyRole("USER", "ADMIN")
                                .antMatchers("/api/**").hasAnyRole("USER", "ADMIN")

                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin
                                // URL của trang đăng nhập tùy chỉnh mà chúng ta sẽ tạo
                                .loginPage("/login")
                                // URL mà form đăng nhập sẽ gửi đến. Spring Security sẽ tự xử lý request này.
                                .loginProcessingUrl("/doLogin")
                                // URL sẽ chuyển đến sau khi đăng nhập thành công.
                                // 'true' ở đây để luôn luôn chuyển hướng đến trang này.
                                .defaultSuccessUrl("/boards", true)
                                .permitAll() // Cho phép tất cả mọi người truy cập vào trang đăng nhập
                )
                .logout(logout ->
                        logout
                                // URL để thực hiện đăng xuất
                                .logoutUrl("/logout")
                                // URL sẽ chuyển đến sau khi đăng xuất thành công
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                ).exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedPage("/error/403")
                )
                .csrf(csrf -> csrf.disable()); // Tạm thời tắt CSRF để dễ làm việc

        return http.build();
    }
}
