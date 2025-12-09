package com.minhhue.IntelliTask.repository;
 
import com.minhhue.IntelliTask.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long>{
    //find task in project by project_id return list task
    List<Task> findByProjectId(Integer projectId);
    
    // Load task với project và assignee
    @EntityGraph(attributePaths = {"project", "assignee", "updatedBy"})
    @Query("SELECT t FROM Task t WHERE t.id = :id")
    Optional<Task> findByIdWithRelations(@Param("id") Long id);
    
    // Load tasks với project và assignee
    @EntityGraph(attributePaths = {"project", "assignee", "updatedBy"})
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId")
    List<Task> findByProjectIdWithRelations(@Param("projectId") Integer projectId);
}
