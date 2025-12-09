package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Comment;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.CommentRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository; // Sẽ dùng khi có đăng nhập

    // DTO để nhận dữ liệu comment từ frontend
    public static class CommentRequest {
        private String content;
        public String getContent() { return content; }
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable long taskId, @RequestBody CommentRequest request) {
        Task task = taskRepository.findById(taskId).orElse(null);
        User author = getCurrentUser();

        if (task == null || author == null) {
            return ResponseEntity.badRequest().build();
        }

        Comment newComment = new Comment();
        newComment.setContent(request.getContent());
        newComment.setTask(task);
        newComment.setAuthor(author);
        newComment.setCreatedAt(LocalDateTime.now());
        
        Comment savedComment = commentRepository.save(newComment);
        return ResponseEntity.ok(savedComment);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}