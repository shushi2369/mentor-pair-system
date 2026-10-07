package com.mentorpair.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 导师课程列表（含成绩条数） */
@Data
public class CourseVO {

    private Long id;
    private String courseName;
    private Integer gradeCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
