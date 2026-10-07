package com.mentorpair.dto;

import lombok.Data;

/** 双选时间窗状态（学员端/管理端展示） */
@Data
public class WindowVO {

    private Integer enabled;

    /** 当前是否开放申请 */
    private boolean open;

    private String startTime;

    private String endTime;

    /** 未开放原因；开放且设置截止时间时为"双选进行中"提示 */
    private String message;
}
