package com.minhhue.IntelliTask.service;

import com.minhhue.IntelliTask.entity.*;
import com.minhhue.IntelliTask.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Service xử lý thông báo
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Tạo thông báo khi task được giao cho người mới
     */
    public void notifyTaskAssigned(Task task, User assignee, User assigner) {
        if (assignee == null) return;

        Notification notification = new Notification();
        notification.setTitle("Công việc mới được giao");
        notification.setMessage(String.format(
            "%s đã giao công việc \"%s\" cho bạn trong dự án \"%s\"",
            assigner.getFullName(),
            task.getTitle(),
            task.getProject().getName()
        ));
        notification.setRecipient(assignee);
        notification.setTask(task);
        notification.setProject(task.getProject());
        notification.setType(Notification.NotificationType.TASK_ASSIGNED);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    /**
     * Tạo thông báo khi task được chuyển từ người này sang người khác
     */
    public void notifyTaskReassigned(Task task, User oldAssignee, User newAssignee, User reassigner, String reason) {
        // Thông báo cho người cũ
        if (oldAssignee != null && !oldAssignee.getId().equals(newAssignee.getId())) {
            Notification notificationOld = new Notification();
            notificationOld.setTitle("Công việc đã được chuyển");
            notificationOld.setMessage(String.format(
                "%s đã chuyển công việc \"%s\" từ bạn sang %s. Lý do: %s",
                reassigner.getFullName(),
                task.getTitle(),
                newAssignee.getFullName(),
                reason != null ? reason : "Không có lý do"
            ));
            notificationOld.setRecipient(oldAssignee);
            notificationOld.setTask(task);
            notificationOld.setProject(task.getProject());
            notificationOld.setType(Notification.NotificationType.TASK_REASSIGNED);
            notificationOld.setCreatedAt(LocalDateTime.now());
            notificationOld.setIsRead(false);
            notificationRepository.save(notificationOld);
        }

        // Thông báo cho người mới
        if (newAssignee != null) {
            Notification notificationNew = new Notification();
            notificationNew.setTitle("Công việc mới được giao");
            notificationNew.setMessage(String.format(
                "%s đã giao công việc \"%s\" cho bạn trong dự án \"%s\". Lý do: %s",
                reassigner.getFullName(),
                task.getTitle(),
                task.getProject().getName(),
                reason != null ? reason : "Không có lý do"
            ));
            notificationNew.setRecipient(newAssignee);
            notificationNew.setTask(task);
            notificationNew.setProject(task.getProject());
            notificationNew.setType(Notification.NotificationType.TASK_REASSIGNED);
            notificationNew.setCreatedAt(LocalDateTime.now());
            notificationNew.setIsRead(false);
            notificationRepository.save(notificationNew);
        }
    }

    /**
     * Tạo thông báo khi task được cập nhật
     */
    public void notifyTaskUpdated(Task task, User updater) {
        if (task.getAssignee() == null) return;

        Notification notification = new Notification();
        notification.setTitle("Công việc đã được cập nhật");
        notification.setMessage(String.format(
            "%s đã cập nhật công việc \"%s\" trong dự án \"%s\"",
            updater.getFullName(),
            task.getTitle(),
            task.getProject().getName()
        ));
        notification.setRecipient(task.getAssignee());
        notification.setTask(task);
        notification.setProject(task.getProject());
        notification.setType(Notification.NotificationType.TASK_UPDATED);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    /**
     * Tạo thông báo khi user được mời vào project
     */
    public void notifyProjectInvited(Project project, User invitee, User inviter) {
        Notification notification = new Notification();
        notification.setTitle("Bạn đã được mời vào dự án");
        notification.setMessage(String.format(
            "%s đã mời bạn tham gia dự án \"%s\"",
            inviter.getFullName(),
            project.getName()
        ));
        notification.setRecipient(invitee);
        notification.setProject(project);
        notification.setType(Notification.NotificationType.PROJECT_INVITED);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    /**
     * Tạo thông báo khi nhận tin nhắn mới
     */
    public void notifyMessageReceived(Message message) {
        Notification notification = new Notification();
        notification.setTitle("Tin nhắn mới");
        notification.setMessage(String.format(
            "%s đã gửi tin nhắn cho bạn trong dự án \"%s\"",
            message.getSender().getFullName(),
            message.getProject().getName()
        ));
        notification.setRecipient(message.getRecipient());
        notification.setProject(message.getProject());
        notification.setType(Notification.NotificationType.MESSAGE_RECEIVED);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }
}

