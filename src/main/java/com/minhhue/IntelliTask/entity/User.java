package com.minhhue.IntelliTask.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; //import all tools of Jpa
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Data; //work for annotation @data của thư viện lombok
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.annotation.JsonIgnore; 

@Entity//đại diện cho 1 bảng 
@Table(name="users")
@Data
public class User{
    @Id //1 field
    // @GeneratedValue: Chỉ định cách tạo ra giá trị cho khóa chính.
    // strategy = GenerationType.IDENTITY: Giao hoàn toàn việc tạo và quản lý ID
    // cho cột IDENTITY của database (cơ chế auto-increment của PostgreSQL).
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique =true,nullable=false)
    private String username;
    
    @Column(nullable=false)
    private String password;

    @Column(name="full_name")
    private String fullName;
    //@Enumerated: dùng cho type field enum()
    //EnumType.STRING("admin","pm",..)
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    public enum Role{
        Admin,
        PM,
        Team_Member
    }
    @Column(length=512)//set độ dài max 512 ký tự
    private String skills;

    @ManyToMany(mappedBy="members")
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

}
