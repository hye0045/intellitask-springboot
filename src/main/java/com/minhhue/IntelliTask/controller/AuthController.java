package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Trả về file login.html
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Trả về file register.html
    }

    @PostMapping("/register")
    public String handleRegistration(@ModelAttribute User user) {
        userService.registerNewUser(user);
        return "redirect:/login?success"; // Chuyển hướng đến trang login với thông báo thành công
    }
}