package com.minhhue.IntelliTask.service;

import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Import công cụ mã hóa
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Spring sẽ tự động cung cấp Bean này

    public User registerNewUser(User user) {
        // Mã hóa mật khẩu của người dùng trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Mặc định, người dùng mới đăng ký sẽ có vai trò là TEAM_MEMBER
        user.setRole(User.Role.Team_Member);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}