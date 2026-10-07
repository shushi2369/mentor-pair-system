package com.mentorpair.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionVO {

    private Long id;
    private Long selectionId;
    private String title;
    private String description;
    private String fileName;
    private Long fileSize;
    private String studentName;
    private String studentNo;
    private String mentorName;
    private String projectTitle;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
}
