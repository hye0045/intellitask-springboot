package com.minhhue.IntelliTask.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Quan hệ: Nhiều comment thuộc về một Task
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // Quan hệ: Nhiều comment được viết bởi một User
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
}
