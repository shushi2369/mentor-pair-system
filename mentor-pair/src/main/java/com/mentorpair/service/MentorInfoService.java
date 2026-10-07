package com.mentorpair.service;

import com.mentorpair.common.PageVO;
import com.mentorpair.dto.MentorCardVO;
import com.mentorpair.entity.MentorInfo;

public interface MentorInfoService {

    MentorInfo getByUserId(Long userId);

    PageVO<MentorCardVO> pageMentorCards(int page, int size, String kw);

    void saveProfile(Long userId, String jobTitle, String department, String researchArea,
                     Integer maxQuota, String intro);
}
