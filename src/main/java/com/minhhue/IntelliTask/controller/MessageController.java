package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Message;
import com.minhhue.IntelliTask.entity.Project;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.MessageRepository;
import com.minhhue.IntelliTask.repository.ProjectRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import com.minhhue.IntelliTask.service.PermissionService;
import com.minhhue.IntelliTask.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho chức năng chat giữa các thành viên trong team
 */
@RestController
@RequestMapping("/api/projects/{projectId}/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private NotificationService notificationService;

    /**
     * API: Gửi tin nhắn trong project (chỉ member của project mới được)
     */
    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @PathVariable Integer projectId,
            @RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Kiểm tra user có phải member của project không
        if (!permissionService.isProjectMember(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if (projectOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Integer recipientId = (Integer) request.get("recipientId");
        String content = (String) request.get("content");

        if (recipientId == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> recipientOptional = userRepository.findById(recipientId);
        if (recipientOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User recipient = recipientOptional.get();

        // Kiểm tra recipient cũng phải là member của project
        if (!permissionService.isProjectMember(recipient, projectId)) {
            return ResponseEntity.status(403).build();
        }

        Message message = new Message();
        message.setContent(content);
        message.setSender(currentUser);
        message.setRecipient(recipient);
        message.setProject(projectOptional.get());
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        // Gửi thông báo cho người nhận
        notificationService.notifyMessageReceived(savedMessage);

        return ResponseEntity.ok(savedMessage);
    }

    /**
     * API: Lấy tin nhắn giữa 2 người trong project
     */
    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<Message>> getConversation(
            @PathVariable Integer projectId,
            @PathVariable Integer otherUserId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.isProjectMember(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        // Lấy tin nhắn giữa currentUser và otherUserId trong project
        List<Message> messages = messageRepository.findConversationBetweenUsers(
                projectId, currentUser.getId(), otherUserId
        );

        // Đánh dấu là đã đọc
        messages.forEach(msg -> {
            if (msg.getRecipient().getId().equals(currentUser.getId()) && !msg.getIsRead()) {
                msg.setIsRead(true);
                messageRepository.save(msg);
            }
        });

        return ResponseEntity.ok(messages);
    }

    /**
     * API: Lấy tất cả tin nhắn trong project (group chat)
     */
    @GetMapping
    public ResponseEntity<List<Message>> getProjectMessages(@PathVariable Integer projectId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.isProjectMember(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        List<Message> messages = messageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);

        // Đánh dấu tin nhắn gửi cho currentUser là đã đọc
        messages.forEach(msg -> {
            if (msg.getRecipient().getId().equals(currentUser.getId()) && !msg.getIsRead()) {
                msg.setIsRead(true);
                messageRepository.save(msg);
            }
        });

        return ResponseEntity.ok(messages);
    }

    /**
     * API: Lấy tin nhắn chưa đọc
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Message>> getUnreadMessages(@PathVariable Integer projectId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!permissionService.isProjectMember(currentUser, projectId)) {
            return ResponseEntity.status(403).build();
        }

        List<Message> unreadMessages = messageRepository.findByRecipientIdAndProjectIdAndIsReadFalseOrderByCreatedAtAsc(
                currentUser.getId(), projectId);

        return ResponseEntity.ok(unreadMessages);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}

