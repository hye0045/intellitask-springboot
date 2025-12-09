package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Notification;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.NotificationRepository;
import com.minhhue.IntelliTask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho hệ thống thông báo
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * API: Lấy tất cả thông báo của user hiện tại
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(currentUser.getId());

        return ResponseEntity.ok(notifications);
    }

    /**
     * API: Lấy thông báo chưa đọc
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId());

        return ResponseEntity.ok(unreadNotifications);
    }

    /**
     * API: Đánh dấu thông báo là đã đọc
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Notification notification = notificationOptional.get();

        // Kiểm tra thông báo có thuộc về user hiện tại không
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        notification.setIsRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        return ResponseEntity.ok(updatedNotification);
    }

    /**
     * API: Đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId());

        unreadNotifications.forEach(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });

        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả thông báo là đã đọc"));
    }

    /**
     * API: Đếm số thông báo chưa đọc
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Long count = notificationRepository.countByRecipientIdAndIsReadFalse(currentUser.getId());

        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * API: Xóa thông báo
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Notification notification = notificationOptional.get();

        // Kiểm tra thông báo có thuộc về user hiện tại không
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        notificationRepository.delete(notification);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}

