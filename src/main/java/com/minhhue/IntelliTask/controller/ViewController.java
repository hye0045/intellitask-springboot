package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import com.minhhue.IntelliTask.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDateTime;


@Controller
public class ViewController {
    //tiêm ProjectRepository vào controller
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired 
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String indexPage(Model model) {
        User currentUser = getCurrentUser();
        
        List<Project> allProjects = projectRepository.findAllWithOwner();
        
        // Lọc project theo quyền truy cập
        List<Project> projects;
        if (currentUser != null && permissionService.isAdmin(currentUser)) {
            projects = allProjects; // Admin xem tất cả
        } else if (currentUser != null) {
            projects = allProjects.stream()
                    .filter(p -> {
                        try {
                            return permissionService.canViewProject(currentUser, p.getId());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            projects = List.of(); // Chưa đăng nhập thì không xem được gì
        }

        model.addAttribute("projects", projects);
        model.addAttribute("pageTitle", "Danh sách dự án");
        model.addAttribute("newProject", new Project());
        model.addAttribute("currentUser", currentUser);

        return "index";
    }
    //hàm sử lý request Post từ form (chỉ PM/Admin mới được tạo)
    @PostMapping("/projects/add")
    public String addProject(@ModelAttribute("newProject") Project project) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Chỉ PM và Admin mới được tạo project
        if (!permissionService.isPM(currentUser) && !permissionService.isAdmin(currentUser)) {
            return "redirect:/?error=no_permission";
        }

        project.setCreatedAt(LocalDateTime.now());
        project.setOwner(currentUser);
        project.getMembers().add(currentUser);

        projectRepository.save(project);
        return "redirect:/";
    }
    //get project by id (kiểm tra quyền)
    @GetMapping("/projects/{id}")
    @Transactional(readOnly = true)
    public String projectDetailPage(@PathVariable int id, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Project project = projectRepository.findByIdWithRelations(id).orElse(null);
        if (project == null) {
            return "redirect:/?error=project_not_found";
        }

        // Kiểm tra quyền xem project
        try {
            if (!permissionService.canViewProject(currentUser, id)) {
                return "redirect:/?error=no_permission";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=permission_check_failed";
        }

        List<Task> tasks = taskRepository.findByProjectIdWithRelations(id);

        model.addAttribute("project", project);
        model.addAttribute("tasks", tasks);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("canEdit", permissionService.canEditProject(currentUser, id));

        return "project-detail";
    }

    //get detail task (kiểm tra quyền)
    @GetMapping("/projects/{projectId}/tasks/{taskId}")
    @Transactional(readOnly = true)
    public String taskDetailPage(@PathVariable int projectId, @PathVariable long taskId, Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra quyền xem project trước
        try {
            if (!permissionService.canViewProject(currentUser, projectId)) {
                return "redirect:/projects/" + projectId + "?error=no_permission";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/projects/" + projectId + "?error=permission_check_failed";
        }

        Task task = taskRepository.findByIdWithRelations(taskId).orElse(null);
        if (task == null) {
            return "redirect:/projects/" + projectId + "?error=task_not_found";
        }

        // Kiểm tra task có thuộc project không
        if (task.getProject() == null || !task.getProject().getId().equals(projectId)) {
            return "redirect:/projects/" + projectId + "?error=task_not_found";
        }

        // Lấy danh sách users là member của project để gán task
        Project project = projectRepository.findByIdWithRelations(projectId).orElse(null);
        List<User> users;
        if (project != null) {
            users = project.getMembers().stream().collect(Collectors.toList());
            if (project.getOwner() != null && !users.contains(project.getOwner())) {
                users.add(project.getOwner());
            }
        } else {
            users = userRepository.findAll();
        }

        model.addAttribute("task", task);
        model.addAttribute("users", users);
        model.addAttribute("projectId", projectId);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("canEdit", permissionService.canEditTask(currentUser, taskId));

        return "task-detail";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }

    
    
    
}
