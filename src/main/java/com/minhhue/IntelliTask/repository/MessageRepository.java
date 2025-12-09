package com.minhhue.IntelliTask.repository;

import com.minhhue.IntelliTask.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // Lấy tin nhắn giữa 2 người trong một project
    @Query("SELECT m FROM Message m WHERE m.project.id = :projectId " +
           "AND ((m.sender.id = :userId1 AND m.recipient.id = :userId2) " +
           "OR (m.sender.id = :userId2 AND m.recipient.id = :userId1)) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(
        @Param("projectId") Integer projectId,
        @Param("userId1") Integer userId1,
        @Param("userId2") Integer userId2
    );
    
    // Lấy tất cả tin nhắn trong một project (cho group chat)
    List<Message> findByProjectIdOrderByCreatedAtAsc(Integer projectId);
    
    // Lấy tin nhắn chưa đọc của một user trong project
    List<Message> findByRecipientIdAndProjectIdAndIsReadFalseOrderByCreatedAtAsc(Integer recipientId, Integer projectId);
}

