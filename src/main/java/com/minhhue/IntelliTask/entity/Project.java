package com.minhhue.IntelliTask.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;//o chứa data trùng lặp
@Entity
@Table(name= "projects")
@Data
public class Project{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name ;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)// qhe giữa các bảng khác 
    @JoinColumn(name="owner_id",referencedColumnName="id")//referenced tham chiếu đến cột id của bảng users khóa ngoại
    private User owner;//ng sở hữu project 

    @ManyToMany
    @JoinTable(
        name="project_members",//tên bảng trung gian
        joinColumns = @JoinColumn(name="project_id"),//khóa ngoại tham chiếu đến bảng project
        inverseJoinColumns = @JoinColumn(name="user_id")//khóa ngoại tham chiếu đến bảng user
    )
    @JsonIgnore
    private Set<User> members = new HashSet<>();//tập hợp các thành viên trong project
}
