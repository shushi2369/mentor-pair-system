package com.mentorpair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_grade")
public class StudentGrade {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String studentNo;

    private String studentName;

    private BigDecimal score;

    private LocalDateTime createTime;
}
