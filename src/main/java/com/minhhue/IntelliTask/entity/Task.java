package com.minhhue.IntelliTask.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tasks")
@Data
public class Task{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto tăng
    private Long id; //dùng long vì task có thể lớn 

    @Column(nullable=false)
    private String title;

    @Column(columnDefinition ="TEXT")
    private String description ;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;  

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;
    
    @Column(name ="due_date")
    private LocalDate dueDate;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    //nhiều task thuộc 1 project
    //--quan hệ--
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="project_id",nullable= false)
    private Project project;

    //nhiều task thuộc 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="assignee_id")// permit null ,vì có thể task chưa được giao cho ai 
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    //---Enum
    public enum TaskStatus{
        To_do,
        In_progress,
        In_review,
        Done
    }
    public enum TaskPriority{
        Low,
        Medium,
        High
    }
}