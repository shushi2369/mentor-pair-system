package com.mentorpair.dto;

import lombok.Data;

/** 学员端导师卡片 */
@Data
public class MentorCardVO {

    private Long mentorId;
    private String mentorName;
    private String jobTitle;
    private String department;
    private String researchArea;
    private Integer maxQuota;
    private Integer acceptedCount;
    private String intro;

    public int getRemaining() {
        int left = (maxQuota == null ? 0 : maxQuota) - (acceptedCount == null ? 0 : acceptedCount);
        return Math.max(left, 0);
    }
}
