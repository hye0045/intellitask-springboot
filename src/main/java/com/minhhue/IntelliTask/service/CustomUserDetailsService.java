package com.minhhue.IntelliTask.service;

import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

// @Service: Đánh dấu đây là một Bean Service để Spring quản lý.
@Service
// implements UserDetailsService: Đây là "bản hợp đồng" quan trọng nhất.
// Class này hứa với Spring Security rằng nó sẽ thực hiện nhiệm vụ của một UserDetailsService.
public class CustomUserDetailsService implements UserDetailsService {

    // Tiêm UserRepository vào để có thể truy vấn database.
    @Autowired
    private UserRepository userRepository;

    // Đây là phương thức DUY NHẤT mà Spring Security sẽ gọi.
    // Nó sẽ tự động truyền username người dùng nhập vào form cho phương thức này.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Bước 1: Dùng UserRepository để tìm kiếm User trong DB theo username.
        User user = userRepository.findByUsername(username);

        // Bước 2: Kiểm tra kết quả.
        if (user == null) {
            // Nếu không tìm thấy, ném ra một ngoại lệ mà Spring Security hiểu được.
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Bước 3: Nếu tìm thấy, chuyển đổi đối tượng User (của mình)
        // thành một đối tượng UserDetails (mà Spring Security hiểu được).
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),                 // Tham số 1: Tên đăng nhập
                user.getPassword(),                 // Tham số 2: Mật khẩu ĐÃ ĐƯỢC MÃ HÓA trong DB
                getAuthorities(user.getRole())      // Tham số 3: Quyền hạn của người dùng
        );
    }

    // Hàm phụ trợ để chuyển đổi Role (ADMIN, TEAM_MEMBER) của mình
    // thành một danh sách các "quyền" mà Spring Security hiểu.
    private Collection<? extends GrantedAuthority> getAuthorities(User.Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}