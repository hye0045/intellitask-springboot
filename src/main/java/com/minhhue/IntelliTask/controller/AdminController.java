package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import com.minhhue.IntelliTask.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller dành cho Admin với các chức năng:
 * - Quản lý quyền truy cập (phân quyền user)
 * - Cấu hình workflow (cấu hình trạng thái task)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PermissionService permissionService;

    /**
     * API: Lấy danh sách tất cả users (chỉ Admin)
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * API: Cập nhật role của user (quản lý quyền truy cập)
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Integer userId,
            @RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        String roleStr = request.get("role");
        try {
            User.Role newRole = User.Role.valueOf(roleStr);
            user.setRole(newRole);
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Gán user vào project (quản lý quyền truy cập)
     */
    @PostMapping("/projects/{projectId}/members/{userId}")
    public ResponseEntity<Project> assignUserToProject(
            @PathVariable Integer projectId,
            @PathVariable Integer userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Project> projectOptional = projectRepository.findById(projectId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (projectOptional.isEmpty() || userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOptional.get();
        User user = userOptional.get();

        project.getMembers().add(user);
        Project updatedProject = projectRepository.save(project);

        return ResponseEntity.ok(updatedProject);
    }

    /**
     * API: Xóa user khỏi project (quản lý quyền truy cập)
     */
    @DeleteMapping("/projects/{projectId}/members/{userId}")
    public ResponseEntity<Project> removeUserFromProject(
            @PathVariable Integer projectId,
            @PathVariable Integer userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Project> projectOptional = projectRepository.findById(projectId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (projectOptional.isEmpty() || userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOptional.get();
        User user = userOptional.get();

        project.getMembers().remove(user);
        Project updatedProject = projectRepository.save(project);

        return ResponseEntity.ok(updatedProject);
    }

    /**
     * API: Cấu hình workflow - Lấy danh sách trạng thái hiện tại
     */
    @GetMapping("/workflow/statuses")
    public ResponseEntity<Map<String, Object>> getWorkflowStatuses() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> workflow = new HashMap<>();
        workflow.put("taskStatuses", Task.TaskStatus.values());
        workflow.put("taskPriorities", Task.TaskPriority.values());

        return ResponseEntity.ok(workflow);
    }

    /**
     * API: Thống kê hệ thống
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProjects", projectRepository.count());
        stats.put("totalTasks", taskRepository.count());
        stats.put("usersByRole", userRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        u -> u.getRole().name(),
                        java.util.stream.Collectors.counting()
                )));

        return ResponseEntity.ok(stats);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}

