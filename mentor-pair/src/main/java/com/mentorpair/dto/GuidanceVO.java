package com.mentorpair.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GuidanceVO {

    private Long id;
    private Long submissionId;
    private String submissionTitle;
    private String content;
    private String mentorName;
    private String studentName;
    private String studentNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime guidanceTime;
}
