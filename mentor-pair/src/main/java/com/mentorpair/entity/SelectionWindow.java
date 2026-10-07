package com.mentorpair.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 双选时间窗配置（单行，id 固定为 1） */
@Data
@TableName("selection_window")
public class SelectionWindow {

    @TableId(type = IdType.INPUT)
    private Long id;

    /** 1=启用时间窗 0=不限制 */
    private Integer enabled;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime updateTime;
}
