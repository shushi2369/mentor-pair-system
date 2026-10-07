package com.mentorpair.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 学员成绩（关联课程名） */
@Data
public class GradeVO {

    private Long id;
    private Long courseId;
    private String courseName;
    private String studentNo;
    private String studentName;
    private BigDecimal score;
}
