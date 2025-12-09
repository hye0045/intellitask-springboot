package com.minhhue.IntelliTask.config; // ĐẢM BẢO DÒNG NÀY ĐÚNG VỚI CẤU TRÚC THƯ MỤC CỦA BẠN

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF để đơn giản hóa demo (có thể bật lại nếu cần)
            .csrf(csrf -> csrf.disable()) 
            // Bắt đầu định nghĩa các quy tắc cho request
            .authorizeHttpRequests(authz -> authz
                // Cho phép truy cập trang auth và tài nguyên tĩnh
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                // Chỉ Admin mới được vào các API quản trị (nếu bổ sung sau này)
                .requestMatchers("/api/admin/**").hasRole("Admin")
                // Các API /api/** còn lại yêu cầu đăng nhập
                .requestMatchers("/api/**").authenticated()
                // Bất kỳ request khác cũng yêu cầu đăng nhập
                .anyRequest().authenticated()
            )
            // Cấu hình form đăng nhập
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            // Cấu hình đăng xuất
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}