package com.minhhue.IntelliTask.config;

import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ chạy nếu database trống
        if (userRepository.count() == 0) {
            System.out.println("=== Bắt đầu khởi tạo dữ liệu mẫu ===");

            // === 1. TẠO CÁC VAI TRÒ (ROLES) ===
            User admin = createUser("admin", "admin", "Admin IntelliTask", User.Role.Admin, "System, Management");
            User pm1 = createUser("pm_mai", "password", "Mai Nguyễn", User.Role.PM, "Project Management, Client Communication");
            User pm2 = createUser("pm_tuan", "password", "Tuấn Anh", User.Role.PM, "Technical Lead, Agile");
            
            // === 2. TẠO CÁC TEAM MEMBER VỚI KỸ NĂNG CỤ THỂ ===
            // Team Marketing
            User contentWriter = createUser("an_content", "password", "An Trần", User.Role.Team_Member, "Content Writing, SEO, Blog");
            User designer = createUser("binh_design", "password", "Bình Lê", User.Role.Team_Member, "UI/UX Design, Figma, Branding");
            User digitalMkt = createUser("cuong_ads", "password", "Cường Phạm", User.Role.Team_Member, "Facebook Ads, Google Ads, Analytics");

            // Team Phát triển Phần mềm
            User backendDev = createUser("dat_be", "password", "Đạt Võ", User.Role.Team_Member, "Java, Spring Boot, Databases");
            User frontendDev = createUser("ha_fe", "password", "Hà Trịnh", User.Role.Team_Member, "React, JavaScript, CSS");

            System.out.println("-> Đã tạo 8 người dùng mẫu.");
            System.out.println("-> Tài khoản admin: " + admin.getUsername());

            // === 3. TẠO DỰ ÁN VÀ GÁN TEAM ===

            // Dự án 1: Marketing (do Mai quản lý)
            Project marketingProject = new Project();
            marketingProject.setName("Chiến dịch Ra mắt Sản phẩm Mới Q1");
            marketingProject.setDescription("Thực hiện các hoạt động marketing online để quảng bá sản phẩm XYZ.");
            marketingProject.setOwner(pm1);
            // Thêm thành viên vào team dự án
            marketingProject.getMembers().add(pm1);
            marketingProject.getMembers().add(contentWriter);
            marketingProject.getMembers().add(designer);
            marketingProject.getMembers().add(digitalMkt);
            projectRepository.save(marketingProject);

            // Dự án 2: Phát triển Phần mềm (do Tuấn Anh quản lý)
            Project devProject = new Project();
            devProject.setName("Xây dựng Hệ thống Quản lý Nội bộ");
            devProject.setDescription("Phát triển một ứng dụng web nội bộ để quản lý nhân sự.");
            devProject.setOwner(pm2);
            // Thêm thành viên vào team dự án
            devProject.getMembers().add(pm2);
            devProject.getMembers().add(backendDev);
            devProject.getMembers().add(frontendDev);
            projectRepository.save(devProject);

            System.out.println("-> Đã tạo 2 dự án mẫu với team riêng.");

            // === 4. TẠO TASK MẪU CHO TỪNG DỰ ÁN ===
            
            // Tasks cho dự án Marketing
            createTask(marketingProject, "Viết 5 bài blog giới thiệu sản phẩm", contentWriter, Task.TaskStatus.To_do, LocalDate.now().plusDays(10));
            createTask(marketingProject, "Thiết kế bộ nhận diện thương hiệu", designer, Task.TaskStatus.In_progress, LocalDate.now().plusDays(14));
            createTask(marketingProject, "Thiết lập chiến dịch quảng cáo Facebook", digitalMkt, Task.TaskStatus.To_do, LocalDate.now().plusDays(7));
            
            // Tasks cho dự án Phần mềm
            createTask(devProject, "Thiết kế Cơ sở dữ liệu", backendDev, Task.TaskStatus.Done, LocalDate.now().minusDays(5));
            createTask(devProject, "Xây dựng API Đăng nhập", backendDev, Task.TaskStatus.In_progress, LocalDate.now().plusDays(3));
            createTask(devProject, "Dựng giao diện Trang chủ bằng React", frontendDev, Task.TaskStatus.To_do, LocalDate.now().plusDays(8));

            System.out.println("-> Đã tạo 6 task mẫu cho các dự án.");
            System.out.println("=== Hoàn tất khởi tạo dữ liệu ===");
        }
    }

    // --- CÁC HÀM TIỆN ÍCH (HELPER METHODS) ĐỂ CODE GỌN GÀNG HƠN ---

    private User createUser(String username, String rawPassword, String fullName, User.Role role, String skills) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        user.setSkills(skills);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private void createTask(Project project, String title, User assignee, Task.TaskStatus status, LocalDate dueDate) {
        Task task = new Task();
        task.setProject(project);
        task.setTitle(title);
        task.setAssignee(assignee);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }
}