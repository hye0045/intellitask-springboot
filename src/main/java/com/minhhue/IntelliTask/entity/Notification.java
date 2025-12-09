package com.minhhue.IntelliTask.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Người nhận thông báo
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Task liên quan (nếu có)
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    // Project liên quan (nếu có)
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    // Loại thông báo
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    public enum NotificationType {
        TASK_ASSIGNED,      // Task được giao
        TASK_REASSIGNED,    // Task được chuyển người
        TASK_UPDATED,       // Task được cập nhật
        PROJECT_INVITED,    // Được mời vào project
        MESSAGE_RECEIVED    // Nhận tin nhắn
    }
}

