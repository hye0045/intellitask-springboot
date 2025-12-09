package com.minhhue.IntelliTask.controller;

import com.minhhue.IntelliTask.entity.Attachment;
import com.minhhue.IntelliTask.entity.Task;
import com.minhhue.IntelliTask.repository.AttachmentRepository;
import com.minhhue.IntelliTask.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    public static class AttachmentRequest {
        private String fileName;
        private String fileUrl;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }
    }

    @GetMapping
    public ResponseEntity<List<Attachment>> list(@PathVariable long taskId) {
        if (!taskRepository.existsById(taskId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(attachmentRepository.findByTaskId(taskId));
    }

    @PostMapping
    public ResponseEntity<Attachment> add(@PathVariable long taskId, @RequestBody AttachmentRequest request) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        Attachment attachment = new Attachment();
        attachment.setTask(task);
        attachment.setFileName(request.getFileName());
        attachment.setFileUrl(request.getFileUrl());
        attachment.setUploadedAt(LocalDateTime.now());

        Attachment saved = attachmentRepository.save(attachment);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(@PathVariable long taskId, @PathVariable long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
        if (attachment == null || attachment.getTask() == null || attachment.getTask().getId() != taskId) {
            return ResponseEntity.notFound().build();
        }
        attachmentRepository.delete(attachment);
        return ResponseEntity.noContent().build();
    }
}

