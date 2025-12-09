package com.minhhue.IntelliTask.repository;

import com.minhhue.IntelliTask.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Hàm để tìm tất cả comment của một task, sắp xếp theo ngày tạo
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}