package com.mentorpair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mentor_info")
public class MentorInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String jobTitle;

    private String department;

    private String researchArea;

    /** 可带学员名额上限 */
    private Integer maxQuota;

    private String intro;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
