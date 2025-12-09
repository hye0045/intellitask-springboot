package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import com.minhhue.IntelliTask.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;
    // API: Lấy danh sách project mà user có quyền xem
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<Project> allProjects = projectRepository.findAll();
        
        // Admin xem tất cả, các role khác chỉ xem project mà họ là member hoặc owner
        if (permissionService.isAdmin(currentUser)) {
            return ResponseEntity.ok(allProjects);
        }

        List<Project> accessibleProjects = allProjects.stream()
                .filter(project -> permissionService.canViewProject(currentUser, project.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(accessibleProjects);
    }
    
    // API: Tạo project mới (chỉ PM/Admin mới được)
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Chỉ PM và Admin mới được tạo project
        if (!permissionService.isPM(currentUser) && !permissionService.isAdmin(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        project.setOwner(currentUser);
        project.setCreatedAt(LocalDateTime.now());
        // Tự động thêm owner vào members
        project.getMembers().add(currentUser);

        Project savedProject = projectRepository.save(project);
        return ResponseEntity.ok(savedProject);
    }
    
    // API: Lấy thông tin project theo ID (kiểm tra quyền)
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Integer id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Project> projectOptional = projectRepository.findById(id);
        if (projectOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOptional.get();
        if (!permissionService.canViewProject(currentUser, id)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(project);
    }
    
    // API: Cập nhật project (chỉ PM owner hoặc Admin mới được)
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Integer id, @RequestBody Project projectDetails) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.canEditProject(currentUser, id)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Project> optionalProject = projectRepository.findById(id);
        if (optionalProject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project existingProject = optionalProject.get();
        existingProject.setName(projectDetails.getName());
        existingProject.setDescription(projectDetails.getDescription());

        Project updatedProject = projectRepository.save(existingProject);
        return ResponseEntity.ok(updatedProject);
    }

    // API: Xóa project (chỉ PM owner hoặc Admin mới được)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Integer id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.canEditProject(currentUser, id)) {
            return ResponseEntity.status(403).build();
        }

        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    // API: Thêm member vào project (chỉ PM owner hoặc Admin mới được)
    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<Project> addMemberToProject(@PathVariable Integer id, @PathVariable Integer userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.canEditProject(currentUser, id)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Project> projectOptional = projectRepository.findById(id);
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

    // API: Xóa member khỏi project (chỉ PM owner hoặc Admin mới được)
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Project> removeMemberFromProject(@PathVariable Integer id, @PathVariable Integer userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.canEditProject(currentUser, id)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Project> projectOptional = projectRepository.findById(id);
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}


