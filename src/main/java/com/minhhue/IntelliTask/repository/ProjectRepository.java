package com.minhhue.IntelliTask.repository;

import com.minhhue.IntelliTask.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Integer>{
    
    // Load project với owner và members
    @EntityGraph(attributePaths = {"owner", "members"})
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdWithRelations(@Param("id") Integer id);
    
    // Load tất cả projects với owner
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT p FROM Project p")
    List<Project> findAllWithOwner();
}
