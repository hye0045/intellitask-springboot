package com.minhhue.IntelliTask.controller; // Đảm bảo package name đúng

import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping; // ĐẢM BẢO BẠN CÓ IMPORT NÀY
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

// @RestController: Đánh dấu class này xử lý API và trả về JSON
@RestController
// @RequestMapping("/api/users"): Tất cả API trong đây bắt đầu bằng /api/users
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // --- ĐÂY LÀ PHẦN QUAN TRỌNG NHẤT ---
    // @GetMapping: Đánh dấu phương thức này sẽ xử lý các request GET
    // đến đường dẫn gốc của Controller (tức là GET /api/users)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}