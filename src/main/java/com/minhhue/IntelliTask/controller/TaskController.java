package com.minhhue.IntelliTask.controller;
import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.minhhue.IntelliTask.dto.AiTaskDto;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.UserRepository;
import com.minhhue.IntelliTask.service.PermissionService;
import com.minhhue.IntelliTask.service.NotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private NotificationService notificationService;

 
    //API 1: get all tasks in project (chỉ user có quyền xem project mới được)
    @GetMapping
    public ResponseEntity<List<Task>> getTaskByProject(@PathVariable int projectId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.canViewProject(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }

    //--API 2: create task for project (chỉ PM/Admin mới được tạo)
    @PostMapping
    public ResponseEntity<Task> creatTaskForProject(@PathVariable int projectId, @RequestBody Task task) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.canEditProject(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        return projectRepository.findById(projectId).map(project -> {
            task.setProject(project);
            if (task.getCreatedAt() == null) {
                task.setCreatedAt(LocalDateTime.now());
            }
            task.setUpdatedAt(LocalDateTime.now());
            task.setUpdatedBy(currentUser);
            if (task.getStatus() == null) {
                task.setStatus(Task.TaskStatus.To_do);
            }

            Task savedTask = taskRepository.save(task);

            // Gửi thông báo nếu có assignee
            if (savedTask.getAssignee() != null) {
                notificationService.notifyTaskAssigned(savedTask, savedTask.getAssignee(), currentUser);
            }

            return ResponseEntity.ok(savedTask);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    //API3:process update task from form (kiểm tra quyền và gửi thông báo khi assignee thay đổi)
    @PostMapping("/{taskId}/update")
    public RedirectView updateTask(@PathVariable int projectId,
                                   @PathVariable long taskId,
                                   @ModelAttribute Task taskDetail,
                                   @RequestParam(required = false) String reassignReason) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new RedirectView("/login");
        }

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Invalid task Id:" + taskId));

        // Kiểm tra quyền chỉnh sửa
        if (!permissionService.canEditTask(currentUser, taskId)) {
            return new RedirectView("/projects/" + projectId + "?error=no_permission");
        }

        // Lưu assignee cũ để gửi thông báo
        User oldAssignee = task.getAssignee();

        // Cập nhật các field
        task.setTitle(taskDetail.getTitle());
        task.setDescription(taskDetail.getDescription());
        Integer assigneeId = taskDetail.getAssignee() != null ? taskDetail.getAssignee().getId() : null;
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
        task.setStatus(taskDetail.getStatus());
        task.setDueDate(taskDetail.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(currentUser);

        taskRepository.save(task);

        // Gửi thông báo nếu assignee thay đổi
        if (oldAssignee != null && newAssignee != null && !oldAssignee.getId().equals(newAssignee.getId())) {
            // Chuyển từ người này sang người khác
            notificationService.notifyTaskReassigned(task, oldAssignee, newAssignee, currentUser, reassignReason);
        } else if (oldAssignee == null && newAssignee != null) {
            // Giao mới
            notificationService.notifyTaskAssigned(task, newAssignee, currentUser);
        } else if (oldAssignee != null && newAssignee == null) {
            // Bỏ giao
            notificationService.notifyTaskReassigned(task, oldAssignee, null, currentUser, reassignReason);
        } else if (oldAssignee != null && newAssignee != null && oldAssignee.getId().equals(newAssignee.getId())) {
            // Cập nhật task nhưng không đổi assignee
            notificationService.notifyTaskUpdated(task, currentUser);
        }

        return new RedirectView("/projects/" + projectId);
    }
    //API 4: delete task (chỉ PM/Admin mới được xóa)
    @PostMapping("/{taskId}/delete")
    public RedirectView deleteTask(@PathVariable int projectId, @PathVariable long taskId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new RedirectView("/login");
        }

        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return new RedirectView("/projects/" + projectId + "?error=task_not_found");
        }

        // Chỉ PM (owner của project) hoặc Admin mới được xóa
        if (!permissionService.canEditProject(currentUser, projectId)) {
            return new RedirectView("/projects/" + projectId + "?error=no_permission");
        }

        taskRepository.deleteById(taskId);
        return new RedirectView("/projects/" + projectId);
    }
    
    // API đã được nâng cấp để xử lý Assignee (chỉ PM/Admin mới được tạo)
    @PostMapping("/batch-create")
    public ResponseEntity<List<Task>> createTasksInBatch(
            @PathVariable int projectId,
            @RequestBody List<AiTaskDto> aiTasks) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.canEditProject(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project project = projectOptional.get();

        List<Task> tasksToSave = new ArrayList<>();

        for (AiTaskDto dto : aiTasks) {
            Task newTask = new Task();
            newTask.setTitle(dto.getTitle());
            newTask.setDescription(dto.getDescription());
            newTask.setProject(project);
            newTask.setStatus(Task.TaskStatus.To_do);
            newTask.setCreatedAt(LocalDateTime.now());
            newTask.setUpdatedAt(LocalDateTime.now());
            newTask.setUpdatedBy(currentUser);

            if (dto.getSuggestedAssigneeId() != null) {
                int assigneeId = dto.getSuggestedAssigneeId();
                Optional<User> assigneeOptional = userRepository.findById(assigneeId);
                assigneeOptional.ifPresent(newTask::setAssignee);
            }

            if (dto.getDueDate() != null && !dto.getDueDate().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate dueDate = LocalDate.parse(dto.getDueDate(), formatter);
                    newTask.setDueDate(dueDate);
                } catch (Exception e) {
                    System.err.println("Could not parse date: " + dto.getDueDate());
                }
            }

            tasksToSave.add(newTask);
        }

        List<Task> savedTasks = taskRepository.saveAll(tasksToSave);

        // Gửi thông báo cho các assignee
        for (Task savedTask : savedTasks) {
            if (savedTask.getAssignee() != null) {
                notificationService.notifyTaskAssigned(savedTask, savedTask.getAssignee(), currentUser);
            }
        }

        return ResponseEntity.ok(savedTasks);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}
