package com.mentorpair.service;

import com.mentorpair.dto.WindowVO;

public interface SelectionWindowService {

    /** 当前状态（供学员端与管理端展示） */
    WindowVO status();

    /** 未开放原因；null 表示开放（时间窗未启用时始终开放） */
    String closedReason(java.time.LocalDateTime now);

    /** 管理员保存时间窗配置 */
    void save(Integer enabled, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}
