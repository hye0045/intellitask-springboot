package com.minhhue.IntelliTask.controller;

import com.fasterxml.jackson.core.type.TypeReference;//tool Jackson know List<AiTaskDto>
import com.fasterxml.jackson.databind.ObjectMapper; //sepcially read & write Json
import com.minhhue.IntelliTask.dto.*;
import com.minhhue.IntelliTask.entity.User;
import com.minhhue.IntelliTask.repository.UserRepository;
import com.minhhue.IntelliTask.entity.Comment;
import com.minhhue.IntelliTask.repository.CommentRepository;
import com.minhhue.IntelliTask.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    
    @Autowired
    private AIService aiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    //1 class tranfer data help request body has cleanly structure
    public static class AIRequest {
        private String text;
        private Integer projectId;

        //need getter & setter to Sring can deserialize JSON
        public String getText(){ return text;}
        public void setText(String text){ this.text=text;}
        public Integer getProjectId(){ return projectId;}
        public void setProjectId(Integer projectId){ this.projectId=projectId;}
    }
    //Api to analysis word & return offer task
    @PostMapping("/extract-tasks")
    public ResponseEntity<String> extractTask(@RequestBody AIRequest request) {
        try{
            //call service to process
            String extractedTaskJson = aiService.extractTasksFromText(request.getText(), new java.util.ArrayList<>());
            List<User> members = userRepository.findAll();
            extractedTaskJson = aiService.extractTasksFromText(request.getText(), members);
            //call service to process           
            //  //return JSON 
            return ResponseEntity.ok(extractedTaskJson);
        }catch(Exception e){
            //if connection erorr ->return 500 Interal Sercer Erorr
            e.printStackTrace();//in to debug
            return ResponseEntity.internalServerError().body("Error processing AI request : "+e.getMessage());
        }
        
    }
    public static class DescriptionRequest {
        private String taskTitle;

        public String getTaskTitle() { return taskTitle; }
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    }
    @PostMapping("/generate-description")
    public ResponseEntity<Map<String,String>> generateTaskDescription(@RequestBody DescriptionRequest request) {
        try {
            String description = aiService.generateTaskDescription(request.getTaskTitle());
            return ResponseEntity.ok(Map.of("description", description));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error generating task description: " + e.getMessage()));
        }
    }
    // Chatbot tóm tắt comment của một task
    @PostMapping("/summarize-task-comments")
    public ResponseEntity<Map<String, String>> summarizeTaskComments(@RequestBody Map<String, Long> payload) {
        Long taskId = payload.get("taskId");
        // Lấy tất cả comment của task
        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        
        // Nối các comment lại thành một chuỗi hội thoại
        StringBuilder conversation = new StringBuilder();
        for (Comment comment : comments) {
            conversation.append(comment.getAuthor().getFullName()).append(": ")
                        .append(comment.getContent()).append("\n");
        }
        
        // Gọi AI service để tóm tắt
        String summary = aiService.summarizeConversation(conversation.toString());
        
        return ResponseEntity.ok(Map.of("summary", summary));
    }
    
}

