package com.mentorpair.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SelectionVO {

    private Long id;
    private Long studentId;
    private Long mentorId;
    private Long projectId;
    private String projectTitle;
    /** 0=待处理 1=已接受 2=已拒绝 */
    private Integer status;
    private String remark;
    private String studentName;
    private String studentNo;
    private String mentorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;
}
