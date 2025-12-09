package com.minhhue.IntelliTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;//import annotation to skip undefined field in the file AI JSON
import lombok.Data;

@Data //to create getter,setter
//@JsonIgnoreProperties(ignoreUnknow =true ) in4 library Jackson(specially process JSON of Spring) said that if that string JSON had 1 unremain field in that class,so ignore bug and didn't in4 bug
@JsonIgnoreProperties
public class AiTaskDto {
    //Variable
    private String title;
    private String description;
    private String dueDate;
    private Integer suggestedAssigneeId;
    
}
