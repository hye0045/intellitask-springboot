package com.minhhue.IntelliTask.service;

import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service kiểm tra quyền truy cập của người dùng
 */
@Service
public class PermissionService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Kiểm tra xem user có phải Admin không
     */
    public boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.Admin;
    }

    /**
     * Kiểm tra xem user có phải PM không
     */
    public boolean isPM(User user) {
        return user != null && user.getRole() == User.Role.PM;
    }

    /**
     * Kiểm tra xem user có phải Team_Member không
     */
    public boolean isTeamMember(User user) {
        return user != null && user.getRole() == User.Role.Team_Member;
    }

    /**
     * Kiểm tra xem user có quyền xem project không
     * - Admin: xem tất cả
     * - PM: xem project mà họ là owner hoặc member
     * - Team_Member: xem project mà họ là member
     */
    public boolean canViewProject(User user, Integer projectId) {
        if (user == null) return false;
        
        if (isAdmin(user)) {
            return true; // Admin xem tất cả
        }

        Project project = projectRepository.findByIdWithRelations(projectId).orElse(null);
        if (project == null) return false;

        // Kiểm tra xem user có phải owner không
        if (project.getOwner() != null && project.getOwner().getId().equals(user.getId())) {
            return true;
        }

        // Kiểm tra xem user có phải member không
        return project.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
    }

    /**
     * Kiểm tra xem user có quyền chỉnh sửa project không
     * - Admin: chỉnh sửa tất cả
     * - PM: chỉnh sửa project mà họ là owner
     * - Team_Member: không có quyền
     */
    public boolean canEditProject(User user, Integer projectId) {
        if (user == null) return false;
        
        if (isAdmin(user)) {
            return true;
        }

        if (!isPM(user)) {
            return false; // Team_Member không có quyền chỉnh sửa project
        }

        Project project = projectRepository.findByIdWithRelations(projectId).orElse(null);
        if (project == null) return false;

        return project.getOwner() != null && project.getOwner().getId().equals(user.getId());
    }

    /**
     * Kiểm tra xem user có quyền xem task không
     * - Admin: xem tất cả
     * - PM: xem task trong project mà họ là owner hoặc member
     * - Team_Member: xem task trong project mà họ là member hoặc task được giao cho họ
     */
    public boolean canViewTask(User user, Long taskId) {
        if (user == null) return false;
        
        if (isAdmin(user)) {
            return true;
        }

        Task task = taskRepository.findByIdWithRelations(taskId).orElse(null);
        if (task == null) return false;

        if (task.getProject() != null) {
            return canViewProject(user, task.getProject().getId());
        }

        return false;
    }

    /**
     * Kiểm tra xem user có quyền chỉnh sửa task không
     * - Admin: chỉnh sửa tất cả
     * - PM: chỉnh sửa task trong project mà họ là owner
     * - Team_Member: chỉnh sửa task được giao cho họ
     */
    public boolean canEditTask(User user, Long taskId) {
        if (user == null) return false;
        
        if (isAdmin(user)) {
            return true;
        }

        Task task = taskRepository.findByIdWithRelations(taskId).orElse(null);
        if (task == null) return false;

        if (isPM(user)) {
            // PM chỉnh sửa được task trong project mà họ là owner
            return task.getProject() != null && canEditProject(user, task.getProject().getId());
        }

        if (isTeamMember(user)) {
            // Team_Member chỉnh sửa được task được giao cho họ
            return task.getAssignee() != null && task.getAssignee().getId().equals(user.getId());
        }

        return false;
    }

    /**
     * Kiểm tra xem user có phải member của project không
     */
    public boolean isProjectMember(User user, Integer projectId) {
        if (user == null) return false;
        
        Project project = projectRepository.findByIdWithRelations(projectId).orElse(null);
        if (project == null) return false;

        if (project.getOwner() != null && project.getOwner().getId().equals(user.getId())) {
            return true;
        }

        return project.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
    }
}

