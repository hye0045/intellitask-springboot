package com.minhhue.IntelliTask.repository;

import com.minhhue.IntelliTask.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Integer recipientId);
    Long countByRecipientIdAndIsReadFalse(Integer recipientId);
}

