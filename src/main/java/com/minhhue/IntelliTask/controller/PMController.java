package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import com.minhhue.IntelliTask.service.PermissionService;
import com.minhhue.IntelliTask.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller dành cho PM với các chức năng:
 * - Lập kế hoạch (tạo project, task)
 * - Phân bổ nguồn lực (gán task cho member)
 * - Theo dõi tiến độ (xem báo cáo)
 * - Báo cáo (thống kê)
 */
@RestController
@RequestMapping("/api/pm")
public class PMController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private NotificationService notificationService;

    /**
     * API: Lấy danh sách project mà PM là owner
     */
    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getMyProjects() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isPM(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        List<Project> myProjects = projectRepository.findAll().stream()
                .filter(p -> p.getOwner() != null && p.getOwner().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(myProjects);
    }

    /**
     * API: Lập kế hoạch - Tạo project mới với team members
     */
    @PostMapping("/projects/plan")
    public ResponseEntity<Project> planProject(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isPM(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Project project = new Project();
        project.setName((String) request.get("name"));
        project.setDescription((String) request.get("description"));
        project.setOwner(currentUser);
        project.setCreatedAt(java.time.LocalDateTime.now());
        project.getMembers().add(currentUser);

        // Thêm members nếu có
        if (request.containsKey("memberIds")) {
            @SuppressWarnings("unchecked")
            List<Integer> memberIds = (List<Integer>) request.get("memberIds");
            for (Integer memberId : memberIds) {
                userRepository.findById(memberId).ifPresent(project.getMembers()::add);
            }
        }

        Project savedProject = projectRepository.save(project);
        return ResponseEntity.ok(savedProject);
    }

    /**
     * API: Phân bổ nguồn lực - Gán task cho member với lý do
     */
    @PostMapping("/tasks/{taskId}/assign")
    public ResponseEntity<Task> assignResource(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isPM(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        // Kiểm tra PM có phải owner của project không
        if (!permissionService.canEditProject(currentUser, task.getProject().getId())) {
            return ResponseEntity.status(403).build();
        }

        Integer assigneeId = (Integer) request.get("assigneeId");
        String reason = (String) request.get("reason");

        User oldAssignee = task.getAssignee();
        User newAssignee = null;

        if (assigneeId != null) {
            Optional<User> assigneeOptional = userRepository.findById(assigneeId);
            if (assigneeOptional.isPresent()) {
                newAssignee = assigneeOptional.get();
                task.setAssignee(newAssignee);
            }
        } else {
            task.setAssignee(null);
        }

        task.setUpdatedAt(java.time.LocalDateTime.now());
        task.setUpdatedBy(currentUser);
        Task savedTask = taskRepository.save(task);

        // Gửi thông báo
        if (oldAssignee != null && newAssignee != null && !oldAssignee.getId().equals(newAssignee.getId())) {
            notificationService.notifyTaskReassigned(savedTask, oldAssignee, newAssignee, currentUser, reason);
        } else if (oldAssignee == null && newAssignee != null) {
            notificationService.notifyTaskAssigned(savedTask, newAssignee, currentUser);
        }

        return ResponseEntity.ok(savedTask);
    }

    /**
     * API: Theo dõi tiến độ - Lấy báo cáo tiến độ của project
     */
    @GetMapping("/projects/{projectId}/progress")
    public ResponseEntity<Map<String, Object>> trackProgress(@PathVariable Integer projectId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isPM(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        if (!permissionService.canEditProject(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        Map<String, Object> progress = new HashMap<>();
        progress.put("projectId", projectId);
        progress.put("projectName", project.getName());
        progress.put("totalTasks", tasks.size());
        progress.put("tasksByStatus", tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus() != null ? t.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                )));
        progress.put("tasksByPriority", tasks.stream()
                .filter(t -> t.getPriority() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getPriority().name(),
                        Collectors.counting()
                )));
        progress.put("overdueTasks", tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) 
                        && t.getStatus() != Task.TaskStatus.Done)
                .count());
        progress.put("tasksByAssignee", tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAssignee().getFullName(),
                        Collectors.counting()
                )));

        return ResponseEntity.ok(progress);
    }

    /**
     * API: Báo cáo - Lấy thống kê tổng quan
     */
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !permissionService.isPM(currentUser)) {
            return ResponseEntity.status(403).build();
        }

        List<Project> myProjects = projectRepository.findAll().stream()
                .filter(p -> p.getOwner() != null && p.getOwner().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("totalProjects", myProjects.size());
        report.put("totalTasks", myProjects.stream()
                .mapToLong(p -> taskRepository.findByProjectId(p.getId()).size())
                .sum());
        report.put("projects", myProjects.stream()
                .map(p -> {
                    Map<String, Object> projectInfo = new HashMap<>();
                    projectInfo.put("id", p.getId());
                    projectInfo.put("name", p.getName());
                    List<Task> tasks = taskRepository.findByProjectId(p.getId());
                    projectInfo.put("totalTasks", tasks.size());
                    projectInfo.put("completedTasks", tasks.stream()
                            .filter(t -> t.getStatus() == Task.TaskStatus.Done)
                            .count());
                    return projectInfo;
                })
                .collect(Collectors.toList()));

        return ResponseEntity.ok(report);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}

